package align.ui

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.ScrollableDefaults
import androidx.compose.foundation.gestures.rememberScrollableState
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import com.arkivanov.essenty.instancekeeper.getOrCreate
import common.ui.theme.Indigo800
import common.ui.theme.Purple800
import common.ui.theme.Spacing
import common.utils.AppBusiness
import kotlinx.coroutines.launch
import org.koin.java.KoinJavaComponent

@Composable
fun AlignScreen() {
    val viewModel = AppBusiness.instanceKeeper.getOrCreate {
        val res: AlignViewModel by KoinJavaComponent.inject(AlignViewModel::class.java)
        res
    }
    val uiState by viewModel.uiState.collectAsState()
    val editUiState by viewModel.editUiState.collectAsState()

    val stateSource = rememberLazyListState()
    val stateTarget = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollableState { delta ->
        scope.launch {
            stateSource.scrollBy(-delta)
            stateTarget.scrollBy(-delta)
        }
        delta
    }

    Row(
        modifier = Modifier.fillMaxSize().scrollable(
            state = scrollState,
            orientation = Orientation.Vertical,
            flingBehavior = ScrollableDefaults.flingBehavior(),
        ),
    ) {
        SegmentList(
            modifier = Modifier.weight(1f),
            state = stateSource,
            segments = uiState.sourceSegments,
            segmentColor = Purple800,
            selectedId = uiState.selectedSourceIndex,
            isEditing = editUiState.isEditing,
            onItemSelected = { id ->
                viewModel.selectSourceSegment(id)
            },
            onItemEditTriggered = { id ->
                viewModel.selectSourceSegment(id)
                viewModel.toggleEditing()
            },
            onItemTextChanged = { id, text, position ->
                viewModel.editSegment(id = id, value = text, position = position)
            },
        )

        Spacer(modifier = Modifier.width(Spacing.s))

        SegmentList(
            modifier = Modifier.weight(1f),
            state = stateTarget,
            segments = uiState.targetSegments,
            segmentColor = Indigo800,
            selectedId = uiState.selectedTargetIndex,
            isEditing = editUiState.isEditing,
            onItemSelected = { id ->
                viewModel.selectTargetSegment(id)
            },
            onItemEditTriggered = { id ->
                viewModel.selectTargetSegment(id)
                viewModel.toggleEditing()
            },
            onItemTextChanged = { id, text, position ->
                viewModel.editSegment(id = id, value = text, position = position)
            },
        )
    }
}
