package align.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.onClick
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import data.SegmentModel

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun SegmentCard(
    segment: SegmentModel,
    color: Color,
    isSelected: Boolean = false,
    isEditing: Boolean = false,
    onClick: (() -> Unit)? = null,
    onDoubleClick: (() -> Unit)? = null,
    onTextEdited: ((text: String, position: Int) -> Unit)? = null,
) {
    var value by remember(segment.id, isEditing) {
        mutableStateOf(TextFieldValue(text = segment.text, selection = TextRange(segment.text.length)))
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = color, shape = RoundedCornerShape(4.dp)).run {
                if (!isSelected) {
                    border(
                        width = 1.dp,
                        color = color,
                        shape = RoundedCornerShape(4.dp),
                    )
                } else {
                    border(
                        width = 1.dp,
                        color = Color.White,
                        shape = RoundedCornerShape(4.dp),
                    )
                }
            }.onClick(
                onClick = { onClick?.invoke() },
                onDoubleClick = { onDoubleClick?.invoke() },
            ).padding(Spacing.s),
    ) {
        val focusRequester = FocusRequester()
        LaunchedEffect(isSelected && isEditing) {
            if (isEditing) {
                focusRequester.requestFocus()
            }
        }

        Box(modifier = Modifier.fillMaxWidth()) {
            Text(
                modifier = Modifier.alpha(if (isSelected && isEditing) 0f else 1f),
                text = segment.text,
                style = MaterialTheme.typography.caption,
                color = Color.White,
            )
            if (isSelected && isEditing) {
                BasicTextField(
                    modifier = Modifier.matchParentSize().focusRequester(focusRequester),
                    textStyle = MaterialTheme.typography.caption.copy(color = Color.White),
                    cursorBrush = SolidColor(Color.White),
                    value = value,
                    onValueChange = {
                        value = it
                        onTextEdited?.invoke(it.text, it.selection.end)
                    },
                )
            }
        }
    }
}
