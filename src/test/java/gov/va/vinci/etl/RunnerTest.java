package gov.va.vinci.etl;

import com.google.gson.internal.LinkedTreeMap;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.boot.test.system.OutputCaptureRule;

import java.io.IOException;
import java.util.LinkedHashMap;

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


}