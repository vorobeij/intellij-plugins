package de.maibornwolff.its.buildergenerator.generator

data class GeneratorConfig(
    var testClassSuffix: String = "Test",
    var extendsList: String = "ru.vorobeij.backend.sub.di.ClosingKoinTest",
)
