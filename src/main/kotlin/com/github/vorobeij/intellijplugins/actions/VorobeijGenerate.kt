package com.github.vorobeij.intellijplugins.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.psi.PsiDirectory
import de.maibornwolff.its.buildergenerator.actions.SourceRootChoice
import de.maibornwolff.its.buildergenerator.generator.TestClassGenerator
import de.maibornwolff.its.buildergenerator.service.FileService
import de.maibornwolff.its.buildergenerator.settings.AppSettingsState
import de.maibornwolff.its.buildergenerator.util.getClassUnderCaret
import org.jetbrains.kotlin.idea.KotlinFileType
import org.jetbrains.kotlin.psi.KtClass

class VorobeijGenerate : AnAction("vorobeij DAO test") {

    override fun actionPerformed(event: AnActionEvent) {

        event.project?.let {
            val classUnderCaret = event.getClassUnderCaret()
            if (classUnderCaret != null) {
                generateBuilder(classUnderCaret, it)
            } else {
                Messages.showMessageDialog(
                    it,
                    event.dataContext.getData("psi.Element").toString(),
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
        val generatedFileText = TestClassGenerator(currentConfig, project).generateTest(ktClass, generatedName)
        val builderFileName = "${generatedName}.${KotlinFileType.EXTENSION}"
        val fileService = project.service<FileService>()
        overwriteWithPromptAndOpen(project, fileService, builderDirectory, builderFileName, generatedFileText)
    }

    private fun overwriteWithPromptAndOpen(
        project: Project,
        fileService: FileService,
        directory: PsiDirectory,
        fileName: String,
        contents: String
    ) {
        val existingBuilderFile = fileService.getFileOrNull(directory, fileName)
        if (existingBuilderFile == null || getOverwriteConfirmation(project, fileName)) {
            fileService.withWriter {
                val psiFileToOpen = if (existingBuilderFile == null)
                    this.createFile(directory, fileName, contents)
                else
                    this.overwriteFile(existingBuilderFile, contents)
                this.reformat(psiFileToOpen)
                this.openInTab(psiFileToOpen)
            }
        }
    }

    private fun showOnlyDataClassesAllowedMessage(project: Project) {
        Messages.showMessageDialog(
            project,
            "For top level classes only",
            "Builder Generator Error",
            Messages.getErrorIcon()
        )
    }

    private fun getOverwriteConfirmation(project: Project, fileName: String): Boolean {
        val result = Messages.showOkCancelDialog(
            project,
            "Target file '$fileName' already exists and will be overwritten. Continue?",
            "Overwrite Existing File?", "Overwrite", "Cancel",
            Messages.getWarningIcon()
        )

        return result == Messages.OK
    }
}