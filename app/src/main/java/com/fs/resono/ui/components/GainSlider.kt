package com.fs.resono.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlin.math.log10

@Composable
fun GainSlider(
    gain: Float,
    onGainChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    gainRange: ClosedFloatingPointRange<Float> = 0f..2f
) {
    Column(modifier = modifier) {

        Text(
            text = "Gain: ${"%.2f".format(gain)}x  (${gainToDb(gain)} dB)",
            style = MaterialTheme.typography.labelMedium
        )

        Slider(
            value = gain,
            onValueChange = onGainChange,
            valueRange = gainRange,
            steps = 20,
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary,
                inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            modifier = Modifier.padding(horizontal = 8.dp)
        )
    }
}

private fun gainToDb(gain: Float): String {
    return if (gain <= 0f) {
        "-∞"
    } else {
        "%.1f".format(20 * log10(gain))
    }
}
