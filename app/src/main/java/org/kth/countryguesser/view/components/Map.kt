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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntSize
import com.caverock.androidsvg.SVG
import org.kth.countryguesser.R


@Composable
fun Map() {
    val context = LocalContext.current

    val svg = remember {
        context.resources.openRawResource(R.raw.world_map).use { SVG.getFromInputStream(it) }
    }

    var scale by remember { mutableFloatStateOf(4.5f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var mapSize by remember { mutableStateOf(IntSize(0, 0)) }
    var hasCentered by remember { mutableStateOf(false) }

    val svgAspect = remember(svg) {
        svg.documentAspectRatio.takeIf { it > 0f } ?: (16f / 9f)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RectangleShape)
            .background(Color(0xff70d6ef))
            .onSizeChanged { size ->
                mapSize = size
                if (!hasCentered) {
                    hasCentered = true
                    val x = (mapSize.width * (scale - 1)).coerceAtLeast(0f)
                    offset = Offset(-x/2f, 0f)
                }
            }
            .pointerInput(Unit) {
                detectTransformGestures { centroid, pan, zoom, _ ->
                    scale = (scale * zoom).coerceIn(1f, 100f)

                    val maxX = (mapSize.width * (scale - 1)).coerceAtLeast(0f)
                    val maxY = (mapSize.height * (scale - 1)).coerceAtLeast(0f)

                    offset = (offset * zoom) + (centroid - centroid * zoom) + pan
                    offset = Offset(
                        x = offset.x.coerceIn(-maxX, 0f),
                        y = offset.y.coerceIn(-maxY, 0f),
                    )
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasWidth = size.width
            val canvasHeight = size.height

            // fit svg while preserving aspect ratio
            val baseWidth: Float
            val baseHeight: Float
            if (canvasWidth / canvasHeight > svgAspect) {
                baseHeight = canvasHeight
                baseWidth = canvasHeight * svgAspect
            } else {
                baseWidth = canvasWidth
                baseHeight = canvasWidth / svgAspect
            }

            drawIntoCanvas { canvas ->
                val nativeCanvas = canvas.nativeCanvas
                nativeCanvas.save()

                nativeCanvas.translate(offset.x, offset.y)
                nativeCanvas.scale(scale, scale, 0f, 0f)

                svg.documentWidth = baseWidth
                svg.documentHeight = baseHeight
                svg.renderToCanvas(nativeCanvas)

                nativeCanvas.restore()
            }
        }
    }
}