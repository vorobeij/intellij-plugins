package com.github.vorobeij.intellijplugins.actions

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.CommandProcessor
import com.intellij.openapi.project.Project

fun Project?.executeCouldRollBackAction(action: (Project?) -> Unit) {
    CommandProcessor.getInstance().executeCommand(this, {
        ApplicationManager.getApplication().runWriteAction {
            action.invoke(this@executeCouldRollBackAction)
        }
    }, "insertKotlin", "vorobeij")
}