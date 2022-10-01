package gov.va.vinci.etl;

import com.google.gson.internal.LinkedTreeMap;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.logging.LogManager;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(OutputCaptureExtension.class)
class RunnerTest {



    @Test
    @EnabledOnOs({OS.LINUX, OS.MAC})
    void main() {
        Runner.main(new String[]{"conf/runner_config_01.json"});
    }

    @Test
    @EnabledOnOs({OS.LINUX, OS.MAC})
    void main2() {
        Runner.main(new String[]{"conf/runner_config_02.json", "false"});
    }

    @Test
    @EnabledOnOs({OS.LINUX, OS.MAC})
    void main3() {
        Runner.main(new String[]{"conf/runner_config_02.json", "true"});
    }

    @Test
    @EnabledOnOs({OS.LINUX, OS.MAC})
    void main4() {
        Runner.main(new String[]{"conf/runner_config_03.json", "true"});
    }

    @Test
    @EnabledOnOs({OS.LINUX, OS.MAC})
    void main5() {
        Runner.main(new String[]{"conf/runner_config_04.json", "true"});
    }

    @Test
    @EnabledOnOs({OS.LINUX, OS.MAC})
    void main6() {
        Runner.main(new String[]{"conf/runner_config_05.json", "true"});
    }

    @Test
    @EnabledOnOs({OS.WINDOWS})
    void TestSingleScriptRunner(CapturedOutput output) throws IOException, InterruptedException {
        LinkedTreeMap scriptConfig = new LinkedTreeMap();
        scriptConfig.put("name", "test_run");
        scriptConfig.put("location", "scripts/test1.bat");
        SingleScriptRunner srun = new SingleScriptRunner(scriptConfig);
        srun.executeTimes(1);
        assertThat(output).contains("OpenJDK");
    }

    @Test
    @EnabledOnOs({OS.WINDOWS})
    void TestSingleScriptRunner2(CapturedOutput output) throws IOException, InterruptedException {
        LinkedHashMap scriptConfig = new LinkedHashMap<>();
        scriptConfig.put("name", "test_run");
        scriptConfig.put("location", "C:\\Users\\VHASLCShiJ\\.jdks\\openjdk-18.0.2.1\\bin\\java.exe");
        scriptConfig.put("args","-version");
        scriptConfig.put("success","VM");
        SingleScriptRunner srun = new SingleScriptRunner(scriptConfig);
        srun.executeTimes(2);
        assertThat(output).contains("(test_run:0) result:1");
        assertThat(output).contains("(test_run:1) result:1");
    }

    @AfterEach
    void reset() throws Exception {
        LogManager.getLogManager().readConfiguration();
    }



}