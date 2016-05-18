/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lbis.aerovibe.batch.server.schedule.tasks.impl.aqicn.concurrency;

import com.lbis.aerovibe.batch.server.services.ScrapingService;
import com.lbis.aerovibe.enums.DataProvidorsEnums;
import com.lbis.aerovibe.model.Sensor;
import com.lbis.aerovibe.spring.common.mapping.GsonBean;
import com.lbis.aerovibe.utils.AerovibeUtils;
import java.util.List;
import java.util.concurrent.Callable;
import org.apache.log4j.Logger;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 *
 * @author administrator
 */
@Component
@Scope(AerovibeUtils.PROTOTYPE)
public class AQICNSensorCallable implements Callable<Sensor> {

    String link;
    Logger logger = Logger.getLogger(AQICNSensorCallable.class);
    @Autowired
    ScrapingService scrapingService;
    @Autowired
    GsonBean gsonBean;

    public AQICNSensorCallable buildAQICNSensorCallable(String link) {
        this.link = link;
        return this;
    }

    @Override
    public Sensor call() throws Exception {
        Thread.currentThread().setName("AQICN scraper thread for " + link);
        Document currentSensorsWebPage;
        try {
            currentSensorsWebPage = scrapingService.getDocumentFromLink(link);
        } catch (Throwable th) {
            logger.error("Can't parse all sensors web page.", th);
            return null;
        }

        Element cityName = currentSensorsWebPage.getElementById("h1header1");

        if (cityName == null) {
            return null;
        }
        if (cityName.child(0) == null
                || cityName.child(0).getElementsByTag("b") == null
                || cityName.child(0).getElementsByTag("b").get(0) == null
                || cityName.child(0).getElementsByTag("b").get(0).childNodes() == null
                || cityName.child(0).getElementsByTag("b").get(0).childNodes().get(0) == null
                || cityName.child(0).getElementsByTag("b").get(0).childNodes().get(0).toString() == null) {
            return null;
        }
        String cityNameString = cityName.child(0).getElementsByTag("b").get(0).childNodes().get(0).toString();
        Elements jSScripts = currentSensorsWebPage.select("script");

        for (Element script : jSScripts) {

            if (script == null || script.data() == null) {
                continue;
            }

            if (script.data().length() > 20 && script.data().substring(0, 20).contains("mapCityData = ")) {
                String currentDetails = script.data().substring(script.data().indexOf(" = ") + 3, script.data().indexOf(";"));
                Station[] stations;
                try {
                 stations = gsonBean.getGson().fromJson(currentDetails, Station[].class);
                }catch (Throwable th){
                    logger.error("Failed to parse stations JSON.JSON is " +currentDetails + " url is " + link, th );
                    return null;
                }
                for (Station station : stations) {
                    if (station.getCity() == null || !station.getCity().equals(cityNameString)) {
                        continue;
                    }
                    return new Sensor(DataProvidorsEnums.AirQualityBeijing, station.getG()[0], station.getG()[1]);
                }
            }
        }
        return null;
    }

    class Station {

        String city;
        Double[] g;

        public String getCity() {
            return city;
        }

        public Double[] getG() {
            return g;
        }
    }
}
