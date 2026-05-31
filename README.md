# P2P Control VE

Aplicación Android nativa (Kotlin + Jetpack Compose) para el control personal de operaciones de compra/venta en el mercado P2P venezolano.

## Características

- Registro manual de transacciones P2P (Compra/Venta)
- Soporte para monedas fiat USD y VES
- Integración con plataformas: Bybit, Binance, AploPay, Airtm, Zinli, Wally, Syklo
- Bancos locales: Banesco, Mercantil, BDV, BNC
- Control de saldos bancarios en tiempo real
- Cálculo automático de ganancia/pérdida en USDT (costo promedio ponderado / FIFO)
- Movimientos bancarios manuales (ingresos, egresos, transferencias)
- Configuración de tasa USD/VES de referencia
- Exportación de transacciones a CSV
- Funcionamiento 100% offline
- Interfaz en español

## Tecnologías

- **Lenguaje**: Kotlin
- **UI**: Jetpack Compose + Material 3
- **Base de datos**: Room (SQLite)
- **Arquitectura**: MVVM + Repository
- **Navegación**: Navigation Compose
- **Cálculos**: BigDecimal (precisión financiera)

## Pantallas

1. **Dashboard**: Balance total, USDT disponibles, G/P del mes, saldos por banco
2. **Nueva Transacción**: Registro rápido de compra/venta (< 30 seg)
3. **Historial**: Lista filtrada de transacciones con G/P visible
4. **Movimientos Bancarios**: Ingresos, egresos, transferencias entre bancos
5. **Configuración**: Tasa de referencia, administración de bancos/plataformas, exportar CSV

## Modelo de Datos

- `bancos` - Bancos con moneda y saldo inicial
- `plataformas` - Plataformas de intercambio P2P
- `transacciones` - Operaciones de compra/venta
- `movimientos_bancarios` - Entradas/salidas de fiat
- `inventario_usdt` - Inventario de USDT con costo de adquisición
- `configuracion` - Configuración clave-valor

## Compilación

```bash
./gradlew assembleDebug
```

## Licencia

Uso personal - Proyecto privado
