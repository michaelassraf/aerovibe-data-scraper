/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lbis.aerovibe.batch.server.services;

import com.lbis.aerovibe.batch.server.http.get.HttpGETFactory;
import org.apache.log4j.Logger;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ScrapingService {

    @Autowired
    HttpGETFactory httpGETFactory;

    Logger logger = Logger.getLogger(ScrapingService.class);

    public Document getDocumentFromLink(String link) throws Throwable {
        int retries = 5;
        while (retries > 0) {
            try {
                String response = httpGETFactory.getResponseFromCustomSocket(link);
                return Jsoup.parse(response);
            } catch (Throwable th) {
                logger.error("Failed to scrap link " + link + " will try " + retries + " more times.", th);
                Thread.sleep(30000);
                retries--;
            }
        }

//        Connection.Response response = Jsoup.connect(link)
//                .ignoreContentType(true)
//                .userAgent("Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:25.0) Gecko/20100101 Firefox/25.0")
//                .referrer("http://www.google.com")
//                .timeout(0)
//                .ignoreHttpErrors(true)
//                .followRedirects(true)
//                .execute();
        return null;
//        return response.parse();
    }

}
