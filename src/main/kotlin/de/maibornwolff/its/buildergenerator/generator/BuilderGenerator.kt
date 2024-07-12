package de.maibornwolff.its.buildergenerator.generator

import com.intellij.openapi.project.Project
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.LambdaTypeName
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.UNIT
import org.jetbrains.kotlin.backend.common.descriptors.allParameters
import org.jetbrains.kotlin.idea.caches.resolve.analyzeWithContent
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.resolve.BindingContext

class BuilderGenerator(private val config: GeneratorConfig, private val project: Project) {

    fun generateTest(builtClass: KtClass, generatedName: String): String {

        val bindingContext = builtClass.containingKtFile.analyzeWithContent()

        val builtClassDescriptor = bindingContext.get(BindingContext.CLASS, builtClass)
            ?: throw RuntimeException("Cannot get descriptor for the built class, wtf")

        val dataClassSimpleName = builtClass.fqName!!.shortName().asString()
        val packageName = builtClass.fqName!!.parent().asString()

        val properties: List<Property> = builtClassDescriptor.unsubstitutedPrimaryConstructor?.allParameters
            ?.mapNotNull { Property.fromParameterDescriptor(it) }
            ?: throw NotImplementedError("Class descriptor has no primary constructor for us to work with")

        return """
            package $packageName
            
            import org.junit.jupiter.api.Test
            import org.koin.test.inject
            import ru.vorobeij.backend.sub.services.models.testChannelId
            import ru.vorobeij.backend.sub.services.models.testChannelIds
            import ru.vorobeij.backend.sub.services.models.testVideoId
            import ru.vorobeij.backend.sub.services.models.testVideoIds
            import ru.vorobeij.backend.sub.services.videos.domain.SrtMetricsDo
            import ru.vorobeij.suby.client.api.core.ClosingKoinTest
            import ru.vorobeij.suby.client.api.core.testApplicationX

            internal class $generatedName: ${config.extendsList} {
                private val dao: ${builtClass.name} by inject()
            
                // todo
                @Test
                fun metricsBatch(): Unit = testApplicationX {
                    dao.metricsBatch(10)
                }
            }
        """.trimIndent()
    }

    private fun TypeSpec.Builder.addPropertyFields(properties: List<Property>) =
        this.addProperties(properties.map { property ->
            PropertySpec.builder(property.name, property.type.typeName)
                .addModifiers(KModifier.PRIVATE)
                .mutable()
                .initializer(property.getDefaultValue(project, config))
                .build()
        })

    private fun TypeSpec.Builder.addWithFunctions(properties: List<Property>) =
        this.apply {
            properties.forEach {
                this.addWithFunction(it)
                this.addOverloadingWithFunctionForWrappedPrimitive(it)
                this.addOverloadingWithFunctionForTypeWithBuilder(it)
                if (it.type.isNullable) this.addWithoutFunction(it)
            }
        }

    private fun TypeSpec.Builder.addOverloadingWithFunctionForWrappedPrimitive(property: Property): TypeSpec.Builder {
        return if (property.type.wrappedPrimitiveType != null) {
            val wrappingTypeName = property.type.simpleName
            val nonNullableTypeName = property.type.wrappedPrimitiveType.typeName
            this.addFunction(
                FunSpec.builder("${property.name.capitalize()}")
                    .addParameter(property.name, nonNullableTypeName)
                    .addStatement("return·apply·{ this.${property.name}·= $wrappingTypeName(${property.name}) }")
                    .build()
            )
        } else this
    }

    private fun TypeSpec.Builder.addOverloadingWithFunctionForTypeWithBuilder(property: Property): TypeSpec.Builder {
        val propertyBuilder = property.findBuilder(project, config)
        return if (propertyBuilder != null) {
            val propertyBuilderTypename = ClassName(propertyBuilder.packageName, propertyBuilder.name)
            val builderfunctionLambdaTypeName = LambdaTypeName.get(propertyBuilderTypename, emptyList(), UNIT)
            this.addFunction(
                FunSpec.builder("${property.name.capitalize()}")
                    .addParameter("initialize", builderfunctionLambdaTypeName)
                    .addStatement("return·apply·{ this.${property.name}·= ${propertyBuilder.name}().apply(initialize).${config.testClassSuffix}() }")
                    .build()
            )
        } else this
    }

    private fun TypeSpec.Builder.addWithFunction(property: Property): TypeSpec.Builder {
        val nonNullableTypeName = property.type.typeName.copy(nullable = false)
        return this.addFunction(
            FunSpec.builder("${property.name.capitalize()}")
                .addParameter(property.name, nonNullableTypeName)
                .addStatement("return·apply·{ this.${property.name}·= ${property.name} }")
                .build()
        )
    }

    private fun TypeSpec.Builder.addWithoutFunction(property: Property): TypeSpec.Builder {
        return this.addFunction(
            FunSpec.builder("${property.name.capitalize()}")
                .addStatement("return·apply·{ this.${property.name}·= null }")
                .build()
        )
    }

    private fun TypeSpec.Builder.addBuildFunction(
        parameters: List<Property>,
        builtClassSimpleName: String
    ): TypeSpec.Builder {
        return this.addFunction(
            FunSpec.builder("sdfsdf")
                .addStatement("return·${builtClassSimpleName}(${parameters.joinToString(separator = ",\n") { "${it.name}·= ${it.name}" }})")
                .build()
        )
    }
}


