/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lbis.aerovibe.batch.server.location.geonames;

import com.lbis.aerovibe.utils.AerovibeUtils;
import com.lbis.aerovibe.batch.server.http.get.HttpGETFactory;
import com.lbis.aerovibe.batch.server.location.geonames.model.Geonames;
import com.lbis.aerovibe.spring.common.mapping.GsonBean;
import com.lbis.aerovibe.spring.common.properties.PropertiesFieldNames;
import java.util.List;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 *
 * @author Development User
 */
@Component
@Scope(AerovibeUtils.PROTOTYPE)
public class GeoNameAPI {

    Logger logger = Logger.getLogger(GeoNameAPI.class);

    @Value(PropertiesFieldNames.geoNamesURL)
    String geoNamesURL;

    @Value("#{'" + PropertiesFieldNames.geoNamesAccountList + "'.split(',')}")
    private List<String> geoNamesAccountList;

    @Autowired
    HttpGETFactory httpGETFactory;

    @Autowired
    GsonBean gsonBean;

    public Geonames getPlacesFromPoint(Double latitude, Double longtitude) {

        for (String geoNamesAccount : geoNamesAccountList) {
            String currentGeoNamesURL = new String(geoNamesURL);
            currentGeoNamesURL = currentGeoNamesURL.replace("%20%", Double.toString(latitude));
            currentGeoNamesURL = currentGeoNamesURL.replace("%30%", Double.toString(longtitude));
            currentGeoNamesURL = currentGeoNamesURL.replace("%40%", geoNamesAccount);
            Geonames geonames = null;
            try {
                geonames = gsonBean.getGson().fromJson(httpGETFactory.getResponseFromURL(currentGeoNamesURL), Geonames.class);
            } catch (Throwable th) {
                logger.error("Can't get reponse from geo name API", th);
            }
            if (geonames != null && geonames.getGeonames() != null && geonames.getGeonames().length > 0) {
                return geonames;
            }
        }
        return null;
    }

}
