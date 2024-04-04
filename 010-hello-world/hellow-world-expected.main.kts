#!/usr/bin/env kotlin

println("Hello world")

var myName = "Nick"

println("Hello world".replace("world", myName))

// start

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

"Hello world 3".println()

// Переопределение унарного оператора `+`
operator fun String.unaryPlus() = println(this)

+"Hello world 4"

// Возврат результата в Kotlin Script Engine
"Hello world 5"

