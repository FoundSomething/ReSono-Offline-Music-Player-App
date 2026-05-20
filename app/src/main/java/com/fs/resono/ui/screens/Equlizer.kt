package com.fs.resono.ui.screens

import android.app.Application
import androidx.annotation.OptIn
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.util.UnstableApi
import com.fs.resono.ui.components.EqSlider
import com.fs.resono.ui.components.GainSlider
import com.fs.resono.viewmodel.DspViewModel

@OptIn(UnstableApi::class)
@Composable
fun Equlizer(
    vm: DspViewModel = viewModel(
        factory = ViewModelProvider.AndroidViewModelFactory(
            LocalContext.current.applicationContext as Application
        )
    )
) {
    val reverbEnabled = vm.reverbEnabled
    val reverbLevel = vm.reverbLevel

    val eqContentAlpha = if (vm.eqEnabled) 1f else 0.4f
    val reverbContentAlpha = if (reverbEnabled) 1f else 0.4f

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(24.dp)
            .verticalScroll(scrollState)
    ) {

        /* ---------- Title ---------- */
        Text(
            text = "Equalizer",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(24.dp)) // Increased space

        /* ---------- EQ Enable Switch (Encased in Card) ---------- */
        Card(
            modifier = Modifier
                .fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            ),
            shape = MaterialTheme.shapes.medium // Slightly smaller shape
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Enable Equalizer",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Switch(
                    checked = vm.eqEnabled,
                    onCheckedChange = { enabled ->
                        vm.updateEqEnabled(enabled)
                    }
                )
            }
        }
        Spacer(modifier = Modifier.height(32.dp))

        /* ---------- EQ Sliders & Master Gain (Grouped in one Card) ---------- */
        // Applies alpha and animation to the entire section based on EQ enable state
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .alpha(eqContentAlpha)
                .animateContentSize(animationSpec = spring(stiffness = Spring.StiffnessMedium)),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            ),
            shape = MaterialTheme.shapes.extraLarge
        ) {
            Column(modifier = Modifier.padding(20.dp)) { // Inner padding for content
                Text(
                    text = "Frequency Bands",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp),
                    // Removed horizontalArrangement as weight will now manage spacing
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    EqSlider("31", vm.band31, modifier = Modifier.weight(1f)) { vm.updateBand(0, it) }
                    EqSlider("62", vm.band62, modifier = Modifier.weight(1f)) { vm.updateBand(1, it) }
                    EqSlider("125", vm.band125, modifier = Modifier.weight(1f)) { vm.updateBand(2, it) }
                    EqSlider("250", vm.band250, modifier = Modifier.weight(1f)) { vm.updateBand(3, it) }
                    EqSlider("500", vm.band500, modifier = Modifier.weight(1f)) { vm.updateBand(4, it) }
                    EqSlider("1k", vm.band1k, modifier = Modifier.weight(1f)) { vm.updateBand(5, it) }
                    EqSlider("2k", vm.band2k, modifier = Modifier.weight(1f)) { vm.updateBand(6, it) }
                    EqSlider("4k", vm.band4k, modifier = Modifier.weight(1f)) { vm.updateBand(7, it) }
                }

                Spacer(modifier = Modifier.height(24.dp))
                Divider(color = MaterialTheme.colorScheme.outlineVariant) // Material3 Divider

                Spacer(modifier = Modifier.height(24.dp))

                /* ---------- Master Gain Sub-Section ---------- */
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Master Gain",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    GainSlider(
                        gain = vm.masterGain,
                        onGainChange = { vm.updateMasterGain(it) }
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(32.dp))

        /* ---------- Reverb Controls (Encased in Card) ---------- */
        // Applies alpha and animation to the entire section based on Reverb enable state
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .alpha(reverbContentAlpha)
                .animateContentSize(animationSpec = spring(stiffness = Spring.StiffnessMedium)),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            ),
            shape = MaterialTheme.shapes.extraLarge
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp) // Inner padding
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Reverb Effect",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Switch(checked = reverbEnabled, onCheckedChange = vm::updateReverbEnabled)
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Reverb Level: ${"%.0f".format(reverbLevel * 5f)}", // Keep original scale logic
                    style = MaterialTheme.typography.labelMedium, // Adjusted style
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Slider(
                    value = reverbLevel,
                    onValueChange = { newValue ->
                        vm.updateReverbLevel(newValue)
                    },
                    valueRange = 0.0f..1.0f,
                    steps = 4,
                    enabled = reverbEnabled, // Enable/disable slider based on reverbEnabled
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}