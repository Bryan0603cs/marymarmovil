package com.marymar.mobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.core.view.WindowCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.compose.rememberNavController
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
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

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
                        loggedIn = false
                    )
                )

                val statusBarColor = MaterialTheme.colorScheme.primary.toArgb()

                LaunchedEffect(statusBarColor) {
                    window.statusBarColor = statusBarColor
                }

                LaunchedEffect(session.token) {
                    tokenProvider.setToken(session.token)
                }

                val nav = rememberNavController()
                val isLoggedIn = session.loggedIn && !session.token.isNullOrBlank() && session.userId != null
                val sessionVm: SessionViewModel = hiltViewModel()

                Scaffold(
                    topBar = {
                        if (isLoggedIn) {
                            TopAppBar(
                                title = { Text("Mar y Mar") },
                                actions = {
                                    TextButton(onClick = {
                                        sessionVm.logout()
                                        nav.navigate(Routes.Login) {
                                            popUpTo(nav.graph.id) { inclusive = true }
                                            launchSingleTop = true
                                        }
                                    }) {
                                        Text("Salir")
                                    }
                                }
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
                        startDestination = if (isLoggedIn) Routes.Products else Routes.Login,
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
                                            popUpTo(Routes.Login) { inclusive = true }
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
                                        popUpTo(Routes.Register) { inclusive = true }
                                        launchSingleTop = true
                                    }
                                }
                            }

                            RegisterScreen(vm = vm, onBack = { nav.popBackStack() })
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
                                        popUpTo(Routes.Login) { inclusive = true }
                                        launchSingleTop = true
                                    }
                                }
                            }

                            CodeScreen(vm = vm, email = email, onBack = { nav.popBackStack() })
                        }

                        composable(Routes.Products) {
                            val productsVm: ProductsViewModel = hiltViewModel()
                            ProductListScreen(productsVm)
                        }

                        composable(Routes.Profile) {
                            ProfileScreen(
                                session = session,
                                onLogout = {
                                    sessionVm.logout()
                                    nav.navigate(Routes.Login) {
                                        popUpTo(nav.graph.id) { inclusive = true }
                                        launchSingleTop = true
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}