// GdpScreen.kt
package com.example.hackathon_appsync.screens

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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

// yCharts imports
import co.yml.charts.axis.AxisData
import co.yml.charts.common.model.Point
import co.yml.charts.ui.linechart.LineChart
import co.yml.charts.ui.linechart.model.GridLines
import co.yml.charts.ui.linechart.model.IntersectionPoint
import co.yml.charts.ui.linechart.model.Line
import co.yml.charts.ui.linechart.model.LineChartData
import co.yml.charts.ui.linechart.model.LinePlotData
import co.yml.charts.ui.linechart.model.LineStyle

import co.yml.charts.ui.linechart.model.SelectionHighlightPoint
import co.yml.charts.ui.linechart.model.SelectionHighlightPopUp
import androidx.compose.ui.unit.dp

private const val TAG = "GDPScreen"

// Model classes
data class Indicator(
    val id: String,
    val value: String
)

data class Country(
    val id: String,
    val value: String
)

data class GdpData(
    val indicator: Indicator,
    val country: Country,
    val countryIso3Code: String,
    val date: String,
    val value: Double?,
    val unit: String,
    val obsStatus: String,
    val decimal: Int
)

// Sealed class for state management
sealed class GdpState {
    object Loading : GdpState()
    data class Success(val data: List<GdpData>) : GdpState()
    data class Error(val message: String) : GdpState()
}

// Retrofit service interface
interface WorldBankService {
    @GET("v2/country/WLD/indicator/NY.GDP.MKTP.KD.ZG")
    suspend fun getGdpData(
        @Query("format") format: String = "json",
        @Query("per_page") perPage: Int = 100
    ): List<Any>
}

// ViewModel implementation
class GdpViewModel : ViewModel() {
    private val _state = mutableStateOf<GdpState>(GdpState.Loading)
    val state = _state

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api.worldbank.org/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val service = retrofit.create(WorldBankService::class.java)

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                Log.d(TAG, "Making API call...")
                val responseBody = service.getGdpData()
                Log.d(TAG, "API call completed. Response size: ${responseBody.size}")

                withContext(Dispatchers.Main) {
                    if (responseBody.size > 1) {
                        try {
                            Log.d(TAG, "Attempting to parse response...")
                            val rawData = responseBody[1] as? List<Map<String, Any>>
                            val data = rawData?.mapNotNull { map ->
                                try {
                                    val indicatorMap = map["indicator"] as? Map<String, String>
                                    val countryMap = map["country"] as? Map<String, String>

                                    GdpData(
                                        indicator = Indicator(
                                            id = indicatorMap?.get("id") ?: "",
                                            value = indicatorMap?.get("value") ?: ""
                                        ),
                                        country = Country(
                                            id = countryMap?.get("id") ?: "",
                                            value = countryMap?.get("value") ?: ""
                                        ),
                                        countryIso3Code = map["countryiso3code"]?.toString() ?: "",
                                        date = map["date"]?.toString() ?: "",
                                        value = (map["value"] as? Number)?.toDouble(),
                                        unit = map["unit"]?.toString() ?: "",
                                        obsStatus = map["obs_status"]?.toString() ?: "",
                                        decimal = (map["decimal"] as? Number)?.toInt() ?: 0
                                    )
                                } catch (e: Exception) {
                                    Log.e(TAG, "Error parsing item: ${e.message}")
                                    null
                                }
                            }

                            _state.value = if (!data.isNullOrEmpty()) {
                                GdpState.Success(data)
                            } else {
                                GdpState.Error("No GDP data available")
                            }
                        } catch (e: Exception) {
                            _state.value = GdpState.Error("Data format error")
                            Log.e(TAG, "Parsing error", e)
                        }
                    } else {
                        _state.value = GdpState.Error("Unexpected API response format")
                    }
                }
            } catch (e: UnknownHostException) {
                withContext(Dispatchers.Main) {
                    _state.value = GdpState.Error("No internet connection")
                }
                Log.e(TAG, "Network error - no internet", e)
            } catch (e: SocketTimeoutException) {
                withContext(Dispatchers.Main) {
                    _state.value = GdpState.Error("Request timed out")
                }
                Log.e(TAG, "Network error - timeout", e)
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _state.value = GdpState.Error("Unknown error occurred")
                }
                Log.e(TAG, "Unexpected error", e)
            }
        }
    }
}

@Composable
fun GDPContent() {
    val viewModel: GdpViewModel = viewModel()
    val state = viewModel.state.value

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            "📈 World GDP Growth (Annual %)",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        when (state) {
            is GdpState.Loading -> CircularProgressIndicator()
            is GdpState.Error -> {
                Column {
                    Text(state.message, color = MaterialTheme.colorScheme.error)
                    Button(
                        onClick = { viewModel.loadData() },
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Text("Retry")
                    }
                }
            }
            is GdpState.Success -> GdpChartWithGraph(state.data)
        }
    }
}

// GdpScreen.kt (updated chart section only)

@Composable
fun GdpChartWithGraph(data: List<GdpData>) {
    val validData = data
        .filter { it.value != null && it.date.toIntOrNull() != null }
        .sortedBy { it.date.toInt() }

    if (validData.isEmpty()) {
        Text("No valid GDP data to display")
        return
    }

    // Prepare chart points
    val points = validData.mapIndexed { index, gdpData ->
        Point(index.toFloat(), gdpData.value?.toFloat() ?: 0f)
    }

    // X-axis configuration
    val xAxisData = AxisData.Builder()

        .steps(validData.size - 1)
        .labelData { index -> validData[index].date }
        .build()

    // Y-axis configuration
    val maxValue = validData.maxOfOrNull { it.value ?: 0.0 }?.toFloat() ?: 10f
    val yAxisData = AxisData.Builder()
         // Keep as Float for axis values
        .labelData { value -> "${value.toInt()}%" }
        .build()

    // Simplified line chart configuration
    val lineChartData = LineChartData(
        linePlotData = LinePlotData(
            lines = listOf(
                Line(
                    dataPoints = points,
                    lineStyle = LineStyle(
                        color = Color.Blue,
                        // Keep as Dp for line width
                    )
                )
            )
        ),
        xAxisData = xAxisData,
        yAxisData = yAxisData
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // Line Chart
        LineChart(
            modifier = Modifier
                .height(300.dp)
                .fillMaxWidth(),
            lineChartData = lineChartData
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Data Table
        Text(
            "Detailed Data:",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        validData.forEach { item ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Year ${item.date}:")
                Text(
                    "${"%.2f".format(item.value)}%",
                    fontWeight = FontWeight.Bold
                )
            }
            Divider()
        }
    }
}
