package backend

abstract class Data

object None:Data() {
    override fun toString() = "None"
}
class StringData(val value:String): Data() {
    override fun toString() = value;  
}
class IntData(val value:Int): Data() {
    override fun toString(): String = "Int:$value"
}
class DoubleData(val value:Double): Data() {
    override fun toString(): String = "Double:$value"
}

class FuncData(
    val name: String,
    val params: List<String>,
    val body: Expr
): Data() {
    override fun toString()
    = params.joinToString(", ").let {
        "$name($it) { ... }"
    }
}
class BooleanData(val value:Boolean): Data() {
    override fun toString() = 
    "Boolean:${value}"
}
class ArrayData(val elements: List<Data>): Data() {
    override fun toString() = elements.joinToString(prefix = "[", postfix = "]")
}