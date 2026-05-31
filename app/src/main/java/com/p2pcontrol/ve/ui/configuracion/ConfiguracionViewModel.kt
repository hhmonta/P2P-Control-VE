package com.p2pcontrol.ve.ui.configuracion

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.p2pcontrol.ve.data.local.entity.BancoEntity
import com.p2pcontrol.ve.data.local.entity.PlataformaEntity
import com.p2pcontrol.ve.data.local.entity.TransaccionEntity
import com.p2pcontrol.ve.data.model.Moneda
import com.p2pcontrol.ve.data.repository.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileWriter
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.*

data class ConfiguracionUiState(
    val tasaUsdVes: String = "",
    val bancos: List<BancoEntity> = emptyList(),
    val plataformas: List<PlataformaEntity> = emptyList(),
    val nuevoBancoNombre: String = "",
    val nuevoBancoMoneda: Moneda = Moneda.VES,
    val nuevoBancoSaldoInicial: String = "0",
    val nuevaPlataformaNombre: String = "",
    val isSaving: Boolean = false,
    val message: String? = null,
    val error: String? = null
)

class ConfiguracionViewModel(
    private val configuracionRepository: ConfiguracionRepository,
    private val bancoRepository: BancoRepository,
    private val plataformaRepository: PlataformaRepository,
    private val transaccionRepository: TransaccionRepository,
    private val movimientoBancarioRepository: MovimientoBancarioRepository,
    private val inventarioUsdtRepository: InventarioUsdtRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ConfiguracionUiState())
    val uiState: StateFlow<ConfiguracionUiState> = _uiState.asStateFlow()

    init {
        loadDatos()
    }

    private fun loadDatos() {
        viewModelScope.launch {
            configuracionRepository.getTasaUsdVesFlow().collect { tasa ->
                _uiState.update { it.copy(tasaUsdVes = tasa.toPlainString()) }
            }
        }
        viewModelScope.launch {
            bancoRepository.getAllBancos().collect { bancos ->
                _uiState.update { it.copy(bancos = bancos) }
            }
        }
        viewModelScope.launch {
            plataformaRepository.getAllPlataformas().collect { plataformas ->
                _uiState.update { it.copy(plataformas = plataformas) }
            }
        }
    }

    fun setTasaUsdVes(tasa: String) {
        _uiState.update { it.copy(tasaUsdVes = tasa) }
    }

    fun guardarTasa() {
        viewModelScope.launch {
            val tasa = _uiState.value.tasaUsdVes.toBigDecimalOrNull()
            if (tasa != null && tasa.compareTo(BigDecimal.ZERO) > 0) {
                configuracionRepository.setTasaUsdVes(tasa)
                _uiState.update { it.copy(message = "Tasa actualizada correctamente") }
            } else {
                _uiState.update { it.copy(error = "Tasa inválida") }
            }
        }
    }

    fun setNuevoBancoNombre(nombre: String) {
        _uiState.update { it.copy(nuevoBancoNombre = nombre) }
    }

    fun setNuevoBancoMoneda(moneda: Moneda) {
        _uiState.update { it.copy(nuevoBancoMoneda = moneda) }
    }

    fun setNuevoBancoSaldoInicial(saldo: String) {
        _uiState.update { it.copy(nuevoBancoSaldoInicial = saldo) }
    }

    fun guardarBanco() {
        viewModelScope.launch {
            val state = _uiState.value
            if (state.nuevoBancoNombre.isBlank()) {
                _uiState.update { it.copy(error = "Nombre del banco es requerido") }
                return@launch
            }
            try {
                bancoRepository.insert(
                    BancoEntity(
                        nombre = state.nuevoBancoNombre,
                        moneda = state.nuevoBancoMoneda,
                        saldoInicial = state.nuevoBancoSaldoInicial.toBigDecimalOrNull() ?: BigDecimal.ZERO
                    )
                )
                _uiState.update {
                    it.copy(
                        nuevoBancoNombre = "",
                        nuevoBancoSaldoInicial = "0",
                        message = "Banco agregado correctamente"
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun deactivateBanco(id: Long) {
        viewModelScope.launch {
            bancoRepository.deactivate(id)
            _uiState.update { it.copy(message = "Banco desactivado") }
        }
    }

    fun setNuevaPlataformaNombre(nombre: String) {
        _uiState.update { it.copy(nuevaPlataformaNombre = nombre) }
    }

    fun guardarPlataforma() {
        viewModelScope.launch {
            val state = _uiState.value
            if (state.nuevaPlataformaNombre.isBlank()) {
                _uiState.update { it.copy(error = "Nombre de plataforma requerido") }
                return@launch
            }
            try {
                plataformaRepository.insert(
                    PlataformaEntity(nombre = state.nuevaPlataformaNombre)
                )
                _uiState.update {
                    it.copy(nuevaPlataformaNombre = "", message = "Plataforma agregada correctamente")
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun deactivatePlataforma(id: Long) {
        viewModelScope.launch {
            plataformaRepository.deactivate(id)
            _uiState.update { it.copy(message = "Plataforma desactivada") }
        }
    }

    fun exportarCsv(context: Context) {
        viewModelScope.launch {
            try {
                val transacciones = transaccionRepository.getAllTransacciones().first()
                val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                val fileName = "P2PControl_transacciones_${dateFormat.format(Date())}.csv"
                val dir = File(context.getExternalFilesDir(null), "exports")
                dir.mkdirs()
                val file = File(dir, fileName)

                FileWriter(file).use { writer ->
                    writer.append("ID,Tipo,Plataforma ID,Banco ID,Moneda Fiat,Monto Fiat,Cantidad USDT,Tasa,Comisión USDT,G/P USDT,Fecha,Notas\n")
                    transacciones.forEach { t ->
                        writer.append("${t.id},${t.tipo},${t.plataformaId},${t.bancoId},${t.fiatMoneda},${t.montoFiat},${t.cantidadUsdt},${t.tasa},${t.comisionUsdt},${t.gananciaPerdidaUsdt},${t.fechaHora},${t.notas}\n")
                    }
                }

                _uiState.update { it.copy(message = "Exportado: ${file.absolutePath}") }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Error exportando: ${e.message}") }
            }
        }
    }

    fun clearMessage() {
        _uiState.update { it.copy(message = null, error = null) }
    }

    class Factory(
        private val configuracionRepository: ConfiguracionRepository,
        private val bancoRepository: BancoRepository,
        private val plataformaRepository: PlataformaRepository,
        private val transaccionRepository: TransaccionRepository,
        private val movimientoBancarioRepository: MovimientoBancarioRepository,
        private val inventarioUsdtRepository: InventarioUsdtRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ConfiguracionViewModel(
                configuracionRepository,
                bancoRepository,
                plataformaRepository,
                transaccionRepository,
                movimientoBancarioRepository,
                inventarioUsdtRepository
            ) as T
        }
    }
}
