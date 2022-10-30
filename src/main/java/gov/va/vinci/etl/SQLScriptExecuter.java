package gov.va.vinci.etl;

import groovy.util.ConfigObject;
import org.apache.commons.cli.*;
import org.apache.ibatis.jdbc.ScriptRunner;

import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class SQLScriptExecuter implements Runnable {

    public static Logger LOGGER = LogManager.getLogger();
    private String url;
    private String driver;
    private ConfigObject configObject;
    private int id;
    private String name;
    private String readerConfigFile;

    private String sqlScriptFile;

    public static void main(String[] args) throws ParseException {
        Options options = new Options();
        options.addOption("dc", "dbConfigFile", true, "UIMA AS client configuration groovy script file.");
        options.addOption("sf", "sqlScriptFile", true, "Database reader configuration groovy script file.");
        options.addOption("id", "id", true, "id of this execution. Use this to differentiate different instances in logging. Default is 0.");
        options.addOption("name", "name", true, "Execute name. Use this to differentiate different instances in logging." +
                " Default is the sql script file name.");
        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, args);
        String dbConfigFile = cmd.getOptionValue("dbConfigFile");
        String sqlScriptFile = cmd.getOptionValue("sqlScriptFile");
        int id = Integer.parseInt(cmd.getOptionValue("id", "0"));
        String name = cmd.getOptionValue("sqlScriptFile", new File(sqlScriptFile).getName());
        new SQLScriptExecuter(id, name, dbConfigFile, sqlScriptFile).run();

    }

    public SQLScriptExecuter(int id, String name, String dbConfigFile, String sqlScriptFile) {
        init(id, name, dbConfigFile, sqlScriptFile);
    }

    public SQLScriptExecuter(String dbConfigFile, String sqlScriptFile) {
        init(0, new File(sqlScriptFile).getName(), dbConfigFile, sqlScriptFile);
    }


    public void init(int id, String name, String dbConfigFile, String sqlScriptFile) {
        this.id = id;
        this.name = name;
        this.readerConfigFile = dbConfigFile;
        this.sqlScriptFile = sqlScriptFile;
        this.configObject = GroovyUtils.parseGroovyConfig(readerConfigFile);
        this.driver = (String) this.configObject.get("driver");
        this.url = (String) this.configObject.get("url");
    }

    @Override
    public void run() {
        try {
            Class.forName(this.driver);
            Connection conn = DriverManager.getConnection(this.url);
            ScriptRunner runner = new ScriptRunner(conn);
            Reader br = new BufferedReader(new FileReader(sqlScriptFile));
            LOGGER.info("Load and execute sql script: " + sqlScriptFile);
            runner.runScript(br);
            conn.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }


}
