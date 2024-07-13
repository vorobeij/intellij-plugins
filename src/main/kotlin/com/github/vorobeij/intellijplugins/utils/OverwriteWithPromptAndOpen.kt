package com.github.vorobeij.intellijplugins.utils

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDirectory
import de.maibornwolff.its.buildergenerator.service.FileService

fun overwriteWithPromptAndOpen(
    project: Project,
    fileService: FileService,
    directory: PsiDirectory,
    fileName: String,
    contents: String
) {
    val existingTestFile = fileService.getFileOrNull(directory, fileName)
    if (existingTestFile == null || getOverwriteConfirmation(project, fileName)) {
        fileService.withWriter {
            val psiFileToOpen = if (existingTestFile == null)
                this.createFile(directory, fileName, contents)
            else
                this.overwriteFile(existingTestFile, contents)
            this.reformat(psiFileToOpen)
            this.openInTab(psiFileToOpen)
        }
    }
}