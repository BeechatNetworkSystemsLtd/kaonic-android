package network.beechat.kaonic.sampleapp.view

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import network.beechat.kaonic.sampleapp.theme.Primary

@Composable
fun <T> CustomRadioButton(
    label: String,
    value: T,
    groupValue: T,
    onChanged: (T) -> Unit
) {
    val selected = value == groupValue
    OutlinedButton(
        onClick = { onChanged(value) },
        border = BorderStroke(1.dp, if (selected) Color.White else Color.Gray),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = if (selected) Primary else Color.Transparent,
            contentColor = Color.White
        ),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
    ) {
        Text(label, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal)
    }
}
