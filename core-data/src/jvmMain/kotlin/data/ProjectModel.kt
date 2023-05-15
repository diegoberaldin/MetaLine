package data

data class ProjectModel(
    val id: Int = 0,
    val name: String = "",
    val sourceLang: String = "",
    val targetLang: String = "",
    val applyDefaultSegmentationRules: Boolean = true,
)
