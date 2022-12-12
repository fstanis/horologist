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

package com.google.android.horologist.media3.config

import android.annotation.SuppressLint
import android.content.Context
import androidx.media3.exoplayer.ExoPlayer.AudioOffloadListener
import androidx.media3.exoplayer.RenderersFactory
import androidx.media3.exoplayer.audio.AudioCapabilities
import androidx.media3.exoplayer.audio.AudioSink
import androidx.media3.exoplayer.audio.DefaultAudioSink
import androidx.media3.exoplayer.audio.MediaCodecAudioRenderer
import androidx.media3.exoplayer.mediacodec.MediaCodecSelector

@SuppressLint("UnsafeOptInUsageError")
public open class WearMedia3Factory(private val context: Context) {
    public fun audioSink(): DefaultAudioSink {
        return baseAudioSinkBuilder()
            .setOffloadMode(DefaultAudioSink.OFFLOAD_MODE_DISABLED)
            .build()
    }

    public fun audioSinkWithOffload(
        @Suppress("UNUSED_PARAMETER") audioOffloadListener: AudioOffloadListener?
    ): DefaultAudioSink {
        return baseAudioSinkBuilder()
            // Expose when https://github.com/androidx/media/commit/7893531888608555fb09e77f12897752650131d5
            // is in 1.0-RC1
            // For now requires `media3.checkout=false` in local.properties
//            .setExperimentalAudioOffloadListener(audioOffloadListener)
            .setOffloadMode(DefaultAudioSink.OFFLOAD_MODE_ENABLED_GAPLESS_NOT_REQUIRED)
            .build()
    }

    private fun baseAudioSinkBuilder() =
        DefaultAudioSink.Builder()
            .setAudioCapabilities(AudioCapabilities.getCapabilities(context))
            .setAudioProcessorChain(DefaultAudioSink.DefaultAudioProcessorChain())
            .setEnableFloatOutput(false) // default
            .setEnableAudioTrackPlaybackParams(false) // default

    public fun audioOnlyRenderersFactory(
        audioSink: AudioSink,
        mediaCodecSelector: MediaCodecSelector = mediaCodecSelector()
    ): RenderersFactory =
        RenderersFactory { handler, _, audioListener, _, _ ->
                arrayOf(MediaCodecAudioRenderer(
                    context,
                    mediaCodecSelector,
                    handler,
                    audioListener,
                    audioSink
                ))
        }

    public fun audioOnlyWithOffloadRenderersFactory(
        audioSink: AudioSink,
        offloadedAudioSink: AudioSink,
        mediaCodecSelector: MediaCodecSelector = mediaCodecSelector()
    ): RenderersFactory =
        RenderersFactory { handler, _, audioListener, _, _ ->
            arrayOf(MediaCodecAudioRenderer(
                context,
                mediaCodecSelector,
                handler,
                audioListener,
                audioSink
            ),MediaCodecAudioRenderer(
                context,
                mediaCodecSelector,
                handler,
                audioListener,
                offloadedAudioSink
            ))
        }

    public fun mediaCodecSelector(): MediaCodecSelector = MediaCodecSelector.DEFAULT
}
