/*
 * Copyright 2021 The Android Open Source Project
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

package com.google.android.horologist.audio

import android.content.Context
import androidx.mediarouter.media.MediaControlIntent
import androidx.mediarouter.media.MediaRouteSelector
import androidx.mediarouter.media.MediaRouter
import androidx.mediarouter.media.MediaRouter.RouteInfo
import com.google.android.horologist.audio.BluetoothSettings.launchBluetoothSettings
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.*

/**
 * Audio Repository for identifying and controlling available audio devices in a simple manner.
 */
public class SystemAudioRepository(
    private val application: Context,
    private val mediaRouter: MediaRouter,
    selector: MediaRouteSelector = MediaRouteSelector.Builder().build()
) : AudioOutputRepository, VolumeRepository {
    private val _available = MutableStateFlow(mediaRouter.devices)
    private val _output = MutableStateFlow(mediaRouter.output)
    private val _volume = MutableStateFlow(mediaRouter.volume)

    override val volumeState: StateFlow<VolumeState>
        get() = _volume

    override fun increaseVolume() {
        mediaRouter.selectedRoute.requestUpdateVolume(1)
    }

    override fun decreaseVolume() {
        mediaRouter.selectedRoute.requestUpdateVolume(-1)
    }

    override val audioOutput: StateFlow<AudioOutput>
        get() = _output

    override val available: StateFlow<List<AudioOutput>>
        get() = _available

    private val mediaRouterState = callbackFlow {
        val callback = object : MediaRouter.Callback() {
            override fun onRouteAdded(router: MediaRouter, route: RouteInfo) {
                mediaRouter.fixInconsistency()
                this@callbackFlow.trySend(router.state)
            }

            override fun onRouteRemoved(router: MediaRouter, route: RouteInfo) {
                mediaRouter.fixInconsistency()
                this@callbackFlow.trySend(router.state)
            }

            override fun onRouteSelected(router: MediaRouter, route: RouteInfo, reason: Int) {
                mediaRouter.fixInconsistency()
                this@callbackFlow.trySend(router.state)
            }

            override fun onRouteVolumeChanged(router: MediaRouter, route: RouteInfo) {
                mediaRouter.fixInconsistency()
                this@callbackFlow.trySend(router.state)
            }
        }
        mediaRouter.addCallback(
            MediaRouteSelector.Builder()
                .addControlCategory(MediaControlIntent.CATEGORY_LIVE_AUDIO)
                .addSelector(selector)
                .build(),
            callback
        )
        send(mediaRouter.state)
        awaitClose{mediaRouter.removeCallback(callback)}
    }

    override fun close() {
        _output.value = AudioOutput.None
        _available.value = listOf()
    }

    override fun launchOutputSelection(closeOnConnect: Boolean) {
        application.launchBluetoothSettings(closeOnConnect)
    }

    public companion object {
        public fun fromContext(application: Context): SystemAudioRepository {
            return SystemAudioRepository(
                application,
                MediaRouter.getInstance(application)
            )
        }
    }

    private data class MediaRouterState(
        val volume: VolumeState,
        val output: AudioOutput,
        val devices: List<AudioOutput>
    )

    private inline val MediaRouter.state
        get() = MediaRouterState(
            volume = selectedRoute.let {
                VolumeState(current = it.volume, max = it.volumeMax)
            },
            output = selectedRoute.device,
            devices = routes.map { it.device }
        )

    private fun MediaRouter.fixInconsistency() {
        if (selectedRoute !in routes) {
            selectRoute(defaultRoute)
        }
    }

    private inline val RouteInfo.device: AudioOutput
        get() {
            return when {
                isBluetooth -> {
                    AudioOutput.BluetoothHeadset(id, name)
                }
                isDeviceSpeaker -> {
                    AudioOutput.WatchSpeaker(id, name)
                }
                else -> {
                    AudioOutput.Unknown(id, name)
                }
            }
        }
}
