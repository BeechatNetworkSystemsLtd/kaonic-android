package network.beechat.kaonic.sampleapp

import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import network.beechat.kaonic.sampleapp.nodedetails.NodeDetailsScreen
import network.beechat.kaonic.sampleapp.nodedetails.NodeDetailsViewModel
import network.beechat.kaonic.sampleapp.nodedetails.NodeDetailsViewModelFactory
import network.beechat.kaonic.sampleapp.scan.ScanScreen
import network.beechat.kaonic.sampleapp.scan.ScanScreenViewModel

@Composable
fun AppNavigator() {
    val navController = rememberNavController()

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
                viewModel(factory = NodeDetailsViewModelFactory(address))
            NodeDetailsScreen(viewModel = viewModel)
        }
    }
}