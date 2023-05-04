package project.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.onClick
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.ArrowCircleDown
import androidx.compose.material.icons.filled.ArrowCircleUp
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EditOff
import androidx.compose.material.icons.filled.JoinLeft
import androidx.compose.material.icons.filled.JoinRight
import androidx.compose.material.icons.filled.NavigateBefore
import androidx.compose.material.icons.filled.NavigateNext
import androidx.compose.material.icons.filled.Save
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import common.ui.components.CustomTooltipArea
import localized

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MainToolbar(
    isEditing: Boolean,
    needsSaving: Boolean,
    modifier: Modifier = Modifier,
    onSave: (() -> Unit)? = null,
    onMoveUp: (() -> Unit)? = null,
    onMoveDown: (() -> Unit)? = null,
    onMergePrevious: (() -> Unit)? = null,
    onMergeNext: (() -> Unit)? = null,
    onNewBefore: (() -> Unit)? = null,
    onNewAfter: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null,
    onEdit: (() -> Unit)? = null,
    onSplit: (() -> Unit)? = null,
) {
    Row(modifier = modifier) {
        val iconModifier = Modifier.size(28.dp).padding(2.dp)
        CustomTooltipArea(
            text = "tooltip_save".localized(),
        ) {
            Icon(
                modifier = iconModifier.padding(2.dp).onClick(
                    enabled = needsSaving,
                    onClick = { onSave?.invoke() },
                ),
                imageVector = Icons.Default.Save,
                contentDescription = null,
                tint = if (needsSaving) MaterialTheme.colors.primary else Color.Gray,
            )
        }
        CustomTooltipArea(
            text = "tooltip_move_up".localized(),
        ) {
            Icon(
                modifier = iconModifier.padding(2.dp).onClick { onMoveUp?.invoke() },
                imageVector = Icons.Default.ArrowCircleUp,
                contentDescription = null,
                tint = MaterialTheme.colors.primary,
            )
        }
        CustomTooltipArea(
            text = "tooltip_move_down".localized(),
        ) {
            Icon(
                modifier = iconModifier.padding(2.dp).onClick { onMoveDown?.invoke() },
                imageVector = Icons.Default.ArrowCircleDown,
                contentDescription = null,
                tint = MaterialTheme.colors.primary,
            )
        }
        CustomTooltipArea(
            text = "tooltip_merge_previous".localized(),
        ) {
            Icon(
                modifier = iconModifier.padding(1.dp).onClick { onMergePrevious?.invoke() },
                imageVector = Icons.Default.JoinLeft,
                contentDescription = null,
                tint = MaterialTheme.colors.primary,
            )
        }
        CustomTooltipArea(
            text = "tooltip_merge_next".localized(),
        ) {
            Icon(
                modifier = iconModifier.onClick { onMergeNext?.invoke() },
                imageVector = Icons.Default.JoinRight,
                contentDescription = null,
                tint = MaterialTheme.colors.primary,
            )
        }
        CustomTooltipArea(
            text = "tooltip_new_before".localized(),
        ) {
            Box(
                modifier = iconModifier.onClick { onNewBefore?.invoke() },
            ) {
                Icon(
                    modifier = Modifier.align(Alignment.CenterEnd).size(15.dp),
                    imageVector = Icons.Default.AddCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colors.primary,
                )
                Icon(
                    modifier = Modifier.align(Alignment.CenterStart).size(13.dp),
                    imageVector = Icons.Default.NavigateBefore,
                    contentDescription = null,
                    tint = MaterialTheme.colors.primary,
                )
            }
        }
        CustomTooltipArea(
            text = "tooltip_new_after".localized(),
        ) {
            Box(
                modifier = iconModifier.onClick { onNewAfter?.invoke() },
            ) {
                Icon(
                    modifier = Modifier.align(Alignment.CenterStart).size(15.dp),
                    imageVector = Icons.Default.AddCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colors.primary,
                )
                Icon(
                    modifier = Modifier.align(Alignment.CenterEnd).size(13.dp),
                    imageVector = Icons.Default.NavigateNext,
                    contentDescription = null,
                    tint = MaterialTheme.colors.primary,
                )
            }
        }
        CustomTooltipArea(
            text = "tooltip_delete".localized(),
        ) {
            Icon(
                modifier = iconModifier.padding(3.dp).onClick { onDelete?.invoke() },
                imageVector = Icons.Default.Delete,
                contentDescription = null,
                tint = MaterialTheme.colors.primary,
            )
        }
        CustomTooltipArea(
            text = if (isEditing) "tooltip_exit_edit".localized() else "tooltip_edit".localized(),
        ) {
            Icon(
                modifier = iconModifier.padding(3.dp).onClick { onEdit?.invoke() },
                imageVector = if (isEditing) Icons.Default.EditOff else Icons.Default.Edit,
                contentDescription = null,
                tint = MaterialTheme.colors.primary,
            )
        }
        CustomTooltipArea(
            text = "tooltip_split".localized(),
        ) {
            Icon(
                modifier = iconModifier.padding(3.dp).onClick { onSplit?.invoke() },
                imageVector = Icons.Default.ContentCut,
                contentDescription = null,
                tint = MaterialTheme.colors.primary,
            )
        }
    }
}
