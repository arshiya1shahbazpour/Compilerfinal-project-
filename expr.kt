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
        if(x !is IntData || y !is IntData) {
            throw Exception("cannot handle non-integer")
        }
        return IntData(
            when(op) {
                Operator.Add -> x.value + y.value
                Operator.Sub -> x.value - y.value
                Operator.Mul -> x.value * y.value
                Operator.Div -> {
                    if(y.value != 0) {
                        x.value / y.value
                    } else {
                        throw Exception("cannot divide by zero")
                    }
                }
                else -> throw Exception("Unknown operator")
            }
        )
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
    override fun eval(runtime:Runtime): Data {
        val x = left.eval(runtime)
        val y = right.eval(runtime)
        if(x is IntData && y is IntData) {
            return BooleanData(
                when(comparator) {
                    Comparator.LT -> x.value < y.value
                    Comparator.LE -> x.value <= y.value
                    Comparator.GT -> x.value > y.value
                    Comparator.GE -> x.value >= y.value
                    Comparator.EQ -> x.value == y.value
                    Comparator.NE -> x.value != y.value
                }
            )
        } else {
            throw Exception("Non-integer data in comparison")
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


class ArrayAccessExpr(val identifier: String, val index: Expr): Expr() {
    override fun eval(runtime: Runtime): Data {
        // Retrieve the array from the runtime environment
        val array = runtime.symbolTable[identifier] as? ArrayData
            ?: throw RuntimeException("Array not found: $identifier")

        // Evaluate the index expression and ensure it produces an integer
        val indexValue = index.eval(runtime)
        val indexInt = when(indexValue) {
            is IntData -> indexValue.value
            else -> throw RuntimeException("Index must be an integer")
        }

        // Check if the index is within the bounds of the array
        if (indexInt < 0 || indexInt >= array.elements.size) {
            throw RuntimeException("Array index out of bounds: $indexInt")
        }

        // Return the array element at the given index
        return array.elements[indexInt]
            ?: throw RuntimeException("Array element at index $indexInt is null")
    }
}

