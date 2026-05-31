package com.p2pcontrol.ve.data.repository

import com.p2pcontrol.ve.data.local.dao.MovimientoBancarioDao
import com.p2pcontrol.ve.data.local.entity.MovimientoBancarioEntity
import kotlinx.coroutines.flow.Flow
import java.math.BigDecimal

class MovimientoBancarioRepository(private val movimientoDao: MovimientoBancarioDao) {

    fun getMovimientosByBanco(bancoId: Long): Flow<List<MovimientoBancarioEntity>> =
        movimientoDao.getMovimientosByBanco(bancoId)

    fun getAllMovimientos(): Flow<List<MovimientoBancarioEntity>> = movimientoDao.getAllMovimientos()

    fun getTotalIngresosByBanco(bancoId: Long): Flow<BigDecimal> =
        movimientoDao.getTotalIngresosByBanco(bancoId)

    fun getTotalEgresosByBanco(bancoId: Long): Flow<BigDecimal> =
        movimientoDao.getTotalEgresosByBanco(bancoId)

    suspend fun getMovimientoByTransaccion(transaccionId: Long): MovimientoBancarioEntity? =
        movimientoDao.getMovimientoByTransaccion(transaccionId)

    suspend fun insert(movimiento: MovimientoBancarioEntity): Long = movimientoDao.insert(movimiento)

    suspend fun insertAll(movimientos: List<MovimientoBancarioEntity>) = movimientoDao.insertAll(movimientos)

    suspend fun delete(movimiento: MovimientoBancarioEntity) = movimientoDao.delete(movimiento)

    suspend fun deleteByTransaccion(transaccionId: Long) = movimientoDao.deleteByTransaccion(transaccionId)
}
