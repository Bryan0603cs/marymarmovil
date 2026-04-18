package com.marymar.mobile

import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.marymar.mobile.core.auth.GoogleSignInManager
import com.marymar.mobile.core.network.TokenProvider
import com.marymar.mobile.core.storage.SessionSnapshot
import com.marymar.mobile.core.storage.SessionStore
import com.marymar.mobile.domain.model.Role
import com.marymar.mobile.presentation.navigation.BottomNavBar
import com.marymar.mobile.presentation.navigation.Routes
import com.marymar.mobile.presentation.screens.*
import com.marymar.mobile.presentation.viewmodel.*
import com.marymar.mobile.ui.components.AccessibilityFloatingControls
import com.marymar.mobile.ui.theme.MarymarTheme
import com.marymar.mobile.ui.theme.MutedText
import com.marymar.mobile.ui.theme.SurfaceWhite
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var sessionStore: SessionStore
    @Inject lateinit var tokenProvider: TokenProvider
    @Inject lateinit var googleSignInManager: GoogleSignInManager

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            var fontScale by rememberSaveable { mutableFloatStateOf(1f) }
            var highContrast by rememberSaveable { mutableStateOf(false) }
            var accessibilityExpanded by rememberSaveable { mutableStateOf(false) }

            MarymarTheme(
                fontScale = fontScale,
                highContrast = highContrast
            ) {
                val session by sessionStore.sessionFlow.collectAsStateWithLifecycle(
                    initialValue = SessionSnapshot(
                        token = null,
                        email = null,
                        name = null,
                        role = null,
                        userId = null,
                        loggedIn = false,
                        phone = null,
                        address = null,
                        birthDate = null,
                        idNumber = null
                    )
                )

                val sessionRole = session.role.toRoleOrDefault()
                val nav = rememberNavController()
                val scope = rememberCoroutineScope()
                val backStackEntry by nav.currentBackStackEntryAsState()
                val currentRoute = backStackEntry?.destination?.route
                val sessionVm: SessionViewModel = hiltViewModel()
                val cartVm: CartViewModel = hiltViewModel()

                val isLoggedIn = session.loggedIn && !session.token.isNullOrBlank() && session.userId != null
                val isAuthRoute = currentRoute == Routes.Login ||
                        currentRoute == Routes.Register ||
                        currentRoute?.startsWith(Routes.Code) == true
                val isHomeRoute = currentRoute?.startsWith("home/") == true

                LaunchedEffect(session.token, isLoggedIn) {
                    tokenProvider.setToken(session.token.takeIf { isLoggedIn })
                }

                LaunchedEffect(isLoggedIn, currentRoute, sessionRole) {
                    if (!isLoggedIn && isHomeRoute) {
                        nav.navigate(Routes.Login) {
                            popUpTo(nav.graph.findStartDestination().id) { inclusive = true }
                            launchSingleTop = true
                        }
                    } else if (isLoggedIn && (currentRoute == null || isAuthRoute)) {
                        nav.navigate(defaultHomeRoute(sessionRole)) {
                            popUpTo(nav.graph.findStartDestination().id) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                }

                val statusBarColor = if (isLoggedIn) {
                    MaterialTheme.colorScheme.primary.toArgb()
                } else {
                    MaterialTheme.colorScheme.background.toArgb()
                }

                LaunchedEffect(statusBarColor) {
                    window.statusBarColor = statusBarColor
                }

                fun closeSessionNow() {
                    scope.launch {
                        sessionVm.performLogout()
                        nav.navigate(Routes.Login) {
                            popUpTo(nav.graph.findStartDestination().id) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                }

                Box(modifier = Modifier.fillMaxSize()) {
                    Scaffold(
                        topBar = {
                            if (
                                isLoggedIn &&
                                !isAuthRoute &&
                                currentRoute != Routes.Products &&
                                currentRoute != Routes.Kitchen
                            ) {
                                CenterAlignedTopAppBar(
                                    title = {
                                        Column {
                                            Text(
                                                text = when (sessionRole) {
                                                    Role.MESERO -> "Mar y Mar · Mesero"
                                                    Role.COCINERO -> "Mar y Mar · Cocina"
                                                    else -> "Mar y Mar"
                                                },
                                                style = MaterialTheme.typography.headlineSmall,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = when (sessionRole) {
                                                    Role.MESERO -> "Mesas, pedido activo y documentos"
                                                    Role.COCINERO -> "Cola de pedidos y preparación"
                                                    else -> "Seguimiento del pedido"
                                                },
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MutedText
                                            )
                                        }
                                    },
                                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                                        containerColor = SurfaceWhite
                                    )
                                )
                            }
                        },
                        bottomBar = {
                            if (isLoggedIn && !isAuthRoute) {
                                BottomNavBar(navController = nav, role = sessionRole)
                            }
                        }
                    ) { innerPadding ->
                        NavHost(
                            navController = nav,
                            startDestination = Routes.Login,
                            modifier = Modifier.padding(innerPadding)
                        ) {
                            composable(Routes.Login) {
                                val vm: AuthViewModel = hiltViewModel()
                                val authState by vm.ui.collectAsStateWithLifecycle()

                                LaunchedEffect(authState.next) {
                                    when (val next = authState.next) {
                                        is AuthNext.GoToCode -> {
                                            vm.consumeNext()
                                            nav.navigate("${Routes.Code}?email=${next.email}")
                                        }
                                        AuthNext.LoggedIn -> vm.consumeNext()
                                        null -> Unit
                                    }
                                }

                                LoginScreen(
                                    vm = vm,
                                    googleSignInManager = googleSignInManager,
                                    onRegister = { nav.navigate(Routes.Register) },
                                    onGoToCode = { email -> nav.navigate("${Routes.Code}?email=$email") }
                                )
                            }

                            composable(Routes.Register) {
                                val vm: AuthViewModel = hiltViewModel()
                                RegisterScreen(vm = vm, onBack = { nav.popBackStack() })
                            }

                            composable(
                                route = "${Routes.Code}?email={email}",
                                arguments = listOf(navArgument("email") { type = NavType.StringType })
                            ) { backStack ->
                                val email = backStack.arguments?.getString("email") ?: ""
                                val vm: AuthViewModel = hiltViewModel()
                                CodeScreen(vm = vm, email = email, onBack = { nav.popBackStack() })
                            }

                            composable(Routes.Products) {
                                val productsVm: ProductsViewModel = hiltViewModel()
                                ProductListScreen(
                                    productsVm = productsVm,
                                    cartVm = cartVm,
                                    onOpenCart = { nav.navigate(Routes.Cart) }
                                )
                            }

                            composable(Routes.Cart) {
                                CartScreen(
                                    cartVm = cartVm,
                                    sessionUserId = session.userId ?: 0L,
                                    onOrderCreated = { orderId ->
                                        nav.navigate("${Routes.OrderDetail}/$orderId")
                                    }
                                )
                            }

                            composable(Routes.Orders) {
                                val vm: OrdersViewModel = hiltViewModel()
                                OrdersScreen(
                                    vm = vm,
                                    sessionUserId = session.userId ?: 0L,
                                    sessionRole = sessionRole,
                                    onOpenOrder = { orderId ->
                                        nav.navigate("${Routes.OrderDetail}/$orderId")
                                    }
                                )
                            }

                            composable(Routes.Tables) {
                                val vm: TablesViewModel = hiltViewModel()
                                TablesScreen(
                                    vm = vm,
                                    meseroId = session.userId ?: 0L,
                                    onOpenTableOrder = { mesaId, mesaNumero ->
                                        nav.navigate("${Routes.ActiveTableOrder}/$mesaId/$mesaNumero")
                                    }
                                )
                            }

                            composable(Routes.Kitchen) {
                                val vm: KitchenOrdersViewModel = hiltViewModel()
                                KitchenOrdersScreen(vm = vm)
                            }

                            composable(
                                route = "${Routes.ActiveTableOrder}/{mesaId}/{mesaNumero}",
                                arguments = listOf(
                                    navArgument("mesaId") { type = NavType.LongType },
                                    navArgument("mesaNumero") { type = NavType.IntType }
                                )
                            ) { backStack ->
                                val mesaId = backStack.arguments?.getLong("mesaId") ?: 0L
                                val mesaNumero = backStack.arguments?.getInt("mesaNumero") ?: 0
                                val vm: ActiveTableOrderViewModel = hiltViewModel()

                                ActiveTableOrderScreen(
                                    vm = vm,
                                    mesaId = mesaId,
                                    mesaNumero = mesaNumero,
                                    meseroId = session.userId ?: 0L,
                                    authToken = session.token.orEmpty()
                                )
                            }

                            composable(
                                route = "${Routes.OrderDetail}/{orderId}",
                                arguments = listOf(navArgument("orderId") { type = NavType.LongType })
                            ) { backStack ->
                                val orderId = backStack.arguments?.getLong("orderId") ?: 0L
                                val vm: OrderDetailViewModel = hiltViewModel()
                                OrderDetailScreen(vm = vm, orderId = orderId)
                            }

                            composable(Routes.Profile) {
                                val authVm: AuthViewModel = hiltViewModel()
                                val authState by authVm.ui.collectAsStateWithLifecycle()

                                ProfileScreen(
                                    session = session,
                                    passwordChangeLoading = authState.loadingForgot,
                                    passwordChangeInfo = authState.info,
                                    passwordChangeError = authState.error,
                                    onClearPasswordFeedback = { authVm.clearBanners() },
                                    onSaveProfile = { name, email, phone, address ->
                                        sessionVm.updateProfile(name, email, phone, address)
                                    },
                                    onRequestPasswordChange = {
                                        session.email?.takeIf { it.isNotBlank() }?.let(authVm::forgotPassword)
                                    },
                                    onLogout = { closeSessionNow() }
                                )
                            }
                        }
                    }

                    AccessibilityFloatingControls(
                        expanded = accessibilityExpanded,
                        fontScale = fontScale,
                        highContrast = highContrast,
                        onToggleExpanded = { accessibilityExpanded = !accessibilityExpanded },
                        onIncreaseFont = { fontScale = (fontScale + 0.10f).coerceAtMost(1.35f) },
                        onDecreaseFont = { fontScale = (fontScale - 0.10f).coerceAtLeast(0.90f) },
                        onToggleContrast = { highContrast = !highContrast },
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(
                                end = 18.dp,
                                bottom = if (isLoggedIn && !isAuthRoute) 96.dp else 18.dp
                            )
                    )
                }
            }
        }
    }

    private fun defaultHomeRoute(role: Role): String = when (role) {
        Role.MESERO -> Routes.Tables
        Role.COCINERO -> Routes.Kitchen
        else -> Routes.Products
    }

    private fun String?.toRoleOrDefault(): Role = when (this?.uppercase()) {
        "MESERO" -> Role.MESERO
        "COCINERO" -> Role.COCINERO
        "ADMINISTRADOR" -> Role.ADMINISTRADOR
        else -> Role.CLIENTE
    }
}