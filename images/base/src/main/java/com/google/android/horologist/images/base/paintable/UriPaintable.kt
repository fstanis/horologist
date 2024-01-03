/*
 * Copyright 2023 The Android Open Source Project
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

package com.google.android.horologist.images.base.paintable

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.Stable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.graphics.painter.Painter

/** A [Painter] that can be loaded from the given [Uri]. */
@Stable
public data class UriPaintable(
    public val uri: Uri,
    public val placeholder: Paintable = EMPTY_PAINTABLE,
) : Paintable {
    public constructor(
        uriString: String?,
        placeholder: Paintable = EMPTY_PAINTABLE,
    ) : this(uriString?.let { Uri.parse(it) } ?: Uri.EMPTY, placeholder)

    @Composable
    public override fun rememberPainter(): Painter {
        val loader = LocalUriPaintableLoader.current
        return loader?.rememberPainterFromUriPaintable(this) ?: placeholder.rememberPainter()
    }

    public interface Loader {
        @Composable
        public fun rememberPainterFromUriPaintable(uriPaintable: UriPaintable): Painter?
    }

    private companion object {
        private val EMPTY_PAINTABLE = Paintable { ColorPainter(Color.Transparent) }
    }
}

public val LocalUriPaintableLoader: ProvidableCompositionLocal<UriPaintable.Loader?> = staticCompositionLocalOf { null }
