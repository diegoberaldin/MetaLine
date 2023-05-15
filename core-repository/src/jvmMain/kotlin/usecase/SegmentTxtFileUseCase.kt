package usecase

import data.SegmentModel
import repository.ProjectRepository
import repository.SegmentationRuleRepository
import java.io.File
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class SegmentTxtFileUseCase(
    private val projectRepository: ProjectRepository,
    private val segmentationRuleRepository: SegmentationRuleRepository,
) {

    suspend operator fun invoke(path: String, lang: String, projectId: Int): List<SegmentModel> {
        val project = projectRepository.getById(projectId) ?: return emptyList()
        val rules = if (project.applyDefaultSegmentationRules) {
            segmentationRuleRepository.getAllDefault(lang = lang)
        } else {
            segmentationRuleRepository.getAll(lang = lang, projectId = projectId)
        }

        val content = suspendCoroutine {
            val res = runCatching {
                File(path).readText()
            }.getOrElse { "" }
            it.resume(res)
        }
        val breakingPositions = mutableListOf<Int>()
        for (rule in rules) {
            val regex = Regex(rule.before + rule.after)
            val allMatches = regex.findAll(content)
            val matches = allMatches.count()
            println("found $matches matches")
            for (match in allMatches) {
                val matchStart = match.range.first
                val matchEnd = match.range.last
                val matchedString = content.substring(startIndex = matchStart, endIndex = matchEnd)
                val beforeRegex = Regex(rule.before)
                val beforeLength = (beforeRegex.find(matchedString)?.range?.endInclusive ?: 0) + 1
                val index = matchStart + beforeLength
                if (rule.breaking) {
                    breakingPositions += index
                } else if (breakingPositions.contains(index)) {
                    breakingPositions -= index
                }
            }
        }

        return buildList {
            var prevIndex = 0
            for (index in breakingPositions) {
                val seg = content.substring(prevIndex, index).trim()
                this += SegmentModel(text = seg)
                prevIndex = index
            }
            if (prevIndex < content.length) {
                val seg = content.substring(prevIndex, content.length).trim()
                this += SegmentModel(text = seg)
            }
        }
    }
}
