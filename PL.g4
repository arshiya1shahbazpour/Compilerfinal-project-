grammar PL;

@header {
import backend.*;
}

@members {
}

program returns [Expr expr]:
{  List<Expr> list = new ArrayList<Expr>();}
 (statement{list.add($statement.expr);})+ EOF 
{$expr = new Block(list); }
   ;

block returns [Expr expr] :
{  List<Expr> list = new ArrayList<Expr>();}
    '{' (statement {list.add($statement.expr);} )* '}' 
{$expr = new Block(list); }
    ;

statement returns [Expr expr] : 
    assignment (';'?) { $expr = $assignment.expr; }
    | expression (';') { $expr = $expression.expr; }
    | declaration { $expr = $declaration.expr; }
    | ifElseStatement { $expr = $ifElseStatement.expr; }
    | forLoop { $expr = $forLoop.expr; }
    | block  { $expr = $block.expr; }
    | printStatement ';'? { $expr = $printStatement.expr; } 
    ;

assignment returns [Expr expr] : 
    ID '=' expression { $expr = new Assign($ID.text, $expression.expr); }
    ;

expression returns [Expr expr] : 
    left=expression '*' right=expression { $expr= new Arithmetics(Operator.Mul, $left.expr, $right.expr); }
    | left=expression '/' right=expression { $expr= new Arithmetics(Operator.Div, $left.expr, $right.expr); }
    | left=expression '+' right=expression { $expr= new Arithmetics(Operator.Add, $left.expr, $right.expr); }
    | left=expression '-' right=expression { $expr = new Arithmetics(Operator.Sub, $left.expr, $right.expr); }
    | left=expression '++' right=expression { $expr = new Concatenation($left.expr, $right.expr); }
    | left=expression '<' right=expression { $expr = new Compare(Comparator.LT, $left.expr, $right.expr); }
    | left=expression '<=' right=expression { $expr = new Compare(Comparator.LE, $left.expr, $right.expr); }
    | left=expression '>' right=expression { $expr = new Compare(Comparator.GT, $left.expr, $right.expr); }
    | left=expression '>=' right=expression { $expr = new Compare(Comparator.GE, $left.expr, $right.expr); }
    | left=expression '==' right=expression { $expr = new Compare(Comparator.EQ, $left.expr, $right.expr); }
    | left=expression '!=' right=expression { $expr = new Compare(Comparator.NE, $left.expr, $right.expr); }
    |    '-' expression { $expr = new Arithmetics(Operator.Sub, new IntLiteral("0"), $expression.expr); }
    | value { $expr = $value.expr; }
    | '(' expression ')' { $expr = $expression.expr; }
    | funCall { $expr = $funCall.expr; }
    | arrayCreation { $expr = $arrayCreation.expr; }
    | arrayAccess { $expr = $arrayCreation.expr; }
    ;

ifElseStatement returns [Expr expr] :
    'if' '(' expression ')' trueBlock=block ('else' falseBlock=block)? {
        Expr falseExpr = $falseBlock.expr!= null ? $falseBlock.expr: new NoneExpr();
        $expr= new Ifelse($expression.expr, $trueBlock.expr, falseExpr);
    }
    ;


forLoop returns [Expr expr] :
    'for' '(' ID 'in' startExpr=expression '..' endExpr=expression ')' body=block {
        $expr = new ForLoop($ID.text, $startExpr.expr, $endExpr.expr, $body.expr);
    }
    ;


declaration returns [Expr expr] :
    'function' ID '(' params=paramList? ')' block {
        List<String> paramNames = $params.list != null ? $params.list : new ArrayList<>();
        $expr = new Declare($ID.text, paramNames, $block.expr);
    }
    ;

funCall returns [Expr expr] : 
    ID '(' args=argList? ')' {
        List<Expr> arguments = $args.list != null ? $args.list : new ArrayList<>();
        $expr = new Invoke($ID.text, arguments);
    }
    ;
argList returns [List<Expr> list] : 
    { $list = new ArrayList<Expr>(); }
    (expression { $list.add($expression.expr); } (',' expression { $list.add($expression.expr); })*)? 
   ;
    
paramList returns [List<String> list] :
    { $list = new ArrayList<String>(); } 
    ID { $list.add($ID.text); } (',' ID { $list.add($ID.text); })* 
    ;

printStatement returns [Expr expr] : 
    'print' '(' args=argList? ')' { $expr = new Print($args.list); }
    ;

value returns [Expr expr]: 
    STRING {$expr = new StringLiteral($STRING.text.substring(1, $STRING.text.length() - 1));}
    | NUMBER { $expr = new IntLiteral($NUMBER.text); }
    | DOUBLE { $expr = new DoubleLiteral($DOUBLE.text); }
    | FLOAT { $expr = new FloatLiteral($FLOAT.text); }
    | BOOLEAN { $expr = new BooleanLiteral($BOOLEAN.text); }
    |  ID { $expr = new Deref($ID.text); }
    ;

arrayCreation returns [Expr expr]: 
{  List<Expr> list = new ArrayList<Expr>();}
LBRACK  (expression{list.add($expression.expr);} (',' expression{list.add($expression.expr);} )*)? RBRACK
{$expr = new ArrayCreationExpr(list); }
;

arrayAccess returns [Expr expr]
  : identifier=ID LBRACK  index=expression RBRACK {
      $expr = new ArrayAccessExpr($identifier.text, $index.expr);
  }
  ;

LBRACK: '[' ;
RBRACK: ']' ;
BOOLEAN : 'true' | 'false';
ID : [a-zA-Z][a-zA-Z_0-9]*;
STRING : '"' (~["\\])* '"';
NUMBER : INT;
DOUBLE: INT ('.' [0-9]+)?;
FLOAT: DOUBLE'f';
fragment INT : '0' | [1-9] [0-9]*;
WS : [ \t\n\r]+ -> skip;