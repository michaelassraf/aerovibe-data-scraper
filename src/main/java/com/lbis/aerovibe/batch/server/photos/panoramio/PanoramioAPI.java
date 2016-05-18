/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lbis.aerovibe.batch.server.photos.panoramio;

import com.lbis.aerovibe.utils.AerovibeUtils;
import com.lbis.aerovibe.batch.server.http.get.HttpGETFactory;
import com.lbis.aerovibe.spring.common.mapping.GsonBean;
import com.lbis.aerovibe.batch.server.photos.panoramio.model.Photos;
import com.lbis.aerovibe.spring.common.properties.PropertiesFieldNames;
import java.util.Random;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 *
 * @author Assraf
 */
@Component
@Scope(AerovibeUtils.PROTOTYPE)
public class PanoramioAPI {

    Logger logger = Logger.getLogger(PanoramioAPI.class);

    @Autowired
    protected GsonBean gsonBean;

    @Autowired
    protected HttpGETFactory httpGETFactory;

    @Value(PropertiesFieldNames.panoramioBaseURL)
    private String panoramioBaseURL;
    @Value(PropertiesFieldNames.panoramioFromPrefix)
    private String panoramioFromPrefix;
    @Value(PropertiesFieldNames.panoramioToPrefix)
    private String panoramioToPrefix;
    @Value(PropertiesFieldNames.panoramioMinLonPrefix)
    private String panoramioMinLonPrefix;
    @Value(PropertiesFieldNames.panoramioMaxLonPrefix)
    private String panoramioMaxLonPrefix;
    @Value(PropertiesFieldNames.panoramioMinLatPrefix)
    private String panoramioMinLatPrefix;
    @Value(PropertiesFieldNames.panoramioMaxLatPrefix)
    private String panoramioMaxLatPrefix;
    @Value(PropertiesFieldNames.panoramioSizePrefix)
    private String panoramioSizePrefix;
    @Value(PropertiesFieldNames.panoramioMapFilterPrefix)
    private String panoramioMapFilterPrefix;

    Random random = new Random(System.currentTimeMillis());

    private String buildPanoramioURLWIth(Double latitude, Double longtitude) {
        Double bBoxMinLatitude = latitude - (latitude * 0.0001);
        Double bBoxMaxLatitude = latitude + (latitude * 0.0001);
        Double bBoxMinlongtitude = longtitude - (longtitude * 0.0001);
        Double bBoxMaxlongtitude = longtitude + (longtitude * 0.0001);

        return new StringBuilder().append(panoramioBaseURL).append(panoramioFromPrefix).append(0).append(panoramioToPrefix).append(20).append(panoramioMapFilterPrefix).append(panoramioSizePrefix).append(panoramioMaxLatPrefix).append(bBoxMaxLatitude).append(panoramioMinLatPrefix).append(bBoxMinLatitude).append(panoramioMaxLonPrefix).append(bBoxMaxlongtitude).append(panoramioMinLonPrefix).append(bBoxMinlongtitude).toString();
    }

    public String scrapPanoramioForImage(Double latitude, Double longtitude) {
        String imageURL = null;
        Photos photos = null;
        String rawResponse = null;
        try {
            rawResponse = httpGETFactory.getResponseFromURL(buildPanoramioURLWIth(latitude, longtitude));
        } catch (Throwable th) {
            logger.error("Can't get response from Panoramio service ", th);
            return null;
        }
        if (rawResponse != null && !rawResponse.isEmpty()) {
            try {
                photos = gsonBean.getGson().fromJson(rawResponse, Photos.class);
                if (photos == null) {
                    throw new Throwable("Photos object is empty");
                }

            } catch (Throwable th) {
                logger.error("Failed to parse paranimo data. Raw response is " + rawResponse, th);
            }
        }

        if (photos == null || photos.getCount() == null || photos.getCount() < 1 || photos.getPhotos() == null || photos.getPhotos().length < 1) {
            return null;
        }

        Integer randomPlaceInArray = random.nextInt(photos.getPhotos().length < 2 ? 1 : photos.getPhotos().length);
        if (randomPlaceInArray < 1 || photos.getPhotos().length < 1) {
            randomPlaceInArray = 0;
        }
        imageURL = photos.getPhotos()[randomPlaceInArray].getPhoto_file_url();

        return imageURL;
    }
}
