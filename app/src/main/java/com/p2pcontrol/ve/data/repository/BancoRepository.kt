package com.p2pcontrol.ve.data.repository

import com.p2pcontrol.ve.data.local.dao.BancoDao
import com.p2pcontrol.ve.data.local.dao.MovimientoBancarioDao
import com.p2pcontrol.ve.data.local.entity.BancoEntity
import com.p2pcontrol.ve.data.model.Moneda
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.math.BigDecimal

data class BancoConSaldo(
    val banco: BancoEntity,
    val saldoActual: BigDecimal
)

class BancoRepository(
    private val bancoDao: BancoDao,
    private val movimientoDao: MovimientoBancarioDao
) {

    fun getAllBancosActivos(): Flow<List<BancoEntity>> = bancoDao.getAllBancosActivos()

    fun getAllBancos(): Flow<List<BancoEntity>> = bancoDao.getAllBancos()

    fun getBancosConSaldo(): Flow<List<BancoConSaldo>> {
        return bancoDao.getAllBancosActivos().map { bancos ->
            bancos.map { banco ->
                BancoConSaldo(
                    banco = banco,
                    saldoActual = banco.saldoInicial // Se calculará dinámicamente en ViewModel
                )
            }
        }
    }

    suspend fun getBancoById(id: Long): BancoEntity? = bancoDao.getBancoById(id)

    suspend fun insert(banco: BancoEntity): Long = bancoDao.insert(banco)

    suspend fun update(banco: BancoEntity) = bancoDao.update(banco)

    suspend fun deactivate(id: Long) = bancoDao.deactivate(id)
}
