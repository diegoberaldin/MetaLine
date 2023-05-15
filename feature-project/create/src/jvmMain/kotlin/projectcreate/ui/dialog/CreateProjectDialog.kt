package projectcreate.ui.dialog

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
import common.ui.theme.MetaLineTheme
import common.ui.theme.Spacing
import common.utils.AppBusiness
import data.ProjectModel
import kotlinx.coroutines.launch
import localized
import org.koin.java.KoinJavaComponent
import org.koin.java.KoinJavaComponent.inject
import projectmetadata.ui.ProjectMetadataScreen
import projectmetadata.ui.ProjectMetadataViewModel
import projectsegmentation.ui.ProjectSegmentationScreen

@Composable
fun CreateProjectDialog(
    onClose: ((ProjectModel?) -> Unit)? = null,
) {
    val viewModel: CreateProjectViewModel = AppBusiness.instanceKeeper.getOrCreate {
        val res: CreateProjectViewModel by inject(CreateProjectViewModel::class.java)
        res
    }
    val metadataViewModel: ProjectMetadataViewModel = AppBusiness.instanceKeeper.getOrCreate {
        val res: ProjectMetadataViewModel by KoinJavaComponent.inject(ProjectMetadataViewModel::class.java)
        res
    }
    LaunchedEffect(viewModel) {
        launch {
            metadataViewModel.onDone.collect {
                viewModel.setProject(it)
                viewModel.next()
            }
        }
    }
    val uiState by viewModel.uiState.collectAsState()

    MetaLineTheme {
        Window(
            title = "dialog_title_create_project".localized(),
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
                val contentModifier = Modifier.fillMaxWidth().weight(1f).padding(Spacing.xs)
                when (uiState.step) {
                    0 -> {
                        ProjectMetadataScreen(
                            modifier = contentModifier,
                        )
                    }

                    1 -> {
                        ProjectSegmentationScreen(
                            modifier = contentModifier,
                            project = uiState.project,
                        )
                    }
                }
                Spacer(modifier = Modifier.height(Spacing.s))
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
                            when (uiState.step) {
                                0 -> {
                                    metadataViewModel.submit()
                                }

                                1 -> {
                                    onClose?.invoke(uiState.project)
                                }
                            }
                        },
                    ) {
                        Text(text = "button_ok".localized(), style = MaterialTheme.typography.button)
                    }
                }
            }
        }
    }
}
