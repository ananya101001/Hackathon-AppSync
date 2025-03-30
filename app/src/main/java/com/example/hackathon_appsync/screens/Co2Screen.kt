// screens/CO2Screen.kt
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
import com.example.hackathon_appsync.models.*
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
import co.yml.charts.ui.linechart.model.Line
import co.yml.charts.ui.linechart.model.LineChartData
import co.yml.charts.ui.linechart.model.LinePlotData
import co.yml.charts.ui.linechart.model.LineStyle

import com.example.hackathon_appsync.models.Indicator
import com.example.hackathon_appsync.models.Country
import com.example.hackathon_appsync.models.CO2Data

private const val TAG = "CO2Screen"
private const val CO2_INDICATOR = "EN.ATM.CO2E.KT" // CO2 emissions (kt)

@Composable
fun Co2Screen() {
    val viewModel: CO2ViewModel = viewModel()
    val state = viewModel.state.value

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp)
    ) {
        Text("🌍 World CO2 Emissions (Kilotons)",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp))

        when (state) {
            is CO2State.Loading -> CircularProgressIndicator()
            is CO2State.Error -> ErrorMessage(state.message) { viewModel.loadData() }
            is CO2State.Success -> CO2ChartWithGraph(state.data)
        }
    }
}

@Composable
private fun ErrorMessage(message: String, onRetry: () -> Unit) {
    Column {
        Text(message, color = MaterialTheme.colorScheme.error)
        Button(
            onClick = onRetry,
            modifier = Modifier.padding(top = 8.dp)) {
            Text("Retry")
        }
    }
}

@Composable
private fun CO2ChartWithGraph(data: List<CO2Data>) {
    // Filter valid data and sort by year
    val validData = data
        .filter { it.value != null && it.date.toIntOrNull() != null }
        .sortedBy { it.date.toInt() }
        .takeLast(10) // Show last 10 years for simplicity

    if (validData.isEmpty()) {
        Text("No valid CO2 data available")
        return
    }

    // Prepare chart points
    val points = validData.mapIndexed { index, item ->
        Point(index.toFloat(), item.value?.toFloat() ?: 0f)
    }

    // X-axis configuration
    val xAxisData = AxisData.Builder()
        .steps(validData.size - 1)
        .labelData { index -> validData[index].date }
        .build()

    // Y-axis configuration
    val maxValue = validData.maxOf { it.value ?: 0.0 }.toFloat()
    val yAxisData = AxisData.Builder()
        .labelData { value -> "${value.toInt()} kt" }
        .build()

    // Simple line chart configuration
    val lineChartData = LineChartData(
        linePlotData = LinePlotData(
            lines = listOf(
                Line(
                    dataPoints = points,
                    lineStyle = LineStyle(
                        color = Color(0xFF4CAF50), // Green color

                    )
                )
            )
        ),
        xAxisData = xAxisData,
        yAxisData = yAxisData
    )

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Line Chart
        LineChart(
            modifier = Modifier
                .height(300.dp)
                .fillMaxWidth(),
            lineChartData = lineChartData
        )

        // Data Table
        Text("Recent CO2 Data:",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp))

        validData.reversed().forEach { item ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Year ${item.date}:")
                Text("${"%,.0f".format(item.value)} kt",
                    fontWeight = FontWeight.Bold)
            }
            Divider()
        }
    }
}

// State and ViewModel
sealed class CO2State {
    object Loading : CO2State()
    data class Success(val data: List<CO2Data>) : CO2State()
    data class Error(val message: String) : CO2State()
}

class CO2ViewModel : ViewModel() {
    private val _state = mutableStateOf<CO2State>(CO2State.Loading)
    val state = _state

    private val service = Retrofit.Builder()
        .baseUrl("https://api.worldbank.org/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(CO2WorldBankService::class.java)

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = service.getCO2Data()
                if (response.size > 1) {
                    val data = (response[1] as List<Map<String, Any>>).mapNotNull { map ->
                        try {
                            CO2Data(
                                indicator = Indicator(
                                    id = (map["indicator"] as Map<String, String>)["id"] ?: "",
                                    value = (map["indicator"] as Map<String, String>)["value"] ?: ""
                                ),
                                country = Country(
                                    id = (map["country"] as Map<String, String>)["id"] ?: "",
                                    value = (map["country"] as Map<String, String>)["value"] ?: ""
                                ),
                                countryIso3Code = map["countryiso3code"]?.toString() ?: "",
                                date = map["date"]?.toString() ?: "",
                                value = (map["value"] as? Number)?.toDouble(),
                                unit = map["unit"]?.toString() ?: "",
                                obsStatus = map["obs_status"]?.toString() ?: "",
                                decimal = (map["decimal"] as? Number)?.toInt() ?: 0
                            )
                        } catch (e: Exception) {
                            Log.e(TAG, "Error parsing item", e)
                            null
                        }
                    }
                    _state.value = CO2State.Success(data)
                } else {
                    _state.value = CO2State.Error("Unexpected API response")
                }
            } catch (e: Exception) {
                _state.value = CO2State.Error(
                    when (e) {
                        is UnknownHostException -> "No internet connection"
                        is SocketTimeoutException -> "Request timed out"
                        else -> "Error loading data"
                    }
                )
            }
        }
    }
}

interface CO2WorldBankService {
    @GET("v2/country/WLD/indicator/EN.GHG.CO2.AG.MT.CE.AR5")
    suspend fun getCO2Data(
        @Query("format") format: String = "json",
        @Query("per_page") perPage: Int = 30
    ): List<Any>
}