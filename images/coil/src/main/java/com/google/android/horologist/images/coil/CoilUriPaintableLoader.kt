/*
 * Copyright 2024 The Android Open Source Project
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

package com.google.android.horologist.images.coil

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import coil.ImageLoader
import coil.compose.rememberAsyncImagePainter
import coil.imageLoader
import com.google.android.horologist.images.base.paintable.LocalUriPaintableLoader
import com.google.android.horologist.images.base.paintable.UriPaintable

private class CoilUriPaintableLoader(private val imageLoader: ImageLoader) : UriPaintable.Loader {
    @Composable
    public override fun rememberPainterFromUriPaintable(uriPaintable: UriPaintable): Painter = rememberAsyncImagePainter(
        model = uriPaintable.uri,
        placeholder = uriPaintable.placeholder.rememberPainter(),
        imageLoader = imageLoader,
    )
}

@Composable
public fun LoadPaintablesUsingCoil(imageLoader: ImageLoader = LocalContext.current.imageLoader, content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalUriPaintableLoader provides CoilUriPaintableLoader(imageLoader), content)
}
