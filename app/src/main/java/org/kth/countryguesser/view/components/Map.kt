package org.kth.countryguesser.view.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntSize
import org.kth.countryguesser.R

@Composable
fun Map() {
    val painter = painterResource(id = R.drawable.world)

    InteractiveMap(
        painter = painter,
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xff70d6ef)),
    )
}

@Composable
private fun InteractiveMap(
    painter: Painter,
    modifier: Modifier = Modifier,
) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero)}
    var mapSize by remember { mutableStateOf(IntSize(0,0)) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(RectangleShape)
            .onSizeChanged { mapSize = it }
            .pointerInput(Unit) {
                detectTransformGestures { centroid, pan, zoom, _ /* no rotation */ ->

                    scale = (scale * zoom).coerceIn(1f, 10f)

                    // don't pan out of bounds
                    val maxX = (mapSize.width * (scale - 1)).coerceAtLeast(0f)
                    val maxY = (mapSize.width * (scale - 1)).coerceAtLeast(0f)

                    // set zoom at center of finger pinch
                    if (scale < 10f) {
                        offset = (offset * zoom) + (centroid - centroid * zoom) + pan
                    }
                    // don't pan out of bounds
                    offset = Offset(
                        x = offset.x.coerceIn(-maxX, 0f),
                        y = offset.y.coerceIn(-maxY, 0f),
                    )
                }
            }
    ) {
        Image(
            painter = painter,
            contentDescription = "World map",
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale,
                    translationX = offset.x,
                    translationY = offset.y,
                    transformOrigin = TransformOrigin(0f, 0f), // set offset origin to same as scale origin
                ),
        )
    }
}