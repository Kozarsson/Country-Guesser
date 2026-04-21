package org.kth.countryguesser.view.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.unit.dp

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