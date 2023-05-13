package mainintro.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.onClick
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Token
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arkivanov.essenty.instancekeeper.getOrCreate
import common.ui.theme.SelectedBackground
import common.ui.theme.Spacing
import common.utils.AppBusiness
import localized
import org.koin.java.KoinJavaComponent

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun IntroScreen(
    modifier: Modifier = Modifier,
) {
    val lang by L10n.currentLanguage.collectAsState("lang".localized())
    LaunchedEffect(lang) {}

    val viewModel = AppBusiness.instanceKeeper.getOrCreate {
        val res: IntroViewModel by KoinJavaComponent.inject(IntroViewModel::class.java)
        res
    }
    val uiState by viewModel.uiState.collectAsState()
    Column(
        modifier = modifier.padding(horizontal = Spacing.s),
    ) {
        Column(
            modifier = Modifier.padding(top = Spacing.xs, start = Spacing.m),
        ) {
            Text(
                text = "app_intro_title".localized(),
                style = MaterialTheme.typography.h3,
                color = Color.White,
            )

            Spacer(Modifier.height(Spacing.s))

            if (uiState.projects.isEmpty()) {
                Text(
                    text = "app_intro_empty".localized(),
                    style = MaterialTheme.typography.h5.copy(lineHeight = 32.sp),
                    color = Color.White,
                )
            } else {
                Text(
                    text = "app_intro".localized(),
                    style = MaterialTheme.typography.h5,
                    color = Color.White,
                )
            }
        }

        Spacer(Modifier.height(Spacing.m))

        LazyColumn(
            modifier = Modifier.fillMaxWidth().padding(horizontal = Spacing.m),
            verticalArrangement = Arrangement.spacedBy(Spacing.s),
        ) {
            items(uiState.projects) { project ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(color = SelectedBackground, shape = RoundedCornerShape(4.dp))
                        .padding(Spacing.m).onClick {
                            viewModel.open(project)
                        },
                    horizontalArrangement = Arrangement.spacedBy(Spacing.m),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Default.Token,
                        contentDescription = null,
                        tint = Color.White,
                    )
                    Text(
                        text = project.name,
                        style = MaterialTheme.typography.body1,
                        color = Color.White,
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Icon(
                        modifier = Modifier.onClick {
                            viewModel.delete(project)
                        },
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        tint = Color.White,
                    )
                }
            }
        }
    }
}
