package com.github.vorobeij.intellijplugins.utils

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import org.jetbrains.kotlin.idea.refactoring.psiElement
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.psi.KtFile

fun AnActionEvent.containingClassOrObject(): KtClassOrObject? = (this.dataContext.psiElement as? KtClassOrObject)
    ?: (this.dataContext.getData(CommonDataKeys.PSI_FILE) as KtFile)
        .children.filterIsInstance<KtClassOrObject>()
        .firstOrNull()