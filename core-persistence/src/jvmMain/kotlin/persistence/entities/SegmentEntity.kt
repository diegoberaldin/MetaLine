package persistence.entities

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption

object SegmentEntity : IntIdTable() {
    val lang = varchar("lang", 2)
    val text = largeText("text")
    val pairId = reference(name = "pairId", foreign = FilePairEntity, onDelete = ReferenceOption.CASCADE)
    val position = integer("position")
}
