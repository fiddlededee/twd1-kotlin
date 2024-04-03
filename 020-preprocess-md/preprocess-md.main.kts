#!/usr/bin/env kotlin
import java.io.File

fun String.println(): String {
    println(this)
    return this
}

fun String.toFile(fileName: String): String {
    File(fileName).writeText(this)
    return this
}

val scriptDir = __FILE__.parent

val mdText = File("$scriptDir/preprocess-md-source.md").readText()

val regEx = """\{\{[ ]*github-issue[ ]*\[[ ]*(.*)[ ]*][ ]*}}""".toRegex()

mdText
    .replace(regEx, "<a href='https://github.com/$1'>$1</a>")
    .println()

// start

