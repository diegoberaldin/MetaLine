package project.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.PointerMatcher
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.onClick
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pages
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerButton
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import common.ui.components.CustomTooltipArea
import common.ui.theme.SelectedBackground
import common.ui.theme.Spacing
import data.FilePairModel
import localized
import org.jetbrains.skiko.Cursor

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SideBar(
    modifier: Modifier = Modifier,
    filePairs: List<FilePairModel> = emptyList(),
    onOpenFilePair: ((Int) -> Unit)? = null,
) {
    var expanded by remember {
        mutableStateOf(true)
    }
    Box(modifier) {
        var expandableWidth by remember {
            mutableStateOf(200.dp)
        }
        if (expanded) {
            Row(
                modifier = Modifier.fillMaxHeight()
                    .width(expandableWidth)
                    .padding(top = Spacing.xs, start = 30.dp),
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(Spacing.s),
                ) {
                    Text(
                        modifier = Modifier.padding(start = Spacing.s),
                        text = "sidebar_title_project_files".localized(),
                        style = MaterialTheme.typography.body2.copy(fontSize = 13.sp),
                        color = MaterialTheme.colors.onBackground,
                        maxLines = 1,
                    )
                    LazyColumn(
                        modifier = Modifier.fillMaxHeight(),
                        verticalArrangement = Arrangement.spacedBy(Spacing.xs),
                    ) {
                        itemsIndexed(filePairs) { idx, item ->
                            Row(
                                modifier = Modifier.padding(vertical = Spacing.xs, horizontal = Spacing.s)
                                    .onClick(
                                        matcher = PointerMatcher.mouse(PointerButton.Primary),
                                        onClick = {},
                                        onDoubleClick = {
                                            onOpenFilePair?.invoke(idx)
                                        },
                                    ),
                            ) {
                                Text(
                                    text = item.name,
                                    style = MaterialTheme.typography.caption,
                                    color = MaterialTheme.colors.onBackground,
                                    maxLines = 1,
                                )
                            }
                        }
                    }
                }
                val density = LocalDensity.current
                val draggableState = rememberDraggableState {
                    val newHeight = expandableWidth + (it / density.density).dp
                    expandableWidth = newHeight.coerceAtLeast(36.dp)
                }
                Box(
                    modifier = Modifier
                        .pointerHoverIcon(PointerIcon(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR)))
                        .width(4.dp)
                        .fillMaxHeight()
                        .draggable(
                            state = draggableState,
                            orientation = Orientation.Horizontal,
                        )
                        .padding(start = 1.dp, end = 2.dp)
                        .border(width = 1.dp, color = SelectedBackground),
                )
            }
        }
        Column(
            modifier = Modifier.fillMaxHeight()
                .background(MaterialTheme.colors.background)
                .padding(start = Spacing.s),
        ) {
            val iconModifier = Modifier.size(26.dp).padding(1.dp)
            CustomTooltipArea(
                text = "tooltip_project_files".localized(),
            ) {
                Icon(
                    modifier = iconModifier.padding(2.dp).onClick {
                        expanded = !expanded
                    },
                    imageVector = Icons.Default.Pages,
                    contentDescription = null,
                    tint = MaterialTheme.colors.primary,
                )
            }
        }
    }
}
