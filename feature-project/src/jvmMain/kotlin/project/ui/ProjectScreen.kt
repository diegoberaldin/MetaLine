package project.ui

import align.ui.AlignScreen
import align.ui.AlignViewModel
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
import com.arkivanov.essenty.instancekeeper.getOrCreate
import common.ui.components.CustomTabBar
import common.ui.theme.SelectedBackground
import common.ui.theme.Spacing
import common.utils.AppBusiness
import data.FilePairModel
import data.ProjectModel
import org.koin.java.KoinJavaComponent.inject
import project.ui.components.MainToolbar

@Composable
fun ProjectScreen(
    project: ProjectModel,
    currentPairIdx: Int,
    openedFilePairs: List<FilePairModel>,
    modifier: Modifier = Modifier,
    onSelectFilePair: ((Int) -> Unit)? = null,
    onCloseFilePair: ((Int) -> Unit)? = null,
) {
    val alignViewModel = AppBusiness.instanceKeeper.getOrCreate {
        val res: AlignViewModel by inject(AlignViewModel::class.java)
        res
    }
    val alignUiState by alignViewModel.editUiState.collectAsState()

    Column(
        modifier = modifier,
    ) {
        MainToolbar(
            isEditing = alignUiState.isEditing,
            needsSaving = alignUiState.needsSaving,
            modifier = Modifier.fillMaxWidth(),
            onSave = { alignViewModel.save() },
            onMoveUp = { alignViewModel.moveSegmentUp() },
            onMoveDown = { alignViewModel.moveSegmentDown() },
            onNewBefore = { alignViewModel.createSegmentBefore() },
            onNewAfter = { alignViewModel.createSegmentAfter() },
            onMergePrevious = { alignViewModel.mergeWithPreviousSegment() },
            onMergeNext = { alignViewModel.mergeWithNextSegment() },
            onDelete = { alignViewModel.deleteSegment() },
            onEdit = { alignViewModel.toggleEditing() },
            onSplit = { alignViewModel.splitSegment() },
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
                        topStart = 0.dp,
                        topEnd = 4.dp,
                        bottomStart = 4.dp,
                        bottomEnd = 4.dp,
                    ),
                )
                .padding(Spacing.m),
        ) {
            val current = openedFilePairs.getOrNull(currentPairIdx)
            LaunchedEffect(current) {
                alignViewModel.load(pair = current, project = project)
            }
            AlignScreen()
        }
    }
}
