package gov.va.vinci.etl;

import org.apache.commons.cli.ParseException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

class SQLScriptExecutorTest {
    @Test
    public void test1() {
        Logger LOGGER = Logger.getLogger(SQLScriptExecutor.class.getName());
        LOGGER.setLevel(Level.INFO);
        SQLScriptExecutor ssr = new SQLScriptExecutor(0, "test.sql", "conf/BatchDatabaseCollectionReaderConfig.groovy",
                "scripts/sql/test.sql");
        ssr.run();
    }

    @Test
    public void test2() throws ParseException {
        SQLScriptExecutor.main(new String[]{"-id", "0", "-name", "test.sql",
                "-dc", "conf/BatchDatabaseCollectionReaderConfig.groovy",
                "-sf", "scripts/sql/test.sql"});
    }

    @Test
    @EnabledOnOs({OS.WINDOWS})
    public void test3() throws IOException, InterruptedException {
        LinkedHashMap scriptConfig = new LinkedHashMap<>();
        scriptConfig.put("name", "test_sql");
        scriptConfig.put("location", "D:\\Java\\jdk1.8.0_221\\bin\\java.exe");
        scriptConfig.put("args","-Djava.library.path=\"target/dist/lib/\" -cp " +
                "\"target/dist/lib/*\" gov.va.vinci.etl.SQLScriptExecuter -dc conf/BatchDatabaseCollectionReaderConfig.groovy -sf scripts/sql/test.sql");
        scriptConfig.put("success","VM");
        SingleScriptRunner srun = new SingleScriptRunner(scriptConfig);
        srun.executeTimes(1);
    }


}