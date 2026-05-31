package com.p2pcontrol.ve.data.local.dao

import androidx.room.*
import com.p2pcontrol.ve.data.local.entity.BancoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BancoDao {

    @Query("SELECT * FROM bancos WHERE activo = 1 ORDER BY nombre")
    fun getAllBancosActivos(): Flow<List<BancoEntity>>

    @Query("SELECT * FROM bancos ORDER BY nombre")
    fun getAllBancos(): Flow<List<BancoEntity>>

    @Query("SELECT * FROM bancos WHERE id = :id")
    suspend fun getBancoById(id: Long): BancoEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(banco: BancoEntity): Long

    @Update
    suspend fun update(banco: BancoEntity)

    @Query("UPDATE bancos SET activo = 0 WHERE id = :id")
    suspend fun deactivate(id: Long)

    @Query("DELETE FROM bancos WHERE id = :id")
    suspend fun delete(id: Long)
}
