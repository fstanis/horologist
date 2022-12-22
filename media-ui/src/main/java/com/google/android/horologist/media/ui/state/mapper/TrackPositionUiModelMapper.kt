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

@file:OptIn(ExperimentalHorologistMediaApi::class)

package com.google.android.horologist.media.ui.state.mapper

import com.google.android.horologist.media.ExperimentalHorologistMediaApi
import com.google.android.horologist.media.model.PlaybackState
import com.google.android.horologist.media.model.PlaybackStateEvent
import com.google.android.horologist.media.ui.ExperimentalHorologistMediaUiApi
import com.google.android.horologist.media.ui.state.model.TrackPositionUiModel

/**
 * Map a [PlaybackState] into a [TrackPositionUiModel]
 */
@ExperimentalHorologistMediaUiApi
public object TrackPositionUiModelMapper {
    public fun map(event: PlaybackStateEvent): TrackPositionUiModel {
        val currentPositionMs = event.playbackState.currentPosition?.inWholeMilliseconds
        val durationMs = event.playbackState.duration?.inWholeMilliseconds
        if (currentPositionMs == null || durationMs == null) {
            return TrackPositionUiModel.Hidden
        }
        val percent = if (durationMs > 0) {
            currentPositionMs.toFloat() / durationMs.toFloat()
        } else {
            0f
        }

        val predictor = event.createPositionPredictor()
        if (event.playbackState.isPlaying && predictor != null) {
            return TrackPositionUiModel.Predictive(predictor)
        }
        return TrackPositionUiModel.Actual(percent, durationMs, currentPositionMs)
    }
}
