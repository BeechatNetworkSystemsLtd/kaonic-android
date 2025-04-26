package network.beechat.kaonic.sampleapp

import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import network.beechat.kaonic.sampleapp.nodedetails.NodeDetailsScreen
import network.beechat.kaonic.sampleapp.nodedetails.NodeDetailsViewModel
import network.beechat.kaonic.sampleapp.nodedetails.NodeDetailsViewModelFactory
import network.beechat.kaonic.sampleapp.scan.ScanScreen
import network.beechat.kaonic.sampleapp.scan.ScanScreenViewModel
import network.beechat.kaonic.sampleapp.services.ChatService

val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

@Composable
fun AppNavigator() {
    val navController = rememberNavController()
    val chatService = remember {
        ChatService(appScope)
    }
    /**
     * val callService = remember {
     *         CallService(appScope)
     *     }
     *
     *     // 👇 Add this
     *     LaunchedEffect(Unit) {
     *         callService.navigationEvents.collect { route ->
     *             navController.navigate(route)
     *         }
     *     }
     *
     */

    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            val viewModel: ScanScreenViewModel = viewModel()
            ScanScreen(viewModel = viewModel, onNavigate = { name ->
                navController.navigate("nodeDetails/$name")
            })
        }
        composable(
            "nodeDetails/{address}",
            arguments = listOf(navArgument("address") { type = NavType.StringType })
        ) { backStackEntry ->
            val address = backStackEntry.arguments?.getString("address") ?: "Unknown"
            val viewModel: NodeDetailsViewModel =
                viewModel(factory = NodeDetailsViewModelFactory(address, chatService))
            NodeDetailsScreen(viewModel = viewModel)
        }
    }
}