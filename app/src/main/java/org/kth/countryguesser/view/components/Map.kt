package org.kth.countryguesser.view.components

import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.IntSize
import org.kth.countryguesser.R

@Composable
fun Map() {
    var scale by remember { mutableFloatStateOf(4.75f) }
    var offset by remember { mutableStateOf(Offset.Zero)}
    var mapSize by remember { mutableStateOf(IntSize(0,0)) }

    val mapVector : ImageVector = ImageVector.vectorResource(id = R.drawable.world)
    val vectorPainter = rememberVectorPainter(mapVector)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RectangleShape)
            .background(Color(0xff70d6ef))
            .onSizeChanged { mapSize = it }
            .pointerInput(Unit) {
                detectTransformGestures { centroid, pan, zoom, _ /* no rotation */ ->
                    scale = (scale * zoom).coerceIn(4.5f, 100f)

                    // don't pan out of bounds
                    val maxX = (mapSize.width * (scale - 1)).coerceAtLeast(0f)
                    val maxY = (mapSize.height * (scale - 1)).coerceAtLeast(0f)

                    // set zoom at center of finger pinch
                    offset = (offset * zoom) + (centroid - centroid * zoom) + pan
                    // don't pan out of bounds
                    offset = Offset(
                        x = offset.x.coerceIn(-maxX, 0f),
                        y = offset.y.coerceIn(-maxY, 0f),
                    )
                }
            },
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) { // TODO: fix rendering to redraw when zooming
            val aspect = mapVector.defaultWidth.value / mapVector.defaultHeight.value
            val baseHeight = minOf(size.height, size.width / aspect)
            val baseWidth = minOf(size.width, size.height * aspect)

            val scaleX = baseWidth / mapVector.defaultWidth.value
            val scaleY = baseHeight / mapVector.defaultHeight.value

            with(drawContext.canvas) {
                save()
                translate(offset.x, offset.y)

                scale(scale, scale, pivotX = 0f, pivotY = 0f)
                scale(scaleX, scaleY, pivotX = 0f, pivotY = 0f)

                with(vectorPainter) {
                    draw(size = Size(mapVector.defaultWidth.value, mapVector.defaultHeight.value))
                }
                restore()
            }
        }
    }
}