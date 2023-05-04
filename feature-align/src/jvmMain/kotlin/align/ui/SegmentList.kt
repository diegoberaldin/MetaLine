package align.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import common.ui.theme.Spacing
import data.SegmentModel

@Composable
internal fun SegmentList(
    segments: List<SegmentModel>,
    segmentColor: Color,
    modifier: Modifier = Modifier,
    state: LazyListState = rememberLazyListState(),
    selectedId: Int? = null,
    isEditing: Boolean = false,
    onItemSelected: ((Int) -> Unit)? = null,
    onItemEditTriggered: ((Int) -> Unit)? = null,
    onItemTextChanged: ((id: Int, value: String, cursorPosition: Int) -> Unit)? = null,
) {
    LazyColumn(
        modifier = modifier,
        state = state,
        verticalArrangement = Arrangement.spacedBy(Spacing.s),
    ) {
        itemsIndexed(segments, key = { _, it -> it.id }) { _, item ->
            SegmentCard(
                segment = item,
                isEditing = isEditing,
                color = segmentColor,
                isSelected = item.id == selectedId,
                onClick = {
                    onItemSelected?.invoke(item.id)
                },
                onDoubleClick = {
                    onItemEditTriggered?.invoke(item.id)
                },
                onTextEdited = { text, position ->
                    onItemTextChanged?.invoke(item.id, text, position)
                },
            )
        }
    }
}
