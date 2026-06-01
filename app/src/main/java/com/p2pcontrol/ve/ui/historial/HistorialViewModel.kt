package com.p2pcontrol.ve.ui.historial

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.p2pcontrol.ve.data.local.entity.BancoEntity
import com.p2pcontrol.ve.data.local.entity.PlataformaEntity
import com.p2pcontrol.ve.data.local.entity.TransaccionEntity
import com.p2pcontrol.ve.data.model.TipoTransaccion
import com.p2pcontrol.ve.data.repository.BancoRepository
import com.p2pcontrol.ve.data.repository.PlataformaRepository
import com.p2pcontrol.ve.data.repository.TransaccionRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class TransaccionDisplayItem(
    val transaccion: TransaccionEntity,
    val plataformaNombre: String,
    val bancoNombre: String
)

data class HistorialUiState(
    val transaccionesFiltradas: List<TransaccionDisplayItem> = emptyList(),
    val bancos: List<BancoEntity> = emptyList(),
    val plataformas: List<PlataformaEntity> = emptyList(),
    val filtroTipo: TipoTransaccion? = null,
    val filtroPlataformaId: Long? = null,
    val filtroBancoId: Long? = null,
    val isLoading: Boolean = true
)

class HistorialViewModel(
    private val transaccionRepository: TransaccionRepository,
    private val bancoRepository: BancoRepository,
    private val plataformaRepository: PlataformaRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistorialUiState())
    val uiState: StateFlow<HistorialUiState> = _uiState.asStateFlow()

    private val filtroTipo = MutableStateFlow<TipoTransaccion?>(null)
    private val filtroPlataformaId = MutableStateFlow<Long?>(null)
    private val filtroBancoId = MutableStateFlow<Long?>(null)

    // Cache para resolver IDs a nombres
    private val plataformasCache = mutableMapOf<Long, String>()
    private val bancosCache = mutableMapOf<Long, BancoEntity>()

    init {
        loadDatos()
    }

    private fun loadDatos() {
        viewModelScope.launch {
            bancoRepository.getAllBancosActivos().collect { bancos ->
                bancosCache.clear()
                bancos.forEach { bancosCache[it.id] = it }
                _uiState.update { it.copy(bancos = bancos) }
            }
        }
        viewModelScope.launch {
            plataformaRepository.getAllPlataformasActivas().collect { plataformas ->
                plataformasCache.clear()
                plataformas.forEach { plataformasCache[it.id] = it.nombre }
                _uiState.update { it.copy(plataformas = plataformas) }
            }
        }
        // Combine all transactions with filters
        viewModelScope.launch {
            combine(
                transaccionRepository.getAllTransacciones(),
                filtroTipo,
                filtroPlataformaId,
                filtroBancoId
            ) { transacciones, fTipo, fPlatId, fBancoId ->
                val filtered = transacciones.filter { tx ->
                    (fTipo == null || tx.tipo == fTipo) &&
                    (fPlatId == null || tx.plataformaId == fPlatId) &&
                    (fBancoId == null || tx.bancoId == fBancoId)
                }
                val displayItems = filtered.map { tx ->
                    TransaccionDisplayItem(
                        transaccion = tx,
                        plataformaNombre = plataformasCache[tx.plataformaId] ?: "ID:${tx.plataformaId}",
                        bancoNombre = bancosCache[tx.bancoId]?.nombre ?: "ID:${tx.bancoId}"
                    )
                }
                _uiState.update {
                    it.copy(
                        transaccionesFiltradas = displayItems,
                        filtroTipo = fTipo,
                        filtroPlataformaId = fPlatId,
                        filtroBancoId = fBancoId,
                        isLoading = false
                    )
                }
            }.collect()
        }
    }

    fun setFiltroTipo(tipo: TipoTransaccion?) {
        filtroTipo.value = tipo
    }

    fun setFiltroPlataforma(id: Long?) {
        filtroPlataformaId.value = id
    }

    fun setFiltroBanco(id: Long?) {
        filtroBancoId.value = id
    }

    class Factory(
        private val transaccionRepository: TransaccionRepository,
        private val bancoRepository: BancoRepository,
        private val plataformaRepository: PlataformaRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return HistorialViewModel(transaccionRepository, bancoRepository, plataformaRepository) as T
        }
    }
}
