package projectmedatada.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.onClick
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.ArrowCircleDown
import androidx.compose.material.icons.filled.ArrowCircleUp
import androidx.compose.material.icons.filled.RemoveCircleOutline
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import common.ui.components.CustomTooltipArea
import localized

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun FileToolbar(
    modifier: Modifier = Modifier,
    onAdd: (() -> Unit)? = null,
    onMoveUp: (() -> Unit)? = null,
    onMoveDown: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null,
) {
    Row(modifier = modifier) {
        val iconModifier = Modifier.size(24.dp).padding(2.dp)
        Spacer(modifier = Modifier.weight(1f))

        CustomTooltipArea(
            text = "tooltip_add".localized(),
        ) {
            Icon(
                modifier = iconModifier.padding(2.dp).onClick { onAdd?.invoke() },
                imageVector = Icons.Default.AddCircleOutline,
                contentDescription = null,
                tint = MaterialTheme.colors.primary,
            )
        }
        CustomTooltipArea(
            text = "tooltip_remove".localized(),
        ) {
            Icon(
                modifier = iconModifier.padding(2.dp).onClick { onDelete?.invoke() },
                imageVector = Icons.Default.RemoveCircleOutline,
                contentDescription = null,
                tint = MaterialTheme.colors.primary,
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
    }
}