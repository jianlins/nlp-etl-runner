package gov.va.vinci.etl;

import groovy.util.ConfigObject;
import groovy.util.ConfigSlurper;

import java.io.File;
import java.net.MalformedURLException;

public class GroovyUtils {
    public static ConfigObject parseGroovyConfig(String configFile) {
        ConfigSlurper configSlurper = new ConfigSlurper();
        ConfigObject configObject = new ConfigObject();
        try {
            configObject = configSlurper.parse(new File(configFile).toURI().toURL());
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        return configObject;
    }
}
