package de.maibornwolff.its.buildergenerator.generator

import org.jetbrains.kotlin.idea.refactoring.fqName.fqName
import org.jetbrains.kotlin.types.KotlinType

data class Type(
    val simpleName: String,
    val packageName: String,
    val isNullable: Boolean,
    val wrappedPrimitiveType: WrappedPrimitive?,
    val typeArguments: List<Type>
) {

    companion object {

        fun fromKotlinType(type: KotlinType): Type {
            val simpleName = type.fqName?.shortName()?.asString()
                ?: throw NotImplementedError("Type has no FQ name: $type")
            val packageName = type.fqName?.parent()?.toString() ?: ""

            return Type(simpleName = simpleName,
                packageName = packageName,
                isNullable = type.isMarkedNullable,
                wrappedPrimitiveType = WrappedPrimitive.fromKotlinType(type),
                typeArguments = type.arguments.map { fromKotlinType(it.type) })
        }
    }

}