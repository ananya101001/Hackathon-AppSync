package com.example.hackathon_appsync.screens

import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.net.UnknownHostException
import java.net.SocketTimeoutException
import com.example.hackathon_appsync.models.Indicator
import com.example.hackathon_appsync.models.Country
import com.example.hackathon_appsync.models.AgriLandData
import kotlin.math.max
import kotlin.math.min
import androidx.compose.ui.graphics.drawscope.Stroke


private const val TAG = "AgriLandScreen"
private const val AGRI_LAND_INDICATOR = "AG.LND.AGRI.ZS"

@Composable
fun AgriLandScreen() {
    val viewModel: AgriLandViewModel = viewModel()
    val state = viewModel.state.value

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            "🌾 Agricultural Land (% of land area)",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        when (state) {
            is AgriLandState.Loading -> LoadingState()
            is AgriLandState.Error -> ErrorState(state.message) { viewModel.loadData() }
            is AgriLandState.Success -> SafeAgriLandChart(state.data)
        }
    }
}

@Composable
private fun LoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorState(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(message, color = MaterialTheme.colorScheme.error)
        Button(
            onClick = onRetry,
            modifier = Modifier.padding(top = 8.dp)
        ) {
            Text("Retry")
        }
    }
}

@Composable
private fun SafeAgriLandChart(data: List<AgriLandData>) {
    // Robust data processing
    val chartData = remember(data) {
        data.filter {
            it.value != null &&
                    it.date.toIntOrNull() != null &&
                    it.value!! in 0.0..100.0
        }.sortedBy { it.date.toInt() }
            .takeLast(10)
            .ifEmpty { null }
    }

    if (chartData == null || chartData.size < 2) {
        ErrorState(
            message = if (chartData == null) "No valid data available"
            else "Need at least 2 data points",
            onRetry = {}
        )
        return
    }

    // Custom chart implementation
    Column {
        AgriLandCanvasChart(chartData)
        DataTable(chartData.reversed())
    }
}


@Composable
private fun AgriLandCanvasChart(data: List<AgriLandData>) {
    val textPaint = remember {
        Paint().asFrameworkPaint().apply {
            color = android.graphics.Color.BLACK
            textSize = 24f
            textAlign = android.graphics.Paint.Align.CENTER
        }
    }

    Canvas(
        modifier = Modifier
            .height(300.dp)
            .fillMaxWidth()
            .padding(vertical = 16.dp)
    ) {
        val spacePerYear = size.width / (data.size - 1)
        val maxValue = data.maxOf { it.value!! }.toFloat() // Convert to Float
        val minValue = data.minOf { it.value!! }.toFloat() // Convert to Float
        val valueRange = max(1f, maxValue - minValue)
        val heightRatio = size.height / valueRange

        // Draw chart line
        val linePath = Path().apply {
            data.forEachIndexed { index, item ->
                val x = index * spacePerYear
                val y = size.height - ((item.value!!.toFloat() - minValue) * heightRatio) // Convert to Float

                if (index == 0) moveTo(x, y) else lineTo(x, y)
            }
        }

        drawPath(
            path = linePath,
            color = Color(0xFF4CAF50),
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round) // Proper Stroke usage
        )

        // Draw data points
        data.forEachIndexed { index, item ->
            val x = index * spacePerYear
            val y = size.height - ((item.value!!.toFloat() - minValue) * heightRatio) // Convert to Float

            drawCircle(
                color = Color(0xFF4CAF50),
                radius = 5f,
                center = Offset(x, y)
            )

            // Draw year labels (every other year)
            if (index % 2 == 0) {
                drawContext.canvas.nativeCanvas.drawText(
                    data[index].date.takeLast(2),
                    x,
                    size.height + 30,
                    textPaint
                )
            }
        }

        // Draw Y-axis labels
        val yLabelValues = listOf(minValue, minValue + valueRange/2, maxValue)
        yLabelValues.forEach { value ->
            val y = size.height - (value - minValue) * heightRatio
            drawContext.canvas.nativeCanvas.drawText(
                "%.1f%%".format(value),
                30f,
                y + 10,
                textPaint.apply {
                    textAlign = android.graphics.Paint.Align.LEFT
                }
            )
        }
    }
}

@Composable
private fun DataTable(data: List<AgriLandData>) {
    Column(modifier = Modifier.padding(top = 16.dp)) {
        Text("Recent Data:",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp))

        data.forEach { item ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Year ${item.date}:")
                Text("%.1f%%".format(item.value),
                    fontWeight = FontWeight.Bold)
            }
            Divider()
        }
    }
}

// State management
sealed class AgriLandState {
    object Loading : AgriLandState()
    data class Success(val data: List<AgriLandData>) : AgriLandState()
    data class Error(val message: String) : AgriLandState()
}

class AgriLandViewModel : ViewModel() {
    private val _state = mutableStateOf<AgriLandState>(AgriLandState.Loading)
    val state = _state

    private val service by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.worldbank.org/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AgriLandWorldBankService::class.java)
    }

    init {
        loadData()
    }

    fun loadData() {
        _state.value = AgriLandState.Loading
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = service.getAgriLandData()
                if (response.size > 1) {
                    val parsedData = parseResponse(response[1] as? List<*>)
                    withContext(Dispatchers.Main) {
                        _state.value = if (parsedData.isNotEmpty()) {
                            AgriLandState.Success(parsedData)
                        } else {
                            AgriLandState.Error("No valid data available")
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        _state.value = AgriLandState.Error("Invalid API response")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _state.value = AgriLandState.Error(
                        when (e) {
                            is UnknownHostException -> "No internet connection"
                            is SocketTimeoutException -> "Request timed out"
                            else -> "Failed to load data"
                        }
                    )
                }
            }
        }
    }

    private fun parseResponse(data: List<*>?): List<AgriLandData> {
        return data?.mapNotNull { item ->
            try {
                if (item is Map<*, *>) {
                    AgriLandData(
                        indicator = Indicator(
                            id = (item["indicator"] as? Map<*, *>)?.get("id")?.toString() ?: "",
                            value = (item["indicator"] as? Map<*, *>)?.get("value")?.toString() ?: ""
                        ),
                        country = Country(
                            id = (item["country"] as? Map<*, *>)?.get("id")?.toString() ?: "",
                            value = (item["country"] as? Map<*, *>)?.get("value")?.toString() ?: ""
                        ),
                        countryIso3Code = item["countryiso3code"]?.toString() ?: "",
                        date = item["date"]?.toString() ?: "",
                        value = (item["value"] as? Number)?.toDouble(),
                        unit = item["unit"]?.toString() ?: "",
                        obsStatus = item["obs_status"]?.toString() ?: "",
                        decimal = (item["decimal"] as? Number)?.toInt() ?: 0
                    )
                } else null
            } catch (e: Exception) {
                Log.e(TAG, "Error parsing item", e)
                null
            }
        } ?: emptyList()
    }
}

interface AgriLandWorldBankService {
    @GET("v2/country/WLD/indicator/$AGRI_LAND_INDICATOR")
    suspend fun getAgriLandData(
        @Query("format") format: String = "json",
        @Query("per_page") perPage: Int = 30,
        @Query("date") date: String = "2000:2023"
    ): List<Any>
}