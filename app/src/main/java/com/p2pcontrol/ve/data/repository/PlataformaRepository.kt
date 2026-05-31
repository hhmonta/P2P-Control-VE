package com.p2pcontrol.ve.data.repository

import com.p2pcontrol.ve.data.local.dao.PlataformaDao
import com.p2pcontrol.ve.data.local.entity.PlataformaEntity
import kotlinx.coroutines.flow.Flow

class PlataformaRepository(private val plataformaDao: PlataformaDao) {

    fun getAllPlataformasActivas(): Flow<List<PlataformaEntity>> = plataformaDao.getAllPlataformasActivas()

    fun getAllPlataformas(): Flow<List<PlataformaEntity>> = plataformaDao.getAllPlataformas()

    suspend fun insert(plataforma: PlataformaEntity): Long = plataformaDao.insert(plataforma)

    suspend fun update(plataforma: PlataformaEntity) = plataformaDao.update(plataforma)

    suspend fun deactivate(id: Long) = plataformaDao.deactivate(id)
}
