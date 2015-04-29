package com.ft.methodeapi;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.ft.methodeapi.acceptance.AcceptanceTestConfiguration;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.jayway.restassured.RestAssured;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

import static com.jayway.restassured.config.DecoderConfig.decoderConfig;
import static com.jayway.restassured.config.EncoderConfig.encoderConfig;
import static com.jayway.restassured.config.RestAssuredConfig.newConfig;
import static cucumber.runtime.MethodeApiObjectFactory.CONFIG_FILE_PROPERTY_NAME;

/**
 * SetUpHelper
 *
 * @author Simon.Gibbs
 */
public class SetUpHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(SetUpHelper.class);

    public static void configureRestAssuredEncoding() {
        RestAssured.config= newConfig()
        .decoderConfig((decoderConfig().defaultContentCharset("UTF-8")))
        .encoderConfig((encoderConfig().defaultContentCharset("UTF-8")));
    }

    public static AcceptanceTestConfiguration readConfiguration() {
        return readConfiguration(null);
    }
    
    public static AcceptanceTestConfiguration readConfiguration(String defaultConfigFile) {
        final String configFileName = System.getProperty(CONFIG_FILE_PROPERTY_NAME, defaultConfigFile);
        
        Preconditions.checkNotNull(Strings.emptyToNull(configFileName),
                "System property %s is null", CONFIG_FILE_PROPERTY_NAME);
        
        LOGGER.debug("{} = {}", CONFIG_FILE_PROPERTY_NAME, configFileName);
        final ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());
        try {
            final File file = new File(configFileName).getCanonicalFile();
            LOGGER.debug("using {} as config file", file);
            return objectMapper.readValue(file, AcceptanceTestConfiguration.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
