package org.kth.countryguesser.view.components

import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.ZeroCornerSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material.icons.outlined.Error
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import kotlinx.coroutines.launch
import org.kth.countryguesser.R
import org.kth.countryguesser.util.GamePopupState
import org.kth.countryguesser.util.PopupState
import org.kth.countryguesser.viewmodel.AuthVMImpl
import org.kth.countryguesser.viewmodel.GameVMImpl

@Composable
private fun rememberActivityAuthVm(): AuthVMImpl {
    val activity = LocalActivity.current as? ComponentActivity
        ?: error("Top/bottom bars require a ComponentActivity host")
    return hiltViewModel(activity)
}

@Composable
private fun rememberActivityGameVm(): GameVMImpl {
    val activity = LocalActivity.current as? ComponentActivity
        ?: error("Top/bottom bars require a ComponentActivity host")
    return hiltViewModel(activity)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppModalNavigationDrawer(
    navController: NavController,
    drawerState: DrawerState,
    content: @Composable () -> Unit
) {
    val authVM = rememberActivityAuthVm()
    val user by authVM.userEntity.collectAsState()
    val scope = rememberCoroutineScope()
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.background)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // App Logo
                    Image(
                        painter = painterResource(id = R.drawable.countryguesser),
                        contentDescription = "App Logo",
                        modifier = Modifier
                            .size(80.dp)
                            .padding(bottom = 16.dp)
                    )

                    // App Name
                    Text(
                        text = "Country Guesser",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp
                        ),
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )

                    // User Email or Status
                    Text(
                        text = user?.email ?: "No logged in user",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Drawer Items
                    if (user != null && !user!!.isAnonymous) {
                        DrawerItem("Change Password") {
                            navController.navigate(Routes.CHANGE_PASSWORD)
                            scope.launch { drawerState.close() }
                        }
                        DrawerItem("Logout") {
                            authVM.signOut()
                            scope.launch { drawerState.close() }
                        }
                    } else {
                        DrawerItem("Login") {
                            navController.navigate(Routes.LOGIN)
                            scope.launch { drawerState.close() }
                        }
                        DrawerItem("Register") {
                            navController.navigate(Routes.REGISTER)
                            scope.launch { drawerState.close() }
                        }
                    }
                }
            }
        },
        content = content,
    )
}

@Composable
fun DrawerItem(label: String, onClick: () -> Unit) {
    TextButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
fun BottomBarItem(navController: NavController, label: String, route: String, iconName: String) {
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
    val gameVM = rememberActivityGameVm()
    val filledIcon = when (iconName) {
        "Map" -> Icons.Filled.Map
        "Home" -> Icons.Filled.Home
        "Person" -> Icons.Filled.Person
        else -> Icons.Filled.Error
    }
    val outlinedIcon = when (iconName) {
        "Map" -> Icons.Outlined.Map
        "Home" -> Icons.Outlined.Home
        "Person" -> Icons.Outlined.Person
        else -> Icons.Outlined.Error
    }
    TextButton(onClick = {
        if (currentRoute.orEmpty().startsWith(Routes.GAME)) {
            gameVM.setGamePopupState(GamePopupState.CONFIRM_QUIT)
        } else if (currentRoute != route) navController.navigate(route) }) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                if (currentRoute == route) filledIcon else outlinedIcon,
                contentDescription = label,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.background
            )
            Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.background)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(onMenuClick: () -> Unit) {
    val authVM = rememberActivityAuthVm()
    val user by authVM.userEntity.collectAsState()

    TopAppBar(
        title = {
            Image(
                painter = painterResource(id = R.drawable.countryguesser),
                contentDescription = "Open Drawer"
            )
        },
        actions = {
            IconButton(onClick = onMenuClick) {
                Icon(
                    imageVector = if (user != null && !user!!.isAnonymous) {
                        Icons.Filled.AccountCircle
                    } else {
                        Icons.AutoMirrored.Filled.Login
                    },
                    contentDescription = "Login",
                    tint = MaterialTheme.colorScheme.background
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary,
        )
    )
}


@Composable
fun BottomBar(navController: NavController) {
    val authVM = rememberActivityAuthVm()
    val user by authVM.userEntity.collectAsState()

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
            BottomBarItem(navController, "Study", Routes.STUDY, "Map")
            BottomBarItem(navController, "Home", Routes.HOME, "Home")
            if (user != null && !user!!.isAnonymous) BottomBarItem(navController, "Profile", Routes.PROFILE, "Person")
        }
    }
}