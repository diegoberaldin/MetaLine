package usecase

import data.SegmentModel
import org.redundent.kotlin.xml.XmlVersion
import org.redundent.kotlin.xml.xml
import java.io.File
import java.io.FileWriter
import kotlin.coroutines.suspendCoroutine

class ExportTmxUseCase {

    data class Input(
        val sourceLang: String,
        val targetLang: String,
        val sourceSegments: List<SegmentModel> = emptyList(),
        val targetSegments: List<SegmentModel> = emptyList(),
    )

    suspend operator fun invoke(input: Input, destination: File) {
        assert(input.sourceSegments.size == input.targetSegments.size)
        val content = getXml(input)
        suspendCoroutine<Unit> {
            runCatching {
                FileWriter(destination).use {
                    it.write(content)
                }
            }
        }
    }

    private fun getXml(input: Input): String {
        val root = xml("tmx") {
            includeXmlProlog = true
            encoding = "UTF-8"
            version = XmlVersion.V10

            attribute("version", "1.4")

            "header" {
                attribute("creationTool", "MetaLine")
                attribute("creationToolVersion", "1.0.0")
                attribute("segtype", "sentence")
                attribute("o-tmf", "tmx")
                attribute("adminLang", "en-US")
                attribute("srcLang", input.sourceLang)
                attribute("datatype", "plaintext")
            }
            "body" {
                for (i in input.sourceSegments.indices) {
                    "tu" {
                        "tuv" {
                            attribute("xml:lang", input.sourceLang)
                            "seg" {
                                val segment = input.sourceSegments[i]
                                text(segment.text)
                            }
                        }
                        "tuv" {
                            attribute("xml:lang", input.targetLang)
                            "seg" {
                                val segment = input.targetSegments[i]
                                text(segment.text)
                            }
                        }
                    }
                }
            }
        }
        return root.toString(true)
    }
}
