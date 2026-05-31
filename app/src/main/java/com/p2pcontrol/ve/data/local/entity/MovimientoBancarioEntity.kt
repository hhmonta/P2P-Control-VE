package com.p2pcontrol.ve.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.p2pcontrol.ve.data.model.Moneda
import com.p2pcontrol.ve.data.model.TipoMovimiento
import java.math.BigDecimal
import java.time.LocalDate

@Entity(
    tableName = "movimientos_bancarios",
    foreignKeys = [
        ForeignKey(
            entity = BancoEntity::class,
            parentColumns = ["id"],
            childColumns = ["bancoId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = TransaccionEntity::class,
            parentColumns = ["id"],
            childColumns = ["transaccionId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("bancoId"), Index("transaccionId")]
)
data class MovimientoBancarioEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val bancoId: Long,
    val transaccionId: Long? = null,
    val tipo: TipoMovimiento,
    val monto: BigDecimal,
    val moneda: Moneda,
    val fecha: LocalDate = LocalDate.now(),
    val descripcion: String = ""
)
