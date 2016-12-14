package com.ywca.pentref;

import android.support.test.espresso.ViewInteraction;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import com.ywca.pentref.activities.ChoosePageActivity;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Before;
import org.junit.Rule;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withContentDescription;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withParent;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;

/**
 * Synchronises data with server in the {@link Before} annotated method.
 */
@RunWith(AndroidJUnit4.class)
public abstract class BaseTest {
    @Rule
    public ActivityTestRule<ChoosePageActivity> mActivityTestRule =
            new ActivityTestRule<>(ChoosePageActivity.class);

    @Before
    public void initialiseComponents() {
        // Click the settings item in the grid view shown in Launching Activity
        ViewInteraction gridView = onView(
                allOf(childAtPosition(
                        allOf(withId(R.id.grid_view),
                                withParent(withId(R.id.activity_launching))),
                        4),
                        isDisplayed()));
        gridView.perform(click());

        // Click synchronise with server button
        ViewInteraction linearLayout = onView(
                allOf(childAtPosition(withId(android.R.id.list), 1), isDisplayed()));
        linearLayout.perform(click());
    }

    /**
     * Changes to the fragment with text with the given resource id
     *
     * @param resourceId Resource id of the title of the fragment
     */
    void changeFragment(int resourceId) {
        // Open navigation drawer
        ViewInteraction openNavigationDrawerView = onView(
                allOf(withContentDescription("Open navigation drawer"),
                        withParent(withId(R.id.toolbar)),
                        isDisplayed()));
        openNavigationDrawerView.perform(click());

        // Select the fragment with the given text resource id
        ViewInteraction discoverView = onView(
                allOf(withId(R.id.design_menu_item_text), withText(resourceId), isDisplayed()));
        discoverView.perform(click());
    }

    Matcher<View> childAtPosition(
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