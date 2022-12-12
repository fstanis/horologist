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

package com.google.android.horologist.media3.offload

import android.content.Context
import androidx.media3.common.AudioAttributes
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Timeline
import androidx.media3.common.TrackSelectionParameters
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.RendererCapabilities
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.TrackGroupArray
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.media3.exoplayer.trackselection.TrackSelector
import androidx.media3.exoplayer.trackselection.TrackSelectorResult
import androidx.media3.exoplayer.upstream.BandwidthMeter

@UnstableApi
class OffloadAwareTrackSelector(context: Context) : TrackSelector() {
    private val delegate = DefaultTrackSelector(context)

    override fun init(listener: InvalidationListener, bandwidthMeter: BandwidthMeter) {
        super.init(listener, bandwidthMeter)
        delegate.init(listener, bandwidthMeter)
    }

    override fun release() {
        delegate.release()
        super.release()
    }

    override fun getParameters(): TrackSelectionParameters =
        delegate.parameters

    override fun setParameters(parameters: TrackSelectionParameters) =
        delegate.setParameters(parameters)

    override fun isSetParametersSupported(): Boolean =
        delegate.isSetParametersSupported

    override fun setAudioAttributes(audioAttributes: AudioAttributes) =
        delegate.setAudioAttributes(audioAttributes)

    override fun selectTracks(
        rendererCapabilities: Array<out RendererCapabilities>,
        trackGroups: TrackGroupArray,
        periodId: MediaSource.MediaPeriodId,
        timeline: Timeline
    ): TrackSelectorResult {
        val result = delegate.selectTracks(rendererCapabilities, trackGroups, periodId, timeline)
        if (result.length != 2 || result.rendererConfigurations[1] != null || result.selections[1].length() > 0) {
            return result
        }
        val metadata = metadataFromPeriodId(periodId, timeline)
        if (metadata.extras?.getBoolean("shouldOffload") == true) {
            // flip the result
            return TrackSelectorResult(
                arrayOf(
                    result.rendererConfigurations[1],
                    result.rendererConfigurations[0]
                ), arrayOf(
                    result.selections[1],
                    result.selections[0]
                ), result.tracks, result.info
            )
        }
        return result
    }

    private fun metadataFromPeriodId(
        periodId: MediaSource.MediaPeriodId,
        timeline: Timeline
    ): MediaMetadata {
        val period = Timeline.Period()
        timeline.getPeriodByUid(periodId.periodUid, period)
        val window = Timeline.Window()
        timeline.getWindow(period.windowIndex, window)
        return window.mediaItem.mediaMetadata
    }

    override fun onSelectionActivated(info: Any?) =
        delegate.onSelectionActivated(info)
}
