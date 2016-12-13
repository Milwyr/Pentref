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
    public static final String SERVER_URL = "http://comp4521p1.cse.ust.hk";

    public static final String PREF_KEY_IS_FIRST_TIME_INSTALLED = "IsFirstTimeInstalled";
    public static final String PREF_KEY_NOTIFICATION_PREFERENCE = "pref_key_notification_minutes_before_departure";

    public static final String FRAGMENT_INDEX_EXTRA_KEY = "FragmentIndex";
    public static final String SELECTED_POI_EXTRA_KEY = "SelectedPoi";
    public static final String TRANSPORT_EXTRA_KEY = "Transport";
    public static final String USER_REVIEW_RATING_EXTRA_KEY = "Rating";
    public static final String USER_PROFILE_ID_EXTRA_KEY = "UserId";
    public static final String USER_PROFILE_NAME_EXTRA_KEY = "UserName";

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
     * @return A list of {@link LocalTime} objects that are later than the current time
     */
    public static List<LocalTime> getTimesAfterNow(List<LocalTime> localTimes) {
        // -1 indicates no restriction of number of returned results
        return getTimesAfterNow(localTimes, -1);
    }

    /**
     * Reads from the given {@link LocalTime} list, and returns a {@link LocalTime} list
     * with the time that are later than the current time.
     *
     * @param localTimes A list of {@link LocalTime} objects
     * @param max Maximum number of results
     * @return A list of {@link LocalTime} objects that are later than the current time
     */
    public static List<LocalTime> getTimesAfterNow(List<LocalTime> localTimes, int max) {
        List<LocalTime> nextTwoTimes = new ArrayList<>();
        LocalTime now = LocalTime.now();

        for (LocalTime localTime : localTimes) {
            if (localTime.isAfter(now)) {
                nextTwoTimes.add(localTime);

                // Breaks the loop when there number of LocalTime objects reaches maximum
                if (nextTwoTimes.size() == max) {
                    return nextTwoTimes;
                }
            }
        }
        return nextTwoTimes;
    }
}