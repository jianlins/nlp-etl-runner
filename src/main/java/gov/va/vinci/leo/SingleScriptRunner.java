package gov.va.vinci.leo;

import org.apache.commons.io.FileUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

import org.json.simple.JSONObject;

import java.util.logging.Logger;

public class SingleScriptRunner {


    /**
     * Execute a script or command and monitor it's output.
     *
     * @author by Jianlin Shi on 03/08/2022.
     */
    protected String scriptName;
    protected String scriptLocation;
    protected boolean wait = true;
    protected String successStr;
    protected HashMap<String, String> failureStr;
    protected File logDir=new File("logs");
    protected SimpleDateFormat date_format = new SimpleDateFormat("MM/dd/yyy HH:mm");
    private final static Logger LOGGER = Logger.getLogger(SingleScriptRunner.class.getName());


    public SingleScriptRunner(JSONObject scriptConfig) {
        if (scriptConfig.containsKey("name")) {
            this.scriptName = (String) scriptConfig.get("name");
        } else {
            System.out.println();
        }

    }

    private void executePipeline(int pipelineId, Properties properties) throws IOException {
        String args = properties.getProperty("args");
        String version = properties.getProperty("version", "");
        if (version.length() > 0) {

            File execFile = new File(this.scriptLocation);
            if (execFile.exists()) {
                this.scriptLocation = execFile.getAbsolutePath();
                ProcessBuilder pb = new ProcessBuilder(this.scriptLocation, pipelineId + "", args);
                executePipeline(pb, logDir);
            } else {
                System.err.println("Script file: " + this.scriptLocation + " doesn't exists.");
            }
        } else {
            System.err.println("Faircode version hasn't been set in the definition of pipeline " + pipelineId + ".\n" +
                    " You need to set it using 'version=<version_name>' in 'PIPELINE_DESCRIPTION'.");
        }
    }

    private void executePipeline(ProcessBuilder pb, File logDir) throws IOException {
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
                            System.out.println(line);
                            sb.append(line).append('\n');
                        }
                    } catch (IOException e) {
                        System.out
                                .println("Java ProcessBuilder: IOException occured.");
                        e.printStackTrace();
                    } finally {
                        try {
                            is.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }).start();
            // Wait to get exit value
            // the outer thread waits for the process to finish
            processComplete = process.waitFor();
            System.out.println("Java ProcessBuilder result:" + processComplete);
        } catch (Exception e) {
            e.printStackTrace();
        }
//        if log directory is set in pipeline description, save the console's output into log file.
        if (logDir!=null) {
            if (!logDir.exists())
                FileUtils.forceMkdir(logDir);
            File logFile = new File(logDir, "pipeline_" +date_format.format(new Date()) + "_log.txt");
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
