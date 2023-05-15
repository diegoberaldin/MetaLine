package persistence.entities

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption

object SegmentationRuleEntity : IntIdTable() {
    val before = mediumText("before")
    val after = mediumText("after")
    val breaking = bool("breaking")
    val lang = varchar("lang", 2)
    val projectId =
        reference(name = "projectId", foreign = ProjectEntity, onDelete = ReferenceOption.CASCADE).nullable()
}
