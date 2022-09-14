# Leo-runner
This project is designed to simplify the execution of leo server and client, so that a single command can complete a leo pipeline execution. Additional, any error or output in terminal can be monitored and logged.

Desipte of the primary goal, the runner itself is relatively generic in design. That allows it to run any scripts or commands in sequence and monitor the outputs in terminals.

To configure a runner, use the conf/runner_config_01.json as an example configuration.

To execute a runner, run this command: 
```
java -jar leo-runner.jar gov.va.vinci.etl.Runner {config file location}
```