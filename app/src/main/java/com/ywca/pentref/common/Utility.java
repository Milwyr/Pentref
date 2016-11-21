package com.ywca.pentref.common;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.lang.reflect.Type;

/**
 * Created by Milton on 08/11/2016.
 */
public class Utility {
    public static final String SELECTED_POI_EXTRA_KEY = "SelectedPoi";
    public static final String USER_REVIEW_RATING_EXTRA_KEY = "Rating";

    public static final String TRANSPORTATION_JSON_FILE_NAME = "transports.json";

    /**
     * A serialiser that helps Gson to parse {@link LocalTime} object.
     */
    public static class LocalTimeSerializer implements
            JsonDeserializer<LocalTime>, JsonSerializer<LocalTime> {

        private static final DateTimeFormatter formatter = DateTimeFormat.forPattern("HH:mm");

        @Override
        public LocalTime deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {
            final String dateString = json.getAsString();

            if (dateString.length() == 0) {
                return null;
            } else {
                return formatter.parseLocalTime(dateString);
            }
        }

        @Override
        public JsonElement serialize(LocalTime src, Type typeOfSrc, JsonSerializationContext context) {
            String dateString = "";
            if (src != null) {
                dateString = formatter.print(src);
            }
            return new JsonPrimitive(dateString);
        }
    }
}