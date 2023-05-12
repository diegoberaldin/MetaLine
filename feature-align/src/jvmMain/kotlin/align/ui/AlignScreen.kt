package align.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.arkivanov.essenty.instancekeeper.getOrCreate
import common.ui.theme.Indigo800
import common.ui.theme.Purple800
import common.ui.theme.Spacing
import common.utils.AppBusiness
import data.SegmentModel
import org.koin.java.KoinJavaComponent

@Composable
fun AlignScreen() {
    val viewModel = AppBusiness.instanceKeeper.getOrCreate {
        val res: AlignViewModel by KoinJavaComponent.inject(AlignViewModel::class.java)
        res
    }
    val uiState by viewModel.uiState.collectAsState()
    val editUiState by viewModel.editUiState.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(Spacing.s)
    ) {
        val lines = uiState.sourceSegments.zip(uiState.targetSegments)
            .map { pair -> AlignmentLine(source = pair.first, target = pair.second) }
        items(lines) { line ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(Spacing.s)
            ) {
                SegmentCard(
                    modifier = Modifier.weight(1f),
                    segment = line.source,
                    color = Indigo800,
                    isSelected = uiState.selectedSourceId == line.source.id,
                    isEditing = editUiState.isEditing,
                    onTextEdited = { text, position ->
                        viewModel.editSegment(line.source.id, text, position)
                    },
                    onClick = {
                        viewModel.selectSourceSegment(line.source.id)
                    },
                    onDoubleClick = {
                        viewModel.selectSourceSegment(line.source.id)
                        viewModel.toggleEditing()
                    }
                )
                SegmentCard(
                    modifier = Modifier.weight(1f),
                    segment = line.target,
                    color = Purple800,
                    isSelected = uiState.selectedTargetId == line.target.id,
                    isEditing = editUiState.isEditing,
                    onTextEdited = { text, position ->
                        viewModel.editSegment(line.target.id, text, position)
                    },
                    onClick = {
                        viewModel.selectTargetSegment(line.target.id)
                    },
                    onDoubleClick = {
                        viewModel.selectTargetSegment(line.target.id)
                        viewModel.toggleEditing()
                    }
                )
            }
        }
    }
}


data class AlignmentLine(
    val source: SegmentModel,
    val target: SegmentModel
)