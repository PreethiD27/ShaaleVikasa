package com.shaalevikas.app.ui

import androidx.compose.runtime.*
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.shaalevikas.app.ui.screens.*

sealed class Screen(val route: String) {
    object Login         : Screen("login")
    object Register      : Screen("register")
    object Dashboard     : Screen("dashboard")
    object NeedDetail    : Screen("need_detail/{needId}") { fun go(id: String) = "need_detail/$id" }
    object AdminPanel    : Screen("admin_panel")
    object AddEditNeed   : Screen("add_edit_need?needId={needId}") {
        fun go(id: String? = null) = if (id != null) "add_edit_need?needId=$id" else "add_edit_need?needId="
    }
    object ImpactGallery : Screen("impact_gallery")
}

@Composable
fun ShaaleVikasNavHost(vm: MainViewModel) {
    val nav = rememberNavController()
    LaunchedEffect(Unit) { vm.observeUserRole() }

    NavHost(navController = nav, startDestination = Screen.Login.route) {
        composable(Screen.Login.route) {
            LoginScreen(vm) { nav.navigate(Screen.Dashboard.route) { popUpTo(Screen.Login.route) { inclusive = true } } }
        }
        composable(Screen.Register.route) {
            RegisterScreen(vm) { nav.navigate(Screen.Dashboard.route) { popUpTo(Screen.Login.route) { inclusive = true } } }
        }
        composable(Screen.Dashboard.route) {
            DashboardScreen(
                vm = vm,
                onNeedClick = { nav.navigate(Screen.NeedDetail.go(it)) },
                onAdminClick = { nav.navigate(Screen.AdminPanel.route) },
                onImpactClick = { nav.navigate(Screen.ImpactGallery.route) },
                onLogout = { vm.logout { nav.navigate(Screen.Login.route) { popUpTo(0) } } }
            )
        }
        composable(Screen.NeedDetail.route,
            arguments = listOf(navArgument("needId") { type = NavType.StringType })) { back ->
            val needId = back.arguments?.getString("needId") ?: return@composable
            NeedDetailScreen(vm = vm, needId = needId, onBack = { nav.popBackStack() })
        }
        composable(Screen.AdminPanel.route) {
            AdminPanelScreen(vm = vm,
                onAddClick = { nav.navigate(Screen.AddEditNeed.go()) },
                onEditClick = { nav.navigate(Screen.AddEditNeed.go(it)) },
                onBack = { nav.popBackStack() })
        }
        composable(Screen.AddEditNeed.route,
            arguments = listOf(navArgument("needId") { type = NavType.StringType; defaultValue = "" })) { back ->
            val needId = back.arguments?.getString("needId")?.takeIf { it.isNotBlank() }
            AddEditNeedScreen(vm = vm, editNeedId = needId, onDone = { nav.popBackStack() })
        }
        composable(Screen.ImpactGallery.route) {
            ImpactGalleryScreen(vm = vm, onBack = { nav.popBackStack() })
        }
    }
}
