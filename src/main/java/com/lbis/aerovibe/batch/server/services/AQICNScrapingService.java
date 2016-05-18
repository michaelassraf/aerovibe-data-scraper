/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lbis.aerovibe.batch.server.services;

import com.google.common.base.Charsets;
import com.lbis.aerovibe.batch.server.http.get.HttpGETFactory;
import com.lbis.aerovibe.model.Sensor;
import com.lbis.aerovibe.spring.common.properties.PropertiesFieldNames;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import net.lingala.zip4j.core.ZipFile;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.apache.log4j.Logger;
import org.codehaus.plexus.archiver.tar.TarGZipUnArchiver;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class AQICNScrapingService {

    Logger logger = Logger.getLogger(AQICNScrapingService.class);

    private final String AQICN_RESOURCE_FOLDER_NAME = File.separator + "AQICNScraper";

    private final String AQI_CITY_PREFIX= "http://aqicn.org/city";
    
    private final String OS_TYPE_PROPERTY_NAME = "os.name";

    private final String WINDOWS_OS_NAME = "win";

    File tempFolderPath;

    boolean isScriptRun = false;
    
    @Value(PropertiesFieldNames.aqiCNScrapingGetAllSensorsURL)
    String aqiCNScrapingGetAllSensorsURL;

    @Autowired
    HttpGETFactory httpGETFactory;

    @Value(PropertiesFieldNames.aqiCNTempFolderPath)
    String aqiCNTempFolderPath;

    @Value(PropertiesFieldNames.aqiCNExecFile)
    String aqiCNExecFile;

    @Value(PropertiesFieldNames.aqiCNLinuxExecCommand)
    String aqiCNLinuxExecCommand;

    @Value(PropertiesFieldNames.aqiCNWindowsExecCommand)
    String aqiCNWindowsExecCommand;

    @Value(PropertiesFieldNames.aqiCNWindowsKillCommand)
    String aqiCNWindowsKillCommand;

    @Value(PropertiesFieldNames.aqiCNLinuxKillCommand)
    String aqiCNLinuxKillCommand;

    @Value(PropertiesFieldNames.aqiCNWindowsListCommand)
    String aqiCNWindowsListCommand;

    @Value(PropertiesFieldNames.aqiCNLinuxListCommand)
    String aqiCNLinuxListCommand;

    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-M-yyyy_hh:mm:ss");

    ExecutorService executorService = Executors.newFixedThreadPool(1);
    private String AQI_TEMP_FILE_NAME = "AQICNScraper.zip";

    @PostConstruct
    private void preparePythonScriptsPlayground() {
        File file;
        try {
            file = new ClassPathResource(AQICN_RESOURCE_FOLDER_NAME).getFile();
        } catch (Throwable th) {
            logger.error("Can't find python files.", th);
            return;
        }
        file.setExecutable(true);
        file.setReadable(true);
        file.setWritable(true);
        file.setExecutable(true, true);
        file.setReadable(true, true);
        file.setWritable(true, false);
        logger.debug("Successfully gave python scripts X permissions.");
        File playgroundFolder = new File(aqiCNTempFolderPath);
        playgroundFolder.mkdirs();
        logger.debug("Successfully created temp folder " + playgroundFolder.getAbsolutePath());
        tempFolderPath = new File(aqiCNTempFolderPath);
    }

    @Async
    public ProcessResult execOSCommand(Logger logger, String... args) {
        int exitValue = -1;
        InputStreamReader inputStreamReader = null;
        BufferedReader bufferedReader = null;
        String logPath = new StringBuilder()
                .append(tempFolderPath.getAbsolutePath())
                .append(File.separator)
                .append(getPurePrcocessName(args[0]))
                .append(simpleDateFormat.format(new Date()))
                .append(".log").toString();
        try {
            logger.debug("Going to run command " + Arrays.deepToString(args));
            Process process = Runtime.getRuntime().exec(args);
            File logPathFile = new File(logPath);
            inputStreamReader = new InputStreamReader(process.getInputStream());
            bufferedReader = new BufferedReader(inputStreamReader);
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                FileUtils.writeStringToFile(logPathFile, line + "\n", true);
            }
            process.waitFor();
            process.destroy();
            logger.debug("Successfully ran command " + Arrays.deepToString(args));
        } catch (Throwable th) {
            logger.error("Failed to run " + Arrays.deepToString(args), th);
        } finally {
            if (inputStreamReader != null) {
                try {
                    inputStreamReader.close();
                } catch (Throwable th) {
                    logger.error("Can't close input stream.", th);
                }
            }
        }
        return new ProcessResult(logPath, exitValue);

    }

    public synchronized File[] getAQICNArrayFilesUsingScriptRun() {
        if (!isScriptRun()) {
            logger.debug("Python script is not running, will run it now.");
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    runScript();
                    logger.debug("Python script run ended.");
                }
            });
        } else {
            logger.debug("Python script is already running, will return the folder content.");
        }

        return tempFolderPath.listFiles();
    }

    public void runScript() {
        String pythonProcessName = aqiCNLinuxExecCommand;
        if (System.getProperty(OS_TYPE_PROPERTY_NAME).toLowerCase().contains(WINDOWS_OS_NAME)) {
            pythonProcessName = aqiCNWindowsExecCommand;
        }
        String scriptPath = null;
        try {
            scriptPath = new StringBuilder().append(new ClassPathResource(AQICN_RESOURCE_FOLDER_NAME + File.separator + aqiCNExecFile).getFile().getAbsolutePath()).toString();
        } catch (Throwable th) {
            logger.error("Can't get python script path.", th);
            return;
        }
        int exitValue = execOSCommand(logger, pythonProcessName,
                scriptPath,
                new StringBuilder().append(tempFolderPath.getAbsoluteFile()).append(File.separator).append("_").toString()).getProcessResultExitValue();
    }

    @PreDestroy
    private void killAllProcesses() {
        if (isWindows()) {
            execOSCommand(logger, aqiCNWindowsKillCommand, "/IM", new File(aqiCNWindowsExecCommand).getName(), "/F");
            return;
        }
        execOSCommand(logger, aqiCNLinuxKillCommand, "-9", "-f", aqiCNLinuxExecCommand);
    }

    private boolean isWindows() {
        return System.getProperty(OS_TYPE_PROPERTY_NAME).toLowerCase().contains(WINDOWS_OS_NAME);
    }

    private boolean isScriptRun() {
        String logPath;
        String processName;
        if (isWindows()) {
            processName = getPurePrcocessName(aqiCNWindowsExecCommand);
            logPath = execOSCommand(logger, aqiCNWindowsListCommand).getProcessResultLogPath();
        } else {
            processName = getPurePrcocessName(aqiCNLinuxExecCommand);
            logPath = execOSCommand(logger, aqiCNLinuxListCommand, "-ef").getProcessResultLogPath();
        }

        LineIterator it = null;
        try {
            it = FileUtils.lineIterator(new File(logPath), Charsets.UTF_8.toString());
            while (it.hasNext()) {
                String line = it.nextLine();
                if (line.contains(processName)) {
                    return true;
                }
            }
        } catch (Throwable th) {
            logger.error("Can't read file " + logPath, th);
        } finally {
            if (it != null) {
                it.close();
            }
        }
        return false;
    }

    public File[] getAQICNArrayFilesFromWeb(String currentUrl) {
        logger.debug("Cleaning directory " + aqiCNTempFolderPath);
        try {
            FileUtils.cleanDirectory(new File(aqiCNTempFolderPath));
        } catch (Throwable th) {
            logger.error("Failed to clean directory." + aqiCNTempFolderPath, th);
        }

        logger.debug("Downloading Tar file from " + currentUrl);
        File aQICNTempFile;
        try {
            aQICNTempFile = httpGETFactory.downloadFileFromWeb(currentUrl, aqiCNTempFolderPath + File.separator + AQI_TEMP_FILE_NAME);
        } catch (Throwable th) {
            logger.error("Can't download file from " + currentUrl, th);
            return null;
        }
        logger.debug("Successfully downloaded Zip file.");

        logger.info("Unzipping file " + aQICNTempFile.getAbsolutePath());
        try {
            readZipFile(aQICNTempFile.getAbsolutePath(), aqiCNTempFolderPath);
        } catch (Throwable th) {
            logger.error("Can't read Zip file " + aQICNTempFile.getAbsolutePath(), th);
            return null;
        }
        try {
            for (int i = 0; i < 5; i++) {
                Thread.sleep(500);
                if (aQICNTempFile.exists()) {
                    aQICNTempFile.delete();
                }
            }
            logger.debug("Successfully deleted Zip temp file " + aQICNTempFile.getAbsolutePath());
        } catch (Throwable th) {
            logger.error("Failed to delete file " + aQICNTempFile.getAbsolutePath(), th);
        }

        return new File(aqiCNTempFolderPath).listFiles();
    }

    private void readTarFile(String path, String destPath) {
        TarGZipUnArchiver tarGZipUnArchiver = new TarGZipUnArchiver();
        File pathFile = new File(path);
        tarGZipUnArchiver.setSourceFile(pathFile);
        tarGZipUnArchiver.setDestDirectory(new File(destPath));
        tarGZipUnArchiver.extract();
    }

    public void readZipFile(String source, String destination) {
        try {
            ZipFile zipFile = new ZipFile(source);
            zipFile.extractAll(destination);
            logger.debug("Extracted zip file " + source + " to " + destination);
        } catch (Throwable th) {
            logger.error("Failed to extract zip file " + source, th);
        }
    }

    class ProcessResult {

        private String processResultLogPath;
        private int processResultExitValue;

        public ProcessResult(String processResultLogPath, int processResultExitValue) {
            this.processResultLogPath = processResultLogPath;
            this.processResultExitValue = processResultExitValue;
        }

        public String getProcessResultLogPath() {
            return processResultLogPath;
        }

        public void setProcessResultLogPath(String processResultLogPath) {
            this.processResultLogPath = processResultLogPath;
        }

        public int getProcessResultExitValue() {
            return processResultExitValue;
        }

        public void setProcessResultExitValue(int processResultExitValue) {
            this.processResultExitValue = processResultExitValue;
        }
    }

    private String getPurePrcocessName(String processName) {
        File file = new File(processName);
        String processPureName = file.getName();
        return processPureName;

    }
    
    public Sensor[] getAllSensors() throws Throwable{
        
        Document doc = Jsoup.connect(aqiCNScrapingGetAllSensorsURL).get();
        Elements allLinks = doc.select("a");
        List<String> sensorsLinks = new ArrayList<>();
        
        for (Element link : allLinks){
            if (link == null || link.attr("href") == null){
                continue;
            }
            
            if (link.attr("href").contains(AQI_CITY_PREFIX)){
                sensorsLinks.add(link.attr("href"));
            }
        }
        
        return null;
    }
    
}
