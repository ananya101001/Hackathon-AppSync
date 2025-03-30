// ChatGPTScreen.kt
package com.example.hackathon_appsync.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.ColorTemplate
import org.json.JSONObject
import com.example.hackathon_appsync.llm.LLMService

@SuppressLint("UnrememberedMutableState")
@Composable
fun ChatGPTScreen() {
    var prompt by remember { mutableStateOf("") }
    var response by remember { mutableStateOf("") }
    var showPieChart by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var plotData by remember { mutableStateOf<JSONObject?>(null) }
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Input field
        OutlinedTextField(
            value = prompt,
            onValueChange = { prompt = it },
            label = { Text("Enter your prompt") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        )

        // Submit button
        Button(
            onClick = {
                if (prompt.isNotBlank()) {
                    isLoading = true
                    response = "Loading..."
                    showPieChart = false

                    LLMService.askLLM(prompt, object : LLMService.PlotCallback {
                        override fun onPlotDataReceived(data: JSONObject) {
                            isLoading = false
                            if (data.optString("type") == "pie") {
                                plotData = data
                                showPieChart = true
                                response = ""
                            } else {
                                showPieChart = false
                                response = data.optString("response")
                            }
                        }
                    })
                }
            },
            enabled = !isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = Color.White
                )
            } else {
                Text("Submit")
            }
        }

        // Pie Chart (shown conditionally)
        if (showPieChart && plotData != null) {
            AndroidView(
                factory = { context ->
                    PieChart(context).apply {
                        setUsePercentValues(true)
                        description.isEnabled = false
                        setExtraOffsets(5f, 10f, 5f, 5f)
                        dragDecelerationFrictionCoef = 0.95f
                        isDrawHoleEnabled = true
                        setHoleColor(android.graphics.Color.WHITE)
                        setTransparentCircleColor(android.graphics.Color.WHITE)
                        setTransparentCircleAlpha(110)
                        holeRadius = 58f
                        transparentCircleRadius = 61f
                        setDrawCenterText(true)
                        rotationAngle = 0f
                        isRotationEnabled = true
                        isHighlightPerTapEnabled = true
                    }
                },
                modifier = Modifier
                    .size(300.dp),

                update = { pieChart ->
                    try {
                        val data = plotData ?: return@AndroidView
                        val labels = data.getJSONArray("labels")
                        val values = data.getJSONArray("values")

                        val entries = mutableListOf<PieEntry>()
                        for (i in 0 until labels.length()) {
                            entries.add(PieEntry(values.getDouble(i).toFloat(), labels.getString(i)))
                        }

                        val dataSet = PieDataSet(entries, data.optString("title", "Chart"))
                        dataSet.colors = ColorTemplate.MATERIAL_COLORS.toList()
                        val pieData = PieData(dataSet)
                        pieChart.data = pieData
                        pieChart.invalidate()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            )
        }

        // Text response (shown conditionally)
        if (response.isNotBlank()) {
            Text(
                text = response,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )
        }
    }
}