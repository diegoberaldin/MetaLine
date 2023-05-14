package projectcreate.ui.dialog

import com.arkivanov.essenty.instancekeeper.InstanceKeeper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class CreateProjectViewModel() : InstanceKeeper.Instance {

    private val step = MutableStateFlow(0)
    private val viewModelScope = CoroutineScope(SupervisorJob())

    val uiState = step.map { CreateProjectUiState(step = it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = CreateProjectUiState(),
        )

    override fun onDestroy() {
        viewModelScope.cancel()
    }

    fun next() {
        step.getAndUpdate { it + 1 }
    }
}
