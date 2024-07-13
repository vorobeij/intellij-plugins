package com.github.vorobeij.intellijplugins.actions

import com.github.vorobeij.intellijplugins.generator.QueryBuilderTestClassGenerator
import com.github.vorobeij.intellijplugins.utils.overwriteWithPromptAndOpen
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import de.maibornwolff.its.buildergenerator.actions.SourceRootChoice
import de.maibornwolff.its.buildergenerator.service.FileService
import de.maibornwolff.its.buildergenerator.settings.AppSettingsState
import org.jetbrains.kotlin.idea.KotlinFileType
import org.jetbrains.kotlin.idea.refactoring.psiElement
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtFile

class VorobeijGenerateQueryBuilderTest : AnAction("vorobeij QueryBuilder test") {

    override fun actionPerformed(event: AnActionEvent) {

        event.project?.let {
            val classUnderCaret = (event.dataContext.psiElement as? KtClass)
                ?: (event.dataContext.getData(CommonDataKeys.PSI_FILE) as KtFile)
                    .children.filterIsInstance<KtClass>()
                    .firstOrNull()
            if (classUnderCaret != null) {
                generateBuilder(classUnderCaret, it)
            } else {
                Messages.showMessageDialog(
                    it,
                    event.dataContext.psiElement.toString(),
                    "Builder Generator Error",
                    Messages.getErrorIcon()
                )
            }
        }
    }

    private fun generateBuilder(ktClass: KtClass, project: Project) {
        val currentConfig = AppSettingsState.getInstance().config
        val builderDirectory = SourceRootChoice.chooseTargetDirectory(ktClass, project)
        val generatedName = ktClass.name + currentConfig.testClassSuffix
        val generatedFileText = QueryBuilderTestClassGenerator(currentConfig, project).generateTest(ktClass, generatedName)
        val builderFileName = "${generatedName}.${KotlinFileType.EXTENSION}"
        val fileService = project.service<FileService>()
        overwriteWithPromptAndOpen(project, fileService, builderDirectory, builderFileName, generatedFileText)
    }
}