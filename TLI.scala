import scala.collection.mutable.Map
import scala.collection.mutable.ListBuffer
import scala.io.Source
import scala.io.StdIn._

// used to store a parsed TL expressions which are
// constant numbers, constant strings, variable names, and binary expressions
abstract class Expr
case class Var(name: String) extends Expr
case class Str(name: String) extends Expr
case class Constant(num: Double) extends Expr
case class BinOp(operator: String, left: Expr, right: Expr) extends Expr
case class ExprError() extends Expr

// used to store a parsed TL statement
abstract class Stmt
case class Let(variable: String, expr: Expr) extends Stmt
case class If(expr: Expr, label: String) extends Stmt
case class Input(variable: String) extends Stmt
case class Print(exprList: List[Expr]) extends Stmt
case class Error() extends Stmt
case class Empty() extends Stmt

// used to store any potential return value for a goto operation or error
class PerformResult(goto: Int = 0, error: Boolean = false) {
    def getLine(): Int = goto
    def getError(): Boolean = error

}

object TLI {
    def isLabel(label: String): Boolean = {
        if (label.length < 1 || label(label.length-1) != ':') {
            false
        } else {
            val subLabel = label.slice(0, label.length-1)
            var alphaNum = true
            // println(subLabel)
            for (c <- subLabel) {
                if (!(c.isDigit || c.isLetter)) {
                    alphaNum = false
                    // println(c)
                    // println(alphaNum)
                }
            }
            alphaNum
        }    
    }

    def eval(expr: Expr, symTab: Map[String, Double]): Double = expr match {
        case BinOp("+",e1,e2) => eval(e1,symTab) + eval(e2,symTab)
        case BinOp("-",e1,e2) => eval(e1,symTab) - eval(e2,symTab)
        case BinOp("*",e1,e2) => eval(e1,symTab) * eval(e2,symTab)
        case BinOp("/",e1,e2) => eval(e1,symTab) / eval(e2,symTab)
        case BinOp("<",e1,e2) => if (eval(e1,symTab) < eval(e2,symTab)) 1.0 else 0.0
        case BinOp(">",e1,e2) => if (eval(e1,symTab) > eval(e2,symTab)) 1.0 else 0.0
        case BinOp("<=",e1,e2) => if (eval(e1,symTab) <= eval(e2,symTab)) 1.0 else 0.0
        case BinOp(">=",e1,e2) => if (eval(e1,symTab) >= eval(e2,symTab)) 1.0 else 0.0
        case BinOp("==",e1,e2) => if (eval(e1,symTab) == eval(e2,symTab)) 1.0 else 0.0
        case BinOp("!=",e1,e2) => if (eval(e1,symTab) != eval(e2,symTab)) 1.0 else 0.0
        case Var(name) => symTab(name)
        case Constant(num) => num
	    case _ => Double.PositiveInfinity // symbolizes error
    }

    def parseExpr(expr: Array[String] = Array(), str: String = "NO"): Expr = {
        if (str != "NO") {
            Str(str.slice(1, str.length-1))
        } else if (expr.length == 1) {
            if (expr(0)(0).isDigit) {
                Constant(expr(0).toDouble)
            } else {
                Var(expr(0))
            }
        } else if (expr.contains("+")) {
            val i = expr.indexOf("+")
            BinOp("+", parseExpr(expr.slice(0, i)), parseExpr(expr.drop(i+1)))
        } else if (expr.contains("-")) {
            val i = expr.indexOf("-")
            BinOp("-", parseExpr(expr.slice(0, i)), parseExpr(expr.drop(i+1)))
        } else if (expr.contains("*")) {
            val i = expr.indexOf("*")
            BinOp("*", parseExpr(expr.slice(0, i)), parseExpr(expr.drop(i+1)))
        } else if (expr.contains("/")) {
            val i = expr.indexOf("/")
            BinOp("/", parseExpr(expr.slice(0, i)), parseExpr(expr.drop(i+1)))
        } else if (expr.contains("<")) {
            val i = expr.indexOf("<")
            BinOp("<", parseExpr(expr.slice(0, i)), parseExpr(expr.drop(i+1)))
        } else if (expr.contains(">")) {
            val i = expr.indexOf(">")
            BinOp(">", parseExpr(expr.slice(0, i)), parseExpr(expr.drop(i+1)))
        } else if (expr.contains("<=")) {
            val i = expr.indexOf("<=")
            BinOp("<=", parseExpr(expr.slice(0, i)), parseExpr(expr.drop(i+1)))
        } else if (expr.contains(">=")) {
            val i = expr.indexOf(">=")
            BinOp(">=", parseExpr(expr.slice(0, i)), parseExpr(expr.drop(i+1)))
        } else if (expr.contains("==")) {
            val i = expr.indexOf("==")
            BinOp("==", parseExpr(expr.slice(0, i)), parseExpr(expr.drop(i+1)))
        } else if (expr.contains("!=")) {
            val i = expr.indexOf("!=")
            BinOp("!=", parseExpr(expr.slice(0, i)), parseExpr(expr.drop(i+1)))
        } else {
            ExprError()
        }
    }

    def parseStmt(line: Array[String], symTab: Map[String, Double]): Stmt = {
        if (line(0) == "let") {
            Let(line(1), parseExpr(line.drop(3)))
        } else if (line(0) == "if") {
            val i = line.indexOf("goto")
            If(parseExpr(line.slice(1, i)), line(line.length-1) + ':')
        } else if (line(0) == "input") {
            Input(line(1))
        } else if (line(0) == "print") {
            var exprs = ListBuffer[Expr]()
            var raw = line.drop(1)
            if (raw.length == 1) {
                if (raw(0)(0) == '\"' || raw(0)(0) == '\''){
                    exprs += parseExpr(str = raw(0))
                } else {
                    exprs += parseExpr(raw)
                }
                Print(exprs.toList)
            } else {
                var raws = ""
                for (i <- raw) {
                    if (i.length == 0 || i == ' ' || i == " ") {
                        raws += " "
                    } else {
                        raws += i + " "
                    }
                }
                while (raws.length > 0) {
                    if (raws(0) == '\"') {
                        var i = raws.indexOf("\" ,", 1)
                        if (i == -1) {
                            if (raws(raws.length-1) == '\"') {
                                exprs += parseExpr(str = raws)
                                Print(exprs.toList)
                            } else {
                                Error()
                            }
                        } else {
                            exprs += parseExpr(str = raws.slice(0, i+1))
                            raws = raws.drop(i+4)
                        }
                    } else if (raws(0) == '\'') {
                        var i = raws.indexOf("\' ,", 1)
                        if (i == -1) {
                            if (raws(raws.length-1) == '\'') {
                                exprs += parseExpr(str = raws)
                                Print(exprs.toList)
                            } else {
                                Error()
                            }
                        } else {
                            exprs += parseExpr(str = raws.slice(0, i+1))
                            raws = raws.drop(i+4)
                        }
                    } else {
                        var i = raws.indexOf(" , ")
                        if (i == -1) {
                            exprs += parseExpr(raws.split(' '))
                            return Print(exprs.toList)
                        } else {
                            exprs += parseExpr(Array[String](raws.slice(0, i)))
                            raws = raws.drop(i+3)
                        }
                    }
                }
                Print(exprs.toList)
            }
        } else {
            Error()
        }
    }

    def parseLine(content: ListBuffer[Array[String]], symTab: Map[String, Double]): ListBuffer[Stmt] = {
        var stmts = ListBuffer[Stmt]()
        for (lineNum <- 0 to content.length-1) {
            if (content(lineNum).length == 0) {
                stmts += Empty()
            } else {
                var line = content(lineNum)
                if (line(0).length == 0 || line(0) == ' ' || line(0) == " ") {
                    line = line.drop(1)
                }
                if (isLabel(line(0))) {
                    symTab += (line(0) -> lineNum)
                    stmts += parseStmt(line.drop(1), symTab)
                } else if (line(0).length == 0) {
                    stmts += parseStmt(line.drop(1), symTab)
                } else {
                    stmts += parseStmt(line, symTab)
                }
            }
        }
        stmts
    }

    def perform(stmt: Stmt, symTab: Map[String, Double]): PerformResult = {
        try {
            if (stmt.isInstanceOf[Let]) {
                var letStmt = stmt.asInstanceOf[Let]
                symTab += (letStmt.variable -> eval(letStmt.expr, symTab))
                new PerformResult()
            } else if (stmt.isInstanceOf[Print]) {
                var s = ""
                var printStmt = stmt.asInstanceOf[Print]
                for (i <- printStmt.exprList) {
                    if (i.isInstanceOf[ExprError]) {
                        new PerformResult(error = true)
                    }
                    if (i.isInstanceOf[Str]) {
                        s += i.asInstanceOf[Str].name + " "
                    } else {
                        s += eval(i, symTab).toString() + " "
                    }
                }
                println(s.init)
                new PerformResult()
            } else if (stmt.isInstanceOf[Input]) {
                var inputStmt = stmt.asInstanceOf[Input]
                var input = readDouble()
                symTab += (inputStmt.variable -> input)
                new PerformResult()
            } else if (stmt.isInstanceOf[If]) {
                var ifStmt = stmt.asInstanceOf[If]
                if (ifStmt.expr.isInstanceOf[ExprError]) {
                    new PerformResult(error = true)
                }
                if (eval(ifStmt.expr, symTab) == 1.0) {
                    var goto = eval(Var(ifStmt.label), symTab)
                    new PerformResult(goto.toInt)
                } else {
                    new PerformResult()
                }
            } else if (stmt.isInstanceOf[Empty]) {
                new PerformResult()
            } else {
                new PerformResult(error = true)
            }
        } catch {
            case _: Throwable => new PerformResult(error = true)
        }
    }

    def run(stmts: ListBuffer[Stmt], symTab: Map[String, Double]): Unit = {
        var lineNum = 0
        while (lineNum < stmts.length) {
            if (stmts(lineNum).isInstanceOf[Error]) {
                println(s"Syntax error on line ${lineNum + 1}.")
                System.exit(1)
            } else if (stmts(lineNum).isInstanceOf[Empty]) {
                lineNum += 1
            } else {
                var result = perform(stmts(lineNum), symTab)
                if (result.getError()) {
                    println(s"Syntax error on line ${lineNum + 1}.")
                    System.exit(1)
                } else if (result.getLine() != 0) {
                    lineNum = result.getLine()
                } else {
                    lineNum += 1
                }
            }
        }
    }

    def main(args: Array[String]): Unit = {
    	if (args.length != 1) {
            println("Error: File name not provided. \nUsage: scala TLI FILENAME")
            System.exit(1)
        }
        val fileName = args(0)
        val fd = Source.fromFile(fileName)
        var content = ListBuffer[Array[String]]()
        for (line <- fd.getLines()) {
            content += line.replace('\t', ' ').split(' ')
        }
        var symTab = Map[String, Double]()
        fd.close()
        run(parseLine(content, symTab), symTab)
    }
}