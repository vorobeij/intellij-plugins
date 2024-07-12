package de.maibornwolff.its.buildergenerator.generator

import org.jetbrains.kotlin.descriptors.ParameterDescriptor

data class Property(
    val name: String,
    val type: Type
) {

    companion object {

        val primitiveDefaultValuesMap = mapOf(
            "String" to "\"a·string\"",
            "Float" to "1.0F",
            "Double" to "1.0",
            "Int" to "42",
            "Boolean" to "false",
            "Long" to "23L"
        )

        fun fromParameterDescriptor(param: ParameterDescriptor) =
            Property(
                name = param.name.asString(),
                type = Type.fromKotlinType(param.type)
            )
                .takeIf { it.name != "<this>" }
    }
}

