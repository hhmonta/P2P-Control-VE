package com.p2pcontrol.ve.ui.transaccion

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.p2pcontrol.ve.data.local.entity.*
import com.p2pcontrol.ve.data.model.*
import com.p2pcontrol.ve.data.repository.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDateTime

data class TransaccionUiState(
    val tipo: TipoTransaccion = TipoTransaccion.COMPRA,
    val plataformas: List<PlataformaEntity> = emptyList(),
    val bancos: List<BancoEntity> = emptyList(),
    val plataformaSeleccionada: PlataformaEntity? = null,
    val bancoSeleccionado: BancoEntity? = null,
    val fiatMoneda: Moneda = Moneda.VES,
    val montoFiat: String = "",
    val cantidadUsdt: String = "",
    val tasa: String = "",
    val comisionUsdt: String = "0",
    val notas: String = "",
    val inventarioDisponible: List<InventarioUsdtEntity> = emptyList(),
    val costoPromedioPonderado: BigDecimal = BigDecimal.ZERO,
    val gananciaPerdidaCalculada: BigDecimal = BigDecimal.ZERO,
    val isSaving: Boolean = false,
    val saved: Boolean = false,
    val error: String? = null
)

class TransaccionViewModel(
    private val transaccionRepository: TransaccionRepository,
    private val bancoRepository: BancoRepository,
    private val plataformaRepository: PlataformaRepository,
    private val movimientoBancarioRepository: MovimientoBancarioRepository,
    private val inventarioUsdtRepository: InventarioUsdtRepository,
    private val configuracionRepository: ConfiguracionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TransaccionUiState())
    val uiState: StateFlow<TransaccionUiState> = _uiState.asStateFlow()

    init {
        loadDatos()
    }

    private fun loadDatos() {
        viewModelScope.launch {
            combine(
                plataformaRepository.getAllPlataformasActivas(),
                bancoRepository.getAllBancosActivos(),
                inventarioUsdtRepository.getInventarioDisponible()
            ) { plataformas, bancos, inventario ->
                _uiState.update { state ->
                    // Only set defaults if nothing is selected yet
                    val platSel = state.plataformaSeleccionada
                        ?: plataformas.firstOrNull()
                        ?: if (plataformas.any { it.id == state.plataformaSeleccionada?.id }) state.plataformaSeleccionada else plataformas.firstOrNull()
                    val bancoSel = state.bancoSeleccionado
                        ?: bancos.firstOrNull()
                        ?: if (bancos.any { it.id == state.bancoSeleccionado?.id }) state.bancoSeleccionado else bancos.firstOrNull()

                    state.copy(
                        plataformas = plataformas,
                        bancos = bancos,
                        inventarioDisponible = inventario,
                        plataformaSeleccionada = if (state.plataformaSeleccionada == null) plataformas.firstOrNull() else state.plataformaSeleccionada,
                        bancoSeleccionado = if (state.bancoSeleccionado == null) bancos.firstOrNull() else state.bancoSeleccionado
                    )
                }
                calcularCostoPromedio(inventario)
            }.collect()
        }
    }

    fun setTipo(tipo: TipoTransaccion) {
        _uiState.update { it.copy(tipo = tipo, gananciaPerdidaCalculada = BigDecimal.ZERO) }
        calcularGananciaPerdida()
    }

    fun setPlataforma(plataforma: PlataformaEntity) {
        _uiState.update { it.copy(plataformaSeleccionada = plataforma) }
    }

    fun setBanco(banco: BancoEntity) {
        _uiState.update { it.copy(bancoSeleccionado = banco, fiatMoneda = banco.moneda) }
    }

    fun setMontoFiat(monto: String) {
        _uiState.update { it.copy(montoFiat = monto) }
        calcularTasa()
        calcularGananciaPerdida()
    }

    fun setCantidadUsdt(cantidad: String) {
        _uiState.update { it.copy(cantidadUsdt = cantidad) }
        calcularTasa()
        calcularGananciaPerdida()
    }

    fun setComision(comision: String) {
        _uiState.update { it.copy(comisionUsdt = comision) }
        calcularGananciaPerdida()
    }

    fun setNotas(notas: String) {
        _uiState.update { it.copy(notas = notas) }
    }

    private fun calcularTasa() {
        val monto = _uiState.value.montoFiat.toBigDecimalOrNull() ?: return
        val cantidad = _uiState.value.cantidadUsdt.toBigDecimalOrNull() ?: return
        if (cantidad.compareTo(BigDecimal.ZERO) == 0) return
        val tasa = monto.divide(cantidad, 6, RoundingMode.HALF_UP)
        _uiState.update { it.copy(tasa = tasa.toPlainString()) }
    }

    private fun calcularCostoPromedio(inventario: List<InventarioUsdtEntity>) {
        if (inventario.isEmpty()) return
        var totalCantidad = BigDecimal.ZERO
        var totalCosto = BigDecimal.ZERO
        inventario.forEach { item ->
            totalCantidad = totalCantidad.add(item.cantidadDisponible)
            totalCosto = totalCosto.add(item.cantidadDisponible.multiply(item.costoUnitarioFiat))
        }
        if (totalCantidad.compareTo(BigDecimal.ZERO) == 0) return
        val promedio = totalCosto.divide(totalCantidad, 6, RoundingMode.HALF_UP)
        _uiState.update { it.copy(costoPromedioPonderado = promedio) }
    }

    private fun calcularGananciaPerdida() {
        val state = _uiState.value
        if (state.tipo != TipoTransaccion.VENTA) return

        val montoFiat = state.montoFiat.toBigDecimalOrNull() ?: return
        val cantidadUsdt = state.cantidadUsdt.toBigDecimalOrNull() ?: return
        if (cantidadUsdt.compareTo(BigDecimal.ZERO) == 0) return
        val comision = state.comisionUsdt.toBigDecimalOrNull() ?: BigDecimal.ZERO
        val tasa = montoFiat.divide(cantidadUsdt, 6, RoundingMode.HALF_UP)

        if (state.costoPromedioPonderado.compareTo(BigDecimal.ZERO) == 0) return

        val costoBaseUsdt = if (state.fiatMoneda == Moneda.VES) {
            val costoBaseVes = cantidadUsdt.multiply(state.costoPromedioPonderado)
            if (tasa.compareTo(BigDecimal.ZERO) == 0) return
            costoBaseVes.divide(tasa, 6, RoundingMode.HALF_UP)
        } else {
            cantidadUsdt.multiply(state.costoPromedioPonderado)
        }

        if (tasa.compareTo(BigDecimal.ZERO) == 0) return
        val ingresoUsdt = montoFiat.divide(tasa, 6, RoundingMode.HALF_UP)
        val ganancia = ingresoUsdt.subtract(costoBaseUsdt).subtract(comision)

        _uiState.update { it.copy(gananciaPerdidaCalculada = ganancia) }
    }

    fun guardarTransaccion() {
        viewModelScope.launch {
            val state = _uiState.value
            val banco = state.bancoSeleccionado ?: return@launch
            val plataforma = state.plataformaSeleccionada ?: return@launch
            val montoFiat = state.montoFiat.toBigDecimalOrNull() ?: return@launch
            val cantidadUsdt = state.cantidadUsdt.toBigDecimalOrNull() ?: return@launch

            // Bug 2 fix: Guard against zero division
            if (cantidadUsdt.compareTo(BigDecimal.ZERO) == 0) {
                _uiState.update { it.copy(error = "La cantidad de USDT debe ser mayor a 0") }
                return@launch
            }

            val comision = state.comisionUsdt.toBigDecimalOrNull() ?: BigDecimal.ZERO
            val tasa = montoFiat.divide(cantidadUsdt, 6, RoundingMode.HALF_UP)

            _uiState.update { it.copy(isSaving = true) }

            try {
                val transaccion = TransaccionEntity(
                    tipo = state.tipo,
                    plataformaId = plataforma.id,
                    bancoId = banco.id,
                    fiatMoneda = state.fiatMoneda,
                    montoFiat = montoFiat,
                    cantidadUsdt = cantidadUsdt,
                    tasa = tasa,
                    comisionUsdt = comision,
                    gananciaPerdidaUsdt = state.gananciaPerdidaCalculada,
                    fechaHora = LocalDateTime.now(),
                    notas = state.notas
                )

                val transaccionId = transaccionRepository.insert(transaccion)

                // Crear movimiento bancario
                val tipoMovimiento = when (state.tipo) {
                    TipoTransaccion.VENTA -> TipoMovimiento.INGRESO
                    TipoTransaccion.COMPRA -> TipoMovimiento.EGRESO
                }
                val movimiento = MovimientoBancarioEntity(
                    bancoId = banco.id,
                    transaccionId = transaccionId,
                    tipo = tipoMovimiento,
                    monto = montoFiat,
                    moneda = state.fiatMoneda,
                    descripcion = "${state.tipo.etiqueta} P2P - ${plataforma.nombre}"
                )
                movimientoBancarioRepository.insert(movimiento)

                // Actualizar inventario USDT
                when (state.tipo) {
                    TipoTransaccion.COMPRA -> {
                        val costoUnitario = montoFiat.divide(cantidadUsdt, 6, RoundingMode.HALF_UP)
                        inventarioUsdtRepository.insert(
                            InventarioUsdtEntity(
                                transaccionId = transaccionId,
                                cantidadDisponible = cantidadUsdt,
                                costoUnitarioFiat = costoUnitario,
                                fiatMoneda = state.fiatMoneda.name
                            )
                        )
                    }
                    TipoTransaccion.VENTA -> {
                        descontarInventario(cantidadUsdt)
                    }
                }

                _uiState.update { it.copy(isSaving = false, saved = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, error = e.message) }
            }
        }
    }

    private suspend fun descontarInventario(cantidadVendida: BigDecimal) {
        var restante = cantidadVendida
        val inventario = inventarioUsdtRepository.getInventarioDisponible().first()

        for (item in inventario) {
            if (restante.compareTo(BigDecimal.ZERO) <= 0) break

            if (item.cantidadDisponible.compareTo(restante) <= 0) {
                restante = restante.subtract(item.cantidadDisponible)
                inventarioUsdtRepository.update(item.copy(cantidadDisponible = BigDecimal.ZERO))
            } else {
                val nuevaCantidad = item.cantidadDisponible.subtract(restante)
                inventarioUsdtRepository.update(item.copy(cantidadDisponible = nuevaCantidad))
                restante = BigDecimal.ZERO
            }
        }
    }

    class Factory(
        private val transaccionRepository: TransaccionRepository,
        private val bancoRepository: BancoRepository,
        private val plataformaRepository: PlataformaRepository,
        private val movimientoBancarioRepository: MovimientoBancarioRepository,
        private val inventarioUsdtRepository: InventarioUsdtRepository,
        private val configuracionRepository: ConfiguracionRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return TransaccionViewModel(
                transaccionRepository,
                bancoRepository,
                plataformaRepository,
                movimientoBancarioRepository,
                inventarioUsdtRepository,
                configuracionRepository
            ) as T
        }
    }
}
