package projectstatistics.ui.dialog

sealed interface StatisticsItem {
    object Divider : StatisticsItem
    data class Header(val title: String = "") : StatisticsItem
    data class SubHeader(val title: String = "") : StatisticsItem
    data class TextRow(val title: String = "", val value: String = "") : StatisticsItem
    data class BarChartRow(val title: String = "", val value: Float = 0f) : StatisticsItem
}
