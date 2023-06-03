package projectmetadata.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import common.ui.components.CustomOpenFileDialog
import common.ui.components.CustomTextField
import common.ui.theme.Spacing
import localized
import projectmetadata.ui.components.FilePairSelector
import projectmetadata.ui.components.LanguagesSelector

@Composable
fun ProjectMetadataScreen(
    component: ProjectMetadataComponent,
    modifier: Modifier = Modifier,
) {
    val uiState by component.uiState.collectAsState()
    val errorState by component.errorUiState.collectAsState()
    val languageState by component.languagesUiState.collectAsState()
    val fileState by component.fileUiState.collectAsState()
    var pickSourceDialogOpen by remember {
        mutableStateOf(false)
    }
    var pickTargetDialogOpen by remember {
        mutableStateOf(false)
    }

    Column(
        modifier = modifier,
    ) {
        Spacer(modifier = Modifier.height(Spacing.s))
        CustomTextField(
            modifier = Modifier.height(48.dp),
            label = "create_project_name".localized(),
            value = uiState.name,
            singleLine = true,
            onValueChange = {
                component.setName(it)
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
                component.setSourceLanguage(it)
            },
            onTargetSelected = {
                component.setTargetLanguage(it)
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
            onSelectSourceFile = { component.selectSourceFile(it) },
            onSelectTargetFile = { component.selectTargetFile(it) },
            onAddSource = { pickSourceDialogOpen = true },
            onAddTarget = { pickTargetDialogOpen = true },
            onMoveUpSource = { component.moveSourceUp() },
            onMoveDownSource = { component.moveSourceDown() },
            onMoveUpTarget = { component.moveTargetUp() },
            onMoveDownTarget = { component.moveTargetDown() },
            onDeleteSource = { component.deleteSourceFile() },
            onDeleteTarget = { component.deleteTargetFile() },
        )
        Text(
            modifier = Modifier.padding(horizontal = Spacing.xs),
            text = errorState.filesError,
            style = MaterialTheme.typography.caption,
            color = MaterialTheme.colors.error,
        )
    }

    if (pickSourceDialogOpen) {
        CustomOpenFileDialog(
            title = "dialog_title_open_file".localized(),
            nameFilter = { it.endsWith(".txt") },
            onCloseRequest = { path ->
                path?.also {
                    component.addSourceFile(it)
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
                    component.addTargetFile(it)
                }
                pickTargetDialogOpen = false
            },
        )
    }
}
