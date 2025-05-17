@file:OptIn(ExperimentalMaterial3Api::class)

package network.beechat.kaonic.sampleapp.settings

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import network.beechat.kaonic.sampleapp.models.OFDMOptions
import network.beechat.kaonic.sampleapp.models.OFDMRate
import network.beechat.kaonic.sampleapp.view.MainButton
import network.beechat.kaonic.sampleapp.view.MainTextField
import network.beechat.kaonic.sampleapp.view.MainTopBar

@Composable
fun SettingsScreen(viewModel: SettingsViewModel, onBack: () -> Unit) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            MainTopBar("Settings", onBack = onBack)
        },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp)
                    .padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding())
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                CenteredLabel("Radio")
                Spacer(modifier = Modifier.height(16.dp))

                LabeledRow("Frequency") {
                    MainTextField(
                        value = state.frequency,
                        onValueChange = {
                            viewModel.updateFrequency(it)
                        },
                        keyboardType = KeyboardType.Number,
                        suffix = { Text("kHz", fontSize = 13.sp, color = Color.Black) }
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                LabeledRow("Tx Power") {
                    MainTextField(
                        value = state.txPower,
                        onValueChange = {
                            viewModel.updateTxPower(state.txPower)
                        },
                        keyboardType = KeyboardType.Number,
                        suffix = { Text("dB", fontSize = 13.sp, color = Color.Black) }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                LabeledRow("Channel") {
                    DropdownMenuBox(
                        value = state.channel,
                        onValueChange = { viewModel.updateChannel(it) },
                        options = (1..11).toList()
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                LabeledRow("Channel Spacing") {
                    MainTextField(
                        value = state.channelSpacing,
                        onValueChange = {
                            viewModel.updateChannelSpacing(it)
                        },
                        keyboardType = KeyboardType.Number,
                        suffix = { Text("kHz", fontSize = 13.sp, color = Color.Black) }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                RadioGroupSection(
                    label = "OFDM Option",
                    options = OFDMOptions.entries,
                    selected = state.optionNumber,
                    onSelected = { viewModel.updateOption(it as OFDMOptions) }
                )

                Spacer(modifier = Modifier.height(24.dp))

                RadioGroupSection(
                    label = "OFDM Rate",
                    options = OFDMRate.entries,
                    selected = state.mcs,
                    onSelected = {
                        viewModel.updateMcs(it as OFDMRate)
                    }
                )

                Spacer(modifier = Modifier.height(32.dp))

                MainButton(
                    label = "Save",
                    onPressed = {
                        viewModel.sendConfig()
                        Toast.makeText(context, "Configs Sent", Toast.LENGTH_SHORT).show()
                    }
                )
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    )

}