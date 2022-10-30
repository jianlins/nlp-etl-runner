package gov.va.vinci.etl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;


 import org.apache.logging.log4j.core.LoggerContext;
 import org.apache.logging.log4j.core.config.Configuration;
 import org.apache.logging.log4j.core.config.LoggerConfig;
public class CmdRunnable implements Runnable {
    protected int id;
    private final static Logger LOGGER = LogManager.getLogger();

    protected InputStream is, es;

    protected String successStr = "";
    protected HashMap<String, Map<String, String>> failureDict = new HashMap<>();

    protected int status = 0;

    protected Process process;

    protected ProcessBuilder pb;

    protected String name = "";

    private boolean wait = true;

    public CmdRunnable(int id, String name, ProcessBuilder pb, boolean wait, String successStr, HashMap<String, Map<String, String>> failureDict) {
        this.id = id;
        this.name = name;
        this.pb = pb;
        this.successStr = successStr;
        this.failureDict = failureDict;
        this.wait = wait;
        try {
            this.process = pb.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        // store parameter for later user
    }

    public void run() {
        HashMap<String, Pattern> failureRegex = new HashMap<>();
        try {
            if (wait) {
                this.is = process.getInputStream();
                this.es = process.getErrorStream();
                StringBuffer sb = new StringBuffer();
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(is));
                BufferedReader ereader = new BufferedReader(
                        new InputStreamReader(es));
                String line = "";
                String errorLine = "";

                while ((line = reader.readLine()) != null || (errorLine = ereader.readLine()) != null) {
                    if (LOGGER.isInfoEnabled()) {
                        if (line != null && line.trim().length() > 0) {
                            LOGGER.info(this.name + "[" + this.id + "]"+":\t"+line);
                        }
                        if (errorLine != null && errorLine.trim().length() > 0)
                            LOGGER.warn(this.name + "[" + this.id + "]"+":\t"+ errorLine);
                    }
                    sb.append(line).append('\n');
                    sb.append(errorLine).append('\n');

                    if (successStr.length() > 0 && ((line != null && line.contains(successStr)) || (errorLine != null && errorLine.contains(successStr)))) {
                        LOGGER.info(this.name + "[" + this.id + "]:\tProcess (" + name + ":" + id + ") execution success indicator detected, finish excecution.");
                        status = 1;
                        break;
                    }
                    if (failureDict.size() > 0) {
                        for (String ind : failureDict.keySet()) {
                            if (checkFailure(line, failureDict.get(ind), failureRegex) || checkFailure(errorLine, failureDict.get(ind), failureRegex)) {
                                LOGGER.error("Process " + this.name + "[" + this.id + "]" + " execution failure (" + failureDict.get(ind) + ") indicator detected, finish excecution.");
                                status = -1;
                                process.destroyForcibly();
                                break;
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            LOGGER.error("Java ProcessBuilder: IOException occured in process (" + name + ":" + id + ").");
            LOGGER.error(e.getMessage());
        } finally {
            try {
                if (is != null)
                    is.close();
                if (es != null)
                    es.close();
            } catch (IOException e) {
                LOGGER.warn(e.getMessage());
            }
        }
    }

    final static boolean checkFailure(String line, Map<String, String> stringStringHashMap, HashMap<String, Pattern> failureRegex) {
        if (line == null || line.length() == 0)
            return false;
        if (stringStringHashMap.containsKey("text")) {
            return line.contains(stringStringHashMap.get("text"));
        } else if (stringStringHashMap.containsKey("regex")) {
            String regex = stringStringHashMap.get("regex");
            if (!failureRegex.containsKey(regex)) {
                Pattern p = Pattern.compile(regex);
                failureRegex.put(regex, p);
            }
            return failureRegex.get(regex).matcher(line).find();
        }
        return false;
    }

    public Process getProcess() {
        return this.process;
    }
}

