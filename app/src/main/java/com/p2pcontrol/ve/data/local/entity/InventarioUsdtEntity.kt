package com.p2pcontrol.ve.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.math.BigDecimal
import java.time.LocalDateTime

@Entity(tableName = "inventario_usdt")
data class InventarioUsdtEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val transaccionId: Long,
    val cantidadDisponible: BigDecimal,
    val costoUnitarioFiat: BigDecimal,
    val fiatMoneda: String,
    val fechaAdquisicion: LocalDateTime = LocalDateTime.now()
)
