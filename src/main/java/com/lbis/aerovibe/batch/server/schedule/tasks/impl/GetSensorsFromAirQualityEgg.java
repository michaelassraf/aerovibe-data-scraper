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
import com.lbis.aerovibe.batch.server.schedule.tasks.impl.aqe.model.AQEDataStream;
import com.lbis.aerovibe.batch.server.schedule.tasks.impl.aqe.model.AQEResult;
import com.lbis.aerovibe.batch.server.schedule.tasks.impl.aqe.model.AQEResults;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
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
public class GetSensorsFromAirQualityEgg extends GetSensorsAbs {

    private final int sensorsDetailsInterval = 6 * 60 * 60 * 1000;
    private final int sensorMeasurementsInterval = 15 * 60 * 1000;

    Logger logger = Logger.getLogger(GetSensorsFromAirQualityEgg.class);

    @Value(PropertiesFieldNames.airQualityEggWebServiceStationsURL)
    String airQualityEggWebServiceStationsURL;
    @Value(PropertiesFieldNames.airQualityEggWebServiceStationsURLAuthToken)
    String airQualityEggWebServiceStationsURLAuthToken;

    private static final String GOOGLE_API_COOKIE = "PREF=ID=1111111111111111:FF=0:LD=en:TM=1436519780:LM=1436519780:GM=1:V=1:S=_RBCf-utpscoZXca; HSID=A-Iqh9mbufTOc0onp; APISID=Q5dqaaCAKxgHyTq9/A455RZmxfrn9iZyvu; SID=DQAAACEEAAAUj_7cjoY3BxJ7MlNXZGdZ4iur4GHtWsqzKWIbU3XvoutPlWYALDeRMUtcen6vjYrgYX46tSLiIv2BHl0iSzHPGJWwQVO-Itz5An2w110Dbxe7OLd--wKgPt5cz3Ux_Xgy_GtvCImHMrraDJExWRqyjFPbilQgAcLFpT-xUqP12N1cvXXgqQsvpj9Y8MEyJIeIgIkv1lR1f0SXYI6kECd8lPTPAjmkAF1I3djFRk9SLqv8jYezFO0eV8wr2h0b8JiPQ4hPU5uU-WGVZhIDHUt5hVbgwXd8hfWYCEHCymb2mOk3O53Ctof4FO87wzg0Idl7u8GYN-2T7RxDRvQrTzat2RI1zrd14i-FJbuez2bhG0wI3pb69RgaOeMlaIMvu-q7AZLZ4stezMGYuBeWa9PSX0lJjPanT2jA43vbUSOCuhrnXkUMq3J6chypKubSW2PfVZEiv3Iv0oQ7lmcLhM5NVZq0YTsRssR_WwAp-iyXYqHxQ70kBQycLg8vBfulYUusEAH_Zwq0-8YrN-M5-vGAmaLjaEGkheMI11GJ8yp6Aw4BfsOS_zZ385qgIXl4j8bALjUnF_-iVMF6X_djIiipu8Egb8sKxuaubeROaCkDGbY7DIjkCChuiy9o_PuN1nbOB8VI5faH5cczVOFiPvNhWB77L92rI4LyWm7gM4MIz-4qdpCJCdsb4Xws8ulLGZ22qeS3w-osUFUbeIfHTBC251ZOPAssj7KPq7NmHQXaiAdneiaHpwbPIgjmYEj8ijCIaFA97vDBtCNWIU0pTEosr2f17Wzg1PWH8Jjp4Qf3kPpgQBLqMwTZL59radKMoGO4a5LpLVhf_hRLiECSbr_YK9XtST0Hbzx1Wly9UoSvDutAB15P2iprQnXmEwjunDnK_lBsKgtlHh6DTPz2AZ61My0HLmZ3dr2UTdUBPdwiIUx8QAOAdcfcWXxps468KLrzxhtoLNDJLOREPB9-JW17oiyN306KfBRNnh8M4WwH4zKlq-8SWJT1XxRhY0ensIx1JlI4fT4ljSrZqSOrXF2tw9sq99t1izMSbXuCMOe9CrBC75UOUmWAA33N0sHAldo0GC-5lPnHRaPUhVrgtoqm9OyBEifCVbMUHdWywcbhVPP627y2YX4YCVFjBfsjZH65gcaCfp3ZbkwTx7dmkvac2CAo3keqDY3Jo028fNq_DGZqWceNUVsDt7QTV3r7ikMl3TCrrXA8yM8UzsuaLkhmkpRwxTvEvLKNT22_Ol-aysTreHqZEwGsnn9fTWeoSRfdoNcUlAYW3rgYcgn-5i21qO7qXU0-F-xvOQy8lpenOj3SUHwLNBzqAVvLzzuUKJJ2PJV_AoqVQrgnxxlgNdXabqlgAWcCGsLISh0wlCyJJcoHr5CHvOyHO0eSKrTIT6CxA84mzLo9Tt_uWWVpSxIS; NID=69=AmfqB07iNhe2Gyaqj7npvh1l6G2br0UhyLlieT2u4rR9j6Rsd84ro-Oc5DfKc5LrIPPUPJIvzAcJbE6dtTxYRvzIqYS-2E9cjoepkN0j_oPGBVjj_-wSPFrw44Fg7a3t4KRmUvyM8hPr-NzWXMwV1qjubWmKGq8sItaMIjYgeWr2nMwRDWgcqPpNOMi_L-X6iw_25AGNb4b_llG7wxGnqehzVCuN_uhxPUQJGQnYjW4hqpDQTQzV342nqQA8na4sElpS8NTfqH7nCJri1KT_lXwp0BunXbzryp0JguAZ3dbm3daW1s-DfUcCBUpkJ7FWMEdZY8mS9RbET8aW0ylPK0H5cbzXntIQt272Lbwm43d_jC7H0G9HK6QGf7-qFJbuuwNOqfhFJd-bw5z3FrQ; GOOGLE_ABUSE_EXEMPTION=ID=437521b2e564c47b:TM=1437120831:C=c:IP=192.114.23.209-:S=APGng0vRhuiSZxv-jSkjRfS30zDwyRGw4A";

    DateFormat inputDateFormat = new SimpleDateFormat("MMM d yyyy hh:mma");

    @Override
    @Scheduled(cron = "0 0 16 * * ?")
    public void getSensorsDetails() {
        Thread.currentThread().setName(GetSensorsFromAirQualityEgg.class.getSimpleName() + " details interval");
        logger.info("** Starting Air Quality Egg stations interval. **");
        sensors = getStationsAsSensorsObjects();
        refineAndDropSensorDetailsInDB();
        logger.info("** Finished Air Quality Egg stations interval. **");
    }

    @Override
    @Scheduled(initialDelay = 150000L, fixedDelay = 300000L)
    public void getSensorsMeasurements() {
        Thread.currentThread().setName(GetSensorsFromAirQualityEgg.class.getSimpleName() + " data interval");
        logger.info("** Starting Air Quality Egg stations data interval. **");
        sensorMeasurements = getSensorsMeasurementsAsObjects();
        refineAndDropSensorMeasurementsInDB();
        logger.info("** Finished Air Quality Egg stations data interval. **");
    }

    @Override
    protected LinkedList<SensorMeasurement> getSensorsMeasurementsAsObjects() {
        ArrayList<AQEResult> aQEResults = giveMeAllTheFuckingStations();
        LinkedList<SensorMeasurement> sensorMeasurements = new LinkedList<>();
        Calendar calendar = null;

        for (AQEResult aQEResult : aQEResults) {

            if (aQEResult.getLocation() == null || aQEResult.getLocation().getLat() == null || aQEResult.getLocation().getLon() == null || Double.compare(aQEResult.getLocation().getLat(), 0.0) == 0 || Double.compare(aQEResult.getLocation().getLon(), 0.0) == 0) {
                continue;
            }

            Sensor sensor = new Sensor(DataProvidorsEnums.AirQualityEgg, aQEResult.getLocation().getLat(), aQEResult.getLocation().getLon());
            AQEDataStream[] aQEDataStreams = aQEResult.getDatastreams();

            if (aQEDataStreams == null || aQEDataStreams.length < 1) {
                continue;
            }

            Long latestUpadeteValue = -1L;
            ArrayList<SensorMeasurementValue> sensorMeasurementValues = new ArrayList<>();
            for (AQEDataStream aQEDataStream : aQEDataStreams) {
                if (aQEDataStream.getAt() == null) {
                    continue;
                }

                try {
                    calendar = DatatypeConverter.parseDate(aQEDataStream.getAt());
                } catch (Throwable th) {
                    logger.error("Failed to parse date for " + aQEDataStream.getAt() + " for " + aQEDataStream.getId(), th);
                }
                String refineMeasurementId = aQEDataStream.getId();
                refineMeasurementId = refineMeasurementId
                        .replaceAll("CO_r", "BYPASS")
                        .replaceAll("NO2_r", "BYPASS")
                        .replaceAll("CO_raw", "BYPASS")
                        .replaceAll("Dust_r", "BYPASS")
                        .replaceAll("VOC_r", "BYPASS")
                        .replaceAll("O3_r", "BYPASS")
                        .replaceAll("Humidity_r", "BYPASS");
                Float currentValue = null;

                try {
                    currentValue = Float.parseFloat(aQEDataStream.getCurrent_value());
                } catch (Throwable th) {
                    logger.debug("Can't parse value for " + refineMeasurementId + " as " + aQEDataStream.getCurrent_value());
                }
                if (currentValue == null) {
                    continue;
                }

                PollutantsEnums measuredType = AerovibeUtils.getInstance().validatePollutant(refineMeasurementId, currentValue);

                if (measuredType == null || calendar == null) {
                    continue;
                }

                sensorMeasurementValues.add(new SensorMeasurementValue(measuredType, currentValue, calendar.getTimeInMillis()));
                latestUpadeteValue = Long.max(latestUpadeteValue, calendar.getTimeInMillis());
            }

            if (sensorMeasurementValues.size() < 1) {
                logger.debug("No values for station " + sensor.getObjectKey());
                continue;
            }

            SensorMeasurement sensorMeasurement = new SensorMeasurement(latestUpadeteValue, sensor.getObjectKey(), DataProvidorsEnums.AirQualityEgg, sensorMeasurementValues);
            sensorMeasurements.add(sensorMeasurement);
            logger.debug("Fetched values for station " + sensor.getObjectKey());
        }
        logger.debug("Finished grabing " + sensorMeasurements.size() + " sensor measurements.");
        return sensorMeasurements;
    }

    @Override
    protected LinkedList<Sensor> getStationsAsSensorsObjects() {
        LinkedList<Sensor> aQESensors = new LinkedList<>();
        logger.debug("Starting to grab sensors for Air Quality Egg.");
        ArrayList<AQEResult> aQEResults;
        try {
            aQEResults = giveMeAllTheFuckingStations();
            for (AQEResult aQEResult : aQEResults) {
                if (aQEResult.getLocation() == null || aQEResult.getLocation().getLat() == null || aQEResult.getLocation().getLon() == null || Double.compare(aQEResult.getLocation().getLat(), 0.0) == 0 || Double.compare(aQEResult.getLocation().getLon(), 0.0) == 0) {
                    continue;
                }
                aQESensors.add(new Sensor(DataProvidorsEnums.AirQualityEgg, aQEResult.getDescription(), aQEResult.getLocation().getLat(), aQEResult.getLocation().getLon()));
            }
        } catch (Throwable th) {
            logger.error("I can't get the fucking sensors !! Will exit now !", th);
            return null;
        }
        logger.debug("Recieved " + aQESensors.size() + " good stations !!");
        return aQESensors;
    }

    public ArrayList<AQEResult> giveMeAllTheFuckingStations() {
        AQEResults aQEResults = gsonBean.getGson().fromJson(httpGETFactory.getResponseFromURL(airQualityEggWebServiceStationsURL.replaceAll("%10%", "1"), airQualityEggWebServiceStationsURLAuthToken, GOOGLE_API_COOKIE), AQEResults.class);
        int numberOfPages = (aQEResults.getTotalResults() / 100) + 1;
        ArrayList<AQEResult> results = aQEResults.getResults();
        ExecutorService service = Executors.newFixedThreadPool(numberOfPages);
        List<Future<ArrayList<AQEResult>>> futures = new ArrayList<>();
        for (int i = 2; i <= numberOfPages; i++) {
            final int finali = i;
            futures.add(service.submit(() -> gsonBean.getGson().fromJson(httpGETFactory.getResponseFromURL(airQualityEggWebServiceStationsURL.replaceAll("%10%", String.valueOf(finali)), airQualityEggWebServiceStationsURLAuthToken, GOOGLE_API_COOKIE), AQEResults.class).getResults()));
        }

        for (Future<ArrayList<AQEResult>> singleList : futures) {
            try {
                results.addAll(singleList.get());
            } catch (Throwable th) {
                logger.error("Can't wait for AQE senors call.", th);
            }
        }

        return results;
    }

}
