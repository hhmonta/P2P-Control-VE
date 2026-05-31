package com.p2pcontrol.ve.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.p2pcontrol.ve.data.model.Moneda
import com.p2pcontrol.ve.data.model.TipoTransaccion
import java.math.BigDecimal
import java.time.LocalDateTime

@Entity(
    tableName = "transacciones",
    foreignKeys = [
        ForeignKey(
            entity = PlataformaEntity::class,
            parentColumns = ["id"],
            childColumns = ["plataformaId"],
            onDelete = ForeignKey.RESTRICT
        ),
        ForeignKey(
            entity = BancoEntity::class,
            parentColumns = ["id"],
            childColumns = ["bancoId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [Index("plataformaId"), Index("bancoId")]
)
data class TransaccionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val tipo: TipoTransaccion,
    val plataformaId: Long,
    val bancoId: Long,
    val fiatMoneda: Moneda,
    val montoFiat: BigDecimal,
    val cantidadUsdt: BigDecimal,
    val tasa: BigDecimal,
    val comisionUsdt: BigDecimal = BigDecimal.ZERO,
    val gananciaPerdidaUsdt: BigDecimal = BigDecimal.ZERO,
    val fechaHora: LocalDateTime = LocalDateTime.now(),
    val notas: String = ""
)
