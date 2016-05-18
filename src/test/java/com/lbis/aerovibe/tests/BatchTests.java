/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lbis.aerovibe.tests;

import com.lbis.aerovibe.batch.server.schedule.tasks.impl.GetSensorsFromAirQualityEgg;
import com.lbis.aerovibe.batch.server.schedule.tasks.impl.GetSensorsFromBeijingAirPollution;
import com.lbis.aerovibe.batch.server.services.AQICNScrapingService;
import com.lbis.aerovibe.model.Sensor;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 *
 * @author Development User
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("file:src/main/webapp/WEB-INF/aerovibe-batch-server-dispatcher.xml")
public class BatchTests {

    @Autowired
    GetSensorsFromBeijingAirPollution getSensorsFromBeijingAirPollution;

    @Autowired
    AQICNScrapingService aQICNScrapingService;

    @Test
    public void getAllAQICNFiles() {

//        LinkedList<Sensor> ss = getSensorsFromBeijingAirPollution.getStationsAsSensorsObjects();
//        for (Sensor ssss : ss) {
//            System.err.println(ssss.getSensorLatitude());
//        }

    }
}
