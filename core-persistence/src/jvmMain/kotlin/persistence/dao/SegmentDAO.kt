package persistence.dao

import data.SegmentModel
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.batchInsert
import org.jetbrains.exposed.sql.batchReplace
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insertIgnore
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.update
import persistence.entities.SegmentEntity
import persistence.entities.SegmentEntity.lang
import persistence.entities.SegmentEntity.position
import persistence.entities.SegmentEntity.text

class SegmentDAO {
    suspend fun create(model: SegmentModel, pairId: Int): Int = newSuspendedTransaction {
        SegmentEntity.insertIgnore {
            it[lang] = model.lang
            it[text] = model.text
            it[SegmentEntity.pairId] = pairId
            it[position] = model.position
        }[SegmentEntity.id].value
    }

    suspend fun createAll(models: List<SegmentModel>, pairId: Int) = newSuspendedTransaction {
        SegmentEntity.batchInsert(models, ignore = true) {
            this[lang] = it.lang
            this[text] = it.text
            this[position] = it.position
            this[SegmentEntity.pairId] = pairId
        }
    }

    suspend fun update(model: SegmentModel) = newSuspendedTransaction {
        SegmentEntity.update({ SegmentEntity.id eq model.id }) {
            it[lang] = model.lang
            it[text] = model.text
            it[position] = model.position
        }
    }

    suspend fun updateAll(models: List<SegmentModel>) = newSuspendedTransaction {
        SegmentEntity.batchReplace(data = models, shouldReturnGeneratedValues = false) {
            this[SegmentEntity.id] = it.id
            this[lang] = it.lang
            this[text] = it.text
            this[position] = it.position
        }
    }

    suspend fun delete(model: SegmentModel) = newSuspendedTransaction {
        SegmentEntity.deleteWhere { id eq model.id }
    }

    suspend fun getAll(pairId: Int, lang: String): List<SegmentModel> = newSuspendedTransaction {
        SegmentEntity.select { (SegmentEntity.pairId eq pairId) and (SegmentEntity.lang eq lang) }.map { it.toModel() }
            .sortedBy { it.position }
    }

    suspend fun getById(id: Int): SegmentModel? = newSuspendedTransaction {
        SegmentEntity.select { SegmentEntity.id eq id }.firstOrNull()?.toModel()
    }

    private fun ResultRow.toModel(): SegmentModel {
        return SegmentModel(
            id = this[SegmentEntity.id].value,
            position = this[position],
            lang = this[lang],
            text = this[text],
        )
    }
}
