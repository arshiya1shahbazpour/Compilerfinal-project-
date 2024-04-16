package backend

abstract class Data

object None:Data() {
    override fun toString() = "None"
}
class IntData(val value: Int) : Data() {
    override fun toString() = value.toString()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as IntData
        return value == other.value
    }
    override fun hashCode(): Int {
        return value.hashCode()
    }

}
class FloatData(val value: Float) : Data() {
    override fun toString() = value.toString() + 'f'
    
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        
        other as FloatData
        return value == other.value
    }
    override fun hashCode(): Int {
        return value.hashCode()
    }
}

class DoubleData(val value: Double) : Data() {
    override fun toString() = value.toString()
    
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        
        other as DoubleData
        return value == other.value
    }
    override fun hashCode(): Int {
        return value.hashCode()
    }
}
class StringData(val value: String) : Data() {
    override fun toString() = value

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as StringData
        return value == other.value
    }
    override fun hashCode(): Int {
        return value.hashCode()
    }
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
class ArrayData(val elements: MutableList<Data?>): Data() {
    override fun toString() = elements.joinToString(prefix = "[", postfix = "]")
}

class SetData(val elements: MutableSet<Data>) : Data() {
    override fun toString() = elements.joinToString(prefix = "{", postfix = "}", separator = ", ")

    fun add(element: Data): Data {
        if (!elements.add(element)) {
            throw Exception("Element already exists in the set: $element")
        }
        return None
    }

    fun remove(element: Data): Data {
        if (!elements.remove(element)) {
            throw Exception("Element does not exist in the set: $element")
        }
        return None
    }

    fun contains(element: Data): BooleanData {
        return BooleanData(elements.contains(element))
    }
}

class MapData(val elements: MutableMap<Data, Data>) : Data() {
    override fun toString() = elements.entries.joinToString(prefix = "{", postfix = "}", separator = ", ") {
        "${it.key} -> ${it.value}"
    }
    fun put(key: Data, value: Data) {
        elements[key] = value
    }

    fun remove(key: Data) {
        if (!elements.containsKey(key)) {
            throw Exception("Key does not exist in the map: $key")
        }
        elements.remove(key)
    }

    fun get(key: Data): Data {
        return elements[key] ?: throw Exception("Key not found: $key")
    }
}