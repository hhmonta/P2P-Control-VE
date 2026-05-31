package com.p2pcontrol.ve.data.local.dao

import androidx.room.*
import com.p2pcontrol.ve.data.local.entity.PlataformaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PlataformaDao {

    @Query("SELECT * FROM plataformas WHERE activo = 1 ORDER BY nombre")
    fun getAllPlataformasActivas(): Flow<List<PlataformaEntity>>

    @Query("SELECT * FROM plataformas ORDER BY nombre")
    fun getAllPlataformas(): Flow<List<PlataformaEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(plataforma: PlataformaEntity): Long

    @Update
    suspend fun update(plataforma: PlataformaEntity)

    @Query("UPDATE plataformas SET activo = 0 WHERE id = :id")
    suspend fun deactivate(id: Long)
}
