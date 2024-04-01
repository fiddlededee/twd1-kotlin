#!/usr/bin/env kotlin
@file:DependsOn("com.fasterxml.jackson.module:jackson-module-kotlin:2.16.1")
@file:DependsOn("org.redundent:kotlin-xml-builder:1.9.0")

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.redundent.kotlin.xml.PrintOptions
import org.redundent.kotlin.xml.xml
import java.io.File

fun String.println(): String {
    println(this)
    return this
}

fun String.toFile(fileName: String): String {
    File(fileName).writeText(this)
    return this
}

val json = File("filling-stations.json").readText()

data class Station(
    val streetAddress: String,
    val stationName: String, val cardsAccepted: String
)

val stations = jacksonObjectMapper()
    .setPropertyNamingStrategy(PropertyNamingStrategies.SnakeCaseStrategy())
    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    .readValue<Array<Station>>(json)
    .filter { it.cardsAccepted.matches(""".*[vV]oyager.*""".toRegex()) }

val letterAst = xml("topic") {
    doctype(systemId = "https://resources.jetbrains.com/writerside/1.0/xhtml-entities.dtd")
    arrayOf(
        "xmlns:xsi" to "http://www.w3.org/2001/XMLSchema-instance",
        "xsi:noNamespaceSchemaLocation" to "https://resources.jetbrains.com/writerside/1.0/topic.v2.xsd",
        "title" to "filling-stations",
        "id" to "filling-stations-auto",
        "is-library" to "true"
    ).forEach { attribute(it.first, it.second) }
    //tag::snippet[]
    "snippet" {
        attribute("id", "filling-stations-snippet")
        "p" { -"Dear Boss:" }
        "p" { -"Here are the CNG stations that accept Voyager cards:" }
        "table" {
            "tr" {
                arrayOf("Station", "Address", "Cards Accepted")
                    .forEach { "td" { "p" { -it } } }
            }
            stations.forEach { station ->
                "tr" {
                    arrayOf(
                        station.stationName, station.streetAddress,
                        station.cardsAccepted
                    ).forEach { "td" { "p" { -it } } }
                }
            }
        }
        "p" { -"Your loyal servant" }
        "p" { -"John Hancock" }
    }
    //end::snippet[]
}
letterAst.toString(PrintOptions(singleLineTextElements = true))
    .println()
    .toFile("080-builder/writerside/Writerside/topics/filling-stations-auto.topic")

"Script successfully finished"

