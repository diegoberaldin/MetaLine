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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.rememberWindowState
import com.arkivanov.essenty.instancekeeper.getOrCreate
import common.ui.components.CustomOpenFileDialog
import common.ui.components.CustomTextField
import common.ui.theme.MetaLineTheme
import common.ui.theme.Spacing
import common.utils.AppBusiness
import data.ProjectModel
import kotlinx.coroutines.launch
import localized
import org.koin.java.KoinJavaComponent.inject
import projectcreate.ui.components.FilePairSelector
import projectcreate.ui.components.LanguagesSelector

@Composable
fun CreateProjectDialog(
    project: ProjectModel? = null,
    onClose: ((ProjectModel?) -> Unit)? = null,
) {
    val viewModel: CreateProjectViewModel = AppBusiness.instanceKeeper.getOrCreate {
        val res: CreateProjectViewModel by inject(CreateProjectViewModel::class.java)
        res
    }

    LaunchedEffect(project) {
        viewModel.load(project)
    }
    LaunchedEffect(viewModel) {
        launch {
            viewModel.onDone.collect {
                onClose?.invoke(it)
            }
        }
    }
    val uiState by viewModel.uiState.collectAsState()
    val errorState by viewModel.errorUiState.collectAsState()
    val languageState by viewModel.languagesUiState.collectAsState()
    val fileState by viewModel.fileUiState.collectAsState()
    var pickSourceDialogOpen by remember {
        mutableStateOf(false)
    }
    var pickTargetDialogOpen by remember {
        mutableStateOf(false)
    }

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
                modifier = Modifier.size(800.dp, 600.dp)
                    .background(MaterialTheme.colors.background)
                    .padding(horizontal = Spacing.s),
            ) {
                Spacer(modifier = Modifier.height(Spacing.s))
                CustomTextField(
                    modifier = Modifier.height(48.dp),
                    label = "create_project_name".localized(),
                    value = uiState.name,
                    singleLine = true,
                    onValueChange = {
                        viewModel.setName(it)
                    },
                )
                Text(
                    modifier = Modifier.padding(horizontal = Spacing.xs),
                    text = errorState.nameError,
                    style = MaterialTheme.typography.caption,
                    color = MaterialTheme.colors.error,
                )

                Spacer(modifier = Modifier.height(Spacing.xs))

                // languages
                LanguagesSelector(
                    modifier = Modifier.fillMaxWidth(),
                    availableSourceLanguages = languageState.availableSourceLanguages,
                    availableTargetLanguages = languageState.availableTargetLanguages,
                    sourceLanguage = languageState.sourceLanguage,
                    targetLanguage = languageState.targetLanguage,
                    onSourceSelected = {
                        viewModel.setSourceLanguage(it)
                    },
                    onTargetSelected = {
                        viewModel.setTargetLanguage(it)
                    },
                )
                Text(
                    modifier = Modifier.padding(horizontal = Spacing.xs),
                    text = errorState.languagesError,
                    style = MaterialTheme.typography.caption,
                    color = MaterialTheme.colors.error,
                )

                Spacer(modifier = Modifier.height(Spacing.xs))

                // files
                Text(
                    modifier = Modifier.padding(horizontal = Spacing.xs),
                    text = "create_project_file_pairs".localized(),
                    style = MaterialTheme.typography.caption,
                    color = Color.White,
                )
                Spacer(modifier = Modifier.height(Spacing.s))
                FilePairSelector(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    sourceFiles = fileState.sourceFiles,
                    targetFiles = fileState.targetFiles,
                    selectedSource = fileState.selectedSource,
                    selectedTarget = fileState.selectedTarget,
                    onSelectSourceFile = { viewModel.selectSourceFile(it) },
                    onSelectTargetFile = { viewModel.selectTargetFile(it) },
                    onAddSource = { pickSourceDialogOpen = true },
                    onAddTarget = { pickTargetDialogOpen = true },
                    onMoveUpSource = { viewModel.moveSourceUp() },
                    onMoveDownSource = { viewModel.moveSourceDown() },
                    onMoveUpTarget = { viewModel.moveTargetUp() },
                    onMoveDownTarget = { viewModel.moveTargetDown() },
                    onDeleteSource = { viewModel.deleteSourceFile() },
                    onDeleteTarget = { viewModel.deleteTargetFile() },
                )
                Text(
                    modifier = Modifier.padding(horizontal = Spacing.xs),
                    text = errorState.filesError,
                    style = MaterialTheme.typography.caption,
                    color = MaterialTheme.colors.error,
                )

                Spacer(modifier = Modifier.height(Spacing.m))

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
                            viewModel.submit()
                        },
                    ) {
                        Text(text = "button_ok".localized(), style = MaterialTheme.typography.button)
                    }
                }
            }
        }

        if (pickSourceDialogOpen) {
            CustomOpenFileDialog(
                title = "dialog_title_open_file".localized(),
                nameFilter = { it.endsWith(".txt") },
                onCloseRequest = { path ->
                    path?.also {
                        viewModel.addSourceFile(it)
                    }
                    pickSourceDialogOpen = false
                },
            )
        }
        if (pickTargetDialogOpen) {
            CustomOpenFileDialog(
                title = "dialog_title_open_file".localized(),
                nameFilter = { it.endsWith(".txt") },
                onCloseRequest = { path ->
                    path?.also {
                        viewModel.addTargetFile(it)
                    }
                    pickTargetDialogOpen = false
                },
            )
        }
    }
}
