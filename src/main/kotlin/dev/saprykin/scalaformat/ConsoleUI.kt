package dev.saprykin.scalaformat

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.groups.mutuallyExclusiveOptions
import com.github.ajalt.clikt.parameters.groups.single
import com.github.ajalt.clikt.parameters.options.convert
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.file

class ScalaFormat : CliktCommand(name = "scalaformat") {
    override fun run() = Unit
}

sealed class TargetType {
    data class File(val path: String) : TargetType()
    data class Directory(val path: String) : TargetType()
    data class Project(val path: String) : TargetType()
}

class Verify : CliktCommand(help = "Check .scala files for compliance with code style") {

    private val targetType: TargetType? by mutuallyExclusiveOptions(
        option(
            "-f", "--file",
            help = "Path to .scala file to check"
        ).file(
            mustExist = true,
            canBeDir = false,
            mustBeReadable = true,
            canBeSymlink = false
        ).convert { TargetType.File(it.absolutePath) },
        option(
            "-d", "--directory",
            help = "Path to directory wherein all .scala files will be checked"
        ).file(
            mustExist = true,
            canBeFile = false,
            canBeSymlink = false
        ).convert { TargetType.Directory(it.absolutePath) },
        option(
            "-p", "--project",
            help = """Path to directory wherein all .scala files 
        |(including files from all inner directories) will be checked""".trimMargin()
        ).file(
            mustExist = true,
            canBeFile = false,
            canBeSymlink = false
        ).convert { TargetType.Project(it.absolutePath) }
    ).single()

    private val config by option("-c", "--config", help = "Path to code style configuration file").file(
        mustExist = true,
        canBeDir = false,
        mustBeReadable = true,
        canBeSymlink = false
    )

    override fun run() {
        echo("verify command called with target: \n ${targetType?.javaClass?.canonicalName}")
    }
}

class Format : CliktCommand(help = "Reformat .scala files for compliance with code style") {

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
        ).convert { TargetType.File(it.absolutePath) },
        option(
            "-d", "--directory",
            help = "Path to directory wherein all .scala files will be reformatted"
        ).file(
            mustExist = true,
            canBeFile = false,
            canBeSymlink = false
        ).convert { TargetType.Directory(it.absolutePath) },
        option(
            "-p", "--project",
            help = """Path to directory wherein all .scala files 
        |(including files from all inner directories) will be reformatted""".trimMargin()
        ).file(
            mustExist = true,
            canBeFile = false,
            canBeSymlink = false
        ).convert { TargetType.Project(it.absolutePath) }
    ).single()

    private val config by option("-c", "--config", help = "Path to code style configuration file").file(
        mustExist = true,
        canBeDir = false,
        mustBeReadable = true,
        canBeSymlink = false
    )

    override fun run() {
        echo("format command called with target: \n ${targetType?.javaClass?.canonicalName}")
    }
}

fun main(args: Array<String>) = ScalaFormat()
    .subcommands(Verify(), Format())
    .main(args)
