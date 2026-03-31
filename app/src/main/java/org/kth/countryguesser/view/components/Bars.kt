package org.kth.countryguesser.view.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.ZeroCornerSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import org.kth.countryguesser.model.entity.UserEntity
import org.kth.countryguesser.viewmodel.IAuthViewModel

//TODO: CREATE A TOPAPPBAR WITH USER LOGIN/REGISTER ON RIGHT SIDE AND SETTINGS ON LEFT

@Composable
fun BottomBar(navController: NavController, authViewModel: IAuthViewModel, user: UserEntity?) {
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium.copy(all = ZeroCornerSize),
        color = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        tonalElevation = 8.dp,
        shadowElevation = 4.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            //TODO: DECIDE WHAT WE WANT AT THE BOTTOM BAR
            TextButton(onClick = { /* if (currentRoute != Routes.STUDY) navController.navigate(Routes.STUDY */ }) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Map,
                        contentDescription = "Study",
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.background
                    )
                    Text("Study", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.background)
                }
            }

            TextButton(onClick = { if (currentRoute != Routes.HOME) navController.navigate(Routes.HOME) }) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        if (currentRoute == Routes.HOME) Icons.Filled.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = "Home",
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.background
                    )
                    Text("Home", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.background)
                }
            }


            if (authViewModel.authenticated()) {
                TextButton(onClick = { if (currentRoute != Routes.PROFILE) navController.navigate(Routes.PROFILE) }) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            if (currentRoute == Routes.PROFILE) Icons.Filled.Person else Icons.Default.PersonOutline,
                            contentDescription = "Personal statistics",
                            modifier = Modifier.size(32.dp),
                            tint = MaterialTheme.colorScheme.background
                        )
                        Text("Profile", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.background)
                    }
                }
            }
        }
    }
}