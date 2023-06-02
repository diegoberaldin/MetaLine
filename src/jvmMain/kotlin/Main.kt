import align.di.alignModule
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyShortcut
import androidx.compose.ui.window.MenuBar
import androidx.compose.ui.window.MenuBarScope
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.extensions.compose.jetbrains.lifecycle.LifecycleController
import com.arkivanov.decompose.extensions.compose.jetbrains.subscribeAsState
import com.arkivanov.essenty.instancekeeper.getOrCreate
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import common.di.commonModule
import common.keystore.TemporaryKeyStore
import common.log.LogManager
import common.ui.components.CustomSaveFileDialog
import common.ui.theme.MetaLineTheme
import common.utils.AppBusiness
import common.utils.getByInjection
import common.utils.runOnUiThread
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.runBlocking
import main.di.mainModule
import main.ui.MainScreen
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
import root.RootComponent
import root.rootModule
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
            rootModule,
        )
    }
}

@OptIn(ExperimentalDecomposeApi::class)
fun main() {
    // init DI
    initKoin()
    val log: LogManager = getByInjection()

    // init l10n
    runBlocking {
        val keystore: TemporaryKeyStore = getByInjection()
        val systemLanguage = Locale.getDefault().language
        val lang = keystore.get("lang", "")
        L10n.setLanguage(lang.ifEmpty { systemLanguage })
        if (lang.isEmpty()) {
            keystore.save("lang", "lang".localized())
        }
    }

    // init default segmentation rules
    runBlocking {
        val initializeSegmentationRules: InitializeDefaultSegmentationRulesUseCase = getByInjection()
        initializeSegmentationRules()
    }

    // init root component in the main thread outside the application lifecycle
    val lifecycle = LifecycleRegistry()
    val mainScope = CoroutineScope(SupervisorJob())
    val rootComponent = runOnUiThread {
        getByInjection<RootComponent>(
            DefaultComponentContext(lifecycle = lifecycle),
            mainScope.coroutineContext,
        )
    }

    application {
        log.debug("Application initialized")

        // ties component lifecycle to the window
        val windowState = rememberWindowState()
        LifecycleController(lifecycle, windowState)

        Window(
            onCloseRequest = ::exitApplication,
            title = "app_name".localized(),
            state = windowState,
        ) {
            val lang by L10n.currentLanguage.collectAsState("lang".localized())
            LaunchedEffect(lang) {}

            MenuBar {
                makeMenus(
                    rootComponent = rootComponent,
                )
            }
            MetaLineTheme {
                MainScreen()
            }

            // dialogs
            val dialogState by rootComponent.dialog.subscribeAsState()
            val currentProject by rootComponent.currentProject.collectAsState()
            when (dialogState.child?.configuration) {
                RootComponent.DialogConfig.NewProject -> CreateProjectDialog(
                    onClose = { project ->
                        rootComponent.closeDialog()
                        if (project != null) {
                            rootComponent.openProject(project)
                        }
                    },
                )

                RootComponent.DialogConfig.EditProject -> EditProjectDialog(
                    project = currentProject,
                    onClose = {
                        rootComponent.closeDialog()
                    },
                )

                RootComponent.DialogConfig.Export -> CustomSaveFileDialog(
                    title = "dialog_title_export".localized(),
                    initialFileName = "memory.tmx",
                    nameFilter = { it.endsWith("tmx") },
                    onCloseRequest = {
                        rootComponent.closeDialog()
                        it?.also {
                            rootComponent.exportTmx(path = it)
                        }
                    },
                )

                RootComponent.DialogConfig.Settings -> SettingsDialog(
                    onClose = {
                        rootComponent.closeDialog()
                    },
                )

                RootComponent.DialogConfig.Statistics -> currentProject?.also {
                    StatisticsDialog(
                        project = it,
                        onClose = {
                            rootComponent.closeDialog()
                        },
                    )
                }

                else -> Unit
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun MenuBarScope.makeMenus(
    rootComponent: RootComponent,
) {
    val currentProject by rootComponent.currentProject.collectAsState()
    val needsSaving by rootComponent.needsSaving.collectAsState()
    val isEditing by rootComponent.isEditing.collectAsState()

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
            rootComponent.openDialog(RootComponent.DialogConfig.NewProject)
        }
        Item(
            text = "menu_project_edit".localized(),
            enabled = currentProject != null,
        ) {
            rootComponent.openDialog(RootComponent.DialogConfig.EditProject)
        }
        Item(
            text = "menu_project_save".localized(),
            enabled = needsSaving,
            shortcut = KeyShortcut(Key.S, meta = true),
        ) {
            rootComponent.save()
        }
        Item(
            text = "menu_project_close".localized(),
            enabled = currentProject != null,
        ) {
            rootComponent.closeProject()
        }
        Separator()
        Item(
            text = "menu_project_settings".localized(),
            shortcut = KeyShortcut(Key.Comma, meta = true),
        ) {
            rootComponent.openDialog(RootComponent.DialogConfig.Settings)
        }
        Item(
            text = "menu_project_statistics".localized(),
            enabled = currentProject != null,
        ) {
            rootComponent.openDialog(RootComponent.DialogConfig.Statistics)
        }
        Separator()
        Item(
            text = "menu_project_export".localized(),
            enabled = currentProject != null,
        ) {
            rootComponent.openDialog(RootComponent.DialogConfig.Export)
        }
    }
    Menu("menu_segment".localized()) {
        Item(
            text = "menu_segment_move_up".localized(),
            shortcut = KeyShortcut(Key.DirectionUp, meta = true),
        ) {
            rootComponent.moveSegmentUp()
        }
        Item(
            text = "menu_segment_move_down".localized(),
            shortcut = KeyShortcut(Key.DirectionDown, meta = true),
        ) {
            rootComponent.moveSegmentDown()
        }
        Separator()
        Item(
            text = "menu_segment_merge_previous".localized(),
            shortcut = KeyShortcut(Key.DirectionUp, meta = true, shift = true),
        ) {
            rootComponent.mergeWithPreviousSegment()
        }
        Item(
            text = "menu_segment_merge_next".localized(),
            shortcut = KeyShortcut(Key.DirectionDown, meta = true, shift = true),
        ) {
            rootComponent.mergeWithNextSegment()
        }
        Separator()
        Item(
            text = "menu_segment_create_before".localized(),
            shortcut = KeyShortcut(Key.Enter, meta = true, shift = true),
        ) {
            rootComponent.createSegmentBefore()
        }
        Item(
            text = "menu_segment_create_after".localized(),
            shortcut = KeyShortcut(Key.Enter, meta = true),
        ) {
            rootComponent.createSegmentAfter()
        }
        Separator()
        Item(
            text = if (isEditing) "menu_segment_exit_edit".localized() else "menu_segment_edit".localized(),
            shortcut = KeyShortcut(Key.E, meta = true),
        ) {
            rootComponent.toggleEditing()
        }
        Item(text = "menu_segment_split".localized(), shortcut = KeyShortcut(Key.T, meta = true)) {
            rootComponent.splitSegment()
        }
        Item(text = "menu_segment_delete".localized(), shortcut = KeyShortcut(Key.Backspace, meta = true)) {
            rootComponent.deleteSegment()
        }
    }
}
