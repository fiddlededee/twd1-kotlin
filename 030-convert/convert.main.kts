#!/usr/bin/env kotlin
@file:DependsOn("com.vladsch.flexmark:flexmark-all:0.64.8")
@file:DependsOn("com.github.ajalt.clikt:clikt-jvm:4.2.2")

import com.vladsch.flexmark.html.HtmlRenderer
import com.vladsch.flexmark.parser.Parser
import com.vladsch.flexmark.util.data.MutableDataSet
import java.io.File

fun String.println(): String {
    println(this)
    return this
}

fun String.toFile(fileName: String): String {
    File(fileName).writeText(this)
    return this
}

fun String.md2html() : String {
    val mdDoc = Parser.builder(MutableDataSet()).build().parse(this)
    return HtmlRenderer.builder(MutableDataSet()).build().render(mdDoc)
}

val scriptDir: String = __FILE__.parent

// start

