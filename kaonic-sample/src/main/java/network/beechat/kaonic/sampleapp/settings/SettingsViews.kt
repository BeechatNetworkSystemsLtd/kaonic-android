package network.beechat.kaonic.sampleapp.settings

import android.text.Layout
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import network.beechat.kaonic.sampleapp.view.CustomRadioButton

@Composable
fun LabeledRow(label: String, child: @Composable () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(label, color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Spacer(modifier = Modifier.width(16.dp))
        Box(Modifier.weight(1f)) { child() }
    }
}

@Composable
fun CenteredLabel(text: String) {
    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Text(text, color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 18.sp)
    }
}

@Composable
fun RadioGroupSection(
    label: String,
    options: List<Enum<*>>,
    selected: Enum<*>,
    onSelected: (Enum<*>) -> Unit
) {
    Column {
        Text(label, color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.heightIn(max = 300.dp)
        ) {
            items(options) { option ->
                CustomRadioButton(
                    label = option.name,
                    value = option,
                    groupValue = selected,
                    onChanged = { onSelected(it as Enum<*>) }
                )
            }
        }
    }
}

@Composable
fun DropdownMenuBox(
    value: Int,
    onValueChange: (Int) -> Unit,
    options: List<Int>
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        Text(
            text = value.toString(),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = true }
                .padding(12.dp),
            color = Color.Black,
            textAlign = TextAlign.End
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach {
                DropdownMenuItem(onClick = {
                    onValueChange(it)
                    expanded = false
                }) {
                    Text(it.toString(), color = Color.Black)
                }
            }
        }
    }
}