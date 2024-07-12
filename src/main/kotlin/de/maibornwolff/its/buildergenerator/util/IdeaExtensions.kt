package de.maibornwolff.its.buildergenerator.util

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.psi.PsiDirectory
import org.jetbrains.kotlin.psi.KtClass

fun AnActionEvent.getClassUnderCaret() = this.dataContext.getData("psi.Element") as? KtClass

fun KtClass.getContainingDirectory(): PsiDirectory = this.containingFile.containingDirectory