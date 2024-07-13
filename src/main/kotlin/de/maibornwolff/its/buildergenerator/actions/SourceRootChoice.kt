package de.maibornwolff.its.buildergenerator.actions

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.ui.messages.MessagesService
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiManager
import de.maibornwolff.its.buildergenerator.util.getContainingDirectory
import java.io.File
import org.jetbrains.kotlin.idea.util.projectStructure.module
import org.jetbrains.kotlin.idea.util.sourceRoot
import org.jetbrains.kotlin.idea.util.sourceRoots
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.slf4j.LoggerFactory

object SourceRootChoice {

    private val LOGGER = LoggerFactory.getLogger(SourceRootChoice::class.java)

    fun chooseTargetDirectory(dataClass: KtClassOrObject, project: Project): PsiDirectory {
        return dataClass.module?.let { module ->
            val projectRootPath = project.basePath ?: return@let logAndReturnNull("No project base path")

            val relativeSourceRootPathsFromProjectRoot = listOf("/app/src/test/kotlin") + module.sourceRoots
                .mapNotNull { it.canonicalPath?.replace(projectRootPath, "") }
                .filter { it.contains("/kotlin") }
                .map { it.replace("main", "test") }

            if (relativeSourceRootPathsFromProjectRoot.isEmpty()) return@let logAndReturnNull("No relative source root paths")

            val chosenRelativePath = relativeSourceRootPathsFromProjectRoot
                .singleOrNull()
                ?: promptSourceRootSelection(relativeSourceRootPathsFromProjectRoot, project)
                ?: return@let logAndReturnNull("No source root selected")

            val chosenRootPath = projectRootPath + chosenRelativePath

            val directoryOfDataClass = dataClass.getContainingDirectory()
            val pathOfDirectoryOfDataClass = directoryOfDataClass.virtualFile.canonicalPath
                ?: return@let logAndReturnNull("Data class directory path could not be determined")
            val relativePathFromSourceRootToDataClass = pathOfDirectoryOfDataClass
                .replace(directoryOfDataClass.sourceRoot?.canonicalPath ?: "", "")

            val targetDirectoryPath = chosenRootPath + relativePathFromSourceRootToDataClass

            getPsiDirectory(targetDirectoryPath, project)
        } ?: dataClass.getContainingDirectory()
    }

    private fun logAndReturnNull(log: String): PsiDirectory? {
        LOGGER.debug(log)
        Messages.showWarningDialog(
            "$log\n\n Creating the builder in the same directory as data class instead...",
            "Builder Generator Warning"
        )
        return null
    }

    private fun getPsiDirectory(path: String, project: Project): PsiDirectory? {
        File(path).mkdirs()
        return VirtualFileManager.getInstance().refreshAndFindFileByUrl("file://$path")?.let {
            PsiManager.getInstance(project).findDirectory(it)
        } ?: logAndReturnNull("Could not find virtual file for path '$path'")
    }

    private fun promptSourceRootSelection(sourceRoots: List<String>, project: Project): String? {
        val testSourceRootIndex = sourceRoots
            .indexOfFirst { it.contains("test") && !it.contains("resources") }
            .takeIf { it >= 0 } ?: 0

        val selectedIndex = MessagesService.getInstance().showChooseDialog(
            project,
            null,
            "Choose target source root for the builder class:",
            "Target Source Root",
            sourceRoots.toTypedArray(),
            sourceRoots[testSourceRootIndex],
            Messages.getQuestionIcon()
        )

        if (selectedIndex < 0) return null

        return sourceRoots[selectedIndex]
    }
}