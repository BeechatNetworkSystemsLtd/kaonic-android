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
import org.json.JSONObject


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
    val txPower: String = defaultTxPower,
    val bt: Int = 0,
    val midxs: Int = 0,
    val midxsBits: Int = 0,
    val mord: Int = 0,
    val srate: Int = 0,
    val pdtm: Int = 0,
    val rxo: Int = 0,
    val rxpto: Int = 0,
    val mse: Int = 0,
    val fecs: Int = 0,
    val fecie: Int = 0,
    val sfd32: Int = 0,
    val csfd1: Int = 0,
    val csfd0: Int = 0,
    val sfd: Int = 0,
    val dw: Int = 0,
    val fskpe0: Int = 0,
    val fskpe1: Int = 0,
    val fskpe2: Int = 0,
    val preambleLength: Int = 0,
    val freqInversion: Boolean = false,
    val preambleInversion: Boolean = false,
    val rawbit: Boolean = false,
    val pe: Boolean = false,
    val en: Boolean = false,
    val sftq: Boolean = false
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
                    bt = values["bt"]!!.toString().toInt(),
                    midxs = values["midxs"]!!.toString().toInt(),
                    midxsBits = values["midx"]!!.toString().toInt(),
                    mord = values["mord"]!!.toString().toInt(),
                    srate = values["srate"]!!.toString().toInt(),
                    pdtm = values["pdtm"]!!.toString().toInt(),
                    rxo = values["rxo"]!!.toString().toInt(),
                    rxpto = values["rxpto"]!!.toString().toInt(),
                    mse = values["mse"]!!.toString().toInt(),
                    fecs = values["fecs"]!!.toString().toInt(),
                    fecie = values["fecie"]!!.toString().toInt(),
                    sfd32 = values["sfd32"]!!.toString().toInt(),
                    csfd1 = values["csfd1"]!!.toString().toInt(),
                    csfd0 = values["csfd0"]!!.toString().toInt(),
                    sfd = values["sfd"]!!.toString().toInt(),
                    dw = values["dw"]!!.toString().toInt(),
                    fskpe0 = values["fskpe0"]?.toString()?.toInt() ?: 0,
                    fskpe1 = values["fskpe1"]?.toString()?.toInt() ?: 0,
                    fskpe2 = values["fskpe2"]?.toString()?.toInt() ?: 0,
                    preambleLength = values["preambleLength"]?.toString()?.toInt() ?: 0,
                    freqInversion = values["freqInversion"]?.toString()?.toBoolean() ?: false,
                    preambleInversion = values["preambleInversion"]?.toString()?.toBoolean() ?: false,
                    rawbit = values["rawbit"]?.toString()?.toBoolean() ?: false,
                    pe = values["pe"]?.toString()?.toBoolean() ?: false,
                    en = values["en"]?.toString()?.toBoolean() ?: false,
                    sftq = values["sftq"]?.toString()?.toBoolean() ?: false,
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
            Pair("opt", OFDMOptions.entries.indexOf(state.optionNumber)),
            Pair("module", Module.entries.indexOf(state.module)),
            Pair("freq", state.frequency.toIntOrNull() ?: defaultFrequency.toInt()),
            Pair("channel", state.channel),
            Pair(
                "channel_spacing",
                state.channelSpacing.toIntOrNull() ?: defaultChannelSpacing.toInt()
            ),
            Pair("tx_power", state.txPower.toIntOrNull() ?: defaultTxPower.toInt()),
            Pair("bt", state.bt),
            Pair("midxs", state.midxs),
            Pair("midx", state.midxsBits),
            Pair("mord", state.mord),
            Pair("srate", state.srate),
            Pair("pdtm", state.pdtm),
            Pair("rxo", state.rxo),
            Pair("rxpto", state.rxpto),
            Pair("mse", state.mse),
            Pair("fecs", state.fecs),
            Pair("fecie", state.fecie),
            Pair("sfd32", state.sfd32),
            Pair("csfd1", state.csfd1),
            Pair("csfd0", state.csfd0),
            Pair("sfd", state.sfd),
            Pair("dw", state.dw),
            Pair("fskpe0", state.fskpe0),
            Pair("fskpe1", state.fskpe1),
            Pair("fskpe2", state.fskpe2),
            Pair("preamble_length", state.preambleLength),
            Pair("freq_inversion", state.freqInversion),
            Pair("preamble_inversion", state.preambleInversion),
            Pair("rawbit", state.rawbit),
            Pair("pe", state.pe),
            Pair("en", state.en),
            Pair("sftq", state.sftq),
        )


        val jsonString = JSONObject(values as Map<*, *>).toString()

        KaonicService.sendConfig(jsonString)
        secureStorageHelper.put(SETTINGS_TAG, ObjectMapper().writeValueAsString(values))

    }
}