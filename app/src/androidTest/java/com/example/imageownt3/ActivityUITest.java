package com.example.imageownt3;


import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withClassName;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withParent;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.is;

import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import androidx.test.espresso.ViewInteraction;
import androidx.test.filters.LargeTest;
import androidx.test.rule.ActivityTestRule;
import androidx.test.rule.GrantPermissionRule;
import androidx.test.runner.AndroidJUnit4;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.hamcrest.core.IsInstanceOf;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class ActivityUITest {

    @Rule
    public ActivityTestRule<MainActivity> mActivityTestRule = new ActivityTestRule<>(MainActivity.class);

    @Rule
    public GrantPermissionRule mGrantPermissionRule =
            GrantPermissionRule.grant(
                    "android.permission.CAMERA");

    @Test
    public void textImgTest()
    {
        ViewInteraction switchCompat = onView(
                allOf(withId(R.id.speedSwitch), withId(R.id.speedSwitch),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                5),
                        isDisplayed()));
        switchCompat.perform(click());
        ViewInteraction materialButton = onView(
                allOf(withId(R.id.btnCamera), withId(R.id.btnCamera),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                0),
                        isDisplayed()));
        materialButton.perform(click());

        ViewInteraction imageView = onView(
                allOf(withId(R.id.imgView), withContentDescription("Nazwa"),
                        withParent(allOf(withId(R.id.constraintLayout),
                                withParent(IsInstanceOf.<View>instanceOf(android.view.ViewGroup.class)))),
                        isDisplayed()));
        imageView.check(matches(isDisplayed()));

        ViewInteraction textView2 = onView(
                allOf(withId(R.id.textView),
                        withParent(allOf(withId(R.id.constraintLayout),
                                withParent(IsInstanceOf.<View>instanceOf(android.view.ViewGroup.class)))),
                        isDisplayed()));
        textView2.check(matches(isDisplayed()));

        ViewInteraction imageView2 = onView(
                allOf(withId(R.id.imgView2),
                        withParent(allOf(withId(R.id.constraintLayout),
                                withParent(IsInstanceOf.<View>instanceOf(android.view.ViewGroup.class)))),
                        isDisplayed()));
        imageView2.check(matches(isDisplayed()));

        ViewInteraction textView3 = onView(
                allOf(withId(R.id.textView2),
                        withParent(allOf(withId(R.id.constraintLayout),
                                withParent(IsInstanceOf.<View>instanceOf(android.view.ViewGroup.class)))),
                        isDisplayed()));
        textView3.check(matches(isDisplayed()));

        ViewInteraction textView4 = onView(
                allOf(withId(R.id.textView2),
                        withParent(allOf(withId(R.id.constraintLayout),
                                withParent(IsInstanceOf.<View>instanceOf(android.view.ViewGroup.class)))),
                        isDisplayed()));
        textView4.check(matches(isDisplayed()));

        ViewInteraction button = onView(
                allOf(withId(R.id.cameraOption), withText(R.string.cameraMode1),
                        childAtPosition(
                                allOf(withId(R.id.constraintLayout),
                                        childAtPosition(
                                                withClassName(is("androidx.constraintlayout.widget.ConstraintLayout")),
                                                1)),
                                1),
                        isDisplayed()));
        button.perform(click());

        ViewInteraction button2 = onView(
                allOf(withId(R.id.cameraOption), withText(R.string.cameraMode2),
                        childAtPosition(
                                allOf(withId(R.id.constraintLayout),
                                        childAtPosition(
                                                withClassName(is("androidx.constraintlayout.widget.ConstraintLayout")),
                                                1)),
                                1),
                        isDisplayed()));
        button2.perform(click());

    }
    @Test
    public void speedSwitchTest()
    {
        ViewInteraction switchCompat = onView(
                allOf(withId(R.id.textSwitch), withText(R.string.imgSwitch),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                2),
                        isDisplayed()));
        switchCompat.perform(click());

        ViewInteraction switchCompat2 = onView(
                allOf(withId(R.id.vibSwitch), withText(R.string.vibSwitch),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                4),
                        isDisplayed()));
        switchCompat2.perform(click());

        ViewInteraction materialButton = onView(
                allOf(withId(R.id.btnCamera), withId(R.id.btnCamera),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                0),
                        isDisplayed()));
        materialButton.perform(click());

        ViewInteraction textView3 = onView(
                allOf(withId(R.id.speedText),
                        withParent(allOf(withId(R.id.constraintLayout),
                                withParent(IsInstanceOf.<View>instanceOf(android.view.ViewGroup.class)))),
                        isDisplayed()));
        textView3.check(matches(isDisplayed()));
    }
    @Test
    public void speechInvisibilityTest()
    {
        ViewInteraction switchCompat2 = onView(
                allOf(withId(R.id.textSwitch), withText(R.string.imgSwitch),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                2),
                        isDisplayed()));
        switchCompat2.perform(click());
        ViewInteraction switchCompat3 = onView(
                allOf(withId(R.id.speedSwitch), withId(R.id.speedSwitch),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                5),
                        isDisplayed()));
        switchCompat3.perform(click());

        ViewInteraction switchCompat = onView(
                allOf(withId(R.id.silenceSwitch), withText(R.string.silentMode),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                6),
                        isDisplayed()));
        switchCompat.perform(click());

        ViewInteraction switchCompat4 = onView(
                allOf(withId(R.id.speechSwitch), withText(R.string.speechSwitch),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                3),
                        isDisplayed()));
        switchCompat4.perform(click());

        ViewInteraction materialButton = onView(
                allOf(withId(R.id.btnCamera), withText(R.string.btnCamera),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                0),
                        isDisplayed()));
        materialButton.perform(click());
        ViewInteraction view2 = onView(
                allOf(withId(R.id.cameraFrames),
                        withParent(allOf(withId(R.id.frameLayout),
                                withParent(IsInstanceOf.<View>instanceOf(android.view.ViewGroup.class)))),
                        isDisplayed()));
        view2.check(matches(isDisplayed()));

    }
    @Test
    public void vibrationInvisibilityTest()
    {
        ViewInteraction switchCompat2 = onView(
                allOf(withId(R.id.textSwitch), withText(R.string.imgSwitch),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                2),
                        isDisplayed()));
        switchCompat2.perform(click());
        ViewInteraction switchCompat3 = onView(
                allOf(withId(R.id.speedSwitch), withId(R.id.speedSwitch),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                5),
                        isDisplayed()));
        switchCompat3.perform(click());
        ViewInteraction switchCompat = onView(
                allOf(withId(R.id.vibSwitch), withText(R.string.vibSwitch),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                4),
                        isDisplayed()));
        switchCompat.perform(click());

        ViewInteraction materialButton = onView(
                allOf(withId(R.id.btnCamera), withText(R.string.btnCamera),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                0),
                        isDisplayed()));
        materialButton.perform(click());

        ViewInteraction view2 = onView(
                allOf(withId(R.id.cameraFrames),
                        withParent(allOf(withId(R.id.frameLayout),
                                withParent(IsInstanceOf.<View>instanceOf(android.view.ViewGroup.class)))),
                        isDisplayed()));
        view2.check(matches(isDisplayed()));
    }
    @Test
    public void blockActivityTest()
    {
        ViewInteraction switchCompat3 = onView(
                allOf(withId(R.id.textSwitch), withText(R.string.imgSwitch),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                2),
                        isDisplayed()));
        switchCompat3.perform(click());
        ViewInteraction switchCompat4 = onView(
                allOf(withId(R.id.speedSwitch), withId(R.id.speedSwitch),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                5),
                        isDisplayed()));
        switchCompat4.perform(click());

        ViewInteraction switchCompat = onView(
                allOf(withId(R.id.silenceSwitch), withText(R.string.silentMode),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                6),
                        isDisplayed()));
        switchCompat.perform(click());

        ViewInteraction materialButton = onView(
                allOf(withId(R.id.btnCamera), withText(R.string.btnCamera),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                0),
                        isDisplayed()));
        materialButton.perform(click());

        ViewInteraction button = onView(
                allOf(withId(R.id.btnCamera), withText(R.string.btnCamera),
                        withParent(withParent(withId(android.R.id.content))),
                        isDisplayed()));
        button.check(matches(isDisplayed()));

        ViewInteraction switchCompat2 = onView(
                allOf(withId(R.id.silenceSwitch), withText(R.string.silentMode),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                6),
                        isDisplayed()));
        switchCompat2.perform(click());

        ViewInteraction switchCompat5= onView(
                allOf(withId(R.id.speedSwitch), withText(R.string.speedSwitch),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                5),
                        isDisplayed()));
        switchCompat5.perform(click());

        ViewInteraction materialButton2 = onView(
                allOf(withId(R.id.btnCamera), withText(R.string.btnCamera),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                0),
                        isDisplayed()));
        materialButton2.perform(click());

        ViewInteraction button2 = onView(
                allOf(withId(R.id.btnCamera), withText(R.string.btnCamera),
                        withParent(withParent(withId(android.R.id.content))),
                        isDisplayed()));
        button2.check(matches(isDisplayed()));
    }
    @Test
    public void allOptionTest() {
        ViewInteraction switchCompat = onView(
                allOf(withId(R.id.speechSwitch), withText(R.string.speechSwitch),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                3),
                        isDisplayed()));
        switchCompat.perform(click());

        ViewInteraction switchCompat2 = onView(
                allOf(withId(R.id.vibSwitch), withText(R.string.vibSwitch),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                4),
                        isDisplayed()));
        switchCompat2.perform(click());

        ViewInteraction switchCompat3 = onView(
                allOf(withId(R.id.silenceSwitch), withText(R.string.silentMode),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                6),
                        isDisplayed()));
        switchCompat3.perform(click());

        ViewInteraction materialButton = onView(
                allOf(withId(R.id.btnCamera), withText(R.string.btnCamera),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                0),
                        isDisplayed()));
        materialButton.perform(click());

        ViewInteraction imageView2 = onView(
                allOf(withId(R.id.imgView2),
                        withParent(allOf(withId(R.id.constraintLayout),
                                withParent(IsInstanceOf.<View>instanceOf(android.view.ViewGroup.class)))),
                        isDisplayed()));
        imageView2.check(matches(isDisplayed()));

        ViewInteraction textView = onView(
                allOf(withId(R.id.textView2),
                        withParent(allOf(withId(R.id.constraintLayout),
                                withParent(IsInstanceOf.<View>instanceOf(android.view.ViewGroup.class)))),
                        isDisplayed()));
        textView.check(matches(isDisplayed()));

        ViewInteraction textView2 = onView(
                allOf(withId(R.id.textView),
                        withParent(allOf(withId(R.id.constraintLayout),
                                withParent(IsInstanceOf.<View>instanceOf(android.view.ViewGroup.class)))),
                        isDisplayed()));
        textView2.check(matches(isDisplayed()));

        ViewInteraction textView3 = onView(
                allOf(withId(R.id.speedText),
                        withParent(allOf(withId(R.id.constraintLayout),
                                withParent(IsInstanceOf.<View>instanceOf(android.view.ViewGroup.class)))),
                        isDisplayed()));
        textView3.check(matches(isDisplayed()));
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
