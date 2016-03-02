package com.sreenath.regressionsuite.helper;

import java.io.IOException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sreenath.regressionsuite.constants.Constants;

public class ConfigReader {

    private static Properties properties = new Properties();

    private static final Logger LOGGER = LoggerFactory
            .getLogger(ConfigReader.class);
    static {
        try {
            properties.load(ClassLoader
                    .getSystemResourceAsStream(Constants.CONFIG_FILE_NAME));
        } catch (IOException e) {
            LOGGER.error("Exception while loading configuration file", e);
        }
    }

    public static String getProperty(String property) {

        Object propValue = properties.get(property);
        if (propValue == null) {
            LOGGER.error(
                    "Failed to get the {} from configuration property file",
                    property);
            return null;
        }
        return propValue.toString();
    }
}
