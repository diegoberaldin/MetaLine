package repository

import data.SegmentationRuleModel
import persistence.dao.SegmentationRuleDAO

class SegmentationRuleRepository(
    private val segmentationRuleDao: SegmentationRuleDAO,
    private val languageRepository: LanguageRepository,
) {

    fun getInitialDefaultRules(): List<SegmentationRuleModel> = languageRepository.getDefaultLanguages().map {
        SegmentationRuleModel(lang = it.code, before = "\\\\.", after = "\\\\s")
    }

    suspend fun create(model: SegmentationRuleModel, projectId: Int? = null) =
        segmentationRuleDao.create(model = model, projectId = projectId)

    suspend fun update(model: SegmentationRuleModel) = segmentationRuleDao.update(model)

    suspend fun updateAll(models: List<SegmentationRuleModel>) = segmentationRuleDao.updateAll(models)

    suspend fun delete(model: SegmentationRuleModel) = segmentationRuleDao.delete(model)

    suspend fun getAll(projectId: Int, lang: String) = segmentationRuleDao.getAll(projectId = projectId, lang = lang)

    suspend fun getAllDefault(lang: String) = segmentationRuleDao.getAllDefault(lang)

    suspend fun getById(id: Int) = segmentationRuleDao.getById(id)
}
