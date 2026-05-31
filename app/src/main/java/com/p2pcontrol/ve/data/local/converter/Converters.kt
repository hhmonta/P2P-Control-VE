package com.p2pcontrol.ve.data.local.converter

import androidx.room.TypeConverter
import com.p2pcontrol.ve.data.model.Moneda
import com.p2pcontrol.ve.data.model.TipoMovimiento
import com.p2pcontrol.ve.data.model.TipoTransaccion
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

class Converters {

    @TypeConverter
    fun fromBigDecimal(value: BigDecimal?): String? = value?.toPlainString()

    @TypeConverter
    fun toBigDecimal(value: String?): BigDecimal? = value?.let { BigDecimal(it) }

    @TypeConverter
    fun fromMoneda(moneda: Moneda): String = moneda.name

    @TypeConverter
    fun toMoneda(value: String): Moneda = Moneda.valueOf(value)

    @TypeConverter
    fun fromTipoTransaccion(tipo: TipoTransaccion): String = tipo.name

    @TypeConverter
    fun toTipoTransaccion(value: String): TipoTransaccion = TipoTransaccion.valueOf(value)

    @TypeConverter
    fun fromTipoMovimiento(tipo: TipoMovimiento): String = tipo.name

    @TypeConverter
    fun toTipoMovimiento(value: String): TipoMovimiento = TipoMovimiento.valueOf(value)

    @TypeConverter
    fun fromLocalDateTime(value: LocalDateTime?): String? = value?.toString()

    @TypeConverter
    fun toLocalDateTime(value: String?): LocalDateTime? = value?.let { LocalDateTime.parse(it) }

    @TypeConverter
    fun fromLocalDate(value: LocalDate?): String? = value?.toString()

    @TypeConverter
    fun toLocalDate(value: String?): LocalDate? = value?.let { LocalDate.parse(it) }
}
