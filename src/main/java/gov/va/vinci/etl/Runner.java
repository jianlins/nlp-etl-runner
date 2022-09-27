package gov.va.vinci.etl;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Logger;

public class Runner {
    private final static Logger LOGGER = Logger.getLogger(Runner.class.getName());

    public static void main(String[] args) {
        if (args.length == 0) {
            LOGGER.warning("A configuration file location needs to be specified.");
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
            LOGGER.warning("configuration file doesn't exist, please check: " + configF.getAbsolutePath());
            System.exit(0);
        }
        JSONParser parser = new JSONParser();
        try {
            JSONObject config = (JSONObject) parser.parse(new FileReader(configF));
            if (!config.containsKey("scripts")) {
                LOGGER.warning("scripts configuration needs start with 'scripts'.");
                return;
            }
            JSONArray scriptsConfig = (JSONArray) config.get("scripts");
            for (Object scriptConfig : scriptsConfig) {
                SingleScriptRunner srun = new SingleScriptRunner((JSONObject) scriptConfig);
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
                    if (!success)
                        LOGGER.warning("Execution stopped because of error while processing "+((JSONObject) scriptConfig).get("name"));
                }
            }
        } catch (FileNotFoundException e) {
            LOGGER.warning(e.getMessage());
        } catch (IOException e) {
            LOGGER.warning(e.getMessage());
        } catch (ParseException e) {
            LOGGER.warning(e.getMessage());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
