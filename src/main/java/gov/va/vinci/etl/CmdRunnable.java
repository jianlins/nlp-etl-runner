package gov.va.vinci.etl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

public class CmdRunnable implements Runnable {
    protected int id;
    protected Logger LOGGER;

    protected InputStream is, es;

    protected String successStr = "";
    protected HashMap<String, Map<String, String>> failureDict = new HashMap<>();

    protected int status = 0;

    protected Process process;

    protected String name = "";

    public CmdRunnable(int id, String name, Process process, Logger LOGGER, String successStr, HashMap<String, Map<String, String>> failureDict) {
        this.id = id;
        this.name = name;
        this.LOGGER = LOGGER;
        this.is = process.getInputStream();
        this.es = process.getErrorStream();
        this.successStr = successStr;
        this.failureDict = failureDict;
        this.process = process;


        // store parameter for later user
    }

    public void run() {
        StringBuffer sb = new StringBuffer();
        HashMap<String, Pattern> failureRegex = new HashMap<>();
        try {
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(is));
            BufferedReader ereader = new BufferedReader(
                    new InputStreamReader(es));
            String line = "";
            String errorLine = "";

            while ((line = reader.readLine()) != null || (errorLine = ereader.readLine()) != null) {
                if (LOGGER.isLoggable(Level.INFO)) {
                    if (line != null && line.trim().length()>0)
                        LOGGER.logp(Level.INFO, "", "", line);
                    if (errorLine != null && errorLine.trim().length()>0)
                        LOGGER.logp(Level.INFO, "", "", errorLine);
                }
                sb.append(line).append('\n');
                sb.append(errorLine).append('\n');

                if (successStr.length() > 0 && ((line!=null && line.contains(successStr)) || (errorLine!=null && errorLine.contains(successStr)))) {
                    LOGGER.logp(Level.INFO, "", "", "Process (" + name + ":" + id + ") execution success indicator detected, finish excecution.");
                    status = 1;
                    break;
                }
                if (failureDict.size() > 0) {
                    for (String ind : failureDict.keySet()) {
                        if (checkFailure(line, failureDict.get(ind), failureRegex) || checkFailure(errorLine, failureDict.get(ind), failureRegex)) {
                            LOGGER.warning("Process " + name + ":" + id + " execution failure (" + failureDict.get(ind) + ") indicator detected, finish excecution.");
                            status = -1;
                            process.destroyForcibly();
                            break;
                        }
                    }
                }
            }
        } catch (IOException e) {
            LOGGER.warning("Java ProcessBuilder: IOException occured in process (" + name + ":" + id + ").");
            LOGGER.warning(e.getMessage());
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                LOGGER.warning(e.getMessage());
            }
        }
    }

    final static boolean checkFailure(String line, Map<String, String> stringStringHashMap, HashMap<String, Pattern> failureRegex) {
        if (line==null || line.length()==0)
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

