package com.adjust.sdk.test

import com.adjust.sdk.scheduler.SingleThreadCachedScheduler
import org.junit.Assert
import org.junit.Test
import java.lang.Thread.sleep

class SchedulerTest {
    @Test
    fun stcs_submits_inOrder() {
        val ts = SingleThreadCachedScheduler("s")
        var s = "";
        ts.submit { s += "a" }
        ts.submit { s += "b" }
        ts.submit { s += "c" }
        Thread.sleep(1000)
        Assert.assertEquals("abc", s)
    }

    @Test
    fun stcs_schedules_inOrder() {
        val ts = SingleThreadCachedScheduler("s")
        var s = "";
        ts.submit { s += "a" }
        ts.schedule ({ s += "e"}, 500)
        ts.submit { s += "b" }
        ts.schedule ({ s += "d"}, 250)
        ts.submit { s += "c" }
        Thread.sleep(1000)
        Assert.assertEquals("abcde", s)
    }
}