package com.ywca.pentref;

import android.support.test.espresso.UiController;
import android.support.test.espresso.ViewAction;
import android.support.test.espresso.ViewInteraction;
import android.support.test.espresso.contrib.RecyclerViewActions;
import android.support.test.filters.LargeTest;
import android.support.test.runner.AndroidJUnit4;
import android.view.View;

import com.ywca.pentref.models.Poi;

import org.hamcrest.Matcher;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withClassName;
import static android.support.test.espresso.matcher.ViewMatchers.withContentDescription;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withParent;
import static android.support.test.espresso.matcher.ViewMatchers.withResourceName;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.is;

/**
 * This test case only works when the selected {@link Poi} has not been bookmarked before.
 * Otherwise error will occur.
 */
@LargeTest
@RunWith(AndroidJUnit4.class)
public class BookmarkTest extends BaseTest {
    @Test
    public void bookmarkTest() {
        changeFragment(R.string.discover);

        // Click search button to display the drop down list
        ViewInteraction searchButton = onView(allOf(withId(R.id.action_search),
                withContentDescription(R.string.search_hint), isDisplayed()));
        searchButton.perform(click());

        // Click on one of the results in the auto complete list
        ViewInteraction autoCompleteList = onView(
                allOf(withClassName(is("android.widget.RelativeLayout")),
                        withResourceName("content_main"), isDisplayed()));
        autoCompleteList.perform(click());

        // Click the bookmark button in PoiDetailsActivity
        ViewInteraction floatingActionButton = onView(
                allOf(withId(R.id.bookmark_fab),
                        withParent(allOf(withId(R.id.coordinator_layout),
                                withParent(withId(android.R.id.content)))),
                        isDisplayed()));
        floatingActionButton.perform(click());

        // Navigate back to MainActivity
        ViewInteraction appCompatImageButton = onView(
                allOf(withContentDescription("Navigate up"),
                        withParent(allOf(withId(R.id.toolbar),
                                withParent(withId(R.id.collapsing_toolbar)))),
                        isDisplayed()));
        appCompatImageButton.perform(click());

        changeFragment(R.string.bookmarks);

        // Click on the bookmark icon of the first row (with index 0)
        // of the recycler view in BookmarksFragment
        onView(withId(R.id.bookmarks_recycler_view))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0, new ViewAction() {
                    @Override
                    public Matcher<View> getConstraints() {
                        return click().getConstraints();
                    }

                    @Override
                    public String getDescription() {
                        return click().getDescription();
                    }

                    @Override
                    public void perform(UiController uiController, View view) {
                        click().perform(uiController, view.findViewById(R.id.bookmarked_image_view));
                    }
                }));

        // Click yes in the dialog box to confirm deletion of bookmark
        ViewInteraction yesButton = onView(
                allOf(withId(android.R.id.button1), withText(R.string.yes),
                        withParent(allOf(withClassName(is("android.widget.LinearLayout")),
                                withParent(withClassName(is("android.widget.LinearLayout"))))),
                        isDisplayed()));
        yesButton.perform(click());
    }
}