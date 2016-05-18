/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lbis.aerovibe.batch.server.schedule.tasks.impl;

import com.lbis.aerovibe.enums.DataProvidorsEnums;
import com.lbis.aerovibe.enums.PollutantsEnums;
import com.lbis.aerovibe.model.Sensor;
import com.lbis.aerovibe.model.SensorMeasurement;
import com.lbis.aerovibe.model.SensorMeasurementValue;
import com.lbis.aerovibe.utils.AerovibeUtils;
import com.lbis.aerovibe.spring.common.properties.PropertiesFieldNames;
import com.lbis.aerovibe.batch.server.schedule.tasks.GetSensorsAbs;
import com.lbis.aerovibe.batch.server.schedule.tasks.impl.imep.model.Monitor;
import com.lbis.aerovibe.batch.server.schedule.tasks.impl.imep.model.Station;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.TimeZone;
import javax.xml.bind.DatatypeConverter;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 *
 * @author Development User
 */
@Service
public class GetSensorsFromIsraelMinistryofEnvironmentalProtection extends GetSensorsAbs {

    private final int sensorsDetailsInterval = 6 * 60 * 60 * 1000;
    private final int sensorMeasurementsInterval = 15 * 60 * 1000;

    TimeZone telAvivTimeZone = TimeZone.getTimeZone("Asia/Tel_Aviv");

    Logger logger = Logger.getLogger(GetSensorsFromIsraelMinistryofEnvironmentalProtection.class);

    @Value(PropertiesFieldNames.iMEPWebServiceStationsURL)
    String iMEPWebServiceStationsURL;

    @Value(PropertiesFieldNames.iMEPWebServiceSingleStationValuesURL)
    String iMEPWebServiceSingleStationValuesURL;

    @Override
    @Scheduled(cron = "0 0 4 * * ?")
    public void getSensorsDetails() {
        Thread.currentThread().setName(GetSensorsFromIsraelMinistryofEnvironmentalProtection.class.getSimpleName() + " details interval");
        logger.info("** Starting Israel Ministry of Environmental Protection stations interval. **");
        sensors = getStationsAsSensorsObjects();
        refineAndDropSensorDetailsInDB();
        logger.info("** Finished Israel Ministry of Environmental Protection stations interval. **");
    }

//        @Scheduled(cron = "0 3,8,13,18,23,28,33,38,43,48,53,58 * * * ?")
    
    @Override
    @Scheduled(initialDelay = 75000L, fixedDelay = 300000L)
    public void getSensorsMeasurements() {
        Thread.currentThread().setName(GetSensorsFromIsraelMinistryofEnvironmentalProtection.class.getSimpleName() + " data interval");
        logger.info("** Starting Israel Ministry of Environmental Protection stations data interval. **");
        sensorMeasurements = getSensorsMeasurementsAsObjects();
        refineAndDropSensorMeasurementsInDB();
        logger.info("** Finished Israel Ministry of Environmental Protection stations data interval. **");
    }

    @Override
    protected LinkedList<SensorMeasurement> getSensorsMeasurementsAsObjects() {
        LinkedList<SensorMeasurement> sensorMeasurements = new LinkedList<>();
        LinkedList<Sensor> sensors = getStationsAsSensorsObjects();
        for (Sensor sensor : sensors) {
            try {
                Monitor[] monitors = giveMeFuckingValueForStation("" + sensor.getSensorExternalId());
                ArrayList<SensorMeasurementValue> sensorMeasurementValues = new ArrayList();
                if (monitors == null ){
                    continue;
                }
                Long latestUpadetValue = -1L;
                for (Monitor monitor : monitors) {
                    if (monitor.getLatestValue() != null) {

                        if (monitor.getName() == null) {
                            continue;
                        }

                        PollutantsEnums measuredType = AerovibeUtils.getInstance().validatePollutant(monitor.getName(), monitor.getLatestValue().getValue());

                        if (measuredType == null) {
                            continue;
                        }

                        Calendar sensorUpdateCalendar = DatatypeConverter.parseDateTime(monitor.getLatestValue().getDateTime());
                        sensorUpdateCalendar.setTimeZone(telAvivTimeZone);
                        Long sensorUpdateTimeInMili = sensorUpdateCalendar.getTimeInMillis();
                        SensorMeasurementValue sensorMeasurementValue = new SensorMeasurementValue(measuredType, monitor.getLatestValue().getValue(), sensorUpdateTimeInMili);
                        sensorMeasurementValues.add(sensorMeasurementValue);
                        if (Long.compare(latestUpadetValue, sensorMeasurementValue.getSensorMeasurementValueTimeStamp()) < 0) {
                            latestUpadetValue = sensorMeasurementValue.getSensorMeasurementValueTimeStamp();
                        }
                    }
                }
                if (Long.compare(latestUpadetValue, 0) < 0) {
                    latestUpadetValue = System.currentTimeMillis();
                }

                if (sensorMeasurementValues.size() < 1) {
                    logger.debug("No values for station " + sensor.getObjectKey());
                    continue;
                }
                SensorMeasurement sensorMeasurement = new SensorMeasurement(latestUpadetValue, sensor.getObjectKey(), DataProvidorsEnums.IsraeliMinistryofEnvoirment, sensorMeasurementValues);
                sensorMeasurements.add(sensorMeasurement);
                logger.debug("Fetched values for station " + sensor.getObjectKey());
            } catch (Throwable th) {
                logger.error(sensor.getObjectKey() + " is screwed up !!! ", th);
            }
        }
        logger.debug("Finished grabing " + sensorMeasurements.size() + " sensor measurements.");
        return sensorMeasurements;
    }

    private Station[] giveMeAllTheFuckingStations() throws IOException {
        return gsonBean.getGson().fromJson(httpGETFactory.getResponseFromURL(iMEPWebServiceStationsURL), Station[].class);
    }

    private Monitor[] giveMeFuckingValueForStation(String stationId) throws MalformedURLException, IOException {
        String url = iMEPWebServiceSingleStationValuesURL.replace("%20%", stationId);
        String rawJson = httpGETFactory.getResponseFromURL(url);
        if (rawJson == null) {
            return null;
        }
        String monitors = rawJson.substring(rawJson.indexOf("\"Monitors\"") + 11, rawJson.indexOf(",\"Index\""));
        if (monitors==null){
            return null;
        }
        return gsonBean.getGson().fromJson(monitors, Monitor[].class);
    }

    @Override
    protected LinkedList<Sensor> getStationsAsSensorsObjects() {
        LinkedList<Sensor> sensors = new LinkedList<>();
        logger.debug("Starting to grab sensors for israeli envoirment ministry.");
        Station[] stations;
        try {
            stations = giveMeAllTheFuckingStations();

            for (Station station : stations) {
                if (station.getLocation() != null && station.getLocation().getLatitude() != 0.0 && station.getLocation().getLongitude() != 0.0) {
                    sensors.add(new Sensor("" + station.getID(), DataProvidorsEnums.IsraeliMinistryofEnvoirment, null, null, null, station.getLocation().getLatitude(), station.getLocation().getLongitude(), station.getName(), station.getOwner()));
                }
            }
        } catch (Throwable th) {
            logger.error("I can't get the fucking stations !! Will exit now !", th);
            return null;
        }
        logger.debug("Recieved " + stations.length + " stations !!");
        return sensors;
    }

}
