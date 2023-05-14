package projectedit.ui.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.rememberWindowState
import com.arkivanov.essenty.instancekeeper.getOrCreate
import common.ui.components.CustomTabBar
import common.ui.theme.MetaLineTheme
import common.ui.theme.SelectedBackground
import common.ui.theme.Spacing
import common.utils.AppBusiness
import data.ProjectModel
import kotlinx.coroutines.launch
import localized
import org.koin.java.KoinJavaComponent.inject
import projectmedatada.ui.ProjectMetadataScreen
import projectmedatada.ui.ProjectMetadataViewModel

@Composable
fun EditProjectDialog(
    project: ProjectModel? = null,
    onClose: ((ProjectModel?) -> Unit)? = null,
) {
    val viewModel: EditProjectViewModel = AppBusiness.instanceKeeper.getOrCreate {
        val res: EditProjectViewModel by inject(EditProjectViewModel::class.java)
        res
    }
    val metadataViewModel: ProjectMetadataViewModel = AppBusiness.instanceKeeper.getOrCreate {
        val res: ProjectMetadataViewModel by inject(ProjectMetadataViewModel::class.java)
        res
    }
    LaunchedEffect(project) {
        metadataViewModel.load(project = project)
    }
    LaunchedEffect(viewModel) {
        launch {
            metadataViewModel.onDone.collect {
                onClose?.invoke(null)
            }
        }
    }
    val uiState by viewModel.uiState.collectAsState()

    MetaLineTheme {
        Window(
            title = "dialog_title_edit_project".localized(),
            state = rememberWindowState(width = Dp.Unspecified, height = Dp.Unspecified),
            resizable = false,
            onCloseRequest = {
                onClose?.invoke(null)
            },
        ) {
            Column(
                modifier = Modifier.size(800.dp, 600.dp).background(MaterialTheme.colors.background)
                    .padding(horizontal = Spacing.s),
            ) {
                Spacer(modifier = Modifier.height(Spacing.s))

                val tabs = uiState.tabs
                val tabIndex = tabs.indexOf(uiState.currentTab)
                CustomTabBar(
                    modifier = Modifier.fillMaxWidth(),
                    tabs = tabs.map { it.toReadableName() },
                    current = tabIndex,
                    onTabSelected = {
                        viewModel.selectTab(tabs[it])
                    },
                )
                val bottomModifier = Modifier.fillMaxWidth()
                    .weight(1f)
                    .background(
                        color = SelectedBackground,
                        shape = when (tabIndex) {
                            0 -> RoundedCornerShape(topEnd = 4.dp, bottomStart = 4.dp, bottomEnd = 4.dp)
                            else -> RoundedCornerShape(4.dp)
                        },
                    ).padding(horizontal = Spacing.s)
                when (uiState.currentTab) {
                    EditProjectSection.SEGMENTATION_RULES -> ProjectMetadataScreen(modifier = bottomModifier) // TODO
                    else -> ProjectMetadataScreen(modifier = bottomModifier)
                }
                Row(
                    modifier = Modifier.padding(Spacing.s),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
                ) {
                    Spacer(modifier = Modifier.weight(1f))
                    Button(
                        modifier = Modifier.heightIn(max = 25.dp),
                        contentPadding = PaddingValues(0.dp),
                        onClick = {
                            onClose?.invoke(null)
                        },
                    ) {
                        Text(text = "button_cancel".localized(), style = MaterialTheme.typography.button)
                    }
                    Button(
                        modifier = Modifier.heightIn(max = 25.dp),
                        contentPadding = PaddingValues(0.dp),
                        onClick = {
                            metadataViewModel.submit()
                        },
                    ) {
                        Text(text = "button_ok".localized(), style = MaterialTheme.typography.button)
                    }
                }
            }
        }
    }
}
