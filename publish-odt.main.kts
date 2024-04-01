@file:DependsOn("ru.curs:universal-markup-converter:0.5")
@file:DependsOn("org.asciidoctor:asciidoctorj:2.5.11")

import fodt.FodtGenerator
import model.Document
import model.ListItem
import model.OpenBlock
import model.Paragraph
import org.asciidoctor.Asciidoctor
import org.asciidoctor.Options
import org.redundent.kotlin.xml.PrintOptions
import org.xml.sax.InputSource
import reader.GenericHtmlReader
import reader.HtmlNode
import writer.*
import java.io.File
import java.io.StringReader
import javax.xml.parsers.DocumentBuilderFactory

object AsciidocHtmlFactory {
    private val factory: Asciidoctor = Asciidoctor.Factory.create()
    fun getHtml(string: String): String {
        return factory.convert(
            string,
            Options.builder().standalone(true).backend("html5").sourcemap(true).build()
        )
    }
}

val html = AsciidocHtmlFactory.getHtml(File("plan.adoc").readText())
File("plan-kotlin-tw.html").writeText(html)

val unimarkDocument = Document().apply {
    GenericHtmlReader(
        this,
        HtmlNode(html).selectAtXpath("/html/body")!!,
        setOf("ulist", "title")
    ).apply { iterateAll() }
    descendant { it is OpenBlock }.map { it as OpenBlock }.forEach { openBlock ->
        if (openBlock.roles.contains("title")) {
            openBlock.addAfter(Paragraph()).apply {
                openBlock.descendant().forEach {
                    addChild(it)
                }
            }
            openBlock.removeSelf()
        }
    }
}
val odtStyleList = OdtStyleList(
    OdtStyle {
        if (!it.roles.contains("title")) return@OdtStyle
        paragraphProperties { attribute("fo:keep-with-next", "always") }
    }, OdtStyle {
        val condition = it is Paragraph
                && it == it.parent()?.children()?.first()
                && it != it.parent()?.children()?.last()
        if (!condition) return@OdtStyle
        paragraphProperties { attribute("fo:keep-with-next", "always") }
    }, OdtStyle {
        val condition = it is Paragraph && it.parent() is ListItem
        if (!condition) return@OdtStyle
//        paragraphProperties { attribute("fo:text-indent", "0mm") }
    }
)
val preOdtString = OdWriter(odtStyle = odtStyleList).apply { unimarkDocument.write(this) }.preOdNode
    .toString(PrintOptions(pretty = false))

fun String.parseStringAsXML(): org.w3c.dom.Document {
    return DocumentBuilderFactory.newInstance().apply { isNamespaceAware = true }.newDocumentBuilder()
        .parse(InputSource(StringReader(this)))
}

val fodtDOM = FodtGenerator(
    preOdtString.parseStringAsXML(), File("template.fodt").readText()
        .parseStringAsXML()
)

File("plan-kotlin-tw.fodt").writeText(fodtDOM.serialize())


