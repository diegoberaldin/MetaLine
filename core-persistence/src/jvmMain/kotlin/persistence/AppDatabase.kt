package persistence

import common.files.FileManager
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import persistence.dao.FilePairDAO
import persistence.dao.ProjectDAO
import persistence.dao.SegmentDAO
import persistence.dao.SegmentationRuleDAO
import persistence.entities.FilePairEntity
import persistence.entities.ProjectEntity
import persistence.entities.SegmentEntity
import persistence.entities.SegmentationRuleEntity

class AppDatabase(
    private val filename: String = FILE_NAME,
    private val fileManager: FileManager,
) {
    companion object {
        private const val DRIVER = "org.h2.Driver"
        private const val PROTO = "h2:file"
        private const val EXTRA_PARAMS = ";MODE=MYSQL"
        private const val FILE_NAME = "main"
    }

    init {
        setup()
    }

    private fun setup() {
        val appFileName = fileManager.getFilePath(filename)
        Database.connect("jdbc:$PROTO:$appFileName$EXTRA_PARAMS", driver = DRIVER)

        transaction {
            SchemaUtils.create(
                ProjectEntity,
                FilePairEntity,
                SegmentEntity,
                SegmentationRuleEntity,
            )
        }
    }

    fun projectDao() = ProjectDAO()

    fun filePairDao() = FilePairDAO()

    fun segmentDao() = SegmentDAO()

    fun segmentationRuleDao() = SegmentationRuleDAO()
}
