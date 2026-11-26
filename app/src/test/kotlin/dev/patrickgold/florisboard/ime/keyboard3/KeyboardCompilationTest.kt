/*
 * Copyright (C) 2026 The FlorisBoard Contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.patrickgold.florisboard.ime.keyboard3

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import org.k3lp.lib.meta.source.SourceFileRef
import org.k3lp.lib.meta.source.TextSourceFile
import org.k3lp.lib.text.WillRequireMigrationToRichErrors
import org.k3lp.model.K3CompileResult
import org.k3lp.model.K3ImpliedImports
import org.k3lp.model.K3ImportResolver
import org.k3lp.model.compileToModel
import java.io.File
import java.net.URI

/**
 * Compiles the bundled foundation keyboard layouts the same way
 * [dev.patrickgold.florisboard.ime.keyboard3.extension.loadFoundationKeyboard] does, and prints the
 * actual violation details if compilation fails.
 */
@OptIn(WillRequireMigrationToRichErrors::class)
class KeyboardCompilationTest : FunSpec({
    val assetsDir = File("src/main/assets/extensions/org.florisboard.k3.foundation")

    fun sourceFile(file: File) = TextSourceFile(
        object : SourceFileRef {
            override fun toString(): String = file.name
        },
        file.readText(),
    )

    // Mirrors the resolver in Loader.kt: "flex://<ext-id>/<file>" -> "import/<file>"
    val importResolver = K3ImportResolver { path, _ ->
        val relPath = "import/" + (runCatching { URI(path).path?.removePrefix("/") }.getOrNull() ?: path)
        sourceFile(File(assetsDir, relPath))
    }
    val impliedImports = K3ImpliedImports(
        displays = listOf("flex://org.florisboard.k3.foundation/displays-implied.xml"),
        keys = listOf("flex://org.florisboard.k3.foundation/keys-implied.xml"),
    )

    for (layout in listOf("qwertz.xml", "qwerty.xml")) {
        test("$layout compiles without violations") {
            val file = File(assetsDir, "keyboard/$layout")
            file.exists() shouldBe true

            val result = sourceFile(file).compileToModel(importResolver, impliedImports)
            val details = result.reports.joinToString("\n") { report ->
                "${report::class.qualifiedName}: ${report.cause?.message ?: report}"
            }
            println("[$layout] result=${result::class.simpleName} reports=${result.reports.size}\n$details")
            (result is K3CompileResult.Success) shouldBe true
            result.reports.size shouldBe 0
        }
    }
})
