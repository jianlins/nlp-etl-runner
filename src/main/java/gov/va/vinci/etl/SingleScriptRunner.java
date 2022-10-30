package gov.va.vinci.etl;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class SingleScriptRunner {


    /**
     * Execute a script or command and monitor it's output.
     *
     * @author by Jianlin Shi on 03/08/2022.
     */
    private final static Logger LOGGER = LogManager.getLogger();
    protected String scriptName = "";
    protected String scriptLocation = "";
    protected boolean wait = true;
    protected String successStr = "";
    protected HashMap<String, Map<String, String>> failureDict = new HashMap<>();
    protected File logDir = null;
    protected String args = "";
    protected SimpleDateFormat date_format = new SimpleDateFormat("MM/dd/yyy HH:mm:ss");
    protected int repeat = 1;

    protected boolean generateRepeatId = false;
    protected int repeatInterval = 5;


    private final ArrayList<CmdRunnable> runners = new ArrayList<>();
    private int interval = 5;

    public SingleScriptRunner(Iterable<String> args) {
        LinkedHashMap<String, String> config = new LinkedHashMap<>();
        ArrayList<String> argList = new ArrayList<>();
        for (String arg : args)
            argList.add(arg);
        if (argList.size() == 0) {
            throw new IllegalArgumentException("args need to have at least one item to specify which command to run");
        }
        config.put("location", argList.get(0));
        if (argList.size() > 1) {
            config.put("args", String.join(" ", argList.subList(1, argList.size())));
        }
        init(config);
    }

    public SingleScriptRunner(Map scriptConfig) {
        init(scriptConfig);
    }

    public void init(Map scriptConfig) {
        if (scriptConfig.containsKey("location")) {
            File scriptFile = new File((String) scriptConfig.get("location"));
            if (!scriptFile.exists()) {
                scriptLocation = checkOSCommandExists((String) scriptConfig.get("location"));
                if (scriptLocation.length() > 0) {
                    LOGGER.warn("Found system command: " + this.scriptLocation);
                } else {
                    LOGGER.warn("The script file doesn't exist. This execution will be skipped. Please check: " + this.scriptLocation);
                    return;
                }
            } else {
                this.scriptLocation = scriptFile.getAbsolutePath();
            }
        } else {
            LOGGER.warn("The location of script has not been set. This execution will be skipped.");
            return;
        }
        if (scriptConfig.containsKey("name")) {
            this.scriptName = (String) scriptConfig.get("name");
        } else {
            this.scriptName = FilenameUtils.removeExtension(new File(this.scriptLocation).getName());
            LOGGER.info("script name has not been set, infer a name from script file name as: " + this.scriptName);
        }
        if (scriptConfig.containsKey("wait")) {
            Object value = scriptConfig.get("wait");
            this.wait = value.toString().toLowerCase().startsWith("t");
        }
        if (scriptConfig.containsKey("repeat") && scriptConfig.get("repeat").toString().length() > 0) {
            this.repeat = (int) Double.parseDouble(scriptConfig.get("repeat").toString());
        }
        if (scriptConfig.containsKey("interval") && scriptConfig.get("interval").toString().length() > 0) {
            this.interval = (int) Double.parseDouble(scriptConfig.get("interval").toString());
        }
        if (scriptConfig.containsKey("generate_repeat_id") && scriptConfig.get("generate_repeat_id").toString().length() > 0
                && scriptConfig.get("generate_repeat_id").toString().toLowerCase().startsWith("t")) {
            this.generateRepeatId = true;
        }
        if (scriptConfig.containsKey("repeat_interval") && scriptConfig.get("repeat_interval").toString().length() > 0) {
            this.repeatInterval = Integer.parseInt(scriptConfig.get("repeat_interval").toString());
        }
        if (scriptConfig.containsKey("success")) {
            this.successStr = (String) scriptConfig.get("success");
        } else {
            LOGGER.info("The success indication string has not been set in the configuration file.");
        }
        if (scriptConfig.containsKey("failure")) {
            Map failureStr = (Map) scriptConfig.get("failure");
            for (Object key : failureStr.keySet()) {
                failureDict.put((String) key, (Map) failureStr.get(key));
            }
        } else {
            LOGGER.info("The failure indication string has not been set in the configuration file.");
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

    public int[] executeTimes(int repeatTimes) throws IOException, InterruptedException {
        String[] argsArray = args.split(" +");
        int padArg = 1;
        if (generateRepeatId) {
            padArg = 2;
        }
        String[] combinedArgs = new String[argsArray.length + padArg];
        combinedArgs[0] = this.scriptLocation;
        System.arraycopy(argsArray, 0, combinedArgs, padArg, argsArray.length);
        final int[] status = new int[repeatTimes];
        for (int i = 0; i < repeatTimes; i++) {
            if (generateRepeatId) {
                combinedArgs[1] = "" + i;
            }
            ProcessBuilder pb = new ProcessBuilder(combinedArgs);
            pb.redirectInput(ProcessBuilder.Redirect.PIPE);
            CmdRunnable runner = new CmdRunnable(i, scriptName, pb, wait, successStr, failureDict);
            runners.add(runner);
            new Thread(runner).start();
        }
        if (wait) {
            for (int id = 0; id < repeatTimes; id++) {
                if (runners.get(id).getProcess()!=null && runners.get(id).getProcess().isAlive()) {
                    TimeUnit.SECONDS.sleep(interval);
                    LOGGER.debug("Java Process (" + scriptName + ":" + id + ") current status:" + runners.get(id).status);
                    id--;
                } else {
                    LOGGER.debug("Java Process (" + scriptName + ":" + id + ") result:" + runners.get(id).status);
                    status[id] = runners.get(id).status;
                }
            }
        }
        return status;
    }

    public ArrayList<CmdRunnable> getRunners() {
        return runners;
    }

    private void printInstructions() {
        System.err.println("PipelineRunner takes 0~3 parameters:\n" +
                "0 parameter: PipelineRunner will run all the active pipelines based on the default db configuration file: conf/edw.xml\n" +
                "1 parameter: if the parameter is a number, PipelineRunner will only run this pipeline id based on default db configuration file; otherwise," +
                "  it will consider this parameter is a db configuration file path, and run all the pipeline using this configuration." +
                "2 parameters: PipelineRunner will consider the 1st one is the db configuration file, and the 2nd is the pipeline id.\n"
        );
    }

    public static String checkOSCommandExists(String cmd) {
        for (String path : System.getenv("PATH").split(";")) {
            File dir = new File(path);
            if (dir.exists()) {
                File absPath = new File(dir, cmd);
                if (absPath.exists())
                    return absPath.getAbsolutePath();
            }
        }
        return "";
    }
}

