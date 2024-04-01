#!/usr/bin/env kotlin
@file:DependsOn("org.asciidoctor:asciidoctorj:2.5.11")
@file:DependsOn("org.jsoup:jsoup:1.17.1")

import java.io.File
import org.asciidoctor.Asciidoctor
import org.asciidoctor.Options
import org.asciidoctor.SafeMode
import org.jsoup.Jsoup
import java.util.concurrent.TimeUnit

object AsciidocHtmlFactory {
    private val factory: Asciidoctor = Asciidoctor.Factory.create()
    fun getHtmlFromFile(file: File): String =
        factory.convertFile(
            file, Options.builder().backend("html5").sourcemap(true)
                .safe(SafeMode.UNSAFE).toFile(false).standalone(true)
                .templateDirs(File("./templates"))
                .build()
        )
}

fun String.println(): String {
    println(this)
    return this
}

fun String.toFile(name: String): String {
    File(name).writeText(this)
    return this
}

fun String.runCommand(): String {
    return ProcessBuilder(*split(" ").toTypedArray()).start()
        .apply { waitFor(60, TimeUnit.MINUTES) }
        .run { String(inputStream.readAllBytes()) }
}

data class VerCliRule(val param: String, val cli: String, val regEx: Regex) {
    fun value(): String {
        return cli.runCommand()
            .split("\n")
            .filter { it.contains(regEx) }
            .run { getOrNull(0) ?: throw Exception("Can't define $param") }
            .run { regEx.matchEntire(this)!!.groupValues[1] }
            .trim()
    }
}

arrayOf(
    "kotlin-version" to KotlinVersion.CURRENT.toString(),
    "java-version" to System.getProperty("java.version"),
    *arrayOf(
        VerCliRule("os-version", "lsb_release -a", "Description:(.*)".toRegex()),
        VerCliRule("intellij-version", "idea --version", "(IntelliJ .*)".toRegex())
    ).map { it.param to it.value() }.toTypedArray()
)
    .joinToString("\n") { ":${it.first}: ${it.second}" }
    .toFile("versions.adoc")
    .println()

fun footerHtml(title: String): String {
    return """
            <div class="twd1">
                <div>$title</div>
                <img 
                    src="images/tw-1-1v.png"  
                    alt="TechWriter Days #1" 
                    class="tw-days"/>
            </div>""".trimIndent()
}

//tag::conversion[]
File("twd1-kotlin.adoc")
    .run { AsciidocHtmlFactory.getHtmlFromFile(this) }
    .run {
        val title = Jsoup.parse(this).selectFirst("h1")?.text()
            ?: "Title undefined"
        Jsoup.parse(this).apply {
            select("section:not(.title):not(.no-footer)")
                .forEach { it.append(footerHtml(title)) }
        }.toString()
    }.toFile("twd1-kotlin.html")
//end::conversion[]

"Presentation built"