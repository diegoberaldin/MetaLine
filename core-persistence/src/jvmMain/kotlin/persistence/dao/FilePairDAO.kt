package persistence.dao

import data.FilePairModel
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insertIgnore
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.update
import persistence.entities.FilePairEntity

class FilePairDAO {
    suspend fun create(model: FilePairModel, projectId: Int): Int = newSuspendedTransaction {
        FilePairEntity.insertIgnore {
            it[sourcePath] = model.sourcePath
            it[targetPath] = model.targetPath
            it[FilePairEntity.projectId] = projectId
        }[FilePairEntity.id].value
    }

    suspend fun update(model: FilePairModel) = newSuspendedTransaction {
        FilePairEntity.update({ FilePairEntity.id eq model.id }) {
            it[sourcePath] = model.sourcePath
            it[targetPath] = model.targetPath
        }
    }

    suspend fun delete(model: FilePairModel) = newSuspendedTransaction {
        FilePairEntity.deleteWhere { id eq model.id }
    }

    suspend fun deleteAll(projectId: Int) = newSuspendedTransaction {
        FilePairEntity.deleteWhere { FilePairEntity.projectId eq projectId }
    }

    suspend fun getAll(projectId: Int): List<FilePairModel> = newSuspendedTransaction {
        FilePairEntity.select { FilePairEntity.projectId eq projectId }.map { it.toModel() }
    }

    suspend fun getById(id: Int): FilePairModel? = newSuspendedTransaction {
        FilePairEntity.select { FilePairEntity.id eq id }.firstOrNull()?.toModel()
    }

    suspend fun find(projectId: Int, sourcePath: String, targetPath: String): FilePairModel? = newSuspendedTransaction {
        FilePairEntity.select { (FilePairEntity.projectId eq projectId) and (FilePairEntity.sourcePath eq sourcePath) and (FilePairEntity.targetPath eq targetPath) }
            .firstOrNull()?.toModel()
    }

    private fun ResultRow.toModel() = FilePairModel(
        id = this[FilePairEntity.id].value,
        sourcePath = this[FilePairEntity.sourcePath],
        targetPath = this[FilePairEntity.targetPath],
    )
}
