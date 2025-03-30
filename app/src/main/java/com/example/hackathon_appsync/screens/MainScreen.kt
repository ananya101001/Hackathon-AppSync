// MainScreen.kt
package com.example.hackathon_appsync.screens
import com.example.hackathon_appsync.R
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.height
@Composable
fun MainScreen() {
    val navController = rememberNavController()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = { BottomNavBar(navController) }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "api",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("api") { ApiScreen() }
            composable("chatgpt") { ChatGPTScreen() }
            composable("car") { CarScreen() }
        }
    }
}

@Composable
fun BottomNavBar(navController: NavHostController) {
    val screens = listOf(
        NavItem("API", "api", R.drawable.api),
        NavItem("ChatGPT", "chatgpt", R.drawable.chatgpt),
        NavItem("Car", "car", R.drawable.car)
    )

    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

    NavigationBar {
        screens.forEach { screen ->
            NavigationBarItem(
                selected = currentRoute == screen.route,
                onClick = { navController.navigate(screen.route) },
                icon = {
                    Icon(
                        painter = painterResource(screen.icon),
                        contentDescription = screen.label,
                        modifier = Modifier
                            .width(26.dp)
                            .height(26.dp)
                    )
                },
                label = { Text(screen.label) }
            )
        }
    }
}

data class NavItem(val label: String, val route: String, val icon: Int)

