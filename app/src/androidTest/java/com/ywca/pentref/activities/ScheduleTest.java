package com.ywca.pentref.activities;

import android.support.test.espresso.ViewInteraction;
import android.support.test.espresso.assertion.ViewAssertions;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import com.ywca.pentref.R;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Before;
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

@LargeTest
@RunWith(AndroidJUnit4.class)
public class ScheduleTest {

    @Rule
    public ActivityTestRule<ChoosePageActivity> mActivityTestRule =
            new ActivityTestRule<>(ChoosePageActivity.class);

    @Before
    public void initialiseComponents() {
        // Launch the application
//        InstrumentationRegistry.getContext();
    }

    @Test
    public void scheduleTest() {
        // Click the schedule item in the grid view shown in Launching Activity
        ViewInteraction relativeLayout = onView(
                allOf(childAtPosition(
                        allOf(withId(R.id.grid_view),
                                withParent(withId(R.id.activity_launching))), 3),
                        isDisplayed()));
        relativeLayout.perform(click());

        // The recycler view displays a list of transportation,
        // and the first transportation is selected
        ViewInteraction transportationRecyclerView = onView(
                allOf(withId(R.id.transport_recycler_view), isDisplayed()));
        transportationRecyclerView.perform(actionOnItemAtPosition(0, click()));

        testTimetable();

        // Navigate to the previous Activity, i.e. TransportationFragment within MainActivity
        ViewInteraction backNavigationButton = onView(
                allOf(withContentDescription("Navigate up"),
                        withParent(allOf(withId(R.id.action_bar),
                                withParent(withId(R.id.action_bar_container)))),
                        isDisplayed()));
        backNavigationButton.perform(click());

        // The recycler view displays a list of transportation,
        // and the first transportation is selected
        transportationRecyclerView.perform(actionOnItemAtPosition(1, click()));
        testTimetable();
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

    private static Matcher<View> childAtPosition(
            final Matcher<View> parentMatcher, final int position) {

        return new TypeSafeMatcher<View>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("Child at position " + position + " in parent ");
                parentMatcher.describeTo(description);
            }

            @Override
            public boolean matchesSafely(View view) {
                ViewParent parent = view.getParent();
                return parent instanceof ViewGroup && parentMatcher.matches(parent)
                        && view.equals(((ViewGroup) parent).getChildAt(position));
            }
        };
    }
}