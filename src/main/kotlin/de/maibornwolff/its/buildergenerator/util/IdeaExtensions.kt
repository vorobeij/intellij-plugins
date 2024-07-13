package de.maibornwolff.its.buildergenerator.util

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.psi.PsiDirectory
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtClassOrObject

fun AnActionEvent.getClassUnderCaret() = this.dataContext.getData("psi.Element") as? KtClass

fun KtClassOrObject.getContainingDirectory(): PsiDirectory = this.containingFile.containingDirectory