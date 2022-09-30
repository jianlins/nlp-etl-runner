package gov.va.vinci.etl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

import java.io.IOException;
import java.util.LinkedHashMap;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(OutputCaptureExtension.class)
public class TestRunnerCmd {
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
}
