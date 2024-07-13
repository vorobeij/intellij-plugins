package com.github.vorobeij.intellijplugins.utils

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages

fun getOverwriteConfirmation(project: Project, fileName: String): Boolean {
    val result = Messages.showOkCancelDialog(
        project,
        "Target file '$fileName' already exists and will be overwritten. Continue?",
        "Overwrite Existing File?", "Overwrite", "Cancel",
        Messages.getWarningIcon()
    )

    return result == Messages.OK
}