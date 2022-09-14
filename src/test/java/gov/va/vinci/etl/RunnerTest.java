package gov.va.vinci.etl;

import org.junit.jupiter.api.Test;

class RunnerTest {

    @Test
    void main() {
        Runner.main(new String[]{"conf/runner_config_01.json"});
    }

    @Test
    void main2() {
        Runner.main(new String[]{"conf/runner_config_02.json","false"});
    }
    @Test
    void main3() {
        Runner.main(new String[]{"conf/runner_config_02.json","true"});
    }
    @Test
    void main4() {
        Runner.main(new String[]{"conf/runner_config_03.json","true"});
    }

    @Test
    void main5() {
        Runner.main(new String[]{"conf/runner_config_04.json","true"});
    }
}