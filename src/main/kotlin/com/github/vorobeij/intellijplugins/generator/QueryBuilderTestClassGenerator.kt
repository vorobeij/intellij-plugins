package com.github.vorobeij.intellijplugins.generator

import com.intellij.openapi.project.Project
import de.maibornwolff.its.buildergenerator.generator.GeneratorConfig
import de.maibornwolff.its.buildergenerator.generator.Method
import org.jetbrains.kotlin.descriptors.SimpleFunctionDescriptor
import org.jetbrains.kotlin.idea.caches.resolve.analyzeWithContent
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.lazy.descriptors.LazyClassDescriptor

class QueryBuilderTestClassGenerator(private val config: GeneratorConfig, private val project: Project) {

    fun generateTest(builtClass: KtClassOrObject, generatedName: String): String {

        val bindingContext = builtClass.containingKtFile.analyzeWithContent()

        val builtClassDescriptor =
            bindingContext.get(BindingContext.CLASS, builtClass) ?: throw RuntimeException("Cannot get descriptor for the built class, wtf")

        val packageName = builtClass.fqName!!.parent().asString()

        val methods = (builtClassDescriptor as LazyClassDescriptor).declaredCallableMembers.filterIsInstance<SimpleFunctionDescriptor>()
            .map(Method.Companion::fromDescriptor)

        return """
package $packageName

import org.junit.jupiter.api.Test
import ru.vorobeij.backend.sub.core.ext.exec
import ru.vorobeij.suby.client.api.core.ClosingKoinTest
import ru.vorobeij.suby.client.api.core.testQuery
import org.koin.test.inject

internal class $generatedName: ${config.extendsList} {

    private val builder: ${builtClass.name} by inject()
    
    ${methodsTemplate(methods)}
}
        """.withFixtures()
    }

    private fun methodsTemplate(methods: List<Method>) = methods.joinToString("\n") {
        """
@Test
fun ${it.name}(): Unit = testQuery {
    val query = builder.${it.name}(${it.params.joinToString(",\n") { "${it.name} = TODO()" }})
    query.exec()
}
"""
    }
}

fun String.withFixtures() = this
    .replace("languageCode = TODO()", "languageCode = \"en\"")
    .replace("videoId = TODO()", "videoId = testVideoId")
    .replace("channelId = TODO()", "channelId = testChannelId")
    .replace("userId = TODO()", "userId = testUserId")
    .replace("limit = TODO()", "limit = 10")
    .replace("reason = TODO()", "reason = 1")