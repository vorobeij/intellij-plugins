package com.github.vorobeij.intellijplugins.actions

import com.intellij.codeInsight.CodeInsightActionHandler
import com.intellij.codeInsight.actions.CodeInsightAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile

class VorobeijGenerate: CodeInsightAction(), CodeInsightActionHandler {
    /**
     * Called when user invokes corresponding [CodeInsightAction]. This method is called inside command on EDT.
     * If [.startInWriteAction] returns `true`, this method is also called
     * inside write action.
     *
     * @param project the project where action is invoked.
     * @param editor  the editor where action is invoked.
     * @param file    the file open in the editor.
     */
    override fun invoke(project: Project, editor: Editor, file: PsiFile) {
        println("sakldfnskdljfn invoked!")
    }

    override fun getHandler(): CodeInsightActionHandler {
        return this
    }
}