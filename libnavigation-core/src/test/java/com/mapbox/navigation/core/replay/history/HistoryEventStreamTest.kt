package com.mapbox.navigation.core.replay.history

import org.apache.commons.io.IOUtils
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner


@RunWith(RobolectricTestRunner::class)
class HistoryEventStreamTest {

    @get:Rule
    val memoryTestRule = MemoryTestRule()

//    private val historyFile = "history-10hz.json"
    private val historyFile = "history-events-file.json"

    @Test
    fun `do nothing`() {
        println("memoryUsed: ${memoryTestRule.memoryUsedMB}")
//        run1 681875568
//        run2 683978656
//        run3 683261616
    }


    @Test
    fun `read file as a stream`() {
        val historyEventStream = resourceAsHistoryEventStream(historyFile)
//        val historyEventStream = resourceAsHistoryEventStream("history-events-file.json")
        historyEventStream.historyMapper = ReplayHistoryMapper()

        val replayEvents = historyEventStream.read(10)

        println("memoryUsed: ${memoryTestRule.memoryUsedMB}")
        assertEquals(10, replayEvents.size)
//        run1 681875568
//        run2 683978656
//        run3 683261616
    }

    @Test
    fun `read file as a stream 2`() {
        val historyEventStream = resourceAsHistoryEventStream(historyFile)
//        val historyEventStream = resourceAsHistoryEventStream("history-events-file.json")
        historyEventStream.historyMapper = ReplayHistoryMapper()

        val replayEvents = historyEventStream.read(10)

        println("memoryUsed: ${memoryTestRule.memoryUsedMB}")
        assertEquals(10, replayEvents.size)
//        run1 679087432
//        run2 681411752
//        run3 684316200
    }

    @Test
    fun `read file as a string`() {
        val historyString = resourceAsString(historyFile)
//        val historyEventStream = resourceAsHistoryEventStream("history-events-file.json")
        val replayHistoryMapper = ReplayHistoryMapper()
        val replayEvents = replayHistoryMapper.mapToReplayEvents(historyString)

        println("memoryUsed: ${memoryTestRule.memoryUsedMB}")
        assertEquals(10, replayEvents.size)
//        run1 915000896
//        run2 915480952
//        run3 914965512
    }

    @Test
    fun `read file as a string 2`() {
        val historyString = resourceAsString(historyFile)
//        val historyEventStream = resourceAsHistoryEventStream("history-events-file.json")
        val replayHistoryMapper = ReplayHistoryMapper()
        val replayEvents = replayHistoryMapper.mapToReplayEvents(historyString)

        println("memoryUsed: ${memoryTestRule.memoryUsedMB}")
        assertEquals(10, replayEvents.size)
//        run1 923758632
//        run2 911318600
//        run3 926246488
    }


    private fun resourceAsHistoryEventStream(
        name: String,
        packageName: String = "com.mapbox.navigation.core.replay.history"
    ): HistoryEventStream {
        val inputStream = javaClass.classLoader?.getResourceAsStream("$packageName/$name")
        return HistoryEventStream(inputStream!!)
    }

    private fun resourceAsString(
        name: String,
        packageName: String = "com.mapbox.navigation.core.replay.history"
    ): String {
        val inputStream = javaClass.classLoader?.getResourceAsStream("$packageName/$name")
        return IOUtils.toString(inputStream, "UTF-8")
    }
}
