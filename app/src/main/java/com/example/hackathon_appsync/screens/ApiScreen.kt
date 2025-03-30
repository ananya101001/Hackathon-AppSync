// screens/ApiScreen.kt
package com.example.hackathon_appsync.screens
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
// screens/ApiScreen.kt
import com.example.hackathon_appsync.screens.Co2Screen


@Composable
fun ApiScreen() {
    var selectedOption by remember { mutableStateOf("GDP") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            SelectableButton("GDP", selectedOption == "GDP") { selectedOption = "GDP" }
            SelectableButton("CO2", selectedOption == "CO2") { selectedOption = "CO2" }
            SelectableButton("Agri. Land", selectedOption == "Agri. Land") { selectedOption = "Agri. Land" }
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (selectedOption) {
            "GDP" -> GDPContent()
            "CO2" -> Co2Screen()
            "Agri. Land" -> AgriLandScreen()
        }
    }
}

@Composable
fun SelectableButton(label: String, isSelected: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.surface,
            contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary
            else MaterialTheme.colorScheme.onSurface
        ),
        modifier = Modifier.padding(horizontal = 4.dp)
    ) {
        Text(
            label,
            fontSize = 16.sp,
            modifier = Modifier.padding(vertical = 8.dp)
        )
    }
}