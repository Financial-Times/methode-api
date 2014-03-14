package com.ft.methodeapi.atc;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.joda.time.DateTime;

import java.io.IOException;

/**
 * ISO8601DateFormatter
 *
 * @author Simon.Gibbs
 */
public class ISO8601DateFormatter extends JsonSerializer<DateTime> {

    @Override
    public void serialize(DateTime value, JsonGenerator jsonGenerator,
                          SerializerProvider provider) throws IOException {

        jsonGenerator.writeString(value.toString());
    }
}