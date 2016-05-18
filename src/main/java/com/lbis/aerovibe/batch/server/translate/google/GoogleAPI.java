/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lbis.aerovibe.batch.server.translate.google;

import com.lbis.aerovibe.utils.AerovibeUtils;
import com.lbis.aerovibe.batch.server.http.get.HttpGETFactory;
import com.lbis.aerovibe.spring.common.properties.PropertiesFieldNames;
import java.net.URLEncoder;
import java.nio.charset.Charset;
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
public class GoogleAPI {

    @Value(PropertiesFieldNames.googleTranslateBaseURL)
    private String googleTranslateBaseURL;

    @Autowired
    protected HttpGETFactory httpGETFactory;

    Logger logger = Logger.getLogger(GoogleAPI.class);

    public String translate(String stringToTranslate) {

        if (stringToTranslate == null) {
            return null;
        }

        String translationRawResponse = null;
        String translationResponse = null;

        String stringToTranslateEncoded = stringToTranslate.replaceAll("\\?", "");
        stringToTranslateEncoded = stringToTranslateEncoded.replaceAll("\\''", "-");
        stringToTranslateEncoded = stringToTranslateEncoded.replaceAll("\\'", "");
        stringToTranslateEncoded = stringToTranslateEncoded.replaceAll("\"", "-");

        try {
            stringToTranslateEncoded = URLEncoder.encode(stringToTranslateEncoded, Charset.forName("UTF-8").name());
            translationRawResponse = httpGETFactory.getResponseFromURL(buildTranslationURL(stringToTranslateEncoded));
        } catch (Throwable th) {
            logger.error("Can't get response from Google translation API", th);
        }
        if (translationRawResponse != null && translationRawResponse.indexOf("\"") > 1) {
            String[] results = translationRawResponse.split("\"");
            translationResponse = results[1];
        }
        if (translationResponse == null || translationResponse.isEmpty()) {
            return stringToTranslate;
        }
        return translationResponse;
    }

    String buildTranslationURL(String stringToTranslate) {
        return googleTranslateBaseURL.replaceAll("%10%", stringToTranslate);
    }
}
