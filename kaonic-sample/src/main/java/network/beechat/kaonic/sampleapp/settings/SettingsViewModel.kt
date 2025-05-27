package network.beechat.kaonic.sampleapp.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import network.beechat.kaonic.sampleapp.models.Module
import network.beechat.kaonic.sampleapp.models.OFDMOptions
import network.beechat.kaonic.sampleapp.models.OFDMRate
import network.beechat.kaonic.sampleapp.nodedetails.NodeDetailsViewModel
import network.beechat.kaonic.sampleapp.services.ChatService
import network.beechat.kaonic.sampleapp.services.KaonicService
import network.beechat.kaonic.sampleapp.services.SecureStorageHelper


const val defaultFrequency = "869535"
const val defaultChannelSpacing = "200"
const val defaultTxPower = "10"

@Suppress("UNCHECKED_CAST")
class SettingsViewModelFactory(
    private val secureStorageHelper: SecureStorageHelper
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return SettingsViewModel(secureStorageHelper) as T
    }
}

data class SettingsState(
    val mcs: OFDMRate = OFDMRate.MCS_0,
    val optionNumber: OFDMOptions = OFDMOptions.OPTION1,
    val module: Module = Module.rfA,
    val frequency: String = defaultFrequency,
    val channel: Int = 1,
    val channelSpacing: String = defaultChannelSpacing,
    val txPower: String = defaultTxPower
)

class SettingsViewModel(private val secureStorageHelper: SecureStorageHelper) : ViewModel() {
    private val SETTINGS_TAG = "SETTINGS_TAG"
    private val _uiState = MutableStateFlow(SettingsState())
    val uiState: StateFlow<SettingsState> = _uiState

    init {
        secureStorageHelper.get(SETTINGS_TAG)?.let { valuesStr ->
            val values = ObjectMapper().readValue(valuesStr, HashMap<String, Any>().javaClass)
            /**
             * val values = hashMapOf(
             *             Pair("mcs", OFDMRate.entries.indexOf(state.mcs)),
             *             Pair("optionNumber", OFDMRate.entries.indexOf(state.mcs)),
             *             Pair("module", Module.entries.indexOf(state.module)),
             *             Pair("frequency", state.frequency.toIntOrNull() ?: defaultFrequency.toInt()),
             *             Pair("channel", state.channel),
             *             Pair(
             *                 "channelSpacing",
             *                 state.channelSpacing.toIntOrNull() ?: defaultChannelSpacing.toInt()
             *             ),
             *             Pair("txPower", state.txPower.toIntOrNull() ?: defaultTxPower.toInt()),
             *         )
             */
            _uiState.update {
                it.copy(
                    mcs = OFDMRate.entries[values["mcs"]!!.toString().toInt()],
                    optionNumber = OFDMOptions.entries[values["optionNumber"]!!.toString().toInt()],
                    module = Module.entries[values["module"]!!.toString().toInt()],
                    frequency = values["frequency"]!!.toString(),
                    channel = values["channel"]!!.toString().toInt(),
                    channelSpacing = values["channelSpacing"]!!.toString(),
                    txPower = values["txPower"]!!.toString(),
                )
            }
        }
    }

    fun updateMcs(value: OFDMRate) {
        _uiState.update { it.copy(mcs = value) }
    }

    fun updateModule(value: Module) {
        _uiState.update { it.copy(module = value) }
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
        val values = hashMapOf(
            Pair("mcs", OFDMRate.entries.indexOf(state.mcs)),
            Pair("optionNumber", OFDMOptions.entries.indexOf(state.optionNumber)),
            Pair("module", Module.entries.indexOf(state.module)),
            Pair("frequency", state.frequency.toIntOrNull() ?: defaultFrequency.toInt()),
            Pair("channel", state.channel),
            Pair(
                "channelSpacing",
                state.channelSpacing.toIntOrNull() ?: defaultChannelSpacing.toInt()
            ),
            Pair("txPower", state.txPower.toIntOrNull() ?: defaultTxPower.toInt()),
        )
        KaonicService.sendConfig(
            values["mcs"]!!.toInt(),
            values["optionNumber"]!!.toInt(),
            values["module"]!!.toInt(),
            values["frequency"]!!.toInt(),
            values["channel"]!!.toInt(),
            values["channelSpacing"]!!.toInt(),
            values["txPower"]!!.toInt()
        )
        secureStorageHelper.put(SETTINGS_TAG, ObjectMapper().writeValueAsString(values))

    }
}