package gov.va.vinci.leo;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RunnerTest {

    @Test
    void main() {
        Runner.main(new String[]{"conf/runner_config_01.json"});
    }

    @Test
    void main2() {
        Runner.main(new String[]{"conf/runner_config_02.json"});
    }
}