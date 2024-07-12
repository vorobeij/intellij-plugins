@file:Suppress("DialogTitleCapitalization")

package de.maibornwolff.its.buildergenerator.settings

import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.FormBuilder
import de.maibornwolff.its.buildergenerator.generator.GeneratorConfig
import javax.swing.JPanel

class AppSettingsComponent {

    private val txtBuilderClassSuffix = JBTextField()
    private val txtextendsList = JBTextField()

    private val mainPanel = FormBuilder.createFormBuilder()
        .addLabeledComponent("txtBuilderClassSuffix: ", txtBuilderClassSuffix, 1)
        .addLabeledComponent("txtextendsList: ", txtextendsList, 1)
        .panel

    val panel: JPanel get() = mainPanel

    fun setValues(config: GeneratorConfig) {
        txtBuilderClassSuffix.text = config.testClassSuffix
        txtextendsList.text = config.extendsList
    }

    fun getValues() = GeneratorConfig(
        testClassSuffix = txtBuilderClassSuffix.text,
        extendsList = txtextendsList.text
    )
}