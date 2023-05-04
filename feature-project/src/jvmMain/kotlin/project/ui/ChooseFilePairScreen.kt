package project.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.onClick
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ViewColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import common.ui.theme.SelectedBackground
import common.ui.theme.Spacing
import data.FilePairModel
import localized

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChooseFilePairScreen(
    modifier: Modifier = Modifier,
    filePairs: List<FilePairModel>,
    onOpenFilePair: ((Int) -> Unit)? = null,
) {
    Column(
        modifier = modifier.padding(horizontal = Spacing.xs),
    ) {
        Text(
            text = "message_project_empty".localized(),
            color = Color.White,
            style = MaterialTheme.typography.h6,
        )
        Spacer(modifier = Modifier.height(Spacing.m))
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(Spacing.s),
        ) {
            itemsIndexed(filePairs) { idx, item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(color = SelectedBackground, shape = RoundedCornerShape(4.dp))
                        .padding(Spacing.m)
                        .onClick {
                            onOpenFilePair?.invoke(idx)
                        },
                    horizontalArrangement = Arrangement.spacedBy(Spacing.m),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Default.ViewColumn,
                        contentDescription = null,
                        tint = Color.White,
                    )
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.body1,
                        color = Color.White,
                    )
                }
            }
        }
    }
}
