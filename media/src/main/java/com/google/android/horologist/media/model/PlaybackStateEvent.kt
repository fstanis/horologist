/*
 * Copyright 2022 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.android.horologist.media.model

import com.google.android.horologist.media.ExperimentalHorologistMediaApi
import java.lang.Float.max
import java.lang.Float.min
import kotlin.math.roundToLong
import kotlin.time.Duration

@ExperimentalHorologistMediaApi
public data class PlaybackStateEvent(
    public val playbackState: PlaybackState,
    public val cause: Cause,
    public val timestamp: Duration? = null
) {
    public enum class Cause {
        Initial,
        PlayerStateChanged,
        ParametersChanged,
        PositionDiscontinuity,
        Other
    }

    public fun createPositionPredictor(): PositionPredictor? {
        if (timestamp == null || playbackState.duration == null || playbackState.currentPosition == null) {
            return null
        }
        val eventTimestamp = timestamp.inWholeMilliseconds
        val durationMs = playbackState.duration.inWholeMilliseconds
        val currentPositionMs = playbackState.currentPosition.inWholeMilliseconds
        return object : PositionPredictor {
            override fun predictDuration(timestamp: Long): Long {
                return if (playbackState.isLive) {
                    val staleness = timestamp - eventTimestamp
                    durationMs + staleness
                } else {
                    durationMs
                }
            }

            private fun predictPositionFractional(timestamp: Long): Float {
                val staleness = timestamp - eventTimestamp
                return (currentPositionMs + staleness * playbackState.playbackSpeed)
            }

            override fun predictPosition(timestamp: Long): Long = predictPositionFractional(timestamp).roundToLong()

            override fun predictPercent(timestamp: Long): Float {
                val predictedDuration = predictDuration(timestamp).toFloat()
                val predictedPosition = min(predictedDuration, predictPositionFractional(timestamp))
                return max(0f, predictedPosition / predictedDuration)
            }
        }
    }
}
