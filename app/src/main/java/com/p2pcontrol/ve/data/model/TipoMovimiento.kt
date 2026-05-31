package com.p2pcontrol.ve.data.model

enum class TipoMovimiento(val etiqueta: String) {
    INGRESO("Ingreso"),
    EGRESO("Egreso"),
    TRANSFERENCIA_ENTRADA("Transferencia Entrada"),
    TRANSFERENCIA_SALIDA("Transferencia Salida")
}
