package com.p2pcontrol.ve

import android.app.Application
import com.p2pcontrol.ve.data.local.AppDatabase
import com.p2pcontrol.ve.data.local.dao.*
import com.p2pcontrol.ve.data.repository.*

class P2PControlApp : Application() {

    private val database by lazy { AppDatabase.getDatabase(this) }

    val bancoDao by lazy { database.bancoDao() }
    val plataformaDao by lazy { database.plataformaDao() }
    val transaccionDao by lazy { database.transaccionDao() }
    val movimientoBancarioDao by lazy { database.movimientoBancarioDao() }
    val configuracionDao by lazy { database.configuracionDao() }
    val inventarioUsdtDao by lazy { database.inventarioUsdtDao() }

    val bancoRepository by lazy { BancoRepository(bancoDao, movimientoBancarioDao) }
    val plataformaRepository by lazy { PlataformaRepository(plataformaDao) }
    val transaccionRepository by lazy { TransaccionRepository(transaccionDao) }
    val movimientoBancarioRepository by lazy { MovimientoBancarioRepository(movimientoBancarioDao) }
    val configuracionRepository by lazy { ConfiguracionRepository(configuracionDao) }
    val inventarioUsdtRepository by lazy { InventarioUsdtRepository(inventarioUsdtDao) }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        lateinit var instance: P2PControlApp
            private set
    }
}
