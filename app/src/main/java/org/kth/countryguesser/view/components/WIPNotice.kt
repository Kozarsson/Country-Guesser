package org.kth.countryguesser.view.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

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
