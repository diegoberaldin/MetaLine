package data

data class SegmentationRuleModel(
    val id: Int = 0,
    val breaking: Boolean = true,
    val before: String = "",
    val after: String = "",
    val lang: String = "",
)
