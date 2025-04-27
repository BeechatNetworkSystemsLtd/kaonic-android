package network.beechat.kaonic.sampleapp.scan

import androidx.compose.foundation.clickable
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import network.beechat.kaonic.sampleapp.services.KaonicService


@Composable
fun ScanScreen(viewModel: ScanScreenViewModel, onNavigate: (String) -> Unit) {

    val contacts = viewModel.contacts

    LaunchedEffect(Unit) {
        /// remove this once implement contact found
        KaonicService.emitNodeFound()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        LazyColumn {
            items(contacts.size) { i ->
                Text(
                    text = contacts[i],
                    style = MaterialTheme.typography.body1,
                    modifier = Modifier
                        .padding(vertical = 4.dp)
                        .clickable { onNavigate(contacts[i]) },
                )
            }
        }
    }
}