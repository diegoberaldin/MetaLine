package persistence.entities

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption

object FilePairEntity : IntIdTable() {
    val sourcePath = mediumText("sourcePath")
    val targetPath = mediumText("targetPath")
    val projectId = reference(name = "projectId", foreign = ProjectEntity, onDelete = ReferenceOption.CASCADE)

    init {
        uniqueIndex(sourcePath, targetPath, projectId)
    }
}
