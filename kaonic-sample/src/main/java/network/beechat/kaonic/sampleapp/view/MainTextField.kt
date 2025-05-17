package network.beechat.kaonic.sampleapp.view

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType

@Composable
fun MainTextField(
    value: String,
    onValueChange: (String) -> Unit,
    hint: String = "",
    keyboardType: KeyboardType = KeyboardType.Text,
    suffix: @Composable (() -> Unit)? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(hint) },
        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = keyboardType),
        colors = TextFieldDefaults.outlinedTextFieldColors(
            textColor = Color.Black,
            placeholderColor = Color.LightGray,
            unfocusedBorderColor = Color.Gray,
            focusedBorderColor = Color.Black,
            cursorColor = Color.Black
        ),
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        trailingIcon = suffix
    )
}