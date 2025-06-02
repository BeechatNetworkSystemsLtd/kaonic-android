package network.beechat.kaonic.sampleapp.call

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import network.beechat.kaonic.sampleapp.services.call.CallScreenState

@Composable
fun CallScreen(
    viewModel: CallViewModel,
    onCallEnd: () -> Unit
) {
    val callState = viewModel.callState.collectAsState().value

    if (callState == CallScreenState.idle) {
        onCallEnd()
    }

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (callState) {
                CallScreenState.incoming -> CallIncomingView(viewModel, onCallEnd)

                CallScreenState.outgoing -> CallOutgoing(viewModel, onCallEnd)

                CallScreenState.callInProgress -> CallInProgress(viewModel, onCallEnd)
                else -> {

                }
            }
        }
    }
}

@Composable
fun CallIncomingView(viewModel: CallViewModel, onCallEnd: () -> Unit) {
    val address = viewModel.address

    Text(
        text = "Incoming Call from $address",
        style = MaterialTheme.typography.headlineMedium,
        modifier = Modifier.padding(bottom = 24.dp)
    )

    Row(
        horizontalArrangement = Arrangement.SpaceEvenly,
        modifier = Modifier.fillMaxWidth()
    ) {
        IconButton(
            onClick = {
                viewModel.answerCall()
            },
            modifier = Modifier
                .size(64.dp)
                .background(MaterialTheme.colorScheme.primary)
        ) {
            Icon(
                imageVector = Icons.Default.Call,
                contentDescription = "Answer Call",
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }

        IconButton(
            onClick = {
                viewModel.rejectCall()
//                onCallEnd()
            },
            modifier = Modifier
                .size(64.dp)
                .background(MaterialTheme.colorScheme.error)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Reject Call",
                tint = MaterialTheme.colorScheme.onError
            )
        }
    }
}

@Composable
fun CallOutgoing(viewModel: CallViewModel, onCallEnd: () -> Unit) {
    val address = viewModel.address

    Text(
        text = "Calling $address...",
        style = MaterialTheme.typography.headlineMedium,
        modifier = Modifier.padding(bottom = 24.dp)
    )

    IconButton(
        onClick = {
            viewModel.rejectCall()
//            onCallEnd()
        },
        modifier = Modifier
            .size(64.dp)
            .background(MaterialTheme.colorScheme.error)
    ) {
        Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "Cancel Call",
            tint = MaterialTheme.colorScheme.onError
        )
    }
}

@Composable
fun CallInProgress(viewModel: CallViewModel, onCallEnd: () -> Unit) {
    val address = viewModel.address
    val elapsedTime = viewModel.elapsedTime.collectAsState().value
    val formattedTime = viewModel.formatElapsedTime(elapsedTime)

    Text(
        text = "Call in Progress with $address",
        style = MaterialTheme.typography.headlineMedium,
        modifier = Modifier.padding(bottom = 16.dp)
    )

    Text(
        text = "Elapsed Time: $formattedTime",
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier.padding(bottom = 24.dp)
    )

    IconButton(
        onClick = {
            viewModel.rejectCall()
//            onCallEnd()
        },
        modifier = Modifier
            .size(64.dp)
            .background(MaterialTheme.colorScheme.error)
    ) {
        Icon(
            imageVector = Icons.Default.Call,
            contentDescription = "End Call",
            tint = MaterialTheme.colorScheme.onError
        )
    }
}
