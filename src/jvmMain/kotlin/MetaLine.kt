import align.di.alignModule
import align.ui.AlignViewModel
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyShortcut
import androidx.compose.ui.window.MenuBar
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.arkivanov.essenty.instancekeeper.getOrCreate
import common.di.commonModule
import common.keystore.TemporaryKeyStore
import common.log.LogManager
import common.ui.components.CustomSaveFileDialog
import common.ui.theme.MetaLineTheme
import common.utils.AppBusiness
import kotlinx.coroutines.runBlocking
import main.di.mainModule
import main.ui.MainScreen
import main.ui.MainViewModel
import org.koin.core.context.GlobalContext.startKoin
import org.koin.java.KoinJavaComponent.inject
import persistence.di.persistenceModule
import project.di.projectModule
import projectcreate.ui.dialog.CreateProjectDialog
import projectedit.ui.dialog.EditProjectDialog
import projectmetadata.ui.ProjectMetadataViewModel
import projectsettings.ui.dialog.SettingsDialog
import projectstatistics.ui.dialog.StatisticsDialog
import repository.repositoryModule
import usecase.InitializeDefaultSegmentationRulesUseCase
import usecase.useCaseModule
import java.util.*

private fun initKoin() {
    startKoin {
        modules(
            commonModule,
            persistenceModule,
            repositoryModule,
            useCaseModule,
            mainModule,
            projectModule,
            alignModule,
        )
    }
}

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    // init DI
    initKoin()
    val log: LogManager by inject(LogManager::class.java)

    application {
        log.debug("Application initialized")

        runBlocking {
            // init l10n
            val keystore: TemporaryKeyStore by inject(TemporaryKeyStore::class.java)
            val systemLanguage = Locale.getDefault().language
            val lang = keystore.get("lang", "")
            L10n.setLanguage(lang.ifEmpty { systemLanguage })
            if (lang.isEmpty()) {
                keystore.save("lang", "lang".localized())
            }

            // init default segmentation rules
            val initializeSegmentationRules: InitializeDefaultSegmentationRulesUseCase by inject(
                InitializeDefaultSegmentationRulesUseCase::class.java,
            )
            initializeSegmentationRules()
        }

        Window(onCloseRequest = ::exitApplication, title = "app_name".localized()) {
            val lang by L10n.currentLanguage.collectAsState("lang".localized())
            LaunchedEffect(lang) {}

            val mainViewModel: MainViewModel = AppBusiness.instanceKeeper.getOrCreate {
                val res: MainViewModel by inject(MainViewModel::class.java)
                res
            }
            val alignViewModel: AlignViewModel = AppBusiness.instanceKeeper.getOrCreate {
                val res: AlignViewModel by inject(AlignViewModel::class.java)
                res
            }

            val mainUiState by mainViewModel.uiState.collectAsState()
            val alignEditUiState by alignViewModel.editUiState.collectAsState()
            var newDialogOpen by remember {
                mutableStateOf(false)
            }
            var editDialogOpen by remember {
                mutableStateOf(false)
            }
            var statisticsDialogOpen by remember {
                mutableStateOf(false)
            }
            var settingsDialogOpen by remember {
                mutableStateOf(false)
            }
            var exportDialogOpen by remember {
                mutableStateOf(false)
            }

            MenuBar {
                Menu("menu_project".localized()) {
                    Item(
                        text = "menu_project_new".localized(),
                        shortcut = KeyShortcut(Key.N, meta = true),
                    ) {
                        val vm = AppBusiness.instanceKeeper.getOrCreate {
                            val res: ProjectMetadataViewModel by inject(ProjectMetadataViewModel::class.java)
                            res
                        }
                        vm.load(project = null)
                        newDialogOpen = true
                    }
                    Item(
                        text = "menu_project_edit".localized(),
                        enabled = mainUiState.project != null,
                    ) {
                        editDialogOpen = true
                    }
                    Item(
                        text = "menu_project_save".localized(),
                        enabled = alignEditUiState.needsSaving,
                        shortcut = KeyShortcut(Key.S, meta = true),
                    ) {
                        alignViewModel.save()
                    }
                    Item(
                        text = "menu_project_close".localized(),
                        enabled = mainUiState.project != null,
                    ) {
                        mainViewModel.closeProject()
                    }
                    Separator()
                    Item(
                        text = "menu_project_settings".localized(),
                        shortcut = KeyShortcut(Key.Comma, meta = true),
                    ) {
                        settingsDialogOpen = true
                    }
                    Item(
                        text = "menu_project_statistics".localized(),
                        enabled = mainUiState.project != null,
                    ) {
                        statisticsDialogOpen = true
                    }
                    Separator()
                    Item(
                        text = "menu_project_export".localized(),
                        enabled = mainUiState.project != null,
                    ) {
                        exportDialogOpen = true
                    }
                }
                Menu("menu_segment".localized()) {
                    Item(
                        text = "menu_segment_move_up".localized(),
                        shortcut = KeyShortcut(Key.DirectionUp, meta = true),
                    ) {
                        alignViewModel.moveSegmentUp()
                    }
                    Item(
                        text = "menu_segment_move_down".localized(),
                        shortcut = KeyShortcut(Key.DirectionDown, meta = true),
                    ) {
                        alignViewModel.moveSegmentDown()
                    }
                    Separator()
                    Item(
                        text = "menu_segment_merge_previous".localized(),
                        shortcut = KeyShortcut(Key.DirectionUp, meta = true, shift = true),
                    ) {
                        alignViewModel.mergeWithPreviousSegment()
                    }
                    Item(
                        text = "menu_segment_merge_next".localized(),
                        shortcut = KeyShortcut(Key.DirectionDown, meta = true, shift = true),
                    ) {
                        alignViewModel.mergeWithNextSegment()
                    }
                    Separator()
                    Item(
                        text = "menu_segment_create_before".localized(),
                        shortcut = KeyShortcut(Key.Enter, meta = true, shift = true),
                    ) {
                        alignViewModel.createSegmentBefore()
                    }
                    Item(
                        text = "menu_segment_create_after".localized(),
                        shortcut = KeyShortcut(Key.Enter, meta = true),
                    ) {
                        alignViewModel.createSegmentAfter()
                    }
                    Separator()
                    Item(
                        text = if (alignEditUiState.isEditing) "menu_segment_exit_edit".localized() else "menu_segment_edit".localized(),
                        shortcut = KeyShortcut(Key.E, meta = true),
                    ) {
                        alignViewModel.toggleEditing()
                    }
                    Item(text = "menu_segment_split".localized(), shortcut = KeyShortcut(Key.T, meta = true)) {
                        alignViewModel.splitSegment()
                    }
                    Item(text = "menu_segment_delete".localized(), shortcut = KeyShortcut(Key.Backspace, meta = true)) {
                        alignViewModel.deleteSegment()
                    }
                }
            }
            App()

            if (newDialogOpen) {
                CreateProjectDialog(
                    onClose = { project ->
                        newDialogOpen = false
                        if (project != null) {
                            mainViewModel.openProject(project)
                        }
                    },
                )
            }

            if (editDialogOpen) {
                EditProjectDialog(
                    project = mainUiState.project,
                    onClose = {
                        editDialogOpen = false
                    },
                )
            }

            if (statisticsDialogOpen) {
                mainUiState.project?.also {
                    StatisticsDialog(
                        project = it,
                        onClose = {
                            statisticsDialogOpen = false
                        },
                    )
                }
            }

            if (settingsDialogOpen) {
                SettingsDialog(
                    onClose = {
                        settingsDialogOpen = false
                    },
                )
            }

            if (exportDialogOpen) {
                CustomSaveFileDialog(
                    title = "dialog_title_export".localized(),
                    initialFileName = "memory.tmx",
                    nameFilter = { it.endsWith("tmx") },
                    onCloseRequest = {
                        exportDialogOpen = false
                        it?.also {
                            mainViewModel.exportTmx(path = it)
                        }
                    },
                )
            }
        }
    }
}

@Composable
fun App() {
    MetaLineTheme {
        MainScreen()
    }
}
