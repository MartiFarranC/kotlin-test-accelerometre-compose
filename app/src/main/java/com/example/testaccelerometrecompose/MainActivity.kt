package com.example.testaccelerometrecompose

import android.annotation.SuppressLint
import androidx.compose.ui.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var mAccelerometer: Sensor? = null
    private var lastUpdate: Long = 0

    // Referència per poder actualitzar l'estat de Compose des del listener
    private var colorState: MutableState<Boolean>? = null

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        mAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        if (mAccelerometer == null) {
            // Si no existeix, avisem l'usuari [cite: 214, 304]
            Toast.makeText(this, "Aquest dispositiu no té acceleròmetre", Toast.LENGTH_LONG).show()
        }

        lastUpdate = System.currentTimeMillis()

        enableEdgeToEdge()
        setContent {
            // Utilitzem MaterialTheme per evitar l'error de referència del tema personalitzat
            MaterialTheme {
                // rememberSaveable per persistir el color en girar la pantalla [cite: 1307, 1393]
                val color = rememberSaveable { mutableStateOf(false) }
                colorState = color

                Scaffold(modifier = Modifier.fillMaxSize()) { padding ->
                    // Passem el sensor per mostrar les seves capacitats a l'àrea central [cite: 270]
                    MainLayout(color, mAccelerometer, Modifier.padding(padding))
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        mAccelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            getAccelerometer(event)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Callback obligatori de la interfície [cite: 233, 234]
    }

    private fun getAccelerometer(event: SensorEvent) {
        val accelerationSquareRootThreshold = 200
        val timeThreashold = 1000
        val values = event.values

        val x = values[0]
        val y = values[1]
        val z = values[2]
        val accelerationSquareRoot = (x * x + y * y + z * z
                / (SensorManager.GRAVITY_EARTH * SensorManager.GRAVITY_EARTH))
        val actualTime = System.currentTimeMillis()
        if (accelerationSquareRoot >= accelerationSquareRootThreshold) {
            if (actualTime - lastUpdate < timeThreashold) {
                return
            }
            lastUpdate = actualTime
            Toast.makeText(this, R.string.shuffed, Toast.LENGTH_SHORT).show()

            // Actualitzem l'estat persistent
            colorState?.value = !(colorState?.value ?: false)
        }
    }
}

@Composable
fun MainLayout(color: MutableState<Boolean>, sensor: Sensor?, modifier: Modifier = Modifier) {
    // Estructura de tres àrees verticals
    Column(modifier = modifier.fillMaxSize()) {

        // ÀREA SUPERIOR: Canvia de color
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(if (color.value) Color.Red else Color.Green),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "Àrea de Color", color = Color.White, fontWeight = FontWeight.Bold)
        }

        // ÀREA CENTRAL: Missatge i Capacitats
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (sensor != null) {
                    Text(text = "Shake to get a toast and to switch color", fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(10.dp))
                    // Informació sobre capacitats del sensor [cite: 270, 273, 275, 276]
                    Text(text = "Capacitats del sensor:", fontWeight = FontWeight.Bold)
                    Text(text = "Nom: ${sensor.name}")
                    Text(text = "Resolució: ${sensor.resolution}")
                    Text(text = "Rang màxim: ${sensor.maximumRange}")
                    Text(text = "Consum: ${sensor.power} mA")
                } else {
                    // Missatge si no hi ha acceleròmetre [cite: 214, 215]
                    Text(text = "Sorry, there is no accelerometer", color = Color.Red, fontWeight = FontWeight.Bold)
                }
            }
        }

        // ÀREA INFERIOR: Estàtica
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(Color.DarkGray),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "Àrea Inferior", color = Color.LightGray)
        }
    }
}