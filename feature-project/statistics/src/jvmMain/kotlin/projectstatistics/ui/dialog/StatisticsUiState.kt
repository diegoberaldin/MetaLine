package projectstatistics.ui.dialog

data class StatisticsUiState(
    val items: List<StatisticsItem> = listOf(),
    val loading: Boolean = false,
)
