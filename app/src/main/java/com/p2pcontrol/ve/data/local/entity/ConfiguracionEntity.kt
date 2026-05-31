package com.p2pcontrol.ve.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "configuracion")
data class ConfiguracionEntity(
    @PrimaryKey
    val clave: String,
    val valor: String,
    val fechaActualizacion: LocalDate = LocalDate.now()
)
