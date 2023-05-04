package repository

import data.SegmentModel
import persistence.dao.SegmentDAO

class SegmentRepository(
    private val segmentDao: SegmentDAO,
) {

    suspend fun create(model: SegmentModel, pairId: Int) = segmentDao.create(model = model, pairId = pairId)

    suspend fun createAll(models: List<SegmentModel>, pairId: Int) =
        segmentDao.createAll(models = models, pairId = pairId)

    suspend fun update(model: SegmentModel) = segmentDao.update(model)

    suspend fun updateAll(models: List<SegmentModel>) = segmentDao.updateAll(models)

    suspend fun delete(model: SegmentModel) = segmentDao.delete(model)

    suspend fun getAll(pairId: Int, lang: String) = segmentDao.getAll(pairId = pairId, lang = lang)

    suspend fun getById(id: Int) = segmentDao.getById(id)
}
