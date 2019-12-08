# Tiny Language Interpreter

This is a **Tiny Language Interpreter** implemented in Python3. 
This was an assignment from Professor Charlie McDowell's CSE112 (Fall 2019) at UC Santa Cruz.

# Introduction to Tiny Language

The idea was invented by **Professor Charlie McDowell** of UC Santa Cruz, one of my favorite professor.
Tiny Language only four types of statements:

     - let variableName = expression 
     - if expression goto label 
     - print expression1, expression2, ...
     - input variableName
 Label is also support in Tiny Language. A label is defined as an alphanumeric string ending with a colon (":").
 There are also some rules for Tiny:
 
 1. White space (blanks) are important and must be used to separate each token including around the arithmetic operators. 
 2. There can be only one statement per line. 
 3. The expressions are limited to constant numbers, constant strings, variable names, and binary expressions involving just one of the following operators: "+", "-", "*", "/", "<", ">", "<=", ">=", "==", or "!=", with their conventional meanings. Note again that the operators must be surrounded by spaces, which makes for easier parsing. 
 4. The only types are strings and floating point numbers and strings are only used in print statements. The result of Boolean operations is 0 if false and 1 if true. Furthermore any numeric expression can be used in an if-statement and as with the C language, 0 is false and everything else is true.
 5.  Blank lines are ignored.

# Usage

Simply type `python3 tiny.py FILENAME` in your terminal.

# File

 - `tiny.py` 	The main interpreter program in Python 3.
 - `tests`		The directory containing the test cases along with the input/expected output provided by Prof. Charlie McDowell.
