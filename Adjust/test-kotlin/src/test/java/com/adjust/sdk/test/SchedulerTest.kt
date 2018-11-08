package com.adjust.sdk.test

import com.adjust.sdk.scheduler.*
import org.junit.Assert
import org.junit.Test
import java.util.concurrent.TimeUnit

class SchedulerTest {
    @Test
    fun threadExecutor_submitsVarious_executesInOrder() {
        val threadExecutor: ThreadExecutor = SingleThreadCachedScheduler("s")
        var s = "";
        threadExecutor.submit { s += "a" }
        threadExecutor.submit { s += "b" }
        threadExecutor.submit { s += "c" }
        Thread.sleep(1000)
        Assert.assertEquals("abc", s)
    }

    @Test
    fun threadScheduler_schedulesVariousDelayed_executesInDelayedOrder() {
        val threadScheduler: ThreadScheduler = SingleThreadCachedScheduler("s")
        var s = "";
        threadScheduler.submit { s += "a" }
        threadScheduler.schedule ({ s += "e"}, 500)
        threadScheduler.submit { s += "b" }
        threadScheduler.schedule ({ s += "d"}, 250)
        threadScheduler.submit { s += "c" }
        Thread.sleep(1000)
        Assert.assertEquals("abcde", s)
    }

    @Test
    fun futureScheduler_ScheduleAndWaitWithKeepAlive() {
        // arrange
        val futureScheduler: FutureScheduler = SingleThreadFutureScheduler("s", true)

        // act
        var s = "";
        val scheduledFuture = futureScheduler.scheduleFuture({ s += "a" }, 500)

        val withDelay = scheduledFuture.getDelay(TimeUnit.MILLISECONDS)
        val isNotDone = scheduledFuture.isDone
        val isNotCanceledBefore = scheduledFuture.isCancelled
        // wait for finish
        scheduledFuture.get()

        val noDelay = scheduledFuture.getDelay(TimeUnit.MILLISECONDS)
        val isDone = scheduledFuture.isDone
        val isNotCanceledBeforeAfter = scheduledFuture.isCancelled

        // assert
        // before finish
        Assert.assertTrue(withDelay > 0L)
        Assert.assertTrue(withDelay < 500L)
        Assert.assertTrue(noDelay <= 0L)
        Assert.assertFalse(isNotDone)

        // after finish
        Assert.assertEquals("a", s)
        Assert.assertTrue(isDone)
        Assert.assertFalse(isNotCanceledBefore)
        Assert.assertFalse(isNotCanceledBeforeAfter)
    }

    @Test
    fun futureScheduler_ScheduleAndCancelWithKeepAlive() {
        // arrange
        val futureScheduler: FutureScheduler = SingleThreadFutureScheduler("s", true)

        // act
        var s = "";
        val scheduledFuture = futureScheduler.scheduleFuture({ s += "a" }, 500)

        // cancel
        scheduledFuture.cancel(false)

        val isDone = scheduledFuture.isDone
        val isCancelled = scheduledFuture.isCancelled

        // assert
        Assert.assertEquals("", s)
        Assert.assertTrue(isDone)
        Assert.assertTrue(isCancelled)
    }

    @Test
    fun futureScheduler_ScheduleWithFixedDelayWithKeepAlive() {
        // arrange
        val futureScheduler: FutureScheduler = SingleThreadFutureScheduler("s", true)

        // act
        var s = "";
        val scheduledFuture = futureScheduler.scheduleFutureWithFixedDelay({ s += "a" }, 500, 500)

        Thread.sleep(650)
        val s1 = s
        Thread.sleep(650)

        // assert
        Assert.assertEquals("a", s1)
        Assert.assertEquals("aa", s)
    }

}