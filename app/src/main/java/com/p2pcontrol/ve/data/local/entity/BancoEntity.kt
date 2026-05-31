package com.p2pcontrol.ve.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.p2pcontrol.ve.data.model.Moneda
import java.math.BigDecimal

@Entity(tableName = "bancos")
data class BancoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val nombre: String,
    val moneda: Moneda,
    val saldoInicial: BigDecimal = BigDecimal.ZERO,
    val activo: Boolean = true
)
