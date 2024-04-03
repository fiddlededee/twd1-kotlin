#!/usr/bin/env kotlin
@file:DependsOn("org.jsoup:jsoup:1.16.1")
@file:DependsOn("com.google.guava:guava:21.0")
@file:DependsOn("org.languagetool:language-ru:5.6")

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.languagetool.JLanguageTool
import org.languagetool.language.Russian
import org.languagetool.rules.spelling.SpellingCheckRule
import java.io.File

fun String.println(): String {
    println(this)
    return this
}

fun String.toFile(fileName: String): String {
    File(fileName).writeText(this)
    return this
}

fun String.jsoupParse(): Document {
    return Jsoup.parse(this)
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

val scriptDir: String = __FILE__.parent

// start

File("$scriptDir/text.html")
    .readText()
    .jsoupParse()
    .selectXpath("//p")
    .forEach { paragraph ->
        LangTools
            .setSpellTokens(ignore = arrayOf("десериализация"))
            .check(paragraph.text())
    }