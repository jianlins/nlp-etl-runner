package gov.va.vinci.etl;


import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;

public class Runner {
    private final static Logger LOGGER = LogManager.getLogger();

    public static void main(String[] args) {
        if (args.length == 0) {
            LOGGER.warn("A configuration file location needs to be specified.");
        }
        if (args.length==1)
            new Runner(args[0]);
        else
            new Runner(args[0], args[1].toLowerCase().startsWith("t") || args[1].startsWith("1"));
    }

    public Runner(String configFile){
        execute(configFile, true);
    }
    public Runner(String configFile, boolean stopWhenFail) {
        execute(configFile,stopWhenFail);
    }
    public void execute(String configFile, boolean stopWhenFail) {
        File configF = new File(configFile);
        if (!configF.exists()) {
            LOGGER.warn("configuration file doesn't exist, please check: " + configF.getAbsolutePath());
            System.exit(0);
        }
        Gson parser = new Gson();
        try {
            LinkedHashMap config = parser.fromJson(new FileReader(configF), LinkedHashMap.class);
            if (!config.containsKey("scripts")) {
                LOGGER.warn("scripts configuration needs start with 'scripts'.");
                return;
            }
            ArrayList scriptsConfig = (ArrayList) config.get("scripts");
            for (Object scriptConfig : scriptsConfig) {
                SingleScriptRunner srun = new SingleScriptRunner((LinkedTreeMap) scriptConfig);
                int repeat=srun.repeat;
                int[] status=srun.executeTimes(repeat);
                if (stopWhenFail) {
                    boolean success=true;
                    for( int st : status){
                        if(st<0) {
                            success=false;
                            break;
                        }
                    }
                    if (!success) {
                        LOGGER.warn("Execution stopped because of error while processing elt script:\"" +configFile+"\"\n\tat step: \""+ ((LinkedTreeMap) scriptConfig).get("name")+"\"");
                        break;
                    }
                }
            }
        } catch (FileNotFoundException e) {
            LOGGER.warn(e.getMessage());
        } catch (IOException e) {
            LOGGER.warn(e.getMessage());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
