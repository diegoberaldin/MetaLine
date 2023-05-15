package projectsegmentation.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.onClick
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import common.ui.theme.Spacing

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun SegmentationRuleField(
    pattern: String,
    color: Color,
    modifier: Modifier = Modifier,
    isEditing: Boolean = false,
    onTextEdited: ((text: String) -> Unit)? = null,
) {
    var value by remember {
        mutableStateOf(TextFieldValue(text = pattern, selection = TextRange(pattern.length)))
    }
    val focusRequester = remember {
        FocusRequester()
    }
    Box(
        modifier = modifier
            .background(color = color, shape = RoundedCornerShape(4.dp))
            .onClick {
                if (isEditing) {
                    runCatching {
                        focusRequester.requestFocus()
                    }
                }
            }.padding(Spacing.s),
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Text(
                modifier = Modifier.alpha(if (isEditing) 0f else 1f),
                text = pattern,
                style = MaterialTheme.typography.caption,
                color = Color.White,
            )
            if (isEditing) {
                BasicTextField(
                    modifier = Modifier.matchParentSize().focusRequester(focusRequester),
                    textStyle = MaterialTheme.typography.caption.copy(color = Color.White),
                    cursorBrush = SolidColor(Color.White),
                    value = value,
                    onValueChange = {
                        value = it
                        onTextEdited?.invoke(it.text)
                    },
                )
            }
        }
    }
}
