package data

import common.utils.lastPathSegment
import common.utils.stripExtension

data class FilePairModel(
    val id: Int = 0,
    val sourcePath: String = "",
    val targetPath: String = "",
) {
    val name: String
        get() = buildString {
            append(sourcePath.lastPathSegment().stripExtension())
            append(" – ")
            append(targetPath.lastPathSegment().stripExtension())
        }
}
