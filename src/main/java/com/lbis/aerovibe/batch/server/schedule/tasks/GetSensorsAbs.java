/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lbis.aerovibe.batch.server.schedule.tasks;

import com.lbis.aerovibe.annotations.Translate;
import com.lbis.aerovibe.aqi.calc.AQICalculator;
import com.lbis.aerovibe.model.Sensor;
import com.lbis.aerovibe.model.SensorMeasurement;
import com.lbis.aerovibe.spring.common.couchbase.executors.SensorActions;
import com.lbis.aerovibe.spring.common.couchbase.executors.SensorMeasurementActions;
import com.lbis.aerovibe.batch.server.http.get.HttpGETFactory;
import com.lbis.aerovibe.batch.server.location.geonames.GeoNameAPI;
import com.lbis.aerovibe.batch.server.location.geonames.model.Geonames;
import com.lbis.aerovibe.spring.common.mapping.GsonBean;
import com.lbis.aerovibe.batch.server.photos.flicker.FlickerAPI;
import com.lbis.aerovibe.batch.server.photos.panoramio.PanoramioAPI;
import com.lbis.aerovibe.batch.server.translate.google.GoogleAPI;
import com.lbis.aerovibe.enums.ComparedLevel;
import java.lang.reflect.Field;
import java.util.LinkedList;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author Development User
 */
public abstract class GetSensorsAbs {

    Logger logger = Logger.getLogger(GetSensorsAbs.class);

    @Autowired
    protected SensorActions sensorActions;
    @Autowired
    protected GsonBean gsonBean;
    @Autowired
    protected HttpGETFactory httpGETFactory;
    @Autowired
    protected SensorMeasurementActions sensorMeasurementActions;
    @Autowired
    protected GeoNameAPI geoNameAPI;
    @Autowired
    protected PanoramioAPI panoramioAPI;
    @Autowired
    protected FlickerAPI flickerAPI;
    @Autowired
    protected GoogleAPI googleAPI;

    protected LinkedList<Sensor> sensors = new LinkedList();

    protected LinkedList<SensorMeasurement> sensorMeasurements = new LinkedList();

    public abstract void getSensorsDetails();

    public abstract void getSensorsMeasurements();

    protected void setLocationByPoints() {
        for (Sensor sensor : sensors) {
            String currentCountryName = null;
            String currentCityName = null;
            Geonames geonames = geoNameAPI.getPlacesFromPoint(sensor.getSensorLatitude(), sensor.getSensorLongitude());
            if (geonames == null || geonames.getGeonames() == null || geonames.getGeonames().length < 1) {
                logger.debug("Couldn't get location from geo name API will try flicker");
                String[] location = flickerAPI.getPlaceForGeopintObject(sensor.getSensorLatitude(), sensor.getSensorLongitude());
                if (location != null && location.length > 1) {
                    currentCountryName = location[1];
                    currentCityName = location[0];
                }
                logger.debug("Successfully got location from flicker API.");
            } else {
                logger.debug("Successfully got location from geoname API.");
                currentCountryName = geonames.getGeonames()[0].getCountryName();
                currentCityName = geonames.getGeonames()[0].getName();
            }

            if (currentCountryName != null && currentCityName != null) {
                currentCountryName = refineNames(currentCountryName);
                currentCityName = refineNames(currentCityName);
                sensor.setSensorCountry(currentCountryName);
                sensor.setSensorCity(currentCityName);
            } else {
                logger.error("Couldn't get location from any API, sensor was " + sensor.getObjectKey());
            }

            logger.debug("Successfully grabed location for " + sensor.getObjectKey() + " location is " + currentCountryName + ", " + currentCityName);
        }
    }

    private String refineNames(String originalName) {
        String[] allWords = originalName.split(" ");
        StringBuilder finalWord = new StringBuilder();
        for (String currentWord : allWords) {
            if (currentWord.length() < 1) {
                continue;
            }
            if (Character.isLowerCase(currentWord.charAt(0))) {
                finalWord.append(Character.toUpperCase(currentWord.charAt(0))).append(currentWord.substring(1, currentWord.length())).append(" ");
            } else {
                finalWord.append(currentWord).append(" ");
            }
        }
        if (finalWord.toString().contains("Palestin")) {
            finalWord = new StringBuilder("Israel");
        }
        return finalWord.toString();
    }

    protected void refineAndDropSensorDetailsInDB() {
        logger.debug("Starting to drop sensors in DB");
        try {
            setLocationByPoints();
            translateAllFieldsToEnglish();
            setImageAddressByGeoPoint();
            sensorActions.putAll(sensors);
            logger.debug("Polling interval call ended.");
        } catch (Throwable th) {
            logger.error("Failed to insert sensors to DB.", th);
        }
    }

    protected void translateAllFieldsToEnglish() {
        for (Sensor sensor : sensors) {
            Field[] sensorFields = sensor.getClass().getDeclaredFields();
            int successFields = 0;
            int failedFields = 0;
            for (Field currentSensorField : sensorFields) {
                if (currentSensorField.getAnnotations() != null && currentSensorField.isAnnotationPresent(Translate.class)) {
                    try {
                        currentSensorField.setAccessible(true);
                        String value = (String) currentSensorField.get(sensor);
                        if (value != null) {
                            value = googleAPI.translate(value);
                            currentSensorField.set(sensor, value);
                        }
                        successFields++;
                    } catch (Throwable th) {
                        failedFields++;
                    }
                }
            }
            logger.debug("Finished to translate sensor " + sensor.getObjectKey() + " successfully translated " + successFields + " fields, failed to translate " + failedFields + " fields.");
        }
    }

    protected void refineAndDropSensorMeasurementsInDB() {
        logger.debug("Starting to drop sensors measurements in DB");
        try {
            calcAQI(sensorMeasurements);
            setComparedLevel(sensorMeasurements);
            sensorMeasurementActions.putAll(sensorMeasurements);
            logger.debug("Polling interval call ended.");
        } catch (Throwable th) {
            logger.error("Failed to insert sensors measurements to DB.", th);
        }
    }

    private void calcAQI(LinkedList<SensorMeasurement> sensorMeasurements) {
        sensorMeasurements.stream().forEach((currentSensorMeasurement) -> {
            currentSensorMeasurement.setSensorMeasurementAQILevel(AQICalculator.getInstance().calcMaxAQI(currentSensorMeasurement));
        });
    }

    protected void setImageAddressByGeoPoint() {
        for (Sensor sensor : sensors) {
            String currentSensorImageAddressURL = panoramioAPI.scrapPanoramioForImage(sensor.getSensorLatitude(), sensor.getSensorLongitude());
            if (currentSensorImageAddressURL == null) {
                logger.error("Got bad response from panoramio for sensor " + sensor.getObjectKey() + " will try Flicker API");
                currentSensorImageAddressURL = flickerAPI.scrapImageFromFlicker(sensor.getSensorLatitude(), sensor.getSensorLongitude());
            } else {
                logger.debug("Successfully got response from panoramio API.");
            }
            if (currentSensorImageAddressURL != null) {
                sensor.setSensorAddressImage(currentSensorImageAddressURL);
                logger.debug("Successfully added image for sensor " + sensor.getObjectKey() + " image url is " + sensor.getSensorAddressImage());
            } else {
                logger.error("Can't get image for location for any API sensor was " + sensor.getObjectKey());
            }
        }
    }

    protected abstract LinkedList<SensorMeasurement> getSensorsMeasurementsAsObjects();

    protected abstract LinkedList<Sensor> getStationsAsSensorsObjects();

    private void setComparedLevel(LinkedList<SensorMeasurement> sensorMeasurements) {
        for (SensorMeasurement currentSensorMeasurement : sensorMeasurements) {
            ComparedLevel comparedLevel = ComparedLevel.SAME;
            SensorMeasurement oldSensorMeasurement = null;
            try {
                oldSensorMeasurement = sensorMeasurementActions.getLatestSensorMeasurementForSensorId(currentSensorMeasurement.getSensorMeasurementSensorId());
            } catch (Throwable th) {
                logger.error("Can't get old measurement for " + currentSensorMeasurement.getSensorMeasurementSensorId());
            }

            if (oldSensorMeasurement == null || oldSensorMeasurement.getSensorMeasurementAQILevel() < 1) {
                currentSensorMeasurement.setSensorMeasurementComparedLevel(comparedLevel);
                continue;
            }

            if (currentSensorMeasurement.getObjectKey().equals(oldSensorMeasurement.getObjectKey()) && oldSensorMeasurement.getSensorMeasurementComparedLevel() != null) {
                comparedLevel = oldSensorMeasurement.getSensorMeasurementComparedLevel();
                currentSensorMeasurement.setSensorMeasurementComparedLevel(comparedLevel);
                continue;
            }

            if (currentSensorMeasurement.getSensorMeasurementAQILevel() == oldSensorMeasurement.getSensorMeasurementAQILevel()) {
                currentSensorMeasurement.setSensorMeasurementComparedLevel(comparedLevel);
                continue;
            }

            if (oldSensorMeasurement.getSensorMeasurementAQILevel() > currentSensorMeasurement.getSensorMeasurementAQILevel()) {
                comparedLevel = ComparedLevel.BETTER;
            } else {
                comparedLevel = ComparedLevel.WORSE;
            }

            currentSensorMeasurement.setSensorMeasurementComparedLevel(comparedLevel);
        }
    }

}
