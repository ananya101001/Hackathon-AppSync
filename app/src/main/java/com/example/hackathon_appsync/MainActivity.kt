package com.example.hackathon_appsync
//
//import android.os.Bundle
//import androidx.activity.ComponentActivity
//import androidx.activity.compose.setContent
//import androidx.activity.enableEdgeToEdge
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.vector.ImageVector
//import androidx.compose.ui.res.painterResource
//import androidx.compose.ui.res.vectorResource
//import androidx.navigation.compose.*
//import com.example.hackathon_appsync.ui.theme.HackathonAppSyncTheme
//import androidx.navigation.compose.NavHost
//import androidx.navigation.compose.composable
//import androidx.navigation.compose.rememberNavController
//import androidx.compose.foundation.layout.padding
//import androidx.navigation.NavHostController
//import androidx.navigation.compose.currentBackStackEntryAsState
//import androidx.compose.material3.NavigationBar
//import androidx.compose.material3.NavigationBarItem
//import androidx.navigation.compose.currentBackStackEntryAsState
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import androidx.compose.material3.Icon
//import androidx.compose.material3.NavigationBar
//import androidx.compose.material3.NavigationBarItem
//import androidx.compose.ui.res.painterResource
//import androidx.compose.material3.Text
//import androidx.compose.foundation.layout.size
//import androidx.compose.foundation.layout.width
//import androidx.compose.foundation.layout.height
//import androidx.compose.foundation.layout.*
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//
//
//import androidx.compose.ui.unit.dp
//
//
//
//
//
//class MainActivity : ComponentActivity() {
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
//        setContent {
//            HackathonAppSyncTheme {
//                MainScreen()
//            }
//        }
//    }
//}
//
//@Composable
//fun MainScreen() {
//    val navController = rememberNavController()
//
//    Scaffold(
//        modifier = Modifier.fillMaxSize(),
//        bottomBar = { BottomNavBar(navController) }
//    ) { innerPadding ->
//        NavHost(
//            navController = navController,
//            startDestination = "api",
//            modifier = Modifier.padding(innerPadding)
//        ) {
//            composable("api") { ApiScreen() }
//            composable("chatgpt") { ChatGPTScreen() }
//            composable("car") { CarScreen() }
//        }
//    }
//}
//
//
//@Composable
//fun BottomNavBar(navController: NavHostController) {
//    NavigationBar {
//        val screens = listOf(
//            NavItem("API", "api", R.drawable.api),
//            NavItem("ChatGPT", "chatgpt", R.drawable.chatgpt),
//            NavItem("Car", "car", R.drawable.car)
//        )
//
//        val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
//
//        screens.forEach { screen ->
//            NavigationBarItem(
//                selected = currentRoute == screen.route,
//                onClick = { navController.navigate(screen.route) },
//                icon = {
//                    Icon(
//                        painter = painterResource(screen.icon),
//                        contentDescription = screen.label,
//                        modifier = Modifier.width(26.dp).height(26.dp),
//                         // Set the same size for all icons
//                    )
//                },
//                label = { Text(screen.label) }
//            )
//        }
//    }
//}
//
//@Composable
//fun ApiScreen() {
//    var selectedOption by remember { mutableStateOf("GDP") }
//
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .padding(16.dp)
//    ) {
//        // ✅ Ensuring buttons are evenly spaced and visible
//        Row(
//            modifier = Modifier.fillMaxWidth(),
//            horizontalArrangement = Arrangement.SpaceEvenly
//        ) {
//            SelectableButton("GDP", selectedOption == "GDP") { selectedOption = "GDP" }
//            SelectableButton("CO2", selectedOption == "CO2") { selectedOption = "CO2" }
//            SelectableButton("Agri. Land", selectedOption == "Agri. Land") { selectedOption = "Agri. Land" }
//        }
//
//        Spacer(modifier = Modifier.height(16.dp)) // Adds spacing between buttons and content
//
//        // ✅ Display content based on selection
//        when (selectedOption) {
//            "GDP" -> GDPContent()
//            "CO2" -> CO2Content()
//            "Agri.Land" -> AgriLandContent()
//        }
//    }
//}
//
//// ✅ Fix: Ensure buttons have a visible background when unselected
//
//@Composable
//fun SelectableButton(label: String, isSelected: Boolean, onClick: () -> Unit) {
//    Button(
//        onClick = onClick,
//        colors = ButtonDefaults.buttonColors(
//            containerColor = if (isSelected) MaterialTheme.colorScheme.primary
//            else MaterialTheme.colorScheme.surface,
//            contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary
//            else MaterialTheme.colorScheme.onSurface
//        ),
//        modifier = Modifier.padding(horizontal = 4.dp) // Each button takes 1/3 of the width
//    ) {
//        Text(
//            label,
//            fontSize = 20.sp,  // Increase the font size here
//            modifier = Modifier.padding(vertical = 12.dp)  // Adjust the button's height for better spacing
//        )
//    }
//}
//
//// ✅ Sample content functions
//@Composable
//fun GDPContent() {
//    Text("📈 GDP Data Graph Here", modifier = Modifier.padding(16.dp),fontSize = 24.sp)
//}
//
//@Composable
//fun CO2Content() {
//    Text("🌍 CO2 Emissions Data Graph Here", modifier = Modifier.padding(16.dp), fontSize = 24.sp)
//}
//
//@Composable
//fun AgriLandContent() {
//    Text("🌾 Agricultural Land Data Graph Here", modifier = Modifier.padding(16.dp),fontSize = 24.sp)
//}
//
//
//
//
//
//
//@Composable
//fun ChatGPTScreen() {
//    Text(text = "ChatGPT Screen")
//}
//
//@Composable
//fun CarScreen() {
//    Text(text = "Car Screen")
//}
//
//data class NavItem(val label: String, val route: String, val icon: Int)

// MainActivity.kt
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.hackathon_appsync.screens.MainScreen
import com.example.hackathon_appsync.ui.theme.HackathonAppSyncTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HackathonAppSyncTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen()
                }
            }
        }
    }
}