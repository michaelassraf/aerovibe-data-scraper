/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lbis.aerovibe.batch.server.schedule.tasks.impl;

import com.google.api.client.util.Charsets;
import com.lbis.aerovibe.batch.server.schedule.tasks.GetSensorsAbs;
import com.lbis.aerovibe.batch.server.schedule.tasks.impl.elm.model.SensorResult;
import com.lbis.aerovibe.batch.server.schedule.tasks.impl.elm.model.SensorResults;
import com.lbis.aerovibe.enums.DataProvidorsEnums;
import com.lbis.aerovibe.enums.PollutantsEnums;
import com.lbis.aerovibe.model.Sensor;
import com.lbis.aerovibe.model.SensorMeasurement;
import com.lbis.aerovibe.model.SensorMeasurementValue;
import com.lbis.aerovibe.spring.common.properties.PropertiesFieldNames;
import com.lbis.aerovibe.utils.AerovibeUtils;
import java.lang.reflect.Field;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
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
public class GetSensorsFromELM extends GetSensorsAbs {

    private final int sensorsDetailsInterval = 6 * 60 * 60 * 1000;
    private final int sensorMeasurementsInterval = 15 * 60 * 1000;

    Logger logger = Logger.getLogger(GetSensorsFromELM.class);

    @Value(PropertiesFieldNames.elmWebServiceStationsURL)
    String elmWebServiceStationsURL;

    @Value(PropertiesFieldNames.elmWebServiceSingleStationValuesURL)
    String elmWebServiceSingleStationValuesURL;

    DateFormat inputDateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");

    @Override
    @Scheduled(cron = "0 0 12 * * ?")
    public void getSensorsDetails() {
        Thread.currentThread().setName(GetSensorsFromELM.class.getSimpleName() + " details interval");
        logger.info("** Starting ELM stations interval. **");
        sensors = getStationsAsSensorsObjects();
        refineAndDropSensorDetailsInDB();
        logger.info("** Finished ELM stations interval. **");
    }

//        @Scheduled(cron = "0 2,7,12,17,22,27,32,37,42,47,52,57 * * * ?")
    
    @Override
    @Scheduled(initialDelay = 225000L, fixedDelay = 300000L)
    public void getSensorsMeasurements() {
        Thread.currentThread().setName(GetSensorsFromELM.class.getSimpleName() + " data interval");
        logger.info("** Starting ELM stations data interval. **");
        sensorMeasurements = getSensorsMeasurementsAsObjects();
        refineAndDropSensorMeasurementsInDB();
        logger.info("** Finished ELM stations data interval. **");
    }

    @Override
    protected LinkedList<SensorMeasurement> getSensorsMeasurementsAsObjects() {
        LinkedList<SensorMeasurement> sensorMeasurements = new LinkedList<>();
        LinkedList<Sensor> sensors = getStationsAsSensorsObjects();
        for (Sensor sensor : sensors) {
            try {
                SensorResults sensorResults = giveMeFuckingValueForStation("" + sensor.getSensorExternalId());

                if (sensorResults == null) {
                    continue;
                }

                SensorResult sensorResult[] = sensorResults.getResults();

                Long latestUpadeteValue = -1L;

                int latestIndex = 0;

                if (sensorResult == null) {
                    continue;
                }

                if (sensorResult.length < 1) {
                    continue;
                }

                for (int i = 0; i < sensorResult.length; i++) {
                    String dateToParse = sensorResult[i].getUnixTime();
                    dateToParse = dateToParse.replaceAll("  ", " ");
                    Long currentValue = inputDateFormat.parse(dateToParse).getTime();
                    if (Long.compare(latestUpadeteValue, currentValue) < 0) {
                        latestUpadeteValue = currentValue;
                        latestIndex = i;
                    }
                }

                SensorResult mostUpdatedSensorResult = sensorResult[latestIndex];
                ArrayList<SensorMeasurementValue> sensorMeasurementValues = new ArrayList();
                for (Field currentField : sensorResult[latestIndex].getClass().getDeclaredFields()) {
                    if (!currentField.getType().equals(Float.class)) {
                        continue;
                    }
                    currentField.setAccessible(true);
                    Float currentValue = (Float) currentField.get(mostUpdatedSensorResult);
                    if (currentValue == null) {
                        continue;
                    }
                    PollutantsEnums measuredType = AerovibeUtils.getInstance().validatePollutant(currentField.getName(), currentValue);
                    if (measuredType == null) {
                        continue;
                    }
                    SensorMeasurementValue sensorMeasurementValue = new SensorMeasurementValue(measuredType, currentValue, latestUpadeteValue);
                    sensorMeasurementValues.add(sensorMeasurementValue);

                }

                if (sensorMeasurementValues.size() < 1) {
                    logger.debug("No values for station " + sensor.getObjectKey());
                    continue;
                }

                SensorMeasurement sensorMeasurement = new SensorMeasurement(latestUpadeteValue, sensor.getObjectKey(), DataProvidorsEnums.ELM, sensorMeasurementValues);
                sensorMeasurements.add(sensorMeasurement);
                logger.debug("Fetched values for station " + sensor.getObjectKey());
            } catch (Throwable th) {
                logger.error(sensor.getObjectKey() + " is screwed up !!! ", th);
            }
        }
        logger.debug("Finished grabing " + sensorMeasurements.size() + " sensor measurements.");
        return sensorMeasurements;
    }

    @Override
    protected LinkedList<Sensor> getStationsAsSensorsObjects() {
        LinkedList<Sensor> elmUnifiedSensors = new LinkedList<>();
        logger.debug("Starting to grab sensors for elm.");
        com.lbis.aerovibe.batch.server.schedule.tasks.impl.elm.model.Sensor[] elmSensors;
        try {
            elmSensors = giveMeAllTheFuckingStations();
            for (com.lbis.aerovibe.batch.server.schedule.tasks.impl.elm.model.Sensor sensor : elmSensors) {
                if (sensor.getLatitude() != null && sensor.getLatitude() != 0.0 && sensor.getLongitude() != null && sensor.getLongitude() != 0.0) {
                    elmUnifiedSensors.add(new Sensor("" + sensor.getSensorID(), DataProvidorsEnums.ELM, null, null, null, sensor.getLatitude(), sensor.getLongitude(), sensor.getAddress(), sensor.getDescription()));
                }
            }
        } catch (Throwable th) {
            logger.error("I can't get the fucking sensors !! Will exit now !", th);
            return null;
        }
        logger.debug("Recieved " + elmSensors.length + " stations !!");
        return elmUnifiedSensors;
    }

    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");

    private com.lbis.aerovibe.batch.server.schedule.tasks.impl.elm.model.Sensor[] giveMeAllTheFuckingStations() {
        return gsonBean.getGson().fromJson(httpGETFactory.getResponseFromURL(elmWebServiceStationsURL), com.lbis.aerovibe.batch.server.schedule.tasks.impl.elm.model.Sensor[].class);
    }

    private SensorResults giveMeFuckingValueForStation(String sensorId) {
        String encodedUrl = elmWebServiceSingleStationValuesURL.replace("%10%", sensorId);
        Long now = System.currentTimeMillis();
        String date = simpleDateFormat.format(new Date(now));
        String startDate = null;
        String endDate = null;

        try {
//            startDate = date + " 00:00";
//            endDate = date + " 23:59";

            startDate = URLEncoder.encode(date + " 00:00", Charsets.UTF_8.displayName());
            endDate = URLEncoder.encode(date + " 23:59", Charsets.UTF_8.displayName());
        } catch (Throwable th) {
            logger.error("Can't encode URL", th);
        }
        encodedUrl = encodedUrl.replace("%20%", startDate);
        encodedUrl = encodedUrl.replace("%30%", endDate);

        String reponseForSensorId = httpGETFactory.getResponseFromURL(encodedUrl);
        if (reponseForSensorId == null || reponseForSensorId.length() < 10) {
            logger.debug("Bad response for sensor Id " + sensorId + " in elm API");
            return null;
        }
        reponseForSensorId = reponseForSensorId.replace("\"\"", "null");
        SensorResults sensorResults = null;
        try {
            sensorResults = gsonBean.getGson().fromJson(reponseForSensorId, SensorResults.class);
        } catch (Throwable th) {
            logger.error("Failed to parse SensorResults. String was " + reponseForSensorId, th);
        }
        return sensorResults;
    }

}
