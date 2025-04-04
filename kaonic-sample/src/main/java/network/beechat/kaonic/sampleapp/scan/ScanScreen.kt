package network.beechat.kaonic.sampleapp.scan

import androidx.compose.foundation.clickable
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import network.beechat.kaonic.sampleapp.theme.Light
import kotlin.random.Random


@Composable
fun ScanScreen(viewModel: ScanScreenViewModel, onNavigate: (String) -> Unit) {

    val nodes = viewModel.nodes

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Button(
            onClick = {
                val randomItem = "Item ${Random.nextInt(1000)}"
                viewModel.addNode(randomItem)
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Add Random String")
        }

        Spacer(modifier = Modifier.height(16.dp))
        LazyColumn {
            items(nodes.size) { i ->
                Text(
                    text = nodes[i],
                    style = MaterialTheme.typography.body1,
                    modifier = Modifier
                        .padding(vertical = 4.dp)
                        .clickable { onNavigate(nodes[i]) },
                )
            }
        }
    }
}