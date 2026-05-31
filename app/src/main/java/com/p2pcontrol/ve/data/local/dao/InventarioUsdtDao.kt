package com.p2pcontrol.ve.data.local.dao

import androidx.room.*
import com.p2pcontrol.ve.data.local.entity.InventarioUsdtEntity
import kotlinx.coroutines.flow.Flow
import java.math.BigDecimal

@Dao
interface InventarioUsdtDao {

    @Query("SELECT * FROM inventario_usdt WHERE cantidadDisponible > 0 ORDER BY fechaAdquisicion ASC")
    fun getInventarioDisponible(): Flow<List<InventarioUsdtEntity>>

    @Query("SELECT COALESCE(SUM(cantidadDisponible), 0) FROM inventario_usdt")
    fun getTotalUsdtDisponible(): Flow<BigDecimal>

    @Query("SELECT * FROM inventario_usdt WHERE id = :id")
    suspend fun getById(id: Long): InventarioUsdtEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: InventarioUsdtEntity): Long

    @Update
    suspend fun update(item: InventarioUsdtEntity)

    @Delete
    suspend fun delete(item: InventarioUsdtEntity)

    @Query("SELECT * FROM inventario_usdt WHERE transaccionId = :transaccionId")
    suspend fun getByTransaccion(transaccionId: Long): InventarioUsdtEntity?
}
