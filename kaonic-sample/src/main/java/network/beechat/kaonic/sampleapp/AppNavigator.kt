package network.beechat.kaonic.sampleapp

import android.content.Intent
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import network.beechat.kaonic.sampleapp.call.CallScreen
import network.beechat.kaonic.sampleapp.call.CallViewModel
import network.beechat.kaonic.sampleapp.call.CallViewModelFactory
import network.beechat.kaonic.sampleapp.nodedetails.NodeDetailsScreen
import network.beechat.kaonic.sampleapp.nodedetails.NodeDetailsViewModel
import network.beechat.kaonic.sampleapp.nodedetails.NodeDetailsViewModelFactory
import network.beechat.kaonic.sampleapp.scan.ScanScreen
import network.beechat.kaonic.sampleapp.scan.ScanScreenViewModel
import network.beechat.kaonic.sampleapp.services.ChatService
import network.beechat.kaonic.sampleapp.services.SecureStorageHelper
import network.beechat.kaonic.sampleapp.services.call.CallScreenState
import network.beechat.kaonic.sampleapp.services.call.CallService
import network.beechat.kaonic.sampleapp.settings.SettingsScreen
import network.beechat.kaonic.sampleapp.settings.SettingsViewModel
import network.beechat.kaonic.sampleapp.settings.SettingsViewModelFactory
import network.beechat.kaonic.sampleapp.video.VideoScreen
import network.beechat.kaonic.sampleapp.video.VideoViewModel
import network.beechat.kaonic.video.VideoStreamingActivity

val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

@Composable
fun AppNavigator(callS: CallService, secureStorageHelper: SecureStorageHelper) {
    val navController = rememberNavController()
    val chatService = remember { ChatService(appScope) }
    val callService = remember { callS }

    val callState = remember { callService.callState }.collectAsState().value
    if (callState == CallScreenState.finished) {
        navController.popBackStack()
    }

    LaunchedEffect(callService) {
        callService.navigationEvents.collect { route ->
            navController.navigate(route)
        }
    }

    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            val viewModel: ScanScreenViewModel = viewModel()
            ScanScreen(
                viewModel = viewModel, onOpenChat = { name ->
                    navController.navigate("nodeDetails/$name")
                },
                onOpenSettings = { navController.navigate("settings") },
                onVideo = { navController.navigate("video") })
        }
        composable("video") {
            val context = LocalContext.current
            LaunchedEffect(Unit) {
                val intent = Intent(context, VideoStreamingActivity::class.java)
                context.startActivity(intent)
            }
        }
        composable(
            "nodeDetails/{address}",
            arguments = listOf(navArgument("address") { type = NavType.StringType })
        ) { backStackEntry ->
            val address = backStackEntry.arguments?.getString("address") ?: "Unknown"
            val viewModel: NodeDetailsViewModel =
                viewModel(factory = NodeDetailsViewModelFactory(address, chatService))
            NodeDetailsScreen(
                viewModel = viewModel, onBack = { navController.popBackStack() },
                onCall = {
                    CoroutineScope(Dispatchers.Main).launch {
                        callService.createCall(address)

                        if (callService.activeCallId != null && callService.activeCallAddress != null) {
                            navController.navigate(
                                "outgoingCall/${callService.activeCallId}" +
                                        "/${callService.activeCallAddress}"

                            )
                        }
                    }
                })
        }
        composable("settings") {
            val viewModel: SettingsViewModel =
                viewModel(factory = SettingsViewModelFactory(secureStorageHelper))
            SettingsScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() })
        }
        composable(
            "incomingCall/{callId}/{address}",
            arguments = listOf(navArgument("callId") { type = NavType.StringType })
        ) { backStackEntry ->
            val callId = backStackEntry.arguments?.getString("callId") ?: ""
            val address = backStackEntry.arguments?.getString("address") ?: "Unknown"
            val viewModel: CallViewModel =
                viewModel(
                    factory = CallViewModelFactory(
                        address,
                        callService
                    )
                )

            CallScreen(viewModel)
        }
        composable(
            "outgoingCall/{callId}/{address}",
            arguments = listOf(navArgument("callId") { type = NavType.StringType })
        ) { backStackEntry ->
            val callId = backStackEntry.arguments?.getString("callId") ?: ""
            val address = backStackEntry.arguments?.getString("address") ?: "Unknown"
            val viewModel: CallViewModel =
                viewModel(
                    factory = CallViewModelFactory(
                        address,
                        callService,
                    )
                )

            CallScreen(viewModel)
        }
    }
}