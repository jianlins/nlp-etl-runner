# NLP-ETL-runner
This project is designed to simplify the execution of leo server and client, so that a single command can complete a leo pipeline execution. Additional, any error or output in terminal can be monitored and logged.

Desipte of the primary goal, the runner itself is relatively generic in design. That allows it to run any scripts or commands in sequence and monitor the outputs in terminals.

To configure a runner, use the conf/runner_config_01.json as an example configuration.

To execute a runner, run this command: 
```bash
 java -jar nlp-etl-runner-jdk8-jar-with-dependencies.jar <config json file path>
 # or if using jdk 11
 java -jar nlp-etl-runner-jdk11-jar-with-dependencies.jar <config json file path>
 
```

If you need to suppress the logging information, add log configuration in the command\
```bash
 java -Djava.util.logging.config.file=logging.properties -jar nlp-etl-runner-jdk8-jar-with-dependencies.jar <config json file path>
 # or if using jdk 11
 java -Djava.util.logging.config.file=logging.properties -jar nlp-etl-runner-jdk11-jar-with-dependencies.jar <config json file path>
 
```