package com.scaleup.kotlingithubbrowser.util

import android.support.test.InstrumentationRegistry
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import android.support.v4.app.DialogFragment
import android.support.v4.app.Fragment
import com.scaleup.kotlingithubbrowser.testing.SingleFragmentActivity
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AutoClearedValueTest {
    @Rule @JvmField
    val activityRule = ActivityTestRule<SingleFragmentActivity>(SingleFragmentActivity::class.java, true, true)
    private lateinit var testFragment: TestFragment


    @Before
    fun init(){
        testFragment =  TestFragment()
        activityRule.activity.setFragment(testFragment)
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
    }

    @Test
    fun clearOnReplace(){
        testFragment.testValue = AutoClearedValue(testFragment, "foo")
        activityRule.activity.replaceFragment(testFragment)
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        assertThat(testFragment.testValue!!.get(), nullValue())
    }

    @Test
    fun dontClearForChildFragment(){
        testFragment.testValue = AutoClearedValue(testFragment, "foo")
        testFragment.childFragmentManager.beginTransaction()
                .add(Fragment(), "foo").commit()
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        assertThat(testFragment.testValue?.get(), `is`("foo"))
    }

    @Test
    fun dontClearForDialog(){
        testFragment.testValue = AutoClearedValue(testFragment, "foo")
        val dialogFragment = DialogFragment()
        dialogFragment.show(testFragment.fragmentManager, "dialog")
        dialogFragment.dismiss()
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        assertThat(testFragment.testValue?.get(), `is`("foo"))
    }

    class TestFragment : Fragment() {
        var testValue : AutoClearedValue<String>? = null
    }

}