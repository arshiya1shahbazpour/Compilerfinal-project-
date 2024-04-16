package backend

abstract class Expr {
    abstract fun eval(runtime:Runtime):Data
}

class NoneExpr(): Expr() {
    override fun eval(runtime:Runtime) = None
}

class IntLiteral(val lexeme:String): Expr() {
    override fun eval(runtime:Runtime): Data
    = IntData(Integer.parseInt(lexeme))
}
class FloatLiteral(val lexeme: String): Expr() {
    override fun eval(runtime: Runtime): Data 
    = FloatData(lexeme.toFloat())
}

class DoubleLiteral(val lexeme: String): Expr() {
    override fun eval(runtime: Runtime): Data 
    = DoubleData(lexeme.toDouble())
}
class StringLiteral(val lexeme:String): Expr() {
    override fun eval(runtime:Runtime): Data
    = StringData(lexeme)
}

enum class Operator {
    Add,
    Sub,
    Mul,
    Div
}
class Arithmetics(
    val op:Operator,
    val left:Expr,
    val right:Expr
): Expr() {
    override fun eval(runtime:Runtime): Data {
        val x = left.eval(runtime)
        val y = right.eval(runtime)
        
        
        if (op == Operator.Mul && x is StringData && y is IntData) {
            return StringData(x.value.repeat(y.value))
        }
        
        when (op) {
            Operator.Add -> {
                if (x is IntData && y is DoubleData) {
                    return DoubleData(x.value + y.value)
                }
                if (x is DoubleData && y is IntData) {
                    return DoubleData(x.value + y.value)
                }
                if (x is FloatData && y is DoubleData) {
                    return DoubleData(x.value + y.value)
                }
                if (x is DoubleData && y is FloatData) {
                    return DoubleData(x.value + y.value)
                }
                if (x is DoubleData && y is DoubleData) {
                    return DoubleData(x.value + y.value)
                }
                if (x is IntData && y is IntData) {
                    return IntData(x.value + y.value)
                }
                if (x is FloatData && y is FloatData) {
                    return FloatData(x.value + y.value)
                }
            }
            Operator.Sub -> {
                if (x is IntData && y is DoubleData) {
                    return DoubleData(x.value - y.value)
                }
                if (x is DoubleData && y is IntData) {
                    return DoubleData(x.value - y.value)
                }
                if (x is FloatData && y is DoubleData) {
                    return DoubleData(x.value - y.value)
                }
                if (x is DoubleData && y is FloatData) {
                    return DoubleData(x.value - y.value)
                }
                if (x is DoubleData && y is DoubleData) {
                    return DoubleData(x.value - y.value)
                }
                if (x is IntData && y is IntData) {
                    return IntData(x.value - y.value)
                }
                if (x is FloatData && y is FloatData) {
                    return FloatData(x.value - y.value)
                }
            }
            Operator.Mul -> {
                if (x is IntData && y is DoubleData) {
                    return DoubleData(x.value * y.value)
                }
                if (x is DoubleData && y is IntData) {
                    return DoubleData(x.value * y.value)
                }
                if (x is FloatData && y is DoubleData) {
                    return DoubleData(x.value * y.value)
                }
                if (x is DoubleData && y is FloatData) {
                    return DoubleData(x.value * y.value)
                }
                if (x is DoubleData && y is DoubleData) {
                    return DoubleData(x.value * y.value)
                }
                if (x is IntData && y is IntData) {
                    return IntData(x.value * y.value)
                }
                if (x is FloatData && y is FloatData) {
                    return FloatData(x.value * y.value)
                }
            }
            Operator.Div -> {
                if (x is IntData && y is DoubleData) {
                    return DoubleData(x.value / y.value)
                }
                if (x is DoubleData && y is IntData) {
                    return DoubleData(x.value / y.value)
                }
                if (x is FloatData && y is DoubleData) {
                    return DoubleData(x.value / y.value)
                }
                if (x is DoubleData && y is FloatData) {
                    return DoubleData(x.value / y.value)
                }
                if (x is DoubleData && y is DoubleData) {
                    return DoubleData(x.value / y.value)
                }
                if (x is IntData && y is IntData) {
                    return IntData(x.value / y.value)
                }
                if (x is FloatData && y is FloatData) {
                    return FloatData(x.value / y.value)
                }
            }
        }
        return IntData(0)
    }
}
class BooleanLiteral(val lexeme:String): Expr() {
    override fun eval(runtime:Runtime): Data
    = BooleanData(lexeme.equals("true"))
}

class Assign(val symbol:String, val expr:Expr): Expr() {
    override fun eval(runtime:Runtime): Data
    = expr.eval(runtime).apply {
        runtime.symbolTable.put(symbol, this)
    }
}
class Deref(val name:String): Expr() {
    override fun eval(runtime:Runtime):Data {
        val data = runtime.symbolTable[name]
        if(data == null) {
            throw Exception("$name is not assigned.")
        }
        return data
    }
}
class Block(val exprList: List<Expr>): Expr() {
    override fun eval(runtime:Runtime): Data {
        var result:Data = None
        exprList.forEach {
            result = it.eval(runtime)
        }
        return result
    }
}
enum class Comparator {
    LT,
    LE,
    GT,
    GE,
    EQ,
    NE,
}
class Compare(
    val comparator: Comparator,
    val left: Expr,
    val right: Expr
): Expr() {
    override fun eval(runtime: Runtime): Data {
        val x = left.eval(runtime)
        val y = right.eval(runtime)

        val xVal = when (x) {
            is IntData -> x.value.toDouble()  // Convert Int to Double
            is FloatData -> x.value.toDouble()  // Convert Float to Double
            is DoubleData -> x.value  // Already Double
            else -> throw Exception("Left operand is not a numeric type")
        }

        val yVal = when (y) {
            is IntData -> y.value.toDouble()  // Convert Int to Double
            is FloatData -> y.value.toDouble()  // Convert Float to Double
            is DoubleData -> y.value  // Already Double
            else -> throw Exception("Right operand is not a numeric type")
        }

        return BooleanData(compareValues(xVal, yVal))
    }

    private fun compareValues(x: Double, y: Double): Boolean {
        return when (comparator) {
            Comparator.LT -> x < y
            Comparator.LE -> x <= y
            Comparator.GT -> x > y
            Comparator.GE -> x >= y
            Comparator.EQ -> x == y
            Comparator.NE -> x != y
        }
    }
}

class Ifelse(
    val cond:Expr,
    val trueExpr:Expr,
    val falseExpr:Expr
): Expr() {
    override fun eval(runtime:Runtime): Data {
        val cond_data = cond.eval(runtime)
        if(cond_data !is BooleanData) {
            throw Exception("need boolean data in if-else")
        }
        return if(cond_data.value) {
            return trueExpr.eval(runtime)
        } else {
            return falseExpr.eval(runtime)
        }
    }
}
class While(val cond:Expr, val body:Expr): Expr() {
    override fun eval(runtime:Runtime): Data {
        var flag = cond.eval(runtime) as BooleanData
        var result:Data = None
       
        var iter:Int = 1_000_000
        while(flag.value) {
            result = body.eval(runtime)
            flag = cond.eval(runtime) as BooleanData
            if(iter == 0) {
                println("MAX_ITER reached")
                println(runtime)
                return None
            }
            iter --
        }
        return result
    }
}
class Declare(
    val name: String,
    val params: List<String>,
    val body: Expr
): Expr() {
    override fun eval(runtime:Runtime):Data
    = FuncData(name, params, body).also {
        runtime.symbolTable[name] = it
    }
}

class Invoke(val name:String, val args:List<Expr>):Expr() {
    override fun eval(runtime:Runtime):Data {
        val func:Data? = runtime.symbolTable[name]
        if(func == null) {
            throw Exception("$name does not exist")
        }
        if(func !is FuncData) {
            throw Exception("$name is not a function.")
        }
        if(func.params.size != args.size) {
            throw Exception(
                "$name expects ${func.params.size} arguments "
                + "but received ${args.size}"
            )
        }
        
        val r = runtime.subscope(
            func.params.zip(args.map {it.eval(runtime)}).toMap()
        )
        return func.body.eval(r)
    }
}
class ForLoop(
    val loopVar: String, 
    val startExpr: Expr, 
    val endExpr: Expr, 
    val body: Expr
) : Expr() {
    override fun eval(runtime: Runtime): Data {
        val start = startExpr.eval(runtime)
        val end = endExpr.eval(runtime)

        if (start !is IntData || end !is IntData) {
            throw Exception("For loop bounds must be integers")
        }

        var lastValue: Data = None
        for (i in start.value..end.value) {
            runtime.symbolTable[loopVar] = IntData(i)
            lastValue = body.eval(runtime)
        }
        return lastValue 
    }
}

class Concatenation(
    val left: Expr,
    val right: Expr
) : Expr() {
    override fun eval(runtime: Runtime): Data {
        val leftEval = left.eval(runtime)
        val rightEval = right.eval(runtime)
        val leftStr = when (leftEval) {
            is StringData -> leftEval.value
            is IntData -> leftEval.value.toString()
            else -> throw Exception("Unsupported data type for concatenation")
        }

        val rightStr = when (rightEval) {
            is StringData -> rightEval.value
            is IntData -> rightEval.value.toString()
            else -> throw Exception("Unsupported data type for concatenation")
        }
        return StringData(leftStr + rightStr)
    }
}
class Print(val args: List<Expr>): Expr() {
    override fun eval(runtime:Runtime): Data {
        args.forEach { arg ->
            val data = arg.eval(runtime)
            println(data) 
        }
        return None 
    }
}
class ArrayCreationExpr(val elements: List<Expr>): Expr() {
    override fun eval(runtime: Runtime): Data = 
        ArrayData(elements.map { it.eval(runtime) }.toMutableList())
}

class ArrayAccessExpr(val arrayExpr: String, val indexExpr: Expr): Expr() {
    override fun eval(runtime: Runtime): Data {
        val array = runtime.symbolTable[arrayExpr] ?: throw Exception("Array not found: $arrayExpr")
        val index = indexExpr.eval(runtime)
        if (index !is IntData) {
            throw Exception("Array index must be an integer.")
        }
        if (array !is ArrayData) {
            throw Exception("Attempt to index a non-array type.")
        }
        if (index.value < 0 || index.value >= array.elements.size) {
            throw Exception("Array index out of bounds: ${index.value}")
        }
        return array.elements[index.value]!!
    }
}

class ArrayUpdateExpr(val arrayName: String, val indexExpr: Expr, val newValueExpr: Expr) : Expr() {
    override fun eval(runtime: Runtime): Data {
         val arrayObject = runtime.symbolTable[arrayName]
            if (arrayObject == null) {
                throw NoSuchElementException("Array not found: $arrayName")
            }
            if (arrayObject !is ArrayData) {
                throw IllegalArgumentException("The variable '$arrayName' is not an array.")
            }
            val index = indexExpr.eval(runtime)
            if (index !is IntData) {
                throw IllegalArgumentException("Index must be an integer")
            }
            if (index.value < 0 || index.value >= arrayObject.elements.size) {
                throw IndexOutOfBoundsException("Index out of bounds: ${index.value}")
            }
            val newValue = newValueExpr.eval(runtime)
            arrayObject.elements[index.value] = newValue
            return newValue
    }
}


class SetCreationExpr(val elements: List<Expr>) : Expr() {
    override fun eval(runtime: Runtime): Data {
        val setElements = mutableSetOf<Data>()
        for (expr in elements) {
            val element = expr.eval(runtime)
            if (!setElements.add(element)) {
                throw Exception("Duplicate element detected during set creation: $element")
            }
        }
        return SetData(setElements)
    }
}


class AddToSetExpr(val setName: String, val elementExpr: Expr) : Expr() {
    override fun eval(runtime: Runtime): Data {
        val setObject = runtime.symbolTable[setName]
            if (setObject == null) {
                throw NoSuchElementException("No set found with name $setName")
            }
            if (setObject !is SetData) {
                throw IllegalArgumentException("The variable '$setName' is not a set.")
            }
            val element = elementExpr.eval(runtime)
            setObject.add(element)
            return None 
    }
}

class RemoveFromSetExpr(val setName: String, val elementExpr: Expr) : Expr() {
    override fun eval(runtime: Runtime): Data {
            val setObject = runtime.symbolTable[setName]
            if (setObject == null) {
                throw NoSuchElementException("No set found with name $setName")
            }
            if (setObject !is SetData) {
                throw IllegalArgumentException("The variable '$setName' is not a set.")
            }
            val element = elementExpr.eval(runtime)
             if (!setObject.elements.contains(element)) {
                throw NoSuchElementException("Element not found in set: $element")
            }
            setObject.remove(element)
            return None 
    }
}

class ContainsInSetExpr(val setName: String, val elementExpr: Expr) : Expr() {
    override fun eval(runtime: Runtime): Data {
            val setObject = runtime.symbolTable[setName]
            if (setObject == null) {
                throw NoSuchElementException("set Not found: $setName")
            }
            if (setObject !is SetData) {
                throw IllegalArgumentException("The variable '$setName' is not a set.")
            }
            val element = elementExpr.eval(runtime)
            return setObject.contains(element)
    }
}

class PairExpr(val key: Expr, val value: Expr) : Expr() {
    override fun eval(runtime: Runtime): Data {
        val keyData = key.eval(runtime)
        val valueData = value.eval(runtime)
        return MapData(mutableMapOf(keyData to valueData))
    }
}

class MapCreationExpr(val pairs: List<PairExpr>) : Expr() {
    override fun eval(runtime: Runtime): Data {
       val mapData = MapData(mutableMapOf())
       for (pairExpr in pairs) {
           val pair = pairExpr.eval(runtime)
            if (pair !is MapData) {
                throw RuntimeException("Error while creating the map")
            }
            mapData.elements.putAll(pair.elements)
        }
        return mapData
    }
}

class MapPutExpr(val mapName: String, val keyExpr: Expr, val valueExpr: Expr) : Expr() {
    override fun eval(runtime: Runtime): Data {
            val mapObject = runtime.symbolTable[mapName]
            if (mapObject == null) {
                throw NoSuchElementException("Map not found: $mapName")
            }
            if (mapObject !is MapData) {
                throw IllegalArgumentException("The variable '$mapName' is not a map.")
            }
            val key = keyExpr.eval(runtime)
            val value = valueExpr.eval(runtime)
            mapObject.put(key, value)
            return None  
    }
}

class MapRemoveExpr(val mapName: String, val keyExpr: Expr) : Expr() {
    override fun eval(runtime: Runtime): Data {
            val mapObject = runtime.symbolTable[mapName]
            if (mapObject == null) {
                throw NoSuchElementException("Map not found: $mapName")
            }
            if (mapObject !is MapData) {
                throw IllegalArgumentException("The variable '$mapName' is not a map.")
            }
            val key = keyExpr.eval(runtime)
            val value = mapObject.elements[key]
            if (value == null) {
                throw NoSuchElementException("Key not found in map: $key")
            }
           mapObject.remove(key)
            return None
}
}

class MapAccessExpr(val mapName: String, val keyExpr: Expr) : Expr() {
    override fun eval(runtime: Runtime): Data {
            val mapObject = runtime.symbolTable[mapName]
            if (mapObject == null) {
                throw NoSuchElementException("Map not found: $mapName")
            }
            if (mapObject !is MapData) {
                throw IllegalArgumentException("The variable '$mapName' is not a map.")
            }
            val key = keyExpr.eval(runtime)
            val value = mapObject.elements[key]
            if (value == null) {
                throw NoSuchElementException("Key not found in map: $key")
            }
            return value
}
}
class TraditionalForLoop(
    val init: Expr,
    val condition: Expr,
    val loopExpr: Expr,
    val body: Expr
): Expr() {
    override fun eval(runtime: Runtime): Data {
        init.eval(runtime) 
        while (true) {
            val cond = condition.eval(runtime)  
            if (cond !is BooleanData) {
                throw IllegalArgumentException("Condition of for loop must be Boolean.")
            }
            if (!cond.value){
                break
             }
            body.eval(runtime)  
            loopExpr.eval(runtime) 
        }
        return None  
    }
}

class ArrayLength(val arrayName: String): Expr() {
    override fun eval(runtime: Runtime): Data {
        try {
            val arrayData = runtime.symbolTable[arrayName] as ArrayData
            return IntData(arrayData.elements.size)
        } catch (e: ClassCastException) {
            throw RuntimeException("$arrayName is not an array")
        } catch (e: NullPointerException) {
            throw RuntimeException("Array not found: $arrayName")
        }
    }
}


