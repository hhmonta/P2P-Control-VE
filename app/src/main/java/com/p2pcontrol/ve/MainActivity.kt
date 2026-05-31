package com.p2pcontrol.ve

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.p2pcontrol.ve.ui.configuracion.ConfiguracionScreen
import com.p2pcontrol.ve.ui.dashboard.DashboardScreen
import com.p2pcontrol.ve.ui.historial.HistorialScreen
import com.p2pcontrol.ve.ui.movimientos.MovimientosScreen
import com.p2pcontrol.ve.ui.navigation.Screen
import com.p2pcontrol.ve.ui.navigation.bottomNavItems
import com.p2pcontrol.ve.ui.transaccion.NuevaTransaccionScreen
import com.p2pcontrol.ve.ui.theme.DarkBackground
import com.p2pcontrol.ve.ui.theme.DarkSurface
import com.p2pcontrol.ve.ui.theme.GreenLight
import com.p2pcontrol.ve.ui.theme.P2PControlVETheme
import com.p2pcontrol.ve.ui.theme.TextSecondary

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            P2PControlVETheme {
                P2PControlAppContent()
            }
        }
    }
}

@Composable
fun P2PControlAppContent() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val showBottomBar = bottomNavItems.any { screen ->
        currentDestination?.hierarchy?.any { it.route == screen.route } == true
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = DarkSurface,
                    tonalElevation = 8.dp
                ) {
                    bottomNavItems.forEach { screen ->
                        val selected = currentDestination?.hierarchy?.any {
                            it.route == screen.route
                        } == true

                        NavigationBarItem(
                            icon = {
                                Icon(
                                    screen.icon,
                                    contentDescription = screen.label,
                                    tint = if (selected) GreenLight else TextSecondary
                                )
                            },
                            label = {
                                Text(
                                    screen.label,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selected) GreenLight else TextSecondary
                                )
                            },
                            selected = selected,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = GreenLight.copy(alpha = 0.15f)
                            )
                        )
                    }
                }
            }
        },
        containerColor = DarkBackground
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Dashboard.route) {
                DashboardScreen(
                    onNuevaTransaccion = {
                        navController.navigate(Screen.NuevaTransaccion.route)
                    }
                )
            }
            composable(Screen.NuevaTransaccion.route) {
                NuevaTransaccionScreen(
                    onBack = { navController.popBackStack() },
                    onSaved = { navController.popBackStack() }
                )
            }
            composable(Screen.Historial.route) {
                HistorialScreen()
            }
            composable(Screen.Movimientos.route) {
                MovimientosScreen()
            }
            composable(Screen.Configuracion.route) {
                ConfiguracionScreen()
            }
        }
    }
}

