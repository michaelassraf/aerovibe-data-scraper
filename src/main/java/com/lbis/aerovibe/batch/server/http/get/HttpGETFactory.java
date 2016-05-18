/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lbis.aerovibe.batch.server.http.get;

import com.lbis.aerovibe.utils.AerovibeUtils;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.Charset;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.log4j.Logger;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 *
 * @author Development User
 */
@Component
@Scope(AerovibeUtils.SINGLETON)
public class HttpGETFactory {

    Logger logger = Logger.getLogger(HttpGETFactory.class);

    public String getResponseFromURLUsingJavaHttpClient(String url) throws IOException {
        BufferedReader in = null;
        try {
            HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
            con.setRequestProperty(HttpHeaders.ACCEPT, "application/json");
            con.setRequestProperty(HttpHeaders.CONTENT_TYPE, "application/json;charset=utf-8");
            con.setRequestProperty(HttpHeaders.ACCEPT_CHARSET, "utf-8");
            con.setRequestProperty(HttpHeaders.ACCEPT_LANGUAGE, "en-US");
            con.setRequestProperty(HttpHeaders.USER_AGENT, "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2");
            in = new BufferedReader(
                    new InputStreamReader(con.getInputStream(), Charset.forName("UTF-8")));
            String inputLine;
            StringBuilder response = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            return response.toString();
        } catch (Throwable th) {
            logger.error("Can't preform HTTP GET call URL is " + url, th);
        } finally {
            in.close();
        }
        return null;
    }

    public String getResponseFromURL(String url) {
        return getResponseFromURL(url, null, null);
    }

    public String getResponseFromURL(String url, String authToken, String cookie) {
        CloseableHttpResponse closeableHttpResponse = null;
        CloseableHttpClient closeableHttpClient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(url);
        httpGet.setHeader(HttpHeaders.CONTENT_TYPE, "charset=utf-8");
        httpGet.setHeader(HttpHeaders.ACCEPT_CHARSET, "utf-8");
        httpGet.setHeader(HttpHeaders.ACCEPT_LANGUAGE, "en-US");
        httpGet.setHeader(HttpHeaders.ACCEPT, ContentType.APPLICATION_JSON.getMimeType());
        httpGet.setHeader(HttpHeaders.USER_AGENT, "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2");
        if (authToken != null) {
            httpGet.setHeader(HttpHeaders.AUTHORIZATION, authToken);
            logger.debug("Adding auth token to request " + authToken);
        }
        if (cookie !=null){
            httpGet.setHeader("Cookie",cookie);
        }
        
        try {
            closeableHttpResponse = closeableHttpClient.execute(httpGet);
        } catch (Throwable th) {
            logger.error("Failed to perform http get call, url was " + url, th);
            return null;
        }
        if (closeableHttpResponse.getStatusLine().getStatusCode() != 200) {
            logger.error("Error while tryng to get response from " + url + ". Status code was " + closeableHttpResponse.getStatusLine().getStatusCode());
            return null;
        }
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(closeableHttpResponse.getEntity().getContent(), Charset.forName("UTF-8")));
        } catch (Throwable th) {
            logger.error("Failed to read response in http get call", th);
            return null;
        }
        StringBuilder finalResponseString = new StringBuilder();
        try {
            String currentLine;
            while ((currentLine = br.readLine()) != null) {
                finalResponseString.append(currentLine);
            }
        } catch (Throwable th) {
            logger.error("Cant read http get call lines ", th);
            return null;
        }

        try {
            closeableHttpResponse.close();
            closeableHttpClient.close();
        } catch (Throwable th) {
            logger.error("Failed to open close connection.", th);
        }
        return finalResponseString.toString().isEmpty() ? null : finalResponseString.toString();
    }

    public File downloadFileFromWeb(String url, String filePath) throws Throwable {
        URL uRL = new URL(url);
        ReadableByteChannel readableByteChannel = Channels.newChannel(uRL.openStream());
        FileOutputStream fileOutputStream = new FileOutputStream(filePath);
        fileOutputStream.getChannel().transferFrom(readableByteChannel, 0, Long.MAX_VALUE);
        fileOutputStream.close();
        return new File(filePath);
    }

    public String getResponseFromCustomSocket(String url) {
        StringBuilder stringBuilder = null;
        try {
            url = url.replaceAll("http://", "");
            url = url.replaceAll("https://", "");
            String hostname = url.substring(0, url.indexOf("/"));
            int port = 80;
            InetAddress addr = InetAddress.getByName(hostname);
            Socket socket = new Socket(addr, port);
            socket.setSoTimeout(60000);
            String path = url.substring(url.indexOf("/"), url.length());
            BufferedWriter wr
                    = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF8"));
            wr.write("GET " + path + " HTTP/1.0\r\n");
            wr.write("User-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_4) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/37.0.2062.122 Safari/537.36\r\n");
            wr.write("Accept: text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8\r\n");
            wr.write("Accept-Language: en-US,en;q=0.8,he;q=0.6\r\n");
            wr.write("Host:aqicn.org\r\n");
            wr.write("Cookie:__uvt=; mp_super_properties=%7B%22all%22%3A%20%7B%22%24initial_referrer%22%3A%20%22%22%2C%22%24initial_referring_domain%22%3A%20%22%22%7D%2C%22events%22%3A%20%7B%7D%2C%22funnels%22%3A%20%7B%7D%7D; pgv_pvi=8876516352; __atuvc=9%7C35%2C58%7C36%2C5%7C37%2C19%7C38%2C44%7C39; __utma=42180789.1472092095.1408704482.1411490289.1411548506.22; __utmb=42180789.5.10.1411548507; __utmc=42180789; __utmz=42180789.1411408248.20.6.utmcsr=google|utmccn=(organic)|utmcmd=organic|utmctr=(not%20provided); uvts=1vhWnf27KoMLavFr\r\n");
            wr.write("Connection: Keep-Alive\r\n");
            wr.write("\r\n");

            wr.flush();
            BufferedReader rd = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            stringBuilder = new StringBuilder();
            String line;
            while ((line = rd.readLine()) != null) {
                stringBuilder.append(line);
            }
            wr.close();
            rd.close();
            socket.close();
        } catch (Throwable th) {
            logger.error("Failed to get response from  " + url, th);
        }
        if (stringBuilder == null) {
            return null;
        }
        return stringBuilder.toString();

    }

}
