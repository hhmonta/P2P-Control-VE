package com.p2pcontrol.ve.ui.movimientos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.p2pcontrol.ve.data.local.entity.BancoEntity
import com.p2pcontrol.ve.data.local.entity.MovimientoBancarioEntity
import com.p2pcontrol.ve.data.model.Moneda
import com.p2pcontrol.ve.data.model.TipoMovimiento
import com.p2pcontrol.ve.data.repository.BancoRepository
import com.p2pcontrol.ve.data.repository.ConfiguracionRepository
import com.p2pcontrol.ve.data.repository.MovimientoBancarioRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.time.LocalDate

data class MovimientosUiState(
    val movimientos: List<MovimientoBancarioEntity> = emptyList(),
    val bancos: List<BancoEntity> = emptyList(),
    val bancoSeleccionado: BancoEntity? = null,
    val bancoOrigen: BancoEntity? = null,
    val bancoDestino: BancoEntity? = null,
    val monto: String = "",
    val descripcion: String = "",
    val isLoading: Boolean = true,
    val saved: Boolean = false,
    val error: String? = null
)

class MovimientosViewModel(
    private val movimientoBancarioRepository: MovimientoBancarioRepository,
    private val bancoRepository: BancoRepository,
    private val configuracionRepository: ConfiguracionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MovimientosUiState())
    val uiState: StateFlow<MovimientosUiState> = _uiState.asStateFlow()

    init {
        loadDatos()
    }

    private fun loadDatos() {
        viewModelScope.launch {
            bancoRepository.getAllBancosActivos().collect { bancos ->
                _uiState.update {
                    it.copy(
                        bancos = bancos,
                        bancoSeleccionado = bancos.firstOrNull(),
                        bancoOrigen = bancos.firstOrNull(),
                        bancoDestino = bancos.getOrNull(1) ?: bancos.firstOrNull()
                    )
                }
            }
        }
        viewModelScope.launch {
            movimientoBancarioRepository.getAllMovimientos().collect { movs ->
                _uiState.update { it.copy(movimientos = movs, isLoading = false) }
            }
        }
    }

    fun setBancoSeleccionado(banco: BancoEntity) {
        _uiState.update { it.copy(bancoSeleccionado = banco) }
        loadMovimientosByBanco(banco.id)
    }

    private fun loadMovimientosByBanco(bancoId: Long) {
        viewModelScope.launch {
            movimientoBancarioRepository.getMovimientosByBanco(bancoId).collect { movs ->
                _uiState.update { it.copy(movimientos = movs) }
            }
        }
    }

    fun setBancoOrigen(banco: BancoEntity) {
        _uiState.update { it.copy(bancoOrigen = banco) }
    }

    fun setBancoDestino(banco: BancoEntity) {
        _uiState.update { it.copy(bancoDestino = banco) }
    }

    fun setMonto(monto: String) {
        _uiState.update { it.copy(monto = monto) }
    }

    fun setDescripcion(desc: String) {
        _uiState.update { it.copy(descripcion = desc) }
    }

    fun guardarIngreso() {
        viewModelScope.launch {
            val state = _uiState.value
            val banco = state.bancoSeleccionado ?: return@launch
            val monto = state.monto.toBigDecimalOrNull() ?: return@launch

            try {
                movimientoBancarioRepository.insert(
                    MovimientoBancarioEntity(
                        bancoId = banco.id,
                        tipo = TipoMovimiento.INGRESO,
                        monto = monto,
                        moneda = banco.moneda,
                        descripcion = state.descripcion.ifBlank { "Ingreso manual" }
                    )
                )
                _uiState.update { it.copy(saved = true, monto = "", descripcion = "") }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun guardarEgreso() {
        viewModelScope.launch {
            val state = _uiState.value
            val banco = state.bancoSeleccionado ?: return@launch
            val monto = state.monto.toBigDecimalOrNull() ?: return@launch

            try {
                movimientoBancarioRepository.insert(
                    MovimientoBancarioEntity(
                        bancoId = banco.id,
                        tipo = TipoMovimiento.EGRESO,
                        monto = monto,
                        moneda = banco.moneda,
                        descripcion = state.descripcion.ifBlank { "Egreso manual" }
                    )
                )
                _uiState.update { it.copy(saved = true, monto = "", descripcion = "") }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun guardarTransferencia() {
        viewModelScope.launch {
            val state = _uiState.value
            val origen = state.bancoOrigen ?: return@launch
            val destino = state.bancoDestino ?: return@launch
            val monto = state.monto.toBigDecimalOrNull() ?: return@launch

            if (origen.id == destino.id) {
                _uiState.update { it.copy(error = "Banco origen y destino no pueden ser iguales") }
                return@launch
            }

            try {
                val tasa = configuracionRepository.getTasaUsdVes()
                val movimientos = mutableListOf<MovimientoBancarioEntity>()

                // Egreso del banco origen
                movimientos.add(
                    MovimientoBancarioEntity(
                        bancoId = origen.id,
                        tipo = TipoMovimiento.TRANSFERENCIA_SALIDA,
                        monto = monto,
                        moneda = origen.moneda,
                        descripcion = "Transferencia a ${destino.nombre}"
                    )
                )

                // Ingreso al banco destino (convertir si monedas difieren)
                val montoDestino = if (origen.moneda != destino.moneda) {
                    if (origen.moneda == Moneda.USD && destino.moneda == Moneda.VES) {
                        monto.multiply(tasa)
                    } else {
                        monto.divide(tasa, 2, java.math.RoundingMode.HALF_UP)
                    }
                } else {
                    monto
                }

                movimientos.add(
                    MovimientoBancarioEntity(
                        bancoId = destino.id,
                        tipo = TipoMovimiento.TRANSFERENCIA_ENTRADA,
                        monto = montoDestino,
                        moneda = destino.moneda,
                        descripcion = "Transferencia desde ${origen.nombre}"
                    )
                )

                movimientoBancarioRepository.insertAll(movimientos)
                _uiState.update { it.copy(saved = true, monto = "", descripcion = "") }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun clearSaved() {
        _uiState.update { it.copy(saved = false, error = null) }
    }

    class Factory(
        private val movimientoBancarioRepository: MovimientoBancarioRepository,
        private val bancoRepository: BancoRepository,
        private val configuracionRepository: ConfiguracionRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return MovimientosViewModel(movimientoBancarioRepository, bancoRepository, configuracionRepository) as T
        }
    }
}
