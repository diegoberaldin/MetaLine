package persistence.dao

import data.SegmentationRuleModel
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insertIgnore
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.update
import persistence.entities.SegmentationRuleEntity

class SegmentationRuleDAO {
    suspend fun create(model: SegmentationRuleModel, projectId: Int? = null): Int = newSuspendedTransaction {
        SegmentationRuleEntity.insertIgnore {
            it[before] = model.before
            it[after] = model.after
            it[lang] = model.lang
            it[breaking] = model.breaking
            it[SegmentationRuleEntity.projectId] = projectId
        }[SegmentationRuleEntity.id].value
    }

    suspend fun update(model: SegmentationRuleModel) = newSuspendedTransaction {
        SegmentationRuleEntity.update({ SegmentationRuleEntity.id eq model.id }) {
            it[before] = model.before
            it[after] = model.after
            it[lang] = model.lang
            it[breaking] = model.breaking
        }
    }

    suspend fun delete(model: SegmentationRuleModel) = newSuspendedTransaction {
        SegmentationRuleEntity.deleteWhere { id eq model.id }
    }

    suspend fun getAll(projectId: Int, lang: String): List<SegmentationRuleModel> = newSuspendedTransaction {
        SegmentationRuleEntity.select { (SegmentationRuleEntity.projectId eq projectId and (SegmentationRuleEntity.lang eq lang)) }
            .map { it.toModel() }
    }

    suspend fun getAllDefault(lang: String): List<SegmentationRuleModel> = newSuspendedTransaction {
        SegmentationRuleEntity.select { SegmentationRuleEntity.projectId.isNull() and (SegmentationRuleEntity.lang eq lang) }
            .map { it.toModel() }
    }

    suspend fun getById(id: Int): SegmentationRuleModel? = newSuspendedTransaction {
        SegmentationRuleEntity.select { SegmentationRuleEntity.id eq id }.firstOrNull()?.toModel()
    }

    private fun ResultRow.toModel() = SegmentationRuleModel(
        id = this[SegmentationRuleEntity.id].value,
        before = this[SegmentationRuleEntity.before],
        after = this[SegmentationRuleEntity.after],
        breaking = this[SegmentationRuleEntity.breaking],
        lang = this[SegmentationRuleEntity.lang],
    )
}
