package projectsettings.ui.general

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.arkivanov.essenty.instancekeeper.getOrCreate
import common.ui.components.CustomSpinner
import common.ui.theme.Spacing
import common.utils.AppBusiness
import localized
import org.koin.java.KoinJavaComponent.inject

@Composable
fun SettingsGeneralScreen(
    modifier: Modifier = Modifier,
) {
    val viewModel: SettingsGeneralViewModel = AppBusiness.instanceKeeper.getOrCreate {
        val res: SettingsGeneralViewModel by inject(SettingsGeneralViewModel::class.java)
        res
    }

    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = modifier.padding(vertical = Spacing.m, horizontal = Spacing.s),
        verticalArrangement = Arrangement.spacedBy(Spacing.xs),
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "dialog_settings_language".localized(),
                style = MaterialTheme.typography.caption,
                color = Color.White,
            )
            Spacer(modifier = Modifier.weight(1f))
            val availableLanguages = uiState.availableLanguages
            CustomSpinner(
                values = availableLanguages.map { it.name },
                current = uiState.appLanguage?.name,
                onValueChanged = {
                    val language = availableLanguages[it]
                    viewModel.setLanguage(language)
                },
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "dialog_settings_version".localized(),
                style = MaterialTheme.typography.caption,
                color = Color.White,
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = uiState.appVersion,
                style = MaterialTheme.typography.caption,
                color = Color.White,
            )
        }
    }
}
