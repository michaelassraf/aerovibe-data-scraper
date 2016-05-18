/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lbis.aerovibe.batch.server.schedule.tasks.impl;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.google.gson.reflect.TypeToken;
import com.lbis.aerovibe.batch.server.schedule.tasks.GetSensorsAbs;
import com.lbis.aerovibe.batch.server.schedule.tasks.impl.aqicn.model.Station;
import com.lbis.aerovibe.batch.server.services.AQICNScrapingService;
import com.lbis.aerovibe.batch.server.services.ScrapingService;
import com.lbis.aerovibe.enums.DataProvidorsEnums;
import com.lbis.aerovibe.enums.PollutantsEnums;
import com.lbis.aerovibe.model.Sensor;
import com.lbis.aerovibe.model.SensorMeasurement;
import com.lbis.aerovibe.model.SensorMeasurementValue;
import com.lbis.aerovibe.spring.common.properties.PropertiesFieldNames;
import com.lbis.aerovibe.utils.AerovibeUtils;
import static com.sun.org.apache.xalan.internal.lib.ExsltDatetime.date;
import java.io.File;
import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 *
 * @author Development User
 */
@Service
public class GetSensorsFromBeijingAirPollution extends GetSensorsAbs {

    private final int sensorsDetailsInterval = 6 * 60 * 60 * 1000;
    private final int sensorMeasurementsInterval = 30 * 60 * 1000;

    private final String AQI_CITY_PREFIX = "http://aqicn.org/city";

    Logger logger = Logger.getLogger(GetSensorsFromBeijingAirPollution.class);

    TimeZone gMT0TimeZone = TimeZone.getTimeZone("Etc/GMT");

    @Autowired
    ScrapingService scrapingService;

    @Autowired
    ApplicationContext applicationContext;

    @Autowired
    AQICNScrapingService aQICNScrapingService;

    @Value(PropertiesFieldNames.aqiCNScrapingServiceURLs)
    List<String> aqiCNScrapingServiceURLs;

    @Override
    @Scheduled(cron = "0 0 8 * * ?")
    public void getSensorsDetails() {
        Thread.currentThread().setName(GetSensorsFromBeijingAirPollution.class.getSimpleName() + " details interval");
        logger.info("** Starting Beijing Air Pollution stations interval. **");
        sensors = getStationsAsSensorsObjects();
        refineAndDropSensorDetailsInDB();
        logger.info("** Finished Beijing Air Pollution stations interval. **");
    }

    //    @Scheduled(cron = "0 1,6,11,16,21,26,31,36,41,46,51,56 * * * ?")
    @Override
    @Scheduled(fixedDelay = 300000L)
    public void getSensorsMeasurements() {
        Thread.currentThread().setName(GetSensorsFromBeijingAirPollution.class.getSimpleName() + " data interval");
        logger.info("** Starting Beijing Air Pollution stations data interval. **");
        sensorMeasurements = getSensorsMeasurementsAsObjects();
        refineAndDropSensorMeasurementsInDB();
        logger.info("** Finished Beijing Air Pollution stations data interval. **");
    }

    private String getFormattedStringForFile(File aQICNArrayFile) {
        try {
            String currentaQICNArrayFileText = Files.toString(aQICNArrayFile, Charsets.UTF_8);
            currentaQICNArrayFileText = currentaQICNArrayFileText.replaceAll("\\}\\{", "\\}\\,\\{");
            currentaQICNArrayFileText = currentaQICNArrayFileText.replaceAll("\\\\\"", "");
            currentaQICNArrayFileText = currentaQICNArrayFileText.replaceAll("PM2.5", "PM2_5");
            currentaQICNArrayFileText = currentaQICNArrayFileText.replaceAll("\"-\"", "null");
            currentaQICNArrayFileText = currentaQICNArrayFileText.replaceAll("None", ",");
            currentaQICNArrayFileText = "[" + currentaQICNArrayFileText + "]";
            return currentaQICNArrayFileText;
        } catch (Throwable th) {
            logger.error("Can't read file " + aQICNArrayFile.getAbsolutePath(), th);
        }
        return null;
    }

    private ArrayList<Station> getAllFilesContentAsStations(File[] aQICNArrayFiles) {
        ArrayList<Station> stations = new ArrayList<>();
        for (File aQICNArrayFile : aQICNArrayFiles) {
            String currentFileContent;
            logger.debug("Working on file " + aQICNArrayFile.getAbsolutePath());
            try {
                currentFileContent = getFormattedStringForFile(aQICNArrayFile);
            } catch (Throwable th) {
                logger.error("Can't read content from file.", th);
                continue;
            }
            if (currentFileContent == null) {
                logger.error("File content is null, will exit now.");
                return null;
            }
            ArrayList<Station> currentFileStations;
            try {
                currentFileStations = gsonBean.getGson().fromJson(currentFileContent, new TypeToken<List<Station>>() {
                }.getType());
                logger.debug("Successfully parsed " + currentFileStations.size() + " stations from file " + aQICNArrayFile.getAbsolutePath());
            } catch (Throwable th) {
                logger.debug("Can't serialize string - " + currentFileContent, th);
                continue;
            }
            stations.addAll(currentFileStations);
        }
        logger.debug("Successfully parsed " + stations.size() + " stations from " + aQICNArrayFiles.length + " files ");
        return stations;
    }

    @Override
    protected LinkedList<SensorMeasurement> getSensorsMeasurementsAsObjects() {
        LinkedList<SensorMeasurement> sensorMeasurements = new LinkedList<>();
        for (String aqiCNScrapingServiceURL : aqiCNScrapingServiceURLs) {
            File[] aQICNArrayFiles = aQICNScrapingService.getAQICNArrayFilesFromWeb(aqiCNScrapingServiceURL);
            List<Station> stations = getAllFilesContentAsStations(aQICNArrayFiles);
            for (Station station : stations) {
                logger.debug("Working on station " + station.getName() + " from air quality beijing.");
                ArrayList<SensorMeasurementValue> sensorMeasurementValues = new ArrayList();
                Long measurementDate = getMeasurementTimeStamp(station);
                String[] coordinates = station.getCoordinates().split(",");
                for (Field field : station.getClass().getDeclaredFields()) {
                    field.setAccessible(true);
                    Object currentMeasurementValue = new Object();
                    try {
                        currentMeasurementValue = field.get(station);
                    } catch (Throwable th) {
                        logger.error("Can't read field " + field.getName() + " from object.", th);
                        continue;
                    }
                    if (!(currentMeasurementValue instanceof Float)) {
                        continue;
                    }

                    PollutantsEnums measuredType = AerovibeUtils.getInstance().validatePollutant(field.getName(), (Float) currentMeasurementValue);
                    if (measuredType == null) {
                        continue;
                    }
                    sensorMeasurementValues.add(new SensorMeasurementValue(measuredType, (Float) currentMeasurementValue, measurementDate));
                    logger.debug("Successfully fetched " + sensorMeasurementValues.size() + " for station " + station.getName());
                }
                if (sensorMeasurementValues.size() < 1) {
                    continue;
                }
                sensorMeasurements.add(new SensorMeasurement(measurementDate, new Sensor(DataProvidorsEnums.AirQualityBeijing, Double.parseDouble(coordinates[0]), Double.parseDouble(coordinates[1])).getObjectKey(), DataProvidorsEnums.AirQualityBeijing, sensorMeasurementValues));
            }
            logger.debug("Successfully fetched " + sensorMeasurements.size() + " from " + stations.size() + " stations.");
        }
        return sensorMeasurements;
    }

    @Override
    protected LinkedList<Sensor> getStationsAsSensorsObjects() {
        LinkedList<Sensor> sensors = new LinkedList<>();
        for (String aqiCNScrapingServiceURL : aqiCNScrapingServiceURLs) {
            File[] aQICNArrayFiles;
            try {
                aQICNArrayFiles = aQICNScrapingService.getAQICNArrayFilesFromWeb(aqiCNScrapingServiceURL);
            } catch (Throwable th) {
                logger.error("Failed to get list of files", th);
                return null;
            }
            List<Station> stations = getAllFilesContentAsStations(aQICNArrayFiles);
            for (Station station : stations) {
                String[] coordinates = station.getCoordinates().split(",");
                sensors.add(new Sensor(DataProvidorsEnums.AirQualityBeijing, station.getName(), Double.parseDouble(coordinates[0]), Double.parseDouble(coordinates[1])));
            }
            logger.debug("Successfully built " + sensors.size() + " from " + stations.size() + " stations.");
        }
        return sensors;
    }

    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS a");

    private Long getMeasurementDate(Station station) {

        if (station.getTimestamp() != null) {
            return station.getTimestamp() * 1000;
        }
        Calendar calender = Calendar.getInstance();
        try {
            String date = station.getTime();
            String day = date.substring(0, date.lastIndexOf(" ")).toLowerCase();
            String hour = date.substring(date.lastIndexOf(" ") + 1, date.indexOf(":"));
            String minutes = date.substring(date.indexOf(":") + 1, date.length());

            //logger.debug("Time recieved is : day " + day + " hour " + hour + " minutes " + minutes);
            int today = calender.get(Calendar.DAY_OF_WEEK);
            int dayOfWeek = calender.get(Calendar.DAY_OF_WEEK);
            switch (day) {
                case "yesterday":
                    if (dayOfWeek <= 1) {
                        dayOfWeek = 7;
                    } else {
                        dayOfWeek = -1;
                    }
                    break;
                case "2 days ago":
                case "two days ago":
                    if (dayOfWeek <= 2) {
                        dayOfWeek = 7;
                    } else {
                        dayOfWeek = -2;
                    }
                    break;
                case "sunday":
                    dayOfWeek = Calendar.SUNDAY;
                    break;
                case "monday":
                    dayOfWeek = Calendar.MONDAY;
                    break;
                case "tuesday":
                    dayOfWeek = Calendar.TUESDAY;
                    break;
                case "wednesday":
                    dayOfWeek = Calendar.WEDNESDAY;
                    break;
                case "thursday":
                    dayOfWeek = Calendar.THURSDAY;
                    break;
                case "friday":
                    dayOfWeek = Calendar.FRIDAY;
                    break;
                case "saturday":
                    dayOfWeek = Calendar.SATURDAY;
                    break;
            }

            int hourAsInt = hour.equals("0") || hour.equals("00") ? 0 : Integer.parseInt(hour);
            int minutesAsInt = minutes.equals("0") || minutes.equals("00") ? 0 : Integer.parseInt(minutes);
            calender.set(Calendar.DAY_OF_WEEK, dayOfWeek);
            calender.set(Calendar.YEAR, calender.get(Calendar.YEAR));
            calender.set(Calendar.WEEK_OF_YEAR, dayOfWeek > today ? calender.get(Calendar.WEEK_OF_YEAR) - 1 : calender.get(Calendar.WEEK_OF_YEAR));
            if (hourAsInt > 12) {
                calender.set(Calendar.HOUR, hourAsInt - 12);
                calender.set(Calendar.AM_PM, Calendar.PM);
            } else {
                calender.set(Calendar.HOUR, hourAsInt);
                calender.set(Calendar.AM_PM, Calendar.AM);
            }
            calender.set(Calendar.MINUTE, minutesAsInt);
            calender.set(Calendar.SECOND, 0);
            calender.set(Calendar.MILLISECOND, 0);
            calender.setTimeZone(gMT0TimeZone);
        } catch (Throwable th) {
            logger.error("Can't parse sensot data. Date is - " + String.valueOf(station.getTimestamp()));
        }
        //logger.debug("Date parsed is : " + simpleDateFormat.format(calender.getTime()));
        return calender.getTimeInMillis();
    }
    SimpleDateFormat aQICNsimpleDateFormat = new SimpleDateFormat("MMM dd yyyy HH:mm");

    private Long getMeasurementTimeStamp(Station station) {
        if (station.getTimestamp() != null) {
            return station.getTimestamp() * 1000;
        }
        try {
            String time = station.getTime();
            if (time.charAt(0) == ' ') {
                time = time.substring(1, time.length());
            }
            time = time.substring(time.indexOf(",") + 1, time.length());
            time = time.replaceAll("st", "");
            time = time.replaceAll("nd", "");
            time = time.replaceAll("th", "");
            time = time.replaceAll("rd", "");
            time = time.replaceAll(" am", "");
            time = time.replaceAll(" pm", "");
            time = time.replaceAll(",", "");
            if (time.charAt(0) == ' ') {
                time = time.substring(1, time.length());
            }
            aQICNsimpleDateFormat.setTimeZone(gMT0TimeZone);
            Date date = aQICNsimpleDateFormat.parse(time);
            return date.getTime();
        } catch (Throwable th) {
            logger.error("Can't parse date time is " + station != null ? station.getTime() : " station is null" + " , Time stamp is " + station != null ? String.valueOf(station.getTimestamp()) : "station is null", th);
        }
        return null;
    }

//    ExecutorService service = Executors.newFixedThreadPool(20);
//
//    @Override
//    public LinkedList<Sensor> getStationsAsSensorsObjects() {
//        List<String> sensorsLinks = getAllSensorsLinks();
//        if (sensorsLinks == null) {
//            return null;
//        }
//
//        List<Future<Sensor>> futureSensors = new ArrayList<>();
//
//        for (String link : sensorsLinks) {
//            futureSensors.add(service.submit(applicationContext.getBean(AQICNSensorCallable.class).buildAQICNSensorCallable(link)));
//        }
//
//        LinkedList<Sensor> sensors = new LinkedList<>();
//        
//        for (Future<Sensor> futureSensor : futureSensors) {
//            try {
//                Sensor sensor = futureSensor.get();
//
//                if (sensor == null) {
//                    continue;
//                }
//
//                sensors.add(sensor);
//                logger.info("AQI stations scraper ended " + sensors.size() + " stations.");
//            } catch (Throwable th) {
//                logger.error("Can't wait for sensor.", th);
//            }
//        }
//        return sensors;
//    }
//    private List<String> getAllSensorsLinks() {
//        Document allSensorsWebPage;
//        try {
//            allSensorsWebPage = scrapingService.getDocumentFromLink(aqiCNScrapingGetAllSensorsURL);
//        } catch (Throwable th) {
//            logger.error("Can't parse all sensors web page.", th);
//            return null;
//        }
//
//        Elements allLinks = allSensorsWebPage.select("a");
//        List<String> sensorsLinks = new ArrayList<>();
//
//        for (Element link : allLinks) {
//            if (link == null || link.attr("href") == null) {
//                continue;
//            }
//
//            if (link.attr("href").contains(AQI_CITY_PREFIX)) {
//                sensorsLinks.add(link.attr("href"));
//            }
//        }
//        return sensorsLinks;
//    }
//    @Override
//    protected LinkedList<SensorMeasurement> getSensorsMeasurementsAsObjects() {
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//    }
}
