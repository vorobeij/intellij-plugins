package de.maibornwolff.its.buildergenerator.generator

import org.jetbrains.kotlin.descriptors.SimpleFunctionDescriptor

data class Method(
    val name: String,
    val params: List<Property>
) {
    companion object {

        fun fromDescriptor(descriptor: SimpleFunctionDescriptor): Method {
            return Method(
                name = descriptor.name.toString(),
                params = descriptor.valueParameters.mapNotNull(Property.Companion::fromParameterDescriptor)
            )
        }
    }
}