package com.fiap.manarolling.ui

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlin.math.sqrt
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiceScreen() {
    val context = LocalContext.current
    var selectedSides by remember { mutableStateOf(20) }  // d20 selecionado por padrão
    var result by remember { mutableStateOf<Int?>(null) }

    fun roll() {
        result = Random.nextInt(1, selectedSides + 1)
    }

    // Detectar chacoalhar
    DisposableEffect(selectedSides) {
        val sm = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val accel = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        var lastShake = 0L

        val listener = object : SensorEventListener {
            override fun onSensorChanged(e: SensorEvent) {
                val x = e.values[0]; val y = e.values[1]; val z = e.values[2]
                // magnitude do vetor de aceleração menos gravidade
                val magnitude = sqrt((x * x + y * y + z * z).toDouble()).toFloat()
                val now = System.currentTimeMillis()
                // threshold e debouncing simples
                if (magnitude > 14f && now - lastShake > 800) {
                    lastShake = now
                    roll()
                }
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        sm.registerListener(listener, accel, SensorManager.SENSOR_DELAY_UI)
        onDispose { sm.unregisterListener(listener) }
    }

    // UI
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Rolador de Dados") },
                navigationIcon = { Icon(Icons.Filled.Casino, contentDescription = null) }
            )
        }
    ) { pad ->
        Box(
            Modifier
                .padding(pad)
                .fillMaxSize()
                // ✅ Duplo clique rola o dado
                .pointerInput(selectedSides) {
                    detectTapGestures(onDoubleTap = { roll() })
                }
        ) {
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(8.dp))

                // Resultado grande
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Selecionado: d$selectedSides", style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = result?.toString() ?: "Toque duas vezes ou chacoalhe",
                        style = MaterialTheme.typography.displayLarge,
                        textAlign = TextAlign.Center
                    )
                }

                // Botões inferiores (d100, d20, d10)
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    DieChoice(
                        label = "d100", sides = 100,
                        selected = selectedSides == 100,
                        onClick = { selectedSides = 100 }
                    )
                    DieChoice(
                        label = "d20", sides = 20,
                        selected = selectedSides == 20,
                        onClick = { selectedSides = 20 }
                    )
                    DieChoice(
                        label = "d10", sides = 10,
                        selected = selectedSides == 10,
                        onClick = { selectedSides = 10 }
                    )
                }
            }
        }
    }
}

@Composable
private fun DieChoice(label: String, sides: Int, selected: Boolean, onClick: () -> Unit) {
    if (selected) {
        Button(onClick = onClick, modifier = Modifier.height(48.dp)) { Text(label) }
    } else {
        OutlinedButton(onClick = onClick, modifier = Modifier.height(48.dp)) { Text(label) }
    }
}
