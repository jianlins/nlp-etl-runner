package gov.va.vinci.leo;

import org.apache.commons.io.FileUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

import org.apache.commons.io.FilenameUtils;
import org.json.simple.JSONObject;

import java.util.logging.Logger;

public class SingleScriptRunner {


    /**
     * Execute a script or command and monitor it's output.
     *
     * @author by Jianlin Shi on 03/08/2022.
     */
    private final static Logger LOGGER = Logger.getLogger(SingleScriptRunner.class.getName());
    protected String scriptName = "";
    protected String scriptLocation = "";
    protected boolean wait = true;
    protected String successStr = "";
    protected HashMap<String, String> failureDict = new HashMap<>();
    protected File logDir = null;
    protected String args = "";
    protected SimpleDateFormat date_format = new SimpleDateFormat("MM/dd/yyy HH:mm");


    public SingleScriptRunner(JSONObject scriptConfig) {
        if (scriptConfig.containsKey("location")) {
            this.scriptLocation = (String) scriptConfig.get("location");
            this.scriptLocation=new File(this.scriptLocation).getAbsolutePath();
            if (!new File(this.scriptLocation).exists()){
                LOGGER.warning("The script file doesn't exist. This execution will be skipped. Please check: "+this.scriptLocation);
            }
        } else {
            LOGGER.warning("The location of script has not been set. This execution will be skipped.");
            return;
        }
        if (scriptConfig.containsKey("name")) {
            this.scriptName = (String) scriptConfig.get("name");
        } else {
            this.scriptName = FilenameUtils.removeExtension(new File(this.scriptLocation).getName());
            LOGGER.info("script name has not been set, infer a name from script file name as: " + this.scriptName);
        }
        if (scriptConfig.containsKey("wait")) {
            Object value= scriptConfig.get("name");
        }
        if (scriptConfig.containsKey("success")) {
            this.successStr = (String) scriptConfig.get("success");
        } else {
            LOGGER.info("The success indication string has not been set in the configuration file.");
        }
        if (scriptConfig.containsKey("failure")) {
            JSONObject failureStr = (JSONObject) scriptConfig.get("failure");
            for (Object key : failureStr.keySet()) {
                failureDict.put((String) key, (String) failureStr.get(key));
            }
        } else {
            LOGGER.info("The success indication string has not been set in the configuration file.");
        }
        if (scriptConfig.containsKey("args")) {
            args = (String) scriptConfig.get("args");
        } else {
            LOGGER.info("The success indication string has not been set in the configuration file.");
        }
        if (scriptConfig.containsKey("logdir")) {
            this.logDir = new File((String) scriptConfig.get("logdir"));
            if (!logDir.exists()) {
                LOGGER.info("log directory doesn't exist, try to create one at: " + logDir.getAbsolutePath());
                try {
                    FileUtils.forceMkdir(logDir);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else {
            LOGGER.info("logdir hasn't been set, will skip logging to local file.");
        }
    }

    public void executeScript() throws IOException {
        ProcessBuilder pb = new ProcessBuilder(this.scriptLocation, args);
        final StringBuffer sb = new StringBuffer();
        int processComplete = -1;
        pb.redirectErrorStream(true);
        try {
            final Process process = pb.start();
            final InputStream is = process.getInputStream();
            // the background thread watches the output from the process
            new Thread(new Runnable() {
                public void run() {
                    try {
                        BufferedReader reader = new BufferedReader(
                                new InputStreamReader(is));
                        String line;
                        while ((line = reader.readLine()) != null) {
                            LOGGER.info(line);

                            sb.append(line).append('\n');

                            if (successStr.length()>0 && line.contains(successStr)){
                                LOGGER.info("Execution success indicator detected, finish excecution.");
                                break;
                            }
                            if(failureDict.size()>0){
                                for(String ind: failureDict.keySet()){
                                    if (line.contains(ind)){
                                        LOGGER.info("Execution failure ("+failureDict.get(ind)+") indicator detected, finish excecution.");
                                        break;
                                    }
                                }
                            }
                        }
                    } catch (IOException e) {
                        LOGGER.warning("Java ProcessBuilder: IOException occured.");
                        LOGGER.warning(e.getMessage());
                    } finally {
                        try {
                            is.close();
                        } catch (IOException e) {
                            LOGGER.warning(e.getMessage());
                        }
                    }
                }
            }).start();
            processComplete = process.waitFor();
            LOGGER.info("Java ProcessBuilder result:" + processComplete);
        } catch (Exception e) {
            LOGGER.warning(e.getMessage());
        }
//        if log directory is set in pipeline description, save the console's output into log file.
        if (logDir != null) {
            File logFile = new File(logDir, "pipeline_" + date_format.format(new Date()) + "_log.txt");
            FileUtils.writeStringToFile(logFile, sb.toString(), StandardCharsets.UTF_8);
        }
    }

    private void printInstructions() {
        System.err.println("PipelineRunner takes 0~3 parameters:\n" +
                "0 parameter: PipelineRunner will run all the active pipelines based on the default db configuration file: conf/edw.xml\n" +
                "1 parameter: if the parameter is a number, PipelineRunner will only run this pipeline id based on default db configuration file; otherwise," +
                "  it will consider this parameter is a db configuration file path, and run all the pipeline using this configuration." +
                "2 parameters: PipelineRunner will consider the 1st one is the db configuration file, and the 2nd is the pipeline id.\n"
        );
    }
}
