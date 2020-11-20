package com.mapbox.navigation.core.replay.history

import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonNull
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import com.google.gson.internal.LazilyParsedNumber
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.mapbox.api.directions.v5.models.DirectionsRoute
import java.io.InputStream
import java.io.InputStreamReader

/**
 *
 */
class HistoryEventStream private constructor(
    val inputStream: InputStream?,
    val historyDTO: ReplayHistoryDTO?
) {
    val jsonReader: JsonReader?

    constructor(historyDTO: ReplayHistoryDTO) : this(null, historyDTO)
    constructor(inputStream: InputStream) : this(inputStream, null)

    init {
        this.jsonReader = if (inputStream != null) {
            JsonReader(InputStreamReader(inputStream, "UTF-8"))
        } else null
    }

    var historyMapper: ReplayHistoryMapper = ReplayHistoryMapper()
    var isReadingEvents = false

    fun read(count: Int): List<ReplayEventBase> {
        if (historyDTO != null) {
            return historyMapper.mapToReplayEvents(historyDTO)
        }
        return mapToReplayEvents(jsonReader!!, count)
    }

    fun close() {
        inputStream?.close()
    }

    fun mapToReplayEvents(jsonReader: JsonReader, eventCount: Int): List<ReplayEventBase> {
        val replayEvents = mutableListOf<ReplayEventBase>()

        if (!isReadingEvents) {
            // There are no events so find the event array
            jsonReader.beginObject()
            if (jsonReader.hasNext()) {
                val nextName = jsonReader.nextName()
                if (nextName == "events") {
                    jsonReader.beginArray()
                    isReadingEvents = true
                }
            }
        }

        if (isReadingEvents) {
            while (replayEvents.size < eventCount) {
                val jsonElement = readEvent(jsonReader) as JsonObject
                mapToEvent(jsonElement)?.let {
                    replayEvents.add(it)
                }
            }
        }

        return replayEvents
    }

    private fun readEvent(jsonReader: JsonReader): JsonElement {
        return when (jsonReader.peek()) {
            JsonToken.STRING -> JsonPrimitive(jsonReader.nextString())
            JsonToken.NUMBER -> JsonPrimitive(LazilyParsedNumber(jsonReader.nextString()))
            JsonToken.BOOLEAN -> JsonPrimitive(jsonReader.nextBoolean())
            JsonToken.NULL -> JsonNull.INSTANCE
            JsonToken.BEGIN_ARRAY -> {
                val jsonArray = JsonArray()
                jsonReader.beginArray()
                while (jsonReader.hasNext()) {
                    jsonArray.add(readEvent(jsonReader))
                }
                jsonReader.endArray()
                jsonArray
            }
            JsonToken.BEGIN_OBJECT -> {
                val jsonObject = JsonObject()
                jsonReader.beginObject()
                while (jsonReader.hasNext()) {
                    jsonObject.add(jsonReader.nextName(), readEvent(jsonReader))
                }
                jsonReader.endObject()
                jsonObject
            }
            else -> throw IllegalArgumentException()
        }
    }

    private fun mapToEvent(jsonObject: JsonObject): ReplayEventBase? {
        return when (val eventType = jsonObject["type"].asString) {
            "updateLocation" -> Gson().fromJson(
                jsonObject.toString(),
                ReplayEventUpdateLocation::class.java
            )
            "getStatusMonotonic" -> {
                val eventTimestamp = jsonObject["event_timestamp"].asDouble
                ReplayEventGetStatus(
                    eventTimestamp = eventTimestamp
                )
            }
            "setRoute" -> {
                val directionsRoute = try {
                    if (jsonObject["route"].asString == "{}") {
                        null
                    } else {
                        DirectionsRoute.fromJson(jsonObject["route"].asString)
                    }
                } catch (throwable: Throwable) {
                    println("Unable to setRoute from history file")
                    return null
                }
                ReplaySetRoute(
                    eventTimestamp = jsonObject["event_timestamp"].asDouble,
                    route = directionsRoute
                )
            }
            else -> {
                println("Replay unsupported event $eventType")
                null
            }
        }
    }

}
