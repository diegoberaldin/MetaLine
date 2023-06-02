package main.ui

import L10n
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import common.ui.theme.Spacing
import localized
import mainintro.ui.IntroScreen
import project.ui.ChooseFilePairScreen
import project.ui.ProjectScreen
import project.ui.components.SideBar

@Composable
fun MainScreen(
    component: MainComponent,
) {
    Column(
        modifier = Modifier.fillMaxSize()
            .background(MaterialTheme.colors.background),
    ) {
        val uiState by component.uiState.collectAsState()
        Spacer(modifier = Modifier.height(Spacing.s))
        val currentProject = uiState.project

        if (currentProject == null) {
            IntroScreen(
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            ProjectContainer(
                modifier = Modifier.fillMaxSize(),
                component = component,
            )
        }
    }
}

@Composable
internal fun ProjectContainer(
    modifier: Modifier = Modifier,
    component: MainComponent,
) {
    val uiState by component.uiState.collectAsState()
    val currentFilePairIndex = uiState.currentFilePairIndex
    val currentProject = uiState.project ?: return

    val lang by L10n.currentLanguage.collectAsState("lang".localized())
    LaunchedEffect(lang) {}

    Column(modifier) {
        Row(
            modifier = Modifier.weight(1f),
        ) {
            SideBar(
                filePairs = uiState.filePairs,
                onOpenFilePair = {
                    component.openFilePair(index = it)
                },
            )

            Column(
                modifier = Modifier.weight(1f).padding(end = Spacing.s),
            ) {
                if (currentFilePairIndex == null) {
                    ChooseFilePairScreen(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        filePairs = uiState.filePairs,
                        onOpenFilePair = {
                            component.openFilePair(index = it)
                        },
                    )
                } else {
                    ProjectScreen(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        project = currentProject,
                        currentPairIdx = currentFilePairIndex,
                        openedFilePairs = uiState.openFilePairs,
                        onSelectFilePair = { index ->
                            component.selectFilePair(index = index)
                        },
                        onCloseFilePair = { index ->
                            component.closeFilePair(index = index)
                        },
                    )
                }
            }
        }
        // status bar
        Row(
            modifier = Modifier.padding(top = Spacing.xs, bottom = Spacing.s).fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
        ) {
            Text(
                text = buildString {
                    append("status_bar_project".localized(currentProject.name))
                    append(" – ")
                    append("status_bar_file_pairs".localized(uiState.filePairs.size))
                },
                style = MaterialTheme.typography.caption.copy(fontSize = 10.sp),
                color = Color.White,
            )
        }
    }
}
