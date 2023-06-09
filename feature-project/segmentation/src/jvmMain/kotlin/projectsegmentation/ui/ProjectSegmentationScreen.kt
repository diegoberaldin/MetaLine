package projectsegmentation.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.onClick
import androidx.compose.material.Checkbox
import androidx.compose.material.CheckboxDefaults
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.ArrowCircleDown
import androidx.compose.material.icons.filled.ArrowCircleUp
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EditOff
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import common.ui.components.CustomSpinner
import common.ui.components.CustomTooltipArea
import common.ui.theme.Indigo800
import common.ui.theme.Purple800
import common.ui.theme.Spacing
import data.ProjectModel
import localized

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ProjectSegmentationScreen(
    component: ProjectSegmentationComponent,
    modifier: Modifier = Modifier,
    project: ProjectModel? = null,
) {
    val uiState by component.uiState.collectAsState()

    LaunchedEffect(project) {
        if (project != null) {
            component.load(project)
        }
    }

    Column(
        modifier = modifier,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "segmentation_rules_project_intro".localized(),
                style = MaterialTheme.typography.caption,
                color = Color.White,
            )
            Spacer(modifier = Modifier.weight(1f))
            Checkbox(
                modifier = Modifier.padding(0.dp),
                colors = CheckboxDefaults.colors(
                    uncheckedColor = Color.Gray.copy(alpha = 0.25f),
                    checkedColor = MaterialTheme.colors.primary,
                    checkmarkColor = Color.White,
                ),
                checked = uiState.applyDefaultRules,
                onCheckedChange = {
                    component.toggleApplyDefaultRules()
                },
            )
        }
        Spacer(modifier = Modifier.height(Spacing.xs))
        val availableLanguages = uiState.availableLanguages
        CustomSpinner(
            modifier = Modifier.fillMaxWidth().height(20.dp),
            values = availableLanguages.map { it.name },
            current = uiState.currentLanguage?.name,
            onValueChanged = {
                val lang = availableLanguages[it]
                component.setCurrentLanguage(lang)
            },
        )
        Spacer(modifier = Modifier.height(Spacing.xs))
        Row {
            Spacer(modifier = Modifier.weight(1f))
            val iconModifier = Modifier.size(22.dp).padding(1.dp)
            val buttonEnabled = !uiState.applyDefaultRules
            CustomTooltipArea(
                text = "tooltip_add".localized(),
            ) {
                Icon(
                    modifier = iconModifier.onClick {
                        if (buttonEnabled) {
                            component.createRule()
                        }
                    },
                    imageVector = Icons.Default.AddCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colors.primary,
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(Spacing.xs),
        ) {
            Text(
                modifier = Modifier.weight(1.78f),
                text = "segmentation_rule_pattern_before".localized(),
                style = MaterialTheme.typography.caption,
                color = Color.White,
            )
            Text(
                modifier = Modifier.weight(1f),
                text = "segmentation_rule_pattern_after".localized(),
                style = MaterialTheme.typography.caption,
                color = Color.White,
            )
            Text(
                modifier = Modifier.weight(1f),
                text = "segmentation_rule_break_exception".localized(),
                style = MaterialTheme.typography.caption,
                color = Color.White,
                textAlign = TextAlign.End,
            )
            AnimatedVisibility(
                visible = !uiState.applyDefaultRules,
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                Spacer(modifier = Modifier.width(70.dp))
            }
        }
        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(Spacing.xs),
        ) {
            itemsIndexed(uiState.rules) { idx, item ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    SegmentationRuleField(
                        modifier = Modifier.weight(1f),
                        color = Purple800,
                        pattern = item.before,
                        isEditing = idx == uiState.currentEditedRule,
                        onTextEdited = { text ->
                            component.editRuleBeforePattern(text = text, index = idx)
                        },
                    )
                    SegmentationRuleField(
                        modifier = Modifier.weight(1f),
                        color = Indigo800,
                        pattern = item.after,
                        isEditing = idx == uiState.currentEditedRule,
                        onTextEdited = { text ->
                            component.editRuleAfterPattern(text = text, index = idx)
                        },
                    )
                    Checkbox(
                        modifier = Modifier.padding(0.dp).size(20.dp),
                        colors = CheckboxDefaults.colors(
                            uncheckedColor = Color.Gray.copy(alpha = 0.25f),
                            checkedColor = MaterialTheme.colors.primary,
                            checkmarkColor = Color.White,
                        ),
                        enabled = !uiState.applyDefaultRules,
                        checked = item.breaking,
                        onCheckedChange = {
                            component.toggleBreaking(index = idx)
                        },
                    )

                    AnimatedVisibility(
                        visible = !uiState.applyDefaultRules,
                        enter = fadeIn(),
                        exit = fadeOut(),
                    ) {
                        Row {
                            val isEditing = idx == uiState.currentEditedRule
                            val iconModifier = Modifier.size(22.dp).padding(2.dp)
                            CustomTooltipArea(
                                text = "tooltip_move_up".localized(),
                            ) {
                                Icon(
                                    modifier = iconModifier.onClick {
                                        component.moveRuleUp(index = idx)
                                    },
                                    imageVector = Icons.Default.ArrowCircleUp,
                                    contentDescription = null,
                                    tint = MaterialTheme.colors.primary,
                                )
                            }
                            CustomTooltipArea(
                                text = "tooltip_move_down".localized(),
                            ) {
                                Icon(
                                    modifier = iconModifier.onClick {
                                        component.moveRuleDown(index = idx)
                                    },
                                    imageVector = Icons.Default.ArrowCircleDown,
                                    contentDescription = null,
                                    tint = MaterialTheme.colors.primary,
                                )
                            }
                            CustomTooltipArea(
                                text = "tooltip_edit".localized(),
                            ) {
                                Icon(
                                    modifier = iconModifier.padding(1.dp)
                                        .onClick { component.toggleEditRule(index = idx) },
                                    imageVector = if (isEditing) {
                                        Icons.Default.EditOff
                                    } else {
                                        Icons.Default.Edit
                                    },
                                    contentDescription = null,
                                    tint = MaterialTheme.colors.primary,
                                )
                            }
                            CustomTooltipArea(
                                text = "tooltip_delete".localized(),
                            ) {
                                Icon(
                                    modifier = iconModifier.padding(1.dp).onClick { component.deleteRule(index = idx) },
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = null,
                                    tint = MaterialTheme.colors.primary,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
