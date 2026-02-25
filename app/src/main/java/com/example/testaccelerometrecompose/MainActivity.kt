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
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.testaccelerometrecompose.ui.theme.TestAccelerometreComposeTheme

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
        // Verificació de disponibilitat (Bona pràctica) [cite: 211, 304]
        mAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        lastUpdate = System.currentTimeMillis()

        enableEdgeToEdge()
        setContent {
            // rememberSaveable evita que el color torni a verd en girar la pantalla
            val color = rememberSaveable { mutableStateOf(false) }
            colorState = color

            TestAccelerometreComposeTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) {
                    SensorsInfo(color)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Registrem el listener aquí per reactivar el sensor en tornar a l'app [cite: 1196, 1203]
        mAccelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    override fun onPause() {
        super.onPause()
        // Unregister per estalviar bateria quan l'app no és visible [cite: 252, 258, 1292]
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {
        // Identifiquem el sensor abans de processar [cite: 262, 263]
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
fun SensorsInfo(color: MutableState<Boolean> ) {
    Card(
        modifier = Modifier.fillMaxSize(),
        colors = CardDefaults.cardColors(if (color.value) Color.Red else Color.Green),
        shape = CardDefaults.shape,
        elevation = CardDefaults.cardElevation(),
        border = BorderStroke(10.dp, if (color.value) Color.Black else Color.LightGray)
    ) {
        Column() {
            Text(text = "")
            Row() {
                Text(text = "            ")
                Text(text = stringResource(R.string.shake))
            }
        }
    }
}

