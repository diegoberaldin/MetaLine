package project.ui

import align.ui.AlignComponent
import align.ui.AlignScreen
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import common.ui.components.CustomTabBar
import common.ui.theme.SelectedBackground
import common.ui.theme.Spacing
import data.FilePairModel
import data.ProjectModel
import project.ui.components.MainToolbar

@Composable
fun ProjectScreen(
    alignComponent: AlignComponent,
    project: ProjectModel,
    currentPairIdx: Int,
    openedFilePairs: List<FilePairModel>,
    modifier: Modifier = Modifier,
    onSelectFilePair: ((Int) -> Unit)? = null,
    onCloseFilePair: ((Int) -> Unit)? = null,
) {
    val alignUiState by alignComponent.editUiState.collectAsState()

    Column(
        modifier = modifier,
    ) {
        MainToolbar(
            isEditing = alignUiState.isEditing,
            needsSaving = alignUiState.needsSaving,
            modifier = Modifier.fillMaxWidth(),
            onSave = { alignComponent.save() },
            onMoveUp = { alignComponent.moveSegmentUp() },
            onMoveDown = { alignComponent.moveSegmentDown() },
            onNewBefore = { alignComponent.createSegmentBefore() },
            onNewAfter = { alignComponent.createSegmentAfter() },
            onMergePrevious = { alignComponent.mergeWithPreviousSegment() },
            onMergeNext = { alignComponent.mergeWithNextSegment() },
            onDelete = { alignComponent.deleteSegment() },
            onEdit = { alignComponent.toggleEditing() },
            onSplit = { alignComponent.splitSegment() },
        )

        Spacer(modifier = Modifier.height(Spacing.xs))

        CustomTabBar(
            tabs = openedFilePairs.map { it.name },
            current = currentPairIdx,
            rightIcon = Icons.Default.Close,
            onRightIconClicked = {
                onCloseFilePair?.invoke(it)
            },
            onTabSelected = {
                onSelectFilePair?.invoke(it)
            },
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(
                    color = SelectedBackground,
                    shape = RoundedCornerShape(
                        topStart = if (currentPairIdx == 0) 0.dp else 4.dp,
                        topEnd = 4.dp,
                        bottomStart = 4.dp,
                        bottomEnd = 4.dp,
                    ),
                )
                .padding(Spacing.m),
        ) {
            val current = openedFilePairs.getOrNull(currentPairIdx)
            LaunchedEffect(current) {
                alignComponent.load(pair = current, project = project)
            }
            AlignScreen(
                component = alignComponent
            )
        }
    }
}
