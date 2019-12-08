#! /usr/bin/env python3
import fileinput
import sys

# used to store a parsed TL expressions which are
# constant numbers, constant strings, variable names, and binary expressions
class Expr :
    def __init__(self,op1,operator,op2=None):
        self.op1 = op1
        self.operator = operator
        self.op2 = op2

    def __str__(self):
        if self.op2 == None:
            return self.operator + " " + self.op1
        else:
            return self.op1 + " " + self.operator + " " +  self.op2

    # evaluate this expression given the environment of the symTable
    def eval(self, symTable):
        # print(symTable)
        # print(self.op1, self.op2, self.operator)
        error = "!ErRoR!"
        #print(self.operator, self.op1)
        if self.operator == "Var":
            if self.op1.isnumeric():
                return float(self.op1)
            return lookupVar(self.op1, symTable)
        elif self.operator == "Str":
            return self.op1
        elif self.operator == "+":
            return float(parseExpr(self.op1).eval(symTable)) + float(parseExpr(self.op2).eval(symTable))
        elif self.operator == "-":
            return float(parseExpr(self.op1).eval(symTable)) - float(parseExpr(self.op2).eval(symTable))
        elif self.operator == "*":
            return float(parseExpr(self.op1).eval(symTable)) * float(parseExpr(self.op2).eval(symTable))
        elif self.operator == "/":
            return float(parseExpr(self.op1).eval(symTable)) / float(parseExpr(self.op2).eval(symTable))
        elif self.operator == "<":
            return float(parseExpr(self.op1).eval(symTable)) < float(parseExpr(self.op2).eval(symTable))
        elif self.operator == ">":
            return float(parseExpr(self.op1).eval(symTable)) > float(parseExpr(self.op2).eval(symTable))
        elif self.operator == "<=":
            return float(parseExpr(self.op1).eval(symTable)) <= float(parseExpr(self.op2).eval(symTable))
        elif self.operator == ">=":
            return float(parseExpr(self.op1).eval(symTable)) >= float(parseExpr(self.op2).eval(symTable))
        elif self.operator == "==":
            return float(parseExpr(self.op1).eval(symTable)) == float(parseExpr(self.op2).eval(symTable))
        elif self.operator == "!=":
            return float(parseExpr(self.op1).eval(symTable)) != float(parseExpr(self.op2).eval(symTable))
        else:
            return error

# used to store a parsed TL statement
class Stmt :
    def __init__(self,keyword,exprs,var=None):
        self.keyword = keyword
        self.exprs = exprs
        self.var = var

    # perform/execute this statement given the environment of the symTable
    def perform(self, symTable):
        # a string that symbolizes error.
        error = "!ErRoR!"
        # print ("Doing: " + str(self))
        # try-except block to catch all the error and return error value for generating warning.
        try:
            if self.keyword == "let":
                symTable[self.var] = self.exprs.eval(symTable)
                if self.exprs.eval(symTable) == error or self.exprs.eval(symTable) == '':
                    return error
            elif self.keyword == "print":
                # print(self.exprs)
                if error in self.exprs:
                    return error
                s = str()
                for i in self.exprs:
                    if i.eval(symTable) == error:
                        return error
                    s += str(i.eval(symTable)) + ' '
                print(s[:-1])
            elif self.keyword == "input":
                instr = input()
                if instr.isnumeric():
                    symTable[self.exprs] = float(instr)
                else:
                    symTable[self.exprs] = instr
            elif self.keyword == "if":
                if self.exprs.eval(symTable) == error:
                    return error
                if self.exprs.eval(symTable):
                    return lookupVar(self.var, symTable)
            else:
                return error
            return None
        except:
            return error

# check if the string meets the requirement for a label
def isLabel(label):
    allAlpha = (x.isalnum() for x in label[:-1])
    if(allAlpha and len(label) > 1 and label[-1] == ':'):
        return True
    return False

# resolve variable according to the symbol table.
def lookupVar(valName, symTable):
    # handles float numbers that are wrongly parsed as variable.
    if valName[0].isnumeric():
        return float(valName)
    error = "!ErRoR!"
    # print([valName])
    if valName in symTable:
        return symTable[valName]
    else:
        return error

# takes in a list of strings or string and parse into a Expr
def parseExpr(expr):
    # print(expr)
    if type(expr) is Expr:
        return expr
    if (expr[0] == "\"" and expr[-1]) == "\"" or (expr[0] == "\'" and expr[-1] == "\'"):
        return Expr(expr[1:-1], "Str")
    if len(expr) == 1:
        if expr[0][0] == "\"" or expr[0][0] == "\'":
            return Expr(expr[0][1:-1], "Str")
        return Expr(expr[0], "Var")
    if "+" in expr:
        i = expr.index("+")
        return Expr(parseExpr(expr[:i]), "+", parseExpr(expr[i+1:]))
    elif "-" in expr:
        i = expr.index("-")
        return Expr(parseExpr(expr[:i]), "-", parseExpr(expr[i+1:]))
    elif "*" in expr:
        i = expr.index("*")
        return Expr(parseExpr(expr[:i]), "*", parseExpr(expr[i+1:]))
    elif "/" in expr:
        i = expr.index("/")
        return Expr(parseExpr(expr[:i]), "/", parseExpr(expr[i+1:]))
    elif "<" in expr:
        i = expr.index("<")
        return Expr(parseExpr(expr[:i]), "<", parseExpr(expr[i+1:]))
    elif ">" in expr:
        i = expr.index(">")
        return Expr(parseExpr(expr[:i]), ">", parseExpr(expr[i+1:]))
    elif "<=" in expr:
        i = expr.index("<=")
        return Expr(parseExpr(expr[:i]), "<=", parseExpr(expr[i+1:]))
    elif ">=" in expr:
        i = expr.index(">=")
        return Expr(parseExpr(expr[:i]), ">=", parseExpr(expr[i+1:]))
    elif "==" in expr:
        i = expr.index("==")
        return Expr(parseExpr(expr[:i]), "==", parseExpr(expr[i+1:]))
    elif "!=" in expr:
        i = expr.index("!=")
        return Expr(parseExpr(expr[:i]), "!=", parseExpr(expr[i+1:]))
    else:
        return Expr("", "Str")

# takes a list of string and parse into a Stmt.
def parseStmt(line, symTable):
    error = "!ErRoR!"
    # print(line)
    # if line[0][0] == '\t':
    #     line[0] = line[0][1:]
    if line[0] == "let":
        return Stmt(line[0], parseExpr(line[3:]), line[1])
    elif line[0] == "if":
        i = line.index("goto")
        return Stmt(line[0], parseExpr(line[1:i]), line[i+1]+":")
    elif line[0] == "input":
        return Stmt(line[0], line[1])
    elif line[0] == "print":
        raw = line[1:]
        if len(raw) == 1:
            return Stmt(line[0], [parseExpr(raw)])
        raws = str()
        for i in raw:
            if i is '':
                raws += ' '
            else:
                raws += i + ' '
        pexprs = list()
        while len(raws) > 0:
            # print(raws)
            if raws[0] == "\"":
                try:
                    i = raws.index("\" ,", 1)
                except(ValueError):
                    if raws[-2] == "\"":
                        pexprs.append(parseExpr(raws[:-1]))
                        break
                    else:
                        return error
                pexprs.append(parseExpr(raws[:i+1]))
                raws = raws[i+4:]
            elif raws[0] == "\'":
                try:
                    i = raws.index("\' ,", 1)
                except(ValueError):
                    if raws[-2] == "\"":
                        pexprs.append(parseExpr(raws[:-1]))
                        break
                    else:
                        return error
                pexprs.append(parseExpr(raws[:i+1]))
                raws = raws[i+4:]
            else:
                try:
                    i = raws.index(" , ")
                except(ValueError):
                    pexprs.append(parseExpr(raws.split()))
                    break
                pexprs.append(parseExpr([raws[:i]]))
                raws = raws[i+3:]
        return Stmt(line[0], pexprs)
    else:
        return error

# handle each line of the code and return a list of Stmts.
def parseLine(content, symTable):
    stmts = list()
    for lineNum in range(len(content)):
        if not content[lineNum]:
            stmts.append(None)
            continue
        line = content[lineNum]
        line = line.replace("\t", " ")
        line = line.split(" ")
        # print(line)
        if line[0] == ' ':
            line[0] = line[0][1:]
        if isLabel(line[0]):
            symTable[line[0]] = lineNum
            stmts.append(parseStmt(line[1:], symTable))
        elif line[0] is '':
            stmts.append(parseStmt(line[1:], symTable))
        else:
            stmts.append(parseStmt(line, symTable))
    # print(stmts)
    return stmts

# excutes the list of Stmt
def run(stmts, symTable):
    error = "!ErRoR!"
    lineNum = 0
    while lineNum < len(stmts):
        # print(stmts[lineNum])
        if stmts[lineNum] is None:
            lineNum += 1
            continue
        if stmts[lineNum] == error:
            print("Syntax error on line %d." %(lineNum+1))
            exit(1)
        val = stmts[lineNum].perform(symTable)
        # print("Running line: %d" %(lineNum+1))
        if val != None:
            if val == error:
                print("Syntax error on line %d." %(lineNum+1))
                exit(1)
            lineNum = val
        else:
            lineNum += 1
    return 0

# main function
def main():
    if len(sys.argv) != 2:
        print("Usage: python3 tiny.py FILENAME")
        sys.exit(1)
    file_name = sys.argv[1]
    fd = open(file_name, 'r')
    content = fd.read().split('\n')
    fd.close()
    symTable = dict()
    run(parseLine(content, symTable), symTable)
    #print(symTable)

if __name__ == "__main__":
    main()
