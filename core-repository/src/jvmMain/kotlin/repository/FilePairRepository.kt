package repository

import data.FilePairModel
import persistence.dao.FilePairDAO

class FilePairRepository(
    private val filePairDao: FilePairDAO,
) {
    suspend fun create(model: FilePairModel, projectId: Int) = filePairDao.create(model = model, projectId = projectId)

    suspend fun update(model: FilePairModel) = filePairDao.update(model)

    suspend fun delete(model: FilePairModel) = filePairDao.delete(model)

    suspend fun deleteAll(projectId: Int) = filePairDao.deleteAll(projectId)

    suspend fun getAll(projectId: Int) = filePairDao.getAll(projectId)

    suspend fun getById(id: Int) = filePairDao.getById(id)

    suspend fun find(projectId: Int, sourcePath: String, targetPath: String) =
        filePairDao.find(projectId = projectId, sourcePath = sourcePath, targetPath = targetPath)
}
