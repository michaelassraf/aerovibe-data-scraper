/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lbis.aerovibe.batch.server.photos.flicker;

import com.lbis.aerovibe.utils.AerovibeUtils;
import com.lbis.aerovibe.batch.server.http.get.HttpGETFactory;
import com.lbis.aerovibe.spring.common.mapping.GsonBean;
import com.lbis.aerovibe.batch.server.photos.flicker.model.PhotosWrapper;
import com.lbis.aerovibe.batch.server.photos.flicker.model.Place;
import com.lbis.aerovibe.batch.server.photos.flicker.model.PlacesWrapper;
import com.lbis.aerovibe.spring.common.properties.PropertiesFieldNames;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;
import java.util.Random;
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
public class FlickerAPI {

    @Autowired
    protected HttpGETFactory httpGETFactory;

    Logger logger = Logger.getLogger(FlickerAPI.class);

    Random random = new Random();

    @Autowired
    GsonBean gsonBean;

    @Value(PropertiesFieldNames.flickerBaseURL)
    private String flickerBaseURL;
    @Value(PropertiesFieldNames.flickerPhotoSearchMethod)
    private String flickerPhotoSearchMethod;
    @Value(PropertiesFieldNames.flickerTagsPrefix)
    private String flickerTagsPrefix;
    @Value(PropertiesFieldNames.flickerSafeSearchPrefix)
    private String flickerSafeSearchPrefix;
    @Value(PropertiesFieldNames.flickerFormatPrefix)
    private String flickerFormatPrefix;
    @Value(PropertiesFieldNames.flickerAPIKeyPrefix)
    private String flickerAPIKeyPrefix;
    @Value(PropertiesFieldNames.flickerPerPagePrefix)
    private String flickerPerPagePrefix;
    @Value(PropertiesFieldNames.flickerSortPrefix)
    private String flickerSortPrefix;
    @Value(PropertiesFieldNames.flickerContentTypePrefix)
    private String flickerContentTypePrefix;
    @Value(PropertiesFieldNames.flickerAPIKey)
    private String flickerAPIKey;
    @Value(PropertiesFieldNames.flickerLonPrefix)
    private String flickerLonPrefix;
    @Value(PropertiesFieldNames.flickerLatPrefix)
    private String flickerLatPrefix;
    @Value(PropertiesFieldNames.flickerPlaceSearchMethod)
    private String flickerPlaceSearchMethod;
    @Value(PropertiesFieldNames.flickerWOEIdPrefix)
    private String flickerWOEIdPrefix;
    @Value(PropertiesFieldNames.flickerAccuracyPrefix)
    private String flickerAccuracyPrefix;
    @Value("#{'" + PropertiesFieldNames.flickerTags + "'.split(',')}")
    private List<String> flickerTags;
    @Value(PropertiesFieldNames.flickerMediaPrefix)
    private String flickerMediaPrefix;
    @Value(PropertiesFieldNames.flickerGeoContextPrefix)
    private String flickerGeoContextPrefix;
    @Value(PropertiesFieldNames.flickerPlaceIdPrefix)
    private String flickerPlaceIdPrefix;

    private String buildURLWithTagsAndWOEId(List<String> tags, String wOEId) {
        StringBuilder tagsAsCommaDelimiter = null;
        if (tags != null) {
            tagsAsCommaDelimiter = new StringBuilder().append(tags.get(0));

            for (int i = 1; i < tags.size(); i++) {
                tagsAsCommaDelimiter.append(";").append(tags.get(i));
            }
        }
        return new StringBuilder().append(flickerBaseURL).append(flickerPhotoSearchMethod).append(flickerTagsPrefix).append(tagsAsCommaDelimiter != null ? tagsAsCommaDelimiter.toString() : "").append(flickerMediaPrefix).append(flickerGeoContextPrefix).append(flickerWOEIdPrefix).append(wOEId).append(flickerSafeSearchPrefix).append(flickerFormatPrefix).append(flickerAPIKeyPrefix).append(flickerAPIKey).append(flickerPerPagePrefix).append(flickerSortPrefix).append(flickerContentTypePrefix).toString();
    }

    private String buildURLWithPlaceIdAndWOEId(String placeId, String wOEId) {
        return new StringBuilder().append(flickerBaseURL).append(flickerPhotoSearchMethod).append(flickerPlaceIdPrefix).append(placeId).append(flickerMediaPrefix).append(flickerWOEIdPrefix).append(wOEId).append(flickerFormatPrefix).append(flickerPerPagePrefix).append(flickerSortPrefix).append(flickerContentTypePrefix).append(flickerAPIKeyPrefix).append(flickerAPIKey).toString();
    }

    private String buildURLForWOEId(Double latitude, Double longtitude, Integer accuracy) {
        return new StringBuilder().append(flickerBaseURL).append(flickerPlaceSearchMethod).append(flickerLatPrefix).append(latitude).append(flickerLonPrefix).append(longtitude).append(flickerAccuracyPrefix).append(accuracy).append(flickerMediaPrefix).append(flickerGeoContextPrefix).append(flickerFormatPrefix).append(flickerAPIKeyPrefix).append(flickerAPIKey).toString();

    }

    private Place getPlaceObject(Double latitude, Double longtitude) throws MalformedURLException, IOException {
        for (int i = 16; i > 0; i--) {
            String rawJsonReponse = httpGETFactory.getResponseFromURL(buildURLForWOEId(latitude, longtitude, i));

            if (rawJsonReponse != null && rawJsonReponse.length() > 16) {
                rawJsonReponse = rawJsonReponse.substring(14, rawJsonReponse.length() - 1);
            }

            PlacesWrapper placesWrapper = null;

            try {
                placesWrapper = gsonBean.getGson().fromJson(rawJsonReponse, PlacesWrapper.class);
            } catch (Throwable th) {
                logger.error("Can't parse Flicker response " + rawJsonReponse, th);
            }
            if (placesWrapper != null && placesWrapper.getPlaces() != null && placesWrapper.getPlaces().getPlace().length > 0) {
                return placesWrapper.getPlaces().getPlace()[0];
            }
        }
        return null;
    }

    public String[] getPlaceForGeopintObject(Double latitude, Double longtitude) {
        for (int i = 16; i > 0; i--) {
            String rawJsonReponse = null;
            try {
                rawJsonReponse = httpGETFactory.getResponseFromURL(buildURLForWOEId(latitude, longtitude, i));
            } catch (Throwable th) {
                logger.error("Can't get reponse from flicker API.", th);
            }

            if (rawJsonReponse != null && rawJsonReponse.length() > 16) {
                rawJsonReponse = rawJsonReponse.substring(14, rawJsonReponse.length() - 1);
            }

            PlacesWrapper placesWrapper = null;

            try {
                placesWrapper = gsonBean.getGson().fromJson(rawJsonReponse, PlacesWrapper.class);
                if (placesWrapper != null && placesWrapper.getPlaces() != null && placesWrapper.getPlaces().getPlace().length > 0) {
                    String[] parsedLocationString = placesWrapper.getPlaces().getPlace()[0].getName().split(",");
                    if (parsedLocationString != null && parsedLocationString.length > 2) {
                        return new String[]{parsedLocationString[0], parsedLocationString[parsedLocationString.length - 1]};
                    }
                }
            } catch (Throwable th) {
                logger.error("Can't parse Flicker response " + rawJsonReponse, th);
            }
        }
        return null;
    }

    public String scrapImageFromFlicker(Double latitude, Double longtitude) {
        Place place = null;
        try {
            place = getPlaceObject(latitude, longtitude);
            if (place == null) {
                throw new Throwable("woe Id is empty !!");
            }
        } catch (Throwable th) {
            logger.error("Failed to get woe Id from flicker", th);
            return null;
        }

        String locationImage = null;
        try {
            locationImage = getImageFromFlickerByPlaceIdAndWOEId(place.getPlace_id(), place.getWoeid());
            if (locationImage.isEmpty()) {
                logger.error("Can't get image from flicker API.");
            }
        } catch (Throwable th) {
            logger.error("Failed to get location image from flicker", th);
            return null;
        }

        return locationImage;
    }

    private String getImageFromFlickerByPlaceIdAndWOEId(String placeId, String wOEId) throws IOException {
        String rawJsonReponse = httpGETFactory.getResponseFromURL(buildURLWithPlaceIdAndWOEId(placeId, wOEId));

        if (rawJsonReponse != null && rawJsonReponse.length() > 16) {
            rawJsonReponse = rawJsonReponse.substring(14, rawJsonReponse.length() - 1);
        }
        PhotosWrapper photosWrapper = null;
        try {
            photosWrapper = gsonBean.getGson().fromJson(rawJsonReponse, PhotosWrapper.class);
        } catch (Throwable th) {
            logger.error("Can't parse Flicker response " + rawJsonReponse, th);
        }

        if (photosWrapper != null && photosWrapper.getPhotos() != null && photosWrapper.getPhotos().getPhoto().length > 0) {
            return photosWrapper.getPhotos().getPhoto()[Math.abs(random.nextInt(photosWrapper.getPhotos().getPhoto().length < 2 ? 1 : photosWrapper.getPhotos().getPhoto().length - 1))].getPhotoURL();
        }
        return null;
    }

    private String makeSingleFlickerImageCall(List<String> tags, String wOEId) throws IOException {
        String rawJsonReponse = httpGETFactory.getResponseFromURL(buildURLWithTagsAndWOEId(tags, wOEId));

        if (rawJsonReponse != null && rawJsonReponse.length() > 16) {
            rawJsonReponse = rawJsonReponse.substring(14, rawJsonReponse.length() - 1);
        }
        PhotosWrapper photosWrapper = null;
        try {
            photosWrapper = gsonBean.getGson().fromJson(rawJsonReponse, PhotosWrapper.class);
        } catch (Throwable th) {
            logger.error("Can't parse Flicker response " + rawJsonReponse, th);
        }

        if (photosWrapper != null && photosWrapper.getPhotos() != null && photosWrapper.getPhotos().getPhoto().length > 0) {
            return photosWrapper.getPhotos().getPhoto()[Math.abs(random.nextInt(photosWrapper.getPhotos().getPhoto().length < 2 ? 1 : photosWrapper.getPhotos().getPhoto().length - 1))].getPhotoURL();
        }
        return null;
    }

}
