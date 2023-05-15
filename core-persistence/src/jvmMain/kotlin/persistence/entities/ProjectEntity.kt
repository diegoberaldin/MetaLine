package persistence.entities

import org.jetbrains.exposed.dao.id.IntIdTable

object ProjectEntity : IntIdTable() {
    val name = mediumText("name")
    val sourceLang = varchar("sourceLang", 2)
    val targetLang = varchar("targetLang", 2)
    val applyDefaultSegmentationRules = bool("applyDefaultSegmentationRules")
}
