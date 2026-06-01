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
    fun toBigDecimal(value: String?): BigDecimal? = try {
        value?.let { BigDecimal(it) }
    } catch (e: NumberFormatException) {
        BigDecimal.ZERO
    }

    @TypeConverter
    fun fromMoneda(moneda: Moneda): String = moneda.name

    @TypeConverter
    fun toMoneda(value: String): Moneda = try {
        Moneda.valueOf(value)
    } catch (e: IllegalArgumentException) {
        Moneda.VES
    }

    @TypeConverter
    fun fromTipoTransaccion(tipo: TipoTransaccion): String = tipo.name

    @TypeConverter
    fun toTipoTransaccion(value: String): TipoTransaccion = try {
        TipoTransaccion.valueOf(value)
    } catch (e: IllegalArgumentException) {
        TipoTransaccion.COMPRA
    }

    @TypeConverter
    fun fromTipoMovimiento(tipo: TipoMovimiento): String = tipo.name

    @TypeConverter
    fun toTipoMovimiento(value: String): TipoMovimiento = try {
        TipoMovimiento.valueOf(value)
    } catch (e: IllegalArgumentException) {
        TipoMovimiento.INGRESO
    }

    @TypeConverter
    fun fromLocalDateTime(value: LocalDateTime?): String? = value?.toString()

    @TypeConverter
    fun toLocalDateTime(value: String?): LocalDateTime? = try {
        value?.let { LocalDateTime.parse(it) }
    } catch (e: Exception) {
        null
    }

    @TypeConverter
    fun fromLocalDate(value: LocalDate?): String? = value?.toString()

    @TypeConverter
    fun toLocalDate(value: String?): LocalDate? = try {
        value?.let { LocalDate.parse(it) }
    } catch (e: Exception) {
        null
    }
}
