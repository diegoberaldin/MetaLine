package projectsettings.ui.dialog

import L10n
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
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
import com.arkivanov.decompose.extensions.compose.jetbrains.subscribeAsState
import common.ui.components.CustomTabBar
import common.ui.theme.MetaLineTheme
import common.ui.theme.SelectedBackground
import common.ui.theme.Spacing
import localized
import projectsettings.ui.general.SettingsGeneralScreen
import projectsettings.ui.segmentation.SettingsSegmentationScreen

@Composable
fun SettingsDialog(
    component: SettingsComponent,
    onClose: (() -> Unit)? = null,
) {
    val lang by L10n.currentLanguage.collectAsState("lang".localized())
    LaunchedEffect(lang) {}

    MetaLineTheme {
        Window(
            title = "dialog_title_settings".localized(),
            state = rememberWindowState(width = Dp.Unspecified, height = Dp.Unspecified),
            resizable = false,
            onCloseRequest = {
                onClose?.invoke()
            },
        ) {
            val uiState by component.uiState.collectAsState()
            val content by component.content.subscribeAsState()

            Column(
                modifier = Modifier.size(600.dp, 400.dp)
                    .background(MaterialTheme.colors.background)
                    .padding(start = Spacing.s, end = Spacing.s, top = Spacing.xs),
            ) {
                val tabs = uiState.tabs
                val tabIndex = tabs.indexOf(uiState.currentTab)
                CustomTabBar(
                    modifier = Modifier.fillMaxWidth(),
                    tabs = tabs.map { it.toReadableName() },
                    current = tabIndex,
                    onTabSelected = {
                        component.selectTab(tabs[it])
                    },
                )
                val bottomModifier = Modifier.fillMaxWidth().weight(1f).background(
                    color = SelectedBackground,
                    shape = when (tabIndex) {
                        0 -> RoundedCornerShape(topEnd = 4.dp, bottomStart = 4.dp, bottomEnd = 4.dp)
                        else -> RoundedCornerShape(4.dp)
                    },
                )

                when (content.child?.configuration) {
                    SettingsComponent.Config.General -> SettingsGeneralScreen(modifier = bottomModifier)
                    SettingsComponent.Config.Segmentation -> SettingsSegmentationScreen(modifier = bottomModifier)
                    else -> Unit
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
                            onClose?.invoke()
                        },
                    ) {
                        Text(text = "button_close".localized(), style = MaterialTheme.typography.button)
                    }
                }
            }
        }
    }
}
