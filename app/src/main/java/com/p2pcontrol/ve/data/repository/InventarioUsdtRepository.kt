package com.p2pcontrol.ve.data.repository

import com.p2pcontrol.ve.data.local.dao.InventarioUsdtDao
import com.p2pcontrol.ve.data.local.entity.InventarioUsdtEntity
import kotlinx.coroutines.flow.Flow
import java.math.BigDecimal

class InventarioUsdtRepository(private val inventarioDao: InventarioUsdtDao) {

    fun getInventarioDisponible(): Flow<List<InventarioUsdtEntity>> =
        inventarioDao.getInventarioDisponible()

    fun getTotalUsdtDisponible(): Flow<BigDecimal> = inventarioDao.getTotalUsdtDisponible()

    suspend fun getById(id: Long): InventarioUsdtEntity? = inventarioDao.getById(id)

    suspend fun insert(item: InventarioUsdtEntity): Long = inventarioDao.insert(item)

    suspend fun update(item: InventarioUsdtEntity) = inventarioDao.update(item)

    suspend fun delete(item: InventarioUsdtEntity) = inventarioDao.delete(item)

    suspend fun getByTransaccion(transaccionId: Long): InventarioUsdtEntity? =
        inventarioDao.getByTransaccion(transaccionId)
}
