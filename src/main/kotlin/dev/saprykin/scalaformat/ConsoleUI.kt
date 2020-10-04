package dev.saprykin.scalaformat

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.groups.mutuallyExclusiveOptions
import com.github.ajalt.clikt.parameters.groups.single
import com.github.ajalt.clikt.parameters.options.convert
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.file
import java.io.File

fun main(args: Array<String>) = ScalaFormat()
    .subcommands(Verify(), Format())
    .main(args)

class ScalaFormat : CliktCommand(name = "scalaformat") {
    override fun run() = Unit
}

class Verify : CliktCommand(
    help = "Check .scala files for compliance with code style",
    printHelpOnEmptyArgs = true
) {
    private val targetType: TargetType? by mutuallyExclusiveOptions(
        option(
            "-f", "--file",
            help = "Path to .scala file to check"
        ).file(
            mustExist = true,
            canBeDir = false,
            mustBeReadable = true,
            canBeSymlink = false
        ).convert { TargetType.SingleFile(it) },
        option(
            "-d", "--directory",
            help = "Path to directory wherein all .scala files will be checked"
        ).file(
            mustExist = true,
            canBeFile = false,
            canBeSymlink = false
        ).convert { TargetType.Directory(it) },
        option(
            "-p", "--project",
            help = """Path to directory wherein all .scala files 
        |(including files from all inner directories) will be checked""".trimMargin()
        ).file(
            mustExist = true,
            canBeFile = false,
            canBeSymlink = false
        ).convert { TargetType.Project(it) }
    ).single()

    private val config by option("-c", "--config", help = "Path to code style configuration file").file(
        mustExist = true,
        canBeDir = false,
        mustBeReadable = true,
        canBeSymlink = false
    )

    override fun run() {
        if (targetType == null) {
            echo("Error: please specify verification target using one of the options: --file, --directory or --project")
            return
        }
        val codeStyleConfig = config?.readText() ?: {}::class.java.getResource("/codestyle.config").readText()
        val userSpecificInputForLexer = UserSpecificInputForLexer(getScalaFiles(targetType!!), codeStyleConfig)
        userSpecificInputForLexer.filesContent.forEach { echo("$it \n") }
    }
}

class Format : CliktCommand(
    help = "Reformat .scala files for compliance with code style",
    printHelpOnEmptyArgs = true
) {
    private val targetType: TargetType? by mutuallyExclusiveOptions(
        option(
            "-f", "--file",
            help = "Path to .scala file to reformat"
        ).file(
            mustExist = true,
            canBeDir = false,
            mustBeReadable = true,
            mustBeWritable = true,
            canBeSymlink = false
        ).convert { TargetType.SingleFile(it) },
        option(
            "-d", "--directory",
            help = "Path to directory wherein all .scala files will be reformatted"
        ).file(
            mustExist = true,
            canBeFile = false,
            canBeSymlink = false
        ).convert { TargetType.Directory(it) },
        option(
            "-p", "--project",
            help = """Path to directory wherein all .scala files 
        |(including files from all inner directories) will be reformatted""".trimMargin()
        ).file(
            mustExist = true,
            canBeFile = false,
            canBeSymlink = false
        ).convert { TargetType.Project(it) }
    ).single()

    private val config by option("-c", "--config", help = "Path to code style configuration file").file(
        mustExist = true,
        canBeDir = false,
        mustBeReadable = true,
        canBeSymlink = false
    )

    override fun run() {
        if (targetType == null) {
            echo("Error: please specify reformat target using one of the options: --file, --directory or --project")
            return
        }
        val codeStyleConfig = config?.readText() ?: {}::class.java.getResource("/codestyle.config").readText()
        val userSpecificInputForLexer = UserSpecificInputForLexer(getScalaFiles(targetType!!), codeStyleConfig)
        userSpecificInputForLexer.filesContent.forEach { echo("$it \n") }
    }
}

sealed class TargetType {
    data class SingleFile(val file: File) : TargetType()
    data class Directory(val path: File) : TargetType()
    data class Project(val path: File) : TargetType()
}

data class UserSpecificInputForLexer(val filesContent: List<Pair<File, String>>, val codeStyleConfig: String)

fun getScalaFiles(targetType: TargetType): List<Pair<File, String>> = targetType.run {
    when (this) {
        is TargetType.SingleFile ->
            listOf(Pair(file, file.readText()))
        is TargetType.Directory ->
            path.listFiles { file -> file.isFile && file.extension == "scala" }!!.toList()
                .map { Pair(it, it.readText()) }
                .toList()
        is TargetType.Project ->
            path.walk()
                .filter { it.extension == "scala" }
                .map { Pair(it, it.readText()) }
                .toList()
    }
}

