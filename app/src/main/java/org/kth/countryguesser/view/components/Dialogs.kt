package org.kth.countryguesser.view.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import org.kth.countryguesser.ui.theme.AppTheme

@Composable
fun WIPAlert(
    onPress: () -> Unit = {}
) {
    AlertDialog(
        onDismissRequest = { },
        title = { Text("Work in progress") },
        text = { Text("This page is under construction, please come back later.") },
        confirmButton = {
            TextButton(onClick = { onPress() }) {
                Text("OK")
            }
        }
    )
}

@Composable
fun Alert(
    onPress: () -> Unit = {},
    title: String,
    message: String
) {
    AlertDialog(
        onDismissRequest = { },
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = {
            TextButton(onClick = { onPress() }) {
                Text("OK")
            }
        }
    )
}

@Composable
fun LoadingAlert(
    message: String
) {
    Dialog(
        onDismissRequest = { },
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(message)
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}

@Composable
fun NoInternetAlert(
    onPress: () -> Unit = {},
) {
    AlertDialog(
        onDismissRequest = { },
        title = { Text("No Internet Connection")},
        text = { Text("Please ensure that Wi-Fi or mobile data is turned on, then try again.")},
        confirmButton = {
            TextButton(onClick = { onPress() }) {
                Text("OK")
            }
        }
    )
}

@Composable
fun GameWonAlert(
    onConfirmPress: (() -> Unit)? = null,
    onDismissPress: () -> Unit = {},
    country: String,
    flag: String?,
    guesses: Int,
) {
    Dialog(
        onDismissRequest = { },
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                Text(
                    text = "You Won!",
                    style = MaterialTheme.typography.headlineLarge,
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text(
                    text = "The country was:",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = country,
                    style = MaterialTheme.typography.headlineSmall,
                )
                if (flag != null) {
                    AsyncImage(
                        model = flag,
                        contentDescription = "Flag",
                        modifier = Modifier
                            .size(128.dp)
                    )
                }
                Text(
                    text = "Number of guesses: $guesses",
                    style = MaterialTheme.typography.bodyLarge,
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Button(
                        onClick = { onDismissPress() },
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(
                            text = "Main Menu",
                            style = MaterialTheme.typography.labelSmall,
                            maxLines = 1
                        )
                    }
                    Spacer(modifier = Modifier.size(12.dp))
                    if (onConfirmPress != null) {
                        Button(
                            onClick = { onConfirmPress() },
                            modifier = Modifier.weight(1f),
                        ) {
                            Text(
                                text = "New Game",
                                style = MaterialTheme.typography.labelSmall,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}
@Composable
fun ConfirmQuitAlert(
    onConfirmPress: () -> Unit = {},
    onDismissPress: () -> Unit = {},
) {
    AlertDialog(
        onDismissRequest = { },
        title = { Text("Are you sure you want to leave?")},
        text = { Text("Your current game progress will not be saved!")},
        dismissButton = {
            TextButton(onClick = { onDismissPress() }) {
                Text("Cancel")
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirmPress() }) {
                Text("Confirm")
            }
        }
    )
}

@Composable
fun TutorialAlert(
    onDismissPress: () -> Unit = {}
) {
    Dialog(
        onDismissRequest = { onDismissPress() },
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "Colour Indicators",
                    style = MaterialTheme.typography.headlineLarge,
                )
                Spacer(modifier = Modifier.size(8.dp))
                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 16.dp)
                ) {
                    val indicatorCount = 6
                    val cellSize = (maxWidth / indicatorCount).coerceIn(36.dp, 64.dp)
                    val labelStyle = MaterialTheme.typography.labelSmall.copy(
                        fontSize = (cellSize.value * 0.22f).coerceIn(8f, 12f).sp
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        ColourIndicator(
                            color = AppTheme.colors.guessGreen,
                            text = "Correct",
                            size = cellSize,
                            labelStyle = labelStyle,
                        )
                        ColourIndicator(
                            color = AppTheme.colors.guessRed,
                            text = "Incorrect",
                            size = cellSize,
                            labelStyle = labelStyle,
                        )
                        ColourIndicator(
                            color = AppTheme.colors.guessOrange,
                            text = "Close",
                            size = cellSize,
                            labelStyle = labelStyle,
                        )
                        ColourIndicator(
                            color = AppTheme.colors.guessRed,
                            text = "Lower",
                            size = cellSize,
                            imageVector = Icons.Default.ArrowDownward,
                            labelStyle = labelStyle,
                        )
                        ColourIndicator(
                            color = AppTheme.colors.guessRed,
                            text = "Higher",
                            size = cellSize,
                            imageVector = Icons.Default.ArrowUpward,
                            labelStyle = labelStyle,
                        )
                        ColourIndicator(
                            color = AppTheme.colors.guessGrey,
                            text = "No Data",
                            size = cellSize,
                            labelStyle = labelStyle,
                        )
                    }
                }
                Text(
                    text = buildAnnotatedString {
                        append("You have ")
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                            append("10")
                        }
                        append(" tries to guess the country")
                    },
                    style = MaterialTheme.typography.labelMedium,
                )
                Text(
                    text = "If you run out of guesses, it is game over",
                    style = MaterialTheme.typography.labelSmall,
                )
            }
        }
    }
}

@Composable
private fun ColourIndicator(
    color: Color,
    text: String,
    size: Dp = 48.dp,
    imageVector: ImageVector? = null,
    labelStyle: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.labelSmall,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(size)
                .padding(2.dp)
                .background(
                    color = color,
                    shape = MaterialTheme.shapes.medium
                ),
            contentAlignment = Alignment.Center,
        ) {
            if (imageVector != null) {
                Icon(
                    imageVector = imageVector,
                    contentDescription = "Arrow",
                    tint = lerp(color, Color.Black, 0.10f),
                    modifier = Modifier.fillMaxSize(0.7f)
                )
            }
        }
        Spacer(modifier = Modifier.size(6.dp))
        Text(
            text = text,
            style = labelStyle,
        )
    }
}
