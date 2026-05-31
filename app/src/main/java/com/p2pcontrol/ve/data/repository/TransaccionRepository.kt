package com.p2pcontrol.ve.data.repository

import com.p2pcontrol.ve.data.local.dao.TransaccionDao
import com.p2pcontrol.ve.data.local.entity.TransaccionEntity
import com.p2pcontrol.ve.data.model.TipoTransaccion
import kotlinx.coroutines.flow.Flow
import java.math.BigDecimal
import java.time.LocalDateTime

class TransaccionRepository(private val transaccionDao: TransaccionDao) {

    fun getAllTransacciones(): Flow<List<TransaccionEntity>> = transaccionDao.getAllTransacciones()

    fun getUltimasTransacciones(limit: Int = 5): Flow<List<TransaccionEntity>> =
        transaccionDao.getUltimasTransacciones(limit)

    fun getTransaccionesByTipo(tipo: TipoTransaccion): Flow<List<TransaccionEntity>> =
        transaccionDao.getTransaccionesByTipo(tipo)

    fun getTransaccionesByPlataforma(plataformaId: Long): Flow<List<TransaccionEntity>> =
        transaccionDao.getTransaccionesByPlataforma(plataformaId)

    fun getTransaccionesByBanco(bancoId: Long): Flow<List<TransaccionEntity>> =
        transaccionDao.getTransaccionesByBanco(bancoId)

    fun getTransaccionesByFecha(from: LocalDateTime, to: LocalDateTime): Flow<List<TransaccionEntity>> =
        transaccionDao.getTransaccionesByFecha(from, to)

    fun getGananciaPerdidaPeriodo(from: LocalDateTime, to: LocalDateTime): Flow<BigDecimal> =
        transaccionDao.getGananciaPerdidaPeriodo(from, to)

    fun getTotalUsdtComprado(): Flow<BigDecimal> = transaccionDao.getTotalUsdtComprado()

    fun getTotalUsdtVendido(): Flow<BigDecimal> = transaccionDao.getTotalUsdtVendido()

    suspend fun insert(transaccion: TransaccionEntity): Long = transaccionDao.insert(transaccion)

    suspend fun update(transaccion: TransaccionEntity) = transaccionDao.update(transaccion)

    suspend fun delete(transaccion: TransaccionEntity) = transaccionDao.delete(transaccion)
}
