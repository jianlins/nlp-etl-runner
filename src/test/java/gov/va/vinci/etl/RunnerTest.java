package gov.va.vinci.etl;

import com.google.gson.internal.LinkedTreeMap;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;

class RunnerTest {

    @Test
    void main() {
        Runner.main(new String[]{"conf/runner_config_01.json"});
    }

    @Test
    void main2() {
        Runner.main(new String[]{"conf/runner_config_02.json", "false"});
    }

    @Test
    void main3() {
        Runner.main(new String[]{"conf/runner_config_02.json", "true"});
    }

    @Test
    void main4() {
        Runner.main(new String[]{"conf/runner_config_03.json", "true"});
    }

    @Test
    void main5() {
        Runner.main(new String[]{"conf/runner_config_04.json", "true"});
    }

    @Test
    void main6() {
        Runner.main(new String[]{"conf/runner_config_05.json", "true"});
    }

    @Test
    void testSingleScriptRunner() throws IOException, InterruptedException {
        LinkedTreeMap scriptConfig = new LinkedTreeMap();
        scriptConfig.put("name", "test_run");
        scriptConfig.put("location", "scripts/test1.bat");
        SingleScriptRunner srun = new SingleScriptRunner(scriptConfig);
        srun.executeTimes(1);
    }

    @Test
    void testSingleScriptRunner2() throws IOException, InterruptedException {
        LinkedHashMap scriptConfig = new LinkedHashMap<>();
        scriptConfig.put("name", "test_run");
        scriptConfig.put("location", "C:\\Users\\VHASLCShiJ\\.jdks\\openjdk-18.0.2.1\\bin\\java.exe");
        scriptConfig.put("args","-version");
        scriptConfig.put("success","VM");
        SingleScriptRunner srun = new SingleScriptRunner(scriptConfig);
        srun.executeTimes(2);
    }
}