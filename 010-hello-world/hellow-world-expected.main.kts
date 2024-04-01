#!/usr/bin/env kotlin

println("Hello world")

var myName = "Nick"

println("Hello world".replace("world", myName))

// Печать как часть процесса трансформации строки
// с помощью функции области видимости `apply`
"Hello world 2"
    .replace("world", myName)
    .apply { println(this) }

// Функции расширения: добавление
// метода println() во встроенный класс `String`
fun String.println(): String {
    println(this)
    return this
}

"Hello world 3"
    .replace("world", myName)
    .println()

// Переопределение унарного оператора `+`
operator fun String.unaryPlus() = println(this)

+"Hello world 5"

// Возврат результата в Kotlin Script Engine
"Hello world 4"
    .replace("world", myName)

