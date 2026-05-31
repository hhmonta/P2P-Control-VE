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
import java.time.LocalDateTime

data class HistorialUiState(
    val transacciones: List<TransaccionEntity> = emptyList(),
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

    init {
        loadDatos()
    }

    private fun loadDatos() {
        viewModelScope.launch {
            bancoRepository.getAllBancosActivos().collect { bancos ->
                _uiState.update { it.copy(bancos = bancos) }
            }
        }
        viewModelScope.launch {
            plataformaRepository.getAllPlataformasActivas().collect { plataformas ->
                _uiState.update { it.copy(plataformas = plataformas) }
            }
        }
        viewModelScope.launch {
            transaccionRepository.getAllTransacciones().collect { transacciones ->
                _uiState.update { it.copy(transacciones = transacciones, isLoading = false) }
            }
        }
    }

    fun setFiltroTipo(tipo: TipoTransaccion?) {
        filtroTipo.value = tipo
        _uiState.update { it.copy(filtroTipo = tipo) }
    }

    fun setFiltroPlataforma(id: Long?) {
        filtroPlataformaId.value = id
        _uiState.update { it.copy(filtroPlataformaId = id) }
    }

    fun setFiltroBanco(id: Long?) {
        filtroBancoId.value = id
        _uiState.update { it.copy(filtroBancoId = id) }
    }

    suspend fun getBancoNombre(bancoId: Long): String {
        return bancoRepository.getBancoById(bancoId)?.nombre ?: "Desconocido"
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
