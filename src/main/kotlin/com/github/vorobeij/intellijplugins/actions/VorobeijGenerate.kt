package com.github.vorobeij.intellijplugins.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.LangDataKeys
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.openapi.ui.Messages
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import de.maibornwolff.its.buildergenerator.actions.SourceRootChoice
import de.maibornwolff.its.buildergenerator.generator.BuilderGenerator
import de.maibornwolff.its.buildergenerator.service.FileService
import de.maibornwolff.its.buildergenerator.settings.AppSettingsState
import de.maibornwolff.its.buildergenerator.util.getClassUnderCaret
import de.maibornwolff.its.buildergenerator.util.isNonNullDataClass
import kotlin.contracts.ExperimentalContracts
import org.jetbrains.kotlin.idea.KotlinFileType
import org.jetbrains.kotlin.psi.KtClass

class VorobeijGenerate : AnAction("Koin test class") {

    @OptIn(ExperimentalContracts::class)
    override fun actionPerformed(event: AnActionEvent) {

        event.project?.let {
            val classUnderCaret = event.getClassUnderCaret()
            if (classUnderCaret.isNonNullDataClass()) {
                generateBuilder(classUnderCaret, it)
            } else {
                showOnlyDataClassesAllowedMessage(it)
            }
        }
    }

    private fun generateBuilder(dataClass: KtClass, project: Project) {
        val currentConfig = AppSettingsState.getInstance().config
        val builderSpec = BuilderGenerator(currentConfig, project).generateBuilderForDataClass(dataClass)
        val builderDirectory = SourceRootChoice.chooseTargetDirectory(dataClass, project)
        val builderFileName = "${builderSpec.name}.${KotlinFileType.EXTENSION}"
        val builderFileContents = builderSpec.toString()
        val fileService = project.service<FileService>()
        overwriteWithPromptAndOpen(project, fileService, builderDirectory, builderFileName, builderFileContents)
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
            "Builder generation only works for Kotlin data classes",
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


    private fun directory(event: AnActionEvent): PsiDirectory? {
        val project = event.getData(PlatformDataKeys.PROJECT) ?: return null

        val dataContext = event.dataContext
        val module = LangDataKeys.MODULE.getData(dataContext) ?: return null

        val directory = when (val navigatable = LangDataKeys.NAVIGATABLE.getData(dataContext)) {
            is PsiDirectory -> navigatable
            is PsiFile -> navigatable.containingDirectory
            else -> {
                val root = ModuleRootManager.getInstance(module)
                root.sourceRoots
                    .asSequence()
                    .mapNotNull {
                        PsiManager.getInstance(project).findDirectory(it)
                    }.firstOrNull()
            }
        } ?: return null

        return directory
    }
}