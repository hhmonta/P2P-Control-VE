package com.p2pcontrol.ve.data.local.dao

import androidx.room.*
import com.p2pcontrol.ve.data.local.entity.MovimientoBancarioEntity
import com.p2pcontrol.ve.data.model.Moneda
import com.p2pcontrol.ve.data.model.TipoMovimiento
import kotlinx.coroutines.flow.Flow
import java.math.BigDecimal

@Dao
interface MovimientoBancarioDao {

    @Query("SELECT * FROM movimientos_bancarios WHERE bancoId = :bancoId ORDER BY fecha DESC")
    fun getMovimientosByBanco(bancoId: Long): Flow<List<MovimientoBancarioEntity>>

    @Query("SELECT * FROM movimientos_bancarios ORDER BY fecha DESC")
    fun getAllMovimientos(): Flow<List<MovimientoBancarioEntity>>

    @Query("""
        SELECT COALESCE(SUM(monto), 0) FROM movimientos_bancarios 
        WHERE bancoId = :bancoId AND tipo IN ('INGRESO', 'TRANSFERENCIA_ENTRADA')
    """)
    fun getTotalIngresosByBanco(bancoId: Long): Flow<BigDecimal>

    @Query("""
        SELECT COALESCE(SUM(monto), 0) FROM movimientos_bancarios 
        WHERE bancoId = :bancoId AND tipo IN ('EGRESO', 'TRANSFERENCIA_SALIDA')
    """)
    fun getTotalEgresosByBanco(bancoId: Long): Flow<BigDecimal>

    @Query("SELECT * FROM movimientos_bancarios WHERE transaccionId = :transaccionId")
    suspend fun getMovimientoByTransaccion(transaccionId: Long): MovimientoBancarioEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(movimiento: MovimientoBancarioEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(movimientos: List<MovimientoBancarioEntity>)

    @Delete
    suspend fun delete(movimiento: MovimientoBancarioEntity)

    @Query("DELETE FROM movimientos_bancarios WHERE transaccionId = :transaccionId")
    suspend fun deleteByTransaccion(transaccionId: Long)
}
