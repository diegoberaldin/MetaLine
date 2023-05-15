package projectcreate.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.onClick
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import common.ui.theme.Indigo800
import common.ui.theme.Purple800
import common.ui.theme.Spacing
import common.utils.lastPathSegment
import localized

@Composable
internal fun FilePairSelector(
    modifier: Modifier = Modifier,
    selectedSource: Int? = null,
    selectedTarget: Int? = null,
    sourceFiles: List<String> = emptyList(),
    targetFiles: List<String> = emptyList(),
    onAddSource: (() -> Unit)? = null,
    onAddTarget: (() -> Unit)? = null,
    onSelectSourceFile: ((Int) -> Unit)? = null,
    onSelectTargetFile: ((Int) -> Unit)? = null,
    onMoveUpSource: (() -> Unit)? = null,
    onMoveDownSource: (() -> Unit)? = null,
    onMoveUpTarget: (() -> Unit)? = null,
    onMoveDownTarget: (() -> Unit)? = null,
    onDeleteSource: (() -> Unit)? = null,
    onDeleteTarget: (() -> Unit)? = null,
) {
    Row(
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier.weight(1f),
        ) {
            FileToolbar(
                modifier = Modifier.fillMaxWidth(),
                onAdd = onAddSource,
                onMoveUp = onMoveUpSource,
                onMoveDown = onMoveDownSource,
                onDelete = onDeleteSource,
            )
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(Spacing.s),
            ) {
                if (sourceFiles.isEmpty()) {
                    item {
                        Text(
                            modifier = Modifier.padding(horizontal = Spacing.xs),
                            text = "file_list_placeholder".localized(),
                            style = MaterialTheme.typography.caption,
                            color = MaterialTheme.colors.onBackground,
                        )
                    }
                }
                itemsIndexed(sourceFiles) { idx, path ->
                    FileCard(
                        path = path,
                        color = Purple800,
                        isSelected = idx == selectedSource,
                        onClick = {
                            onSelectSourceFile?.invoke(idx)
                        },
                    )
                }
            }
        }
        Spacer(modifier = Modifier.width(Spacing.m))
        Column(
            modifier = Modifier.weight(1f),
        ) {
            FileToolbar(
                modifier = Modifier.fillMaxWidth(),
                onAdd = onAddTarget,
                onMoveUp = onMoveUpTarget,
                onMoveDown = onMoveDownTarget,
                onDelete = onDeleteTarget,
            )
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(Spacing.s),
            ) {
                if (targetFiles.isEmpty()) {
                    item {
                        Text(
                            modifier = Modifier.padding(horizontal = Spacing.xs),
                            text = "file_list_placeholder".localized(),
                            style = MaterialTheme.typography.caption,
                            color = MaterialTheme.colors.onBackground,
                        )
                    }
                }
                itemsIndexed(targetFiles) { idx, path ->
                    FileCard(
                        path = path,
                        color = Indigo800,
                        isSelected = idx == selectedTarget,
                        onClick = {
                            onSelectTargetFile?.invoke(idx)
                        },
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FileCard(
    path: String,
    color: Color,
    isSelected: Boolean = false,
    onClick: (() -> Unit)? = null,
) {
    val name = path.lastPathSegment()
    Box(
        modifier = Modifier.fillMaxWidth().background(color = color, shape = RoundedCornerShape(4.dp)).run {
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
        }.onClick {
            onClick?.invoke()
        }.padding(Spacing.s),
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.caption,
            color = Color.White,
        )
    }
}
