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
import java.util.ArrayList;
import java.util.List;

/**
 * A utility class that includes constants and helper methods.
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

    /**
     * Reads from the given {@link LocalTime} list, and returns a {@link LocalTime} list
     * with the time that are later than the current time.
     *
     * @param localTimes A list of {@link LocalTime} objects
     * @returnA list of {@link LocalTime} objects that are later than the current time
     */
    public static List<LocalTime> getTimesAfterNow(List<LocalTime> localTimes) {
        List<LocalTime> nextTwoTimes = new ArrayList<>();
        LocalTime now = LocalTime.now();

        for (LocalTime localTime : localTimes) {
            if (localTime.isAfter(now)) {
                nextTwoTimes.add(localTime);

                // Breaks the loop when there are already two LocalTime objects
                if (nextTwoTimes.size() == 2) {
                    return nextTwoTimes;
                }
            }
        }
        return nextTwoTimes;
    }
}