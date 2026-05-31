package com.p2pcontrol.ve.data.local.dao

import androidx.room.*
import com.p2pcontrol.ve.data.local.entity.ConfiguracionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ConfiguracionDao {

    @Query("SELECT * FROM configuracion WHERE clave = :clave")
    suspend fun get(clave: String): ConfiguracionEntity?

    @Query("SELECT * FROM configuracion WHERE clave = :clave")
    fun getFlow(clave: String): Flow<ConfiguracionEntity?>

    @Query("SELECT * FROM configuracion")
    fun getAll(): Flow<List<ConfiguracionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun set(config: ConfiguracionEntity)

    @Query("DELETE FROM configuracion WHERE clave = :clave")
    suspend fun delete(clave: String)
}
