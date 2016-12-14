package com.ywca.pentref;

import android.support.test.espresso.ViewInteraction;
import android.support.test.espresso.assertion.ViewAssertions;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.ywca.pentref.activities.ChoosePageActivity;
import com.ywca.pentref.activities.TimetableActivity;
import com.ywca.pentref.fragments.TransportationFragment;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withContentDescription;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withParent;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anyOf;

/**
 * This Instrumentation test tests {@link TransportationFragment} and {@link TimetableActivity}.
 */
@LargeTest
@RunWith(AndroidJUnit4.class)
public class ScheduleTest extends BaseTest {
    @Test
    public void scheduleTest() {
        changeFragment(R.string.transport_schedule);

        // Repeat the test for the first three transportation
        for (int index = 0; index < 2; index++) {
            // The recycler view displays a list of transportation,
            // and the first transportation is selected
            ViewInteraction transportationRecyclerView = onView(
                    allOf(withId(R.id.transport_recycler_view), isDisplayed()));
            transportationRecyclerView.perform(actionOnItemAtPosition(index, click()));

            testTimetable();

            // Navigate to the previous Activity, i.e. TransportationFragment within MainActivity
            ViewInteraction backNavigationButton = onView(
                    allOf(withContentDescription("Navigate up"),
                            withParent(allOf(withId(R.id.action_bar),
                                    withParent(withId(R.id.action_bar_container)))),
                            isDisplayed()));
            backNavigationButton.perform(click());
        }
    }

    private void testTimetable() {
        // Check whether the departure text view shows Tai O by default
        onView(withId(R.id.departure_station_text_view))
                .check(ViewAssertions.matches(
                        anyOf(withText("Tai O"), withText("大澳"))));

        // Click show full timetable button
        ViewInteraction showFullTimeTableSwitch = onView(
                allOf(withId(R.id.show_full_timetable_switch), isDisplayed()));
        showFullTimeTableSwitch.perform(click());

        // Click change direction button
        ViewInteraction changeDirectionButton = onView(
                allOf(withId(R.id.change_direction_image_view), isDisplayed()));
        changeDirectionButton.perform(click());

        // Check whether the destination text view shows Tai O after the direction is changed
        onView(withId(R.id.destination_station_text_view))
                .check(ViewAssertions.matches(
                        anyOf(withText("Tai O"), withText("大澳"))));
    }
}