package repository

import data.ProjectModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.isActive
import persistence.dao.ProjectDAO

class ProjectRepository(
    private val projectDao: ProjectDAO,
) {
    val observeAll: Flow<List<ProjectModel>> = channelFlow {
        while (true) {
            if (!isActive) {
                break
            }
            val res = getAll()
            trySend(res)
            delay(1000)
        }
    }

    suspend fun create(model: ProjectModel): Int = projectDao.create(model)

    suspend fun update(model: ProjectModel) = projectDao.update(model)

    suspend fun delete(model: ProjectModel) = projectDao.delete(model)

    suspend fun getAll() = projectDao.getAll()

    suspend fun getById(id: Int) = projectDao.getById(id)
}
