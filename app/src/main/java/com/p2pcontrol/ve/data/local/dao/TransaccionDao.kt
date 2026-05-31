package com.p2pcontrol.ve.data.local.dao

import androidx.room.*
import com.p2pcontrol.ve.data.local.entity.TransaccionEntity
import com.p2pcontrol.ve.data.model.Moneda
import com.p2pcontrol.ve.data.model.TipoTransaccion
import kotlinx.coroutines.flow.Flow
import java.math.BigDecimal
import java.time.LocalDateTime

@Dao
interface TransaccionDao {

    @Query("SELECT * FROM transacciones ORDER BY fechaHora DESC")
    fun getAllTransacciones(): Flow<List<TransaccionEntity>>

    @Query("SELECT * FROM transacciones WHERE id = :id")
    suspend fun getTransaccionById(id: Long): TransaccionEntity?

    @Query("SELECT * FROM transacciones WHERE tipo = :tipo ORDER BY fechaHora DESC")
    fun getTransaccionesByTipo(tipo: TipoTransaccion): Flow<List<TransaccionEntity>>

    @Query("SELECT * FROM transacciones WHERE plataformaId = :plataformaId ORDER BY fechaHora DESC")
    fun getTransaccionesByPlataforma(plataformaId: Long): Flow<List<TransaccionEntity>>

    @Query("SELECT * FROM transacciones WHERE bancoId = :bancoId ORDER BY fechaHora DESC")
    fun getTransaccionesByBanco(bancoId: Long): Flow<List<TransaccionEntity>>

    @Query("SELECT * FROM transacciones WHERE fechaHora BETWEEN :from AND :to ORDER BY fechaHora DESC")
    fun getTransaccionesByFecha(from: LocalDateTime, to: LocalDateTime): Flow<List<TransaccionEntity>>

    @Query("SELECT * FROM transacciones ORDER BY fechaHora DESC LIMIT :limit")
    fun getUltimasTransacciones(limit: Int = 5): Flow<List<TransaccionEntity>>

    @Query("""
        SELECT COALESCE(SUM(gananciaPerdidaUsdt), 0) FROM transacciones 
        WHERE tipo = 'VENTA' AND fechaHora BETWEEN :from AND :to
    """)
    fun getGananciaPerdidaPeriodo(from: LocalDateTime, to: LocalDateTime): Flow<BigDecimal>

    @Query("""
        SELECT COALESCE(SUM(cantidadUsdt), 0) FROM transacciones 
        WHERE tipo = 'COMPRA'
    """)
    fun getTotalUsdtComprado(): Flow<BigDecimal>

    @Query("""
        SELECT COALESCE(SUM(cantidadUsdt), 0) FROM transacciones 
        WHERE tipo = 'VENTA'
    """)
    fun getTotalUsdtVendido(): Flow<BigDecimal>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transaccion: TransaccionEntity): Long

    @Update
    suspend fun update(transaccion: TransaccionEntity)

    @Delete
    suspend fun delete(transaccion: TransaccionEntity)
}
