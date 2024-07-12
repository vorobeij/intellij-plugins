package com.github.vorobeij.intellijplugins.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.LangDataKeys
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.PsiManager
import org.jetbrains.kotlin.idea.KotlinLanguage
import org.jetbrains.kotlin.idea.refactoring.psiElement

class VorobeijGenerate : AnAction("Koin test class") {

    override fun actionPerformed(event: AnActionEvent) {
        val psiFileFactory = PsiFileFactory.getInstance(event.project)

        val name = "test.kt"
        val file = psiFileFactory.createFileFromText(
            name,
            KotlinLanguage.INSTANCE,
            createContents(event)
        )

        val dir = directory(event)
        // todo find directory for tests

        event.project.executeCouldRollBackAction {
            dir?.findFile(name) ?: dir?.add(file)
        }
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

    private fun createContents(event: AnActionEvent): String {
        return """
            ${event.dataContext.psiElement?.containingFile}
        """.trimIndent()
    }
}