@file:OptIn(ExperimentalMaterial3Api::class)

package network.beechat.kaonic.sampleapp.scan

import androidx.compose.foundation.clickable
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import network.beechat.kaonic.sampleapp.theme.Dark
import network.beechat.kaonic.sampleapp.view.MainTopBar


@Composable
fun ScanScreen(
    viewModel: ScanScreenViewModel, onOpenChat: (String) -> Unit,
    onOpenSettings: () -> Unit
) {
    val contacts = viewModel.contacts

    Scaffold(
        topBar = {
            MainTopBar(
                "KaonicSample",
                actions = {
                    IconButton(onClick = { onOpenSettings() }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = Color.White
                        )
                    }
                }
            )
        },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                LazyColumn {
                    items(contacts.size) { i ->
                        Text(
                            text = contacts[i],
                            style = MaterialTheme.typography.body1,
                            modifier = Modifier
                                .padding(vertical = 4.dp)
                                .clickable { onOpenChat(contacts[i]) },
                        )
                    }
                }

            }
        }
    )
}