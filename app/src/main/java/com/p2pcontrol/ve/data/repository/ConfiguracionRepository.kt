package com.p2pcontrol.ve.data.repository

import com.p2pcontrol.ve.data.local.dao.ConfiguracionDao
import com.p2pcontrol.ve.data.local.entity.ConfiguracionEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.math.BigDecimal
import java.time.LocalDate

class ConfiguracionRepository(private val configuracionDao: ConfiguracionDao) {

    suspend fun get(clave: String): ConfiguracionEntity? = configuracionDao.get(clave)

    fun getFlow(clave: String): Flow<ConfiguracionEntity?> = configuracionDao.getFlow(clave)

    fun getAll(): Flow<List<ConfiguracionEntity>> = configuracionDao.getAll()

    suspend fun setTasaUsdVes(tasa: BigDecimal) {
        configuracionDao.set(
            ConfiguracionEntity(
                clave = "tasa_usd_ves",
                valor = tasa.toPlainString(),
                fechaActualizacion = LocalDate.now()
            )
        )
    }

    suspend fun getTasaUsdVes(): BigDecimal {
        val config = configuracionDao.get("tasa_usd_ves")
        return config?.valor?.toBigDecimalOrNull() ?: BigDecimal.ONE
    }

    fun getTasaUsdVesFlow(): Flow<BigDecimal> = configuracionDao.getFlow("tasa_usd_ves").map { config ->
        config?.valor?.toBigDecimalOrNull() ?: BigDecimal.ONE
    }

    suspend fun set(clave: String, valor: String) {
        configuracionDao.set(
            ConfiguracionEntity(
                clave = clave,
                valor = valor,
                fechaActualizacion = LocalDate.now()
            )
        )
    }
}
