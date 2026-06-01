package com.p2pcontrol.ve.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.p2pcontrol.ve.data.local.converter.Converters
import com.p2pcontrol.ve.data.local.dao.*
import com.p2pcontrol.ve.data.local.entity.*
import com.p2pcontrol.ve.data.model.Moneda
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.math.BigDecimal

@Database(
    entities = [
        BancoEntity::class,
        PlataformaEntity::class,
        TransaccionEntity::class,
        MovimientoBancarioEntity::class,
        ConfiguracionEntity::class,
        InventarioUsdtEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun bancoDao(): BancoDao
    abstract fun plataformaDao(): PlataformaDao
    abstract fun transaccionDao(): TransaccionDao
    abstract fun movimientoBancarioDao(): MovimientoBancarioDao
    abstract fun configuracionDao(): ConfiguracionDao
    abstract fun inventarioUsdtDao(): InventarioUsdtDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "p2p_control_ve_db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance

                // Seed data AFTER database is built and INSTANCE is assigned
                CoroutineScope(Dispatchers.IO).launch {
                    seedData(instance)
                }

                instance
            }
        }

        private suspend fun seedData(database: AppDatabase) {
            // Only seed if tables are empty
            val existingBancos = database.bancoDao().getAllBancosActivos().first()
            if (existingBancos.isNotEmpty()) return

            // Bancos predeterminados
            val bancos = listOf(
                BancoEntity(nombre = "Banesco", moneda = Moneda.VES, saldoInicial = BigDecimal.ZERO),
                BancoEntity(nombre = "Mercantil", moneda = Moneda.VES, saldoInicial = BigDecimal.ZERO),
                BancoEntity(nombre = "BDV", moneda = Moneda.VES, saldoInicial = BigDecimal.ZERO),
                BancoEntity(nombre = "BNC", moneda = Moneda.VES, saldoInicial = BigDecimal.ZERO),
                BancoEntity(nombre = "Banesco USD", moneda = Moneda.USD, saldoInicial = BigDecimal.ZERO),
                BancoEntity(nombre = "Mercantil USD", moneda = Moneda.USD, saldoInicial = BigDecimal.ZERO)
            )
            bancos.forEach { database.bancoDao().insert(it) }

            // Plataformas predeterminadas
            val plataformas = listOf(
                PlataformaEntity(nombre = "Bybit P2P"),
                PlataformaEntity(nombre = "Binance P2P"),
                PlataformaEntity(nombre = "AploPay"),
                PlataformaEntity(nombre = "Airtm"),
                PlataformaEntity(nombre = "Zinli"),
                PlataformaEntity(nombre = "Wally"),
                PlataformaEntity(nombre = "Syklo")
            )
            plataformas.forEach { database.plataformaDao().insert(it) }

            // Tasa inicial
            database.configuracionDao().set(
                ConfiguracionEntity(
                    clave = "tasa_usd_ves",
                    valor = "50.00",
                    fechaActualizacion = java.time.LocalDate.now()
                )
            )
        }
    }
}
