// CarScreen.kt
package com.example.hackathon_appsync.screens

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import okhttp3.*
import org.json.JSONObject
import java.io.File
import java.io.IOException

@Composable
fun CarScreen() {
    var selectedFileUri by remember { mutableStateOf<Uri?>(null) }
    var predictionResult by remember { mutableStateOf<PredictionResult?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    // File picker launcher
    val audioLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            selectedFileUri = uri
            predictionResult = null
            errorMessage = null
        }
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start
    ) {
        // Select Audio File Button
        Button(
            onClick = { audioLauncher.launch("audio/*") },
            modifier = Modifier
                .wrapContentWidth()
                .padding(bottom = 16.dp)
        ) {
            Text("Select Audio File")
        }

        selectedFileUri?.let { uri ->
            Text(
                text = "Selected: ${uri.lastPathSegment ?: "audio file"}",
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        // Predict ML Button
        Button(
            onClick = {
                selectedFileUri?.let { uri ->
                    isLoading = true
                    errorMessage = null
                    uploadAudio(context, uri) { result ->
                        isLoading = false
                        when (result) {
                            is Result.Success -> {
                                predictionResult = result.data
                                errorMessage = null
                            }
                            is Result.Error -> {
                                errorMessage = result.message
                                predictionResult = null
                            }
                        }
                    }
                } ?: run {
                    errorMessage = "Please select an audio file first"
                }
            },
            enabled = !isLoading,
            modifier = Modifier
                .wrapContentWidth()
                .padding(bottom = 20.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Predict ML")
            }
        }

        // Show loading indicator
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(16.dp)
            )
        }

        // Show error message if any
        errorMessage?.let { message ->
            Text(
                text = message,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 16.dp)
            )
        }

        // Show prediction result if available
        predictionResult?.let { result ->
            Column(
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text(
                    text = "Prediction: ${result.prediction}",
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = "Confidence: ${"%.2f".format(result.confidence * 100)}%",
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
        }
    }
}

// Data classes for handling results
data class PredictionResult(
    val prediction: String,
    val confidence: Double
)

sealed class Result {
    data class Success(val data: PredictionResult) : Result()
    data class Error(val message: String) : Result()
}

private fun uploadAudio(context: Context, uri: Uri, callback: (Result) -> Unit) {
    try {
        val inputStream = context.contentResolver.openInputStream(uri) ?: run {
            callback(Result.Error("Could not open file"))
            return
        }

        val file = File.createTempFile("audio", ".wav", context.cacheDir).apply {
            outputStream().use { output ->
                inputStream.use { it.copyTo(output) }
            }
        }

        val mediaType = MediaType.parse("audio/wav") ?: run {
            callback(Result.Error("Invalid media type"))
            return
        }

        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart(
                "file",  // Matching your curl command
                file.name,
                RequestBody.create(mediaType, file)
            )
            .build()

        val request = Request.Builder()
            .url("http://10.0.2.2:5001/predict")  // Use 10.0.2.2 for Android emulator
            .post(requestBody)
            .build()

        OkHttpClient().newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback(Result.Error("Network error: ${e.message}"))
            }

            override fun onResponse(call: Call, response: Response) {
                try {
                    val responseBody = response.body()?.string() ?: run {
                        callback(Result.Error("Empty response from server"))
                        return
                    }

                    val json = JSONObject(responseBody)
                    val prediction = json.getString("prediction")
                    val confidence = json.getDouble("confidence")

                    callback(Result.Success(PredictionResult(prediction, confidence)))
                } catch (e: Exception) {
                    callback(Result.Error("Error parsing response: ${e.message}"))
                }
            }
        })
    } catch (e: Exception) {
        callback(Result.Error("Error: ${e.message}"))
    }
}
