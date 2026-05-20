package com.fs.resono.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun EqSlider(
    bandName: String,
    value: Float,
    modifier: Modifier = Modifier,
    valueRange: ClosedFloatingPointRange<Float> = -12f..12f,
    onValueChange: (Float) -> Unit
) {
    Column(
        modifier = modifier.width(44.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = "${value.toInt()} dB",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
            maxLines = 1
        )
        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .weight(1f)
                .width(44.dp),
            contentAlignment = Alignment.Center
        ){
            Slider(
                value = value,
                onValueChange = onValueChange,
                valueRange = valueRange,
                modifier = Modifier
                    .graphicsLayer{
                        rotationZ = -90f
                    }
                    .layout { measurable, constraints ->
                        val placeable = measurable.measure(
                            Constraints(
                                minWidth = constraints.minHeight,
                                maxWidth = constraints.maxHeight,
                                minHeight = constraints.minWidth,
                                maxHeight = constraints.minWidth
                            )
                        )
                        layout(placeable.height, placeable.width) {
                            placeable.place(
                                -placeable.width / 2 + placeable.height / 2,
                                -placeable.height / 2 + placeable.width / 2
                            )
                        }
                    }
                        .height(44.dp),
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.primary,
                        activeTrackColor = MaterialTheme.colorScheme.primary,
                        inactiveTrackColor = MaterialTheme.colorScheme.surfaceContainerHighest
                )
            )
        }
        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = bandName,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1
        )
    }
}
val Typography.labelNarrow: androidx.compose.ui.text.TextStyle
    @Composable get() = labelSmall.copy(fontSize = 10.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
