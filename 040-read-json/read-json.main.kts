#!/usr/bin/env kotlin
@file:DependsOn("com.fasterxml.jackson.module:jackson-module-kotlin:2.14.2")

import java.io.File

fun String.println(): String {
    println(this)
    return this
}

fun String.toFile(fileName: String): String {
    File(fileName).writeText(this)
    return this
}

val scriptDir: String = __FILE__.parent

