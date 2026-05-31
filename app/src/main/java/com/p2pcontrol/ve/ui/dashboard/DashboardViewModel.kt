package com.p2pcontrol.ve.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.p2pcontrol.ve.data.local.entity.BancoEntity
import com.p2pcontrol.ve.data.local.entity.TransaccionEntity
import com.p2pcontrol.ve.data.local.entity.MovimientoBancarioEntity
import com.p2pcontrol.ve.data.repository.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDateTime
import java.time.LocalDate

data class BancoSaldoUi(
    val banco: BancoEntity,
    val saldoActual: BigDecimal,
    val saldoEnUsd: BigDecimal
)

data class DashboardUiState(
    val balanceTotalUsd: BigDecimal = BigDecimal.ZERO,
    val usdtDisponibles: BigDecimal = BigDecimal.ZERO,
    val gananciaPerdidaMes: BigDecimal = BigDecimal.ZERO,
    val bancosConSaldo: List<BancoSaldoUi> = emptyList(),
    val ultimasTransacciones: List<TransaccionEntity> = emptyList(),
    val tasaUsdVes: BigDecimal = BigDecimal.ONE,
    val isLoading: Boolean = true
)

class DashboardViewModel(
    private val bancoRepository: BancoRepository,
    private val transaccionRepository: TransaccionRepository,
    private val movimientoBancarioRepository: MovimientoBancarioRepository,
    private val configuracionRepository: ConfiguracionRepository,
    private val inventarioUsdtRepository: InventarioUsdtRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadDashboard()
    }

    private fun loadDashboard() {
        viewModelScope.launch {
            combine(
                bancoRepository.getAllBancosActivos(),
                transaccionRepository.getUltimasTransacciones(5),
                inventarioUsdtRepository.getTotalUsdtDisponible(),
                configuracionRepository.getTasaUsdVesFlow()
            ) { bancos, ultimas, usdtTotal, tasa ->
                val inicioMes = LocalDate.now().withDayOfMonth(1).atStartOfDay()
                val finMes = LocalDateTime.now()

                val bancosConSaldo = bancos.map { banco ->
                    val saldo = calcularSaldoBanco(banco.id, banco.saldoInicial)
                    val saldoUsd = if (banco.moneda.name == "VES") {
                        saldo.divide(tasa, 2, RoundingMode.HALF_UP)
                    } else {
                        saldo
                    }
                    BancoSaldoUi(banco, saldo, saldoUsd)
                }

                val balanceTotal = bancosConSaldo.fold(BigDecimal.ZERO) { acc, b ->
                    acc.add(b.saldoEnUsd)
                }

                DashboardUiState(
                    balanceTotalUsd = balanceTotal,
                    usdtDisponibles = usdtTotal,
                    gananciaPerdidaMes = BigDecimal.ZERO, // Se actualiza abajo
                    bancosConSaldo = bancosConSaldo,
                    ultimasTransacciones = ultimas,
                    tasaUsdVes = tasa,
                    isLoading = false
                )
            }.collect { state ->
                _uiState.value = state
                // Cargar ganancia/pérdida del mes
                loadGananciaPerdida()
            }
        }
    }

    private suspend fun calcularSaldoBanco(bancoId: Long, saldoInicial: BigDecimal): BigDecimal {
        var total = saldoInicial
        movimientoBancarioRepository.getMovimientosByBanco(bancoId).first().forEach { mov ->
            when (mov.tipo.name) {
                "INGRESO", "TRANSFERENCIA_ENTRADA" -> total = total.add(mov.monto)
                "EGRESO", "TRANSFERENCIA_SALIDA" -> total = total.subtract(mov.monto)
            }
        }
        return total
    }

    private fun loadGananciaPerdida() {
        viewModelScope.launch {
            val inicioMes = LocalDate.now().withDayOfMonth(1).atStartOfDay()
            val finMes = LocalDateTime.now()
            transaccionRepository.getGananciaPerdidaPeriodo(inicioMes, finMes).collect { gp ->
                _uiState.update { it.copy(gananciaPerdidaMes = gp) }
            }
        }
    }

    class Factory(
        private val bancoRepository: BancoRepository,
        private val transaccionRepository: TransaccionRepository,
        private val movimientoBancarioRepository: MovimientoBancarioRepository,
        private val configuracionRepository: ConfiguracionRepository,
        private val inventarioUsdtRepository: InventarioUsdtRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return DashboardViewModel(
                bancoRepository,
                transaccionRepository,
                movimientoBancarioRepository,
                configuracionRepository,
                inventarioUsdtRepository
            ) as T
        }
    }
}
