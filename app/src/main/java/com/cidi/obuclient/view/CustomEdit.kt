package com.cidi.obuclient.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cidi.obuclient.R
import com.cidi.obuclient.ui.theme.title_color

/**
 *Created by CIDI zhengxuan on 2023/12/11
 *QQ:1309873105
 */

@Composable
fun CustomEdit(
    text: String = "",
    onValueChange:(String) -> Unit,
    onSave:() -> Unit,
    modifier: Modifier,
    hint: String = "请输入",
    enabled: Boolean = true,
    textStyle: TextStyle,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    cursorBrush: Brush = SolidColor(Color.Gray)
){
    var hasFocus by remember { mutableStateOf(false) }
    BasicTextField(
        value = text,
        onValueChange = onValueChange,
        modifier = modifier,
        textStyle = textStyle,
        singleLine = true,
        enabled = enabled,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        visualTransformation = visualTransformation,
        cursorBrush = cursorBrush,
        decorationBox = @Composable { innerTextField ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier
                    .fillMaxWidth()
                    .weight(1.0f, true)
                    .background(color = Color.White)){
                    // 当空字符时, 显示hint
                    if(text.isEmpty()) {
                        Text(
                            modifier = Modifier.padding(start = 5.dp),
                            text = hint,
                            color = Color.LightGray
                        )
                    }
                    // 原本输入框的内容
                    innerTextField()
                }
                if(text.isNotEmpty()){
                    Image(painter = painterResource(id = R.drawable.right),
                        contentDescription = "",
                        modifier = Modifier.size(40.dp).clickable { onSave() })
                }

            }
        }
    )
}