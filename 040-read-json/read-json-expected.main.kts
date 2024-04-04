#!/usr/bin/env kotlin
@file:DependsOn("com.fasterxml.jackson.module:jackson-module-kotlin:2.14.2")

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import java.io.File

fun String.println(): String {
    println(this)
    return this
}

fun String.toFile(fileName: String): String {
    File(fileName).writeText(this)
    return this
}

inline fun <reified T> File.deserialize(): T {
    return jacksonObjectMapper()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        .readValue<T>(this)
}

val scriptDir: String = __FILE__.parent

data class PackageJson(
    val name: String?, val version: String?,
    val description: String?
)

val packageJson = File("$scriptDir/package.json")
    .deserialize<PackageJson>()

val projectName = packageJson.name
    ?: throw Exception("Project name required")
val projectVersion = packageJson.version
    ?: throw Exception("Project version required")

// Интерполяция и интерполяция внутри интерполяции
"""
    * Project name: $projectName
    * Project version: $projectVersion
    ${if (packageJson.description != null) "* ${packageJson.description}" else ""}
""".trimIndent()
    .toFile("$scriptDir/partial.adoc")

// start

// Задание массива пар, первое значение в которых
// определяет наименование атрибута, а второе --
// значение атрибута
arrayOf(
    packageJson::name.name to projectName,
    packageJson::version.name to projectVersion,
    packageJson::description.name to packageJson.description
)
    // Отбор пар, в которых второй параметр (значение) не является пустым
    .filter { it.second != null }
    // Преобразование пар в синтаксис Asciidoc
    // :attribute-name: attribute value
    // В реальных задачах значение (value) необходимо экранировать
    .joinToString("\n") { ":${it.first}: ${it.second}" }
    .toFile("$scriptDir/attributes.adoc")

"Script successfully finished"



