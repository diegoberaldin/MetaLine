package align.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import common.ui.theme.Indigo800
import common.ui.theme.Purple800
import common.ui.theme.Spacing
import data.SegmentModel

@Composable
fun AlignScreen(
    component: AlignComponent,
) {
    val uiState by component.uiState.collectAsState()
    val editUiState by component.editUiState.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(Spacing.s),
    ) {
        val lines = uiState.sourceSegments.zip(uiState.targetSegments)
            .map { pair -> AlignmentLine(source = pair.first, target = pair.second) }
        items(lines) { line ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(Spacing.s),
            ) {
                SegmentCard(
                    modifier = Modifier.weight(1f),
                    segment = line.source,
                    color = Indigo800,
                    isSelected = uiState.selectedSourceId == line.source.id,
                    isEditing = editUiState.isEditing,
                    onTextEdited = { text, position ->
                        component.editSegment(line.source.id, text, position)
                    },
                    onClick = {
                        component.selectSourceSegment(line.source.id)
                    },
                    onDoubleClick = {
                        component.selectSourceSegment(line.source.id)
                        component.toggleEditing()
                    },
                )
                SegmentCard(
                    modifier = Modifier.weight(1f),
                    segment = line.target,
                    color = Purple800,
                    isSelected = uiState.selectedTargetId == line.target.id,
                    isEditing = editUiState.isEditing,
                    onTextEdited = { text, position ->
                        component.editSegment(line.target.id, text, position)
                    },
                    onClick = {
                        component.selectTargetSegment(line.target.id)
                    },
                    onDoubleClick = {
                        component.selectTargetSegment(line.target.id)
                        component.toggleEditing()
                    },
                )
            }
        }
    }
}

data class AlignmentLine(
    val source: SegmentModel,
    val target: SegmentModel,
)
