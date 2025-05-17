package network.beechat.kaonic.sampleapp.view

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import network.beechat.kaonic.sampleapp.theme.Primary

@Composable
fun MainButton(
    label: String,
    onPressed: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onPressed,
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Primary,
            contentColor = Color.White
        )
    ) {
        Text(text = label, fontWeight = FontWeight.Bold, fontSize = 18.sp)
    }
}