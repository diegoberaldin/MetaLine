package projectmedatada.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import common.ui.components.CustomSpinner
import common.ui.theme.Spacing
import data.LanguageModel
import localized

@Composable
internal fun LanguagesSelector(
    modifier: Modifier = Modifier,
    availableSourceLanguages: List<LanguageModel> = emptyList(),
    sourceLanguage: LanguageModel? = null,
    onSourceSelected: ((LanguageModel) -> Unit)? = null,
    availableTargetLanguages: List<LanguageModel> = emptyList(),
    targetLanguage: LanguageModel? = null,
    onTargetSelected: ((LanguageModel) -> Unit)? = null,
) {
    Row(
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier.weight(1f),
        ) {
            Text(
                modifier = Modifier.padding(horizontal = Spacing.xs),
                text = "create_project_source_language".localized(),
                style = MaterialTheme.typography.caption,
                color = Color.White,
            )
            Spacer(modifier = Modifier.height(Spacing.s))
            CustomSpinner(
                modifier = Modifier.fillMaxWidth().height(28.dp),
                values = availableSourceLanguages.map { it.name },
                current = sourceLanguage?.name ?: "select_placeholder".localized(),
                onValueChanged = {
                    val language = availableSourceLanguages[it]
                    onSourceSelected?.invoke(language)
                },
            )
        }
        Spacer(modifier = Modifier.width(Spacing.m))
        Column(
            modifier = Modifier.weight(1f),
        ) {
            Text(
                modifier = Modifier.padding(horizontal = Spacing.xs),
                text = "create_project_target_language".localized(),
                style = MaterialTheme.typography.caption,
                color = Color.White,
            )
            Spacer(modifier = Modifier.height(Spacing.s))
            CustomSpinner(
                modifier = Modifier.fillMaxWidth().height(28.dp),
                values = availableTargetLanguages.map { it.name },
                current = targetLanguage?.name ?: "select_placeholder".localized(),
                onValueChanged = {
                    val language = availableTargetLanguages[it]
                    onTargetSelected?.invoke(language)
                },
            )
        }
    }
}