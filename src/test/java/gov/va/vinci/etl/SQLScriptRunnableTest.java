package gov.va.vinci.etl;

import org.apache.commons.cli.ParseException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

class SQLScriptRunnableTest {
    @Test
    public void test1() {
        Logger LOGGER = Logger.getLogger(SQLScriptExecuter.class.getName());
        LOGGER.setLevel(Level.INFO);
        SQLScriptExecuter ssr = new SQLScriptExecuter(0, "sql_script/test.sql", "conf/BatchDatabaseCollectionReaderConfig.groovy",
                "scripts/sql/test.sql");
        ssr.run();
    }

    @Test
    public void test2() throws ParseException {
        SQLScriptExecuter.main(new String[]{"-id", "0", "-name", "sql_script/test.sql",
                "-dc", "conf/BatchDatabaseCollectionReaderConfig.groovy",
                "-sf", "scripts/sql/test.sql"});
    }

    @Test
    @EnabledOnOs({OS.WINDOWS})
    public void test3() throws IOException, InterruptedException {
        LinkedHashMap scriptConfig = new LinkedHashMap<>();
        scriptConfig.put("name", "test_sql");
        scriptConfig.put("location", "D:\\Java\\jdk1.8.0_221\\bin\\java.exe");
        scriptConfig.put("args", "-Djava.library.path=\"target/dist/lib/\" -cp " +
                "\"target/dist/lib/*\" gov.va.vinci.etl.SQLScriptExecuter -dc conf/BatchDatabaseCollectionReaderConfig.groovy -sf scripts/sql/test.sql");
        scriptConfig.put("success", "VM");
        SingleScriptRunner srun = new SingleScriptRunner(scriptConfig);
        srun.executeTimes(1);
    }

    @Test
    @EnabledOnOs({OS.WINDOWS})
    public void test4() throws IOException, InterruptedException {
        LinkedHashMap scriptConfig = new LinkedHashMap<>();
        scriptConfig.put("name", "test_sql");
        scriptConfig.put("location", "D:\\Java\\jdk1.8.0_221\\bin\\java.exe");
        scriptConfig.put("args", "-Djava.library.path=\"target/dist/lib/\" -cp " +
                "\"target/dist/lib/*\" gov.va.vinci.etl.SQLScriptExecuter -dc conf/BatchDatabaseCollectionReaderConfig.groovy -sf scripts/sql/preprocessing_checker.sql");
        scriptConfig.put("success", "VM");
        SingleScriptRunner srun = new SingleScriptRunner(scriptConfig);
        srun.executeTimes(1);
    }

    @Test
    @EnabledOnOs({OS.WINDOWS})
    void TestSingleScriptRunner3() throws IOException, InterruptedException {
        LinkedHashMap scriptConfig = new LinkedHashMap<>();
        scriptConfig.put("name", "test_sql");
        scriptConfig.put("location", "scripts/test_sql.bat");
        scriptConfig.put("args", "");
        LinkedHashMap<String, HashMap<String, String>> failures = new LinkedHashMap<>();
        HashMap<String, String> clue = new HashMap<>();
        clue.put("text", "Pre-process step 1: count docs to process\t[nlp].[CurrentETLUpdate_NegCovAll]");
        failures.put("processed", clue);
        scriptConfig.put("failure", failures);
        SingleScriptRunner srun = new SingleScriptRunner(scriptConfig);
        srun.executeTimes(1);
        System.out.println(srun.getRunners().get(0).status);
    }


}