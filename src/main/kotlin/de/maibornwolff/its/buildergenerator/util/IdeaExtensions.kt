package de.maibornwolff.its.buildergenerator.util

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.psi.PsiDirectory
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract
import org.jetbrains.kotlin.psi.KtClass

fun AnActionEvent.getClassUnderCaret() = this.dataContext.getData("psi.Element") as? KtClass

@ExperimentalContracts
fun KtClass?.isNonNullDataClass(): Boolean {
    contract {
        returns(true) implies (this@isNonNullDataClass != null)
    }
    return this != null && this.isData()
}

fun KtClass.getContainingDirectory(): PsiDirectory = this.containingFile.containingDirectory