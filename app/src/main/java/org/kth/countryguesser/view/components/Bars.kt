package org.kth.countryguesser.view.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import org.kth.countryguesser.model.entity.UserEntity
import org.kth.countryguesser.viewmodel.IAuthViewModel

//TODO: CREATE A TOPAPPBAR WITH USER LOGIN/REGISTER ON RIGHT SIDE AND SETTINGS ON LEFT

@Composable
fun BottomBar(navController: NavController, authViewModel: IAuthViewModel, user: UserEntity?) {
    BottomAppBar(
        modifier = Modifier.height(96.dp),
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            //TODO: DECIDE WHAT WE WANT AT THE BOTTOM BAR

            if (authViewModel.authenticated()) {
                TextButton(onClick = { navController.navigate("TODO") }) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Filled.BarChart,
                            contentDescription = "Personal statistics",
                            modifier = Modifier.size(32.dp),
                            tint = MaterialTheme.colorScheme.background
                        )
                        Text("Your Stats", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.background)
                    }
                }
            }
            TextButton(onClick = { navController.navigate("TODO") }) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Filled.Settings, contentDescription = "Settings", modifier = Modifier.size(32.dp), tint = MaterialTheme.colorScheme.background)
                    Text("Settings", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.background)
                }
            }
        }
    }
}