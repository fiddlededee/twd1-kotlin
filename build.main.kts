#!/usr/bin/env kotlin
@file:DependsOn("org.asciidoctor:asciidoctorj:2.5.11")
@file:DependsOn("org.jsoup:jsoup:1.17.1")
@file:DependsOn("com.google.guava:guava:21.0")
@file:DependsOn("org.languagetool:language-ru:5.6")

import java.io.File
import org.asciidoctor.Asciidoctor
import org.asciidoctor.Options
import org.asciidoctor.SafeMode
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.languagetool.JLanguageTool
import org.languagetool.language.Russian
import org.languagetool.rules.spelling.SpellingCheckRule
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

fun String.jsoupParse(): Document {
    return Jsoup.parse(this)
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

//tag::versions[]
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
//end::versions[]

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

object LangTools {
    private val langTool = JLanguageTool(Russian())
    var ruleTokenExceptions: Map<String, Set<String>> = mapOf()
    var ruleExceptions: Set<String> = setOf("")

    fun setSpellTokens(
        ignore: Array<String>,
        accept: Array<String> = arrayOf()
    ): LangTools {
        langTool.allActiveRules.forEach { rule ->
            if (rule is SpellingCheckRule) {
                rule.addIgnoreTokens(ignore.toList())
                rule.acceptPhrases(accept.toList())
            }
        }
        return this
    }

    fun check(text: String) {
        val errs = langTool.check(text).filterNot {
            (this.ruleTokenExceptions[it.rule.id]?.contains(text.substring(it.fromPos, it.toPos)) ?: false) or
                    ((this.ruleExceptions).contains(it.rule.id))
        }

        if (errs.isNotEmpty()) {
            var errorMessage = "Spell failed for:\n$text\n"
            errs.forEachIndexed { index, it ->
                errorMessage += "[${index + 1}] ${it.message}, ${it.rule.id} (${it.fromPos}:${it.toPos} " +
                        "- ${text.substring(it.fromPos, it.toPos)})\n"
            }
            println(errorMessage.split("\n").map { it.chunked(120) }.flatten().joinToString("\n"))
        }
    }
}

//tag::conversion[]
File("twd1-kotlin.adoc")
    .run { AsciidocHtmlFactory.getHtmlFromFile(this) }
    .run {
        val title = Jsoup.parse(this).selectFirst("h1")?.text()
            ?: "Title undefined"
        this.jsoupParse().apply {
            select("section:not(.title):not(.no-footer)")
                .forEach { it.append(footerHtml(title)) }
        }.apply {
            this.selectXpath("//p").forEach {
                LangTools
                    .apply {
                        setSpellTokens(
                            ignore = arrayOf(
                                "Ишуи",
                                "шаблонизаторах",
                                "препроцессинг",
                                "десериализации",
                                "постпроцессинга",
                            )
                        )
                        ruleExceptions = setOf(
                            "UPPERCASE_SENTENCE_START",
                            "RU_UNPAIRED_BRACKETS"
                        )
                    }
                    .check(it.text())
            }
        }
            .toString()
    }.toFile("twd1-kotlin.html")
//end::conversion[]

"Presentation built"