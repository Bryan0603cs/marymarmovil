package com.marymar.mobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.marymar.mobile.core.network.TokenProvider
import com.marymar.mobile.core.storage.SessionSnapshot
import com.marymar.mobile.core.storage.SessionStore
import com.marymar.mobile.presentation.navigation.BottomNavBar
import com.marymar.mobile.presentation.navigation.Routes
import com.marymar.mobile.presentation.screens.CodeScreen
import com.marymar.mobile.presentation.screens.LoginScreen
import com.marymar.mobile.presentation.screens.ProductListScreen
import com.marymar.mobile.presentation.screens.ProfileScreen
import com.marymar.mobile.presentation.screens.RegisterScreen
import com.marymar.mobile.presentation.viewmodel.AuthNext
import com.marymar.mobile.presentation.viewmodel.AuthViewModel
import com.marymar.mobile.presentation.viewmodel.ProductsViewModel
import com.marymar.mobile.presentation.viewmodel.SessionViewModel
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

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            MarymarTheme {
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

                val nav = rememberNavController()
                val scope = rememberCoroutineScope()
                val backStackEntry by nav.currentBackStackEntryAsState()
                val currentRoute = backStackEntry?.destination?.route

                val isLoggedIn = session.loggedIn &&
                        !session.token.isNullOrBlank() &&
                        session.userId != null

                val isAuthRoute = currentRoute == Routes.Login ||
                        currentRoute == Routes.Register ||
                        currentRoute?.startsWith(Routes.Code) == true

                val sessionVm: SessionViewModel = hiltViewModel()

                val statusBarColor = if (isLoggedIn) {
                    MaterialTheme.colorScheme.primary.toArgb()
                } else {
                    MaterialTheme.colorScheme.background.toArgb()
                }

                LaunchedEffect(statusBarColor) {
                    window.statusBarColor = statusBarColor
                }

                LaunchedEffect(session.token) {
                    tokenProvider.setToken(session.token)
                }

                LaunchedEffect(isLoggedIn, currentRoute) {
                    if (isLoggedIn && (currentRoute == null || isAuthRoute)) {
                        nav.navigate(Routes.Products) {
                            popUpTo(nav.graph.findStartDestination().id) {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }
                    }
                }

                fun closeSessionNow() {
                    nav.navigate(Routes.Login) {
                        popUpTo(nav.graph.findStartDestination().id) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                    scope.launch {
                        sessionVm.performLogout()
                    }
                }

                Scaffold(
                    topBar = {
                        if (isLoggedIn) {
                            CenterAlignedTopAppBar(
                                title = {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = "Mar y Mar",
                                            style = MaterialTheme.typography.headlineSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "Restaurante",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MutedText
                                        )
                                    }
                                },
                                navigationIcon = {
                                    Box(modifier = Modifier.width(118.dp))
                                },
                                actions = {
                                    Box(
                                        modifier = Modifier.width(118.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        OutlinedButton(onClick = { closeSessionNow() }) {
                                            Text("Cerrar sesión")
                                        }
                                    }
                                },
                                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                                    containerColor = SurfaceWhite
                                )
                            )
                        }
                    },
                    bottomBar = {
                        if (isLoggedIn) {
                            BottomNavBar(nav)
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

                                    AuthNext.LoggedIn -> {
                                        vm.consumeNext()
                                        nav.navigate(Routes.Products) {
                                            popUpTo(nav.graph.findStartDestination().id) {
                                                inclusive = true
                                            }
                                            launchSingleTop = true
                                        }
                                    }

                                    null -> Unit
                                }
                            }

                            LoginScreen(
                                vm = vm,
                                onRegister = { nav.navigate(Routes.Register) },
                                onGoToCode = { email ->
                                    nav.navigate("${Routes.Code}?email=$email")
                                }
                            )
                        }

                        composable(Routes.Register) {
                            val vm: AuthViewModel = hiltViewModel()
                            val authState by vm.ui.collectAsStateWithLifecycle()

                            LaunchedEffect(authState.next) {
                                if (authState.next == AuthNext.LoggedIn) {
                                    vm.consumeNext()
                                    nav.navigate(Routes.Products) {
                                        popUpTo(nav.graph.findStartDestination().id) {
                                            inclusive = true
                                        }
                                        launchSingleTop = true
                                    }
                                }
                            }

                            RegisterScreen(
                                vm = vm,
                                onBack = { nav.popBackStack() }
                            )
                        }

                        composable(
                            route = "${Routes.Code}?email={email}",
                            arguments = listOf(navArgument("email") { type = NavType.StringType })
                        ) { backStack ->
                            val email = backStack.arguments?.getString("email") ?: ""
                            val vm: AuthViewModel = hiltViewModel()
                            val authState by vm.ui.collectAsStateWithLifecycle()

                            LaunchedEffect(authState.next) {
                                if (authState.next == AuthNext.LoggedIn) {
                                    vm.consumeNext()
                                    nav.navigate(Routes.Products) {
                                        popUpTo(nav.graph.findStartDestination().id) {
                                            inclusive = true
                                        }
                                        launchSingleTop = true
                                    }
                                }
                            }

                            CodeScreen(
                                vm = vm,
                                email = email,
                                onBack = { nav.popBackStack() }
                            )
                        }

                        composable(Routes.Products) {
                            val productsVm: ProductsViewModel = hiltViewModel()
                            ProductListScreen(productsVm = productsVm)
                        }

                        composable(Routes.Profile) {
                            val authVm: AuthViewModel = hiltViewModel()

                            ProfileScreen(
                                session = session,
                                onSaveProfile = { name, email, phone, address ->
                                    sessionVm.updateProfile(
                                        name = name,
                                        email = email,
                                        phone = phone,
                                        address = address
                                    )
                                },
                                onRequestPasswordChange = {
                                    session.email?.takeIf { it.isNotBlank() }?.let { safeEmail ->
                                        authVm.forgotPassword(safeEmail)
                                    }
                                },
                                onLogout = {
                                    closeSessionNow()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}