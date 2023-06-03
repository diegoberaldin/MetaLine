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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.rememberWindowState
import com.arkivanov.decompose.extensions.compose.jetbrains.subscribeAsState
import common.ui.theme.MetaLineTheme
import common.ui.theme.Spacing
import data.ProjectModel
import localized
import projectmetadata.ui.ProjectMetadataComponent
import projectmetadata.ui.ProjectMetadataScreen
import projectsegmentation.ui.ProjectSegmentationComponent
import projectsegmentation.ui.ProjectSegmentationScreen

@Composable
fun CreateProjectDialog(
    component: CreateProjectComponent,
    onClose: ((ProjectModel?) -> Unit)? = null,
) {
    val uiState by component.uiState.collectAsState()

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
                val content by component.content.subscribeAsState()
                when (content.child?.configuration) {
                    CreateProjectComponent.Config.Metadata -> {
                        ProjectMetadataScreen(
                            modifier = contentModifier,
                            component = content.child?.instance as ProjectMetadataComponent,
                        )
                    }

                    CreateProjectComponent.Config.SegmentationRules -> {
                        ProjectSegmentationScreen(
                            component = content.child?.instance as ProjectSegmentationComponent,
                            modifier = contentModifier,
                            project = uiState.project,
                        )
                    }

                    else -> Unit
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
                            when (content.child?.configuration) {
                                CreateProjectComponent.Config.Metadata -> {
                                    component.submitMetadata()
                                }

                                CreateProjectComponent.Config.SegmentationRules -> {
                                    onClose?.invoke(uiState.project)
                                }

                                else -> Unit
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
