package network.beechat.kaonic.sampleapp.settings

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import network.beechat.kaonic.sampleapp.models.OFDMOptions
import network.beechat.kaonic.sampleapp.models.OFDMRate
import network.beechat.kaonic.sampleapp.services.KaonicService


const val defaultFrequency = "869535";
const val defaultChannelSpacing = "200";
const val defaultTxPower = "10";

data class SettingsState(
    val mcs: OFDMRate = OFDMRate.MCS_0,
    val optionNumber: OFDMOptions = OFDMOptions.OPTION1,
    val module: Int = 0,
    val frequency: String = defaultFrequency,
    val channel: Int = 1,
    val channelSpacing: String = defaultChannelSpacing,
    val txPower: String = defaultTxPower
)

class SettingsViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(SettingsState())
    val uiState: StateFlow<SettingsState> = _uiState

    fun updateMcs(value: OFDMRate) {
        _uiState.update { it.copy(mcs = value) }
    }

    fun updateOption(option: OFDMOptions) {
        _uiState.update { it.copy(optionNumber = option) }
    }

    fun updateFrequency(freq: String) {
        _uiState.update { it.copy(frequency = freq) }
    }

    fun updateChannel(channel: Int) {
        _uiState.update { it.copy(channel = channel) }
    }

    fun updateChannelSpacing(spacing: String) {
        _uiState.update { it.copy(channelSpacing = spacing) }
    }

    fun updateTxPower(power: String) {
        _uiState.update { it.copy(txPower = power) }
    }

    fun sendConfig() {
        val state = uiState.value
        KaonicService.sendConfig(
            OFDMRate.entries.indexOf(state.mcs),
            OFDMOptions.entries.indexOf(state.optionNumber),
            0,
            state.frequency.toIntOrNull() ?: defaultFrequency.toInt(),
            state.channel,
            state.channelSpacing.toIntOrNull() ?: defaultChannelSpacing.toInt(),
            state.txPower.toIntOrNull() ?: defaultTxPower.toInt()
        )
    }
}