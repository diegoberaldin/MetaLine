package repository

import data.SegmentationRuleModel
import persistence.dao.SegmentationRuleDAO

class SegmentationRuleRepository(
    private val segmentationRuleDao: SegmentationRuleDAO,
) {
    suspend fun create(model: SegmentationRuleModel, projectId: Int?) =
        segmentationRuleDao.create(model = model, projectId = projectId)

    suspend fun update(model: SegmentationRuleModel) = segmentationRuleDao.update(model)

    suspend fun delete(model: SegmentationRuleModel) = segmentationRuleDao.delete(model)

    suspend fun getAll(projectId: Int, lang: String) = segmentationRuleDao.getAll(projectId = projectId, lang = lang)

    suspend fun getAllDefault() = segmentationRuleDao.getAllDefault()

    suspend fun getById(id: Int) = segmentationRuleDao.getById(id)
}
