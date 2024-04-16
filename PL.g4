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
    assignment (';') { $expr = $assignment.expr; }
    | expression (';') { $expr = $expression.expr; }
    | declaration { $expr = $declaration.expr; }
    | ifElseStatement { $expr = $ifElseStatement.expr; }
    | forLoop { $expr = $forLoop.expr; }
    | block  { $expr = $block.expr; }
    | printStatement ';'? { $expr = $printStatement.expr; } 
    | arrayUpdate { $expr = $arrayUpdate.expr; }
    | mapPut { $expr = $mapPut.expr;}    
    | mapRemove { $expr = $mapRemove.expr;}
    | traditionalForLoop { $expr = $traditionalForLoop.expr; }
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
    | ID '++' { $expr = new Assign($ID.text, new Arithmetics(Operator.Add, new Deref($ID.text), new IntLiteral("1"))); }
    | ID '--' { $expr = new Assign($ID.text, new Arithmetics(Operator.Sub, new Deref($ID.text), new IntLiteral("1"))); }
    | ID '++' { $expr = new Assign($ID.text, new Arithmetics(Operator.Add, new Deref($ID.text), new IntLiteral("1"))); }
    | ID '--' { $expr = new Assign($ID.text, new Arithmetics(Operator.Sub, new Deref($ID.text), new IntLiteral("1"))); }
    | ID '+=' expression { $expr = new Assign($ID.text, new Arithmetics(Operator.Add, new Deref($ID.text), $expression.expr)); }
    | ID '-=' expression { $expr = new Assign($ID.text, new Arithmetics(Operator.Sub, new Deref($ID.text), $expression.expr)); }
    | ID '*=' expression { $expr = new Assign($ID.text, new Arithmetics(Operator.Mul, new Deref($ID.text), $expression.expr)); }
    | ID '/=' expression { $expr = new Assign($ID.text, new Arithmetics(Operator.Div, new Deref($ID.text), $expression.expr)); }
    | 'len' '(' ID ')' { $expr = new ArrayLength($ID.text); }
    | condition=expression '?' trueExpr=expression ':' falseExpr=expression { $expr = new Ifelse($condition.expr, $trueExpr.expr, $falseExpr.expr); }
    | '-' expression { $expr = new Arithmetics(Operator.Sub, new IntLiteral("0"), $expression.expr); }
    | value { $expr = $value.expr; }
    | '(' expression ')' { $expr = $expression.expr; }
    | funCall { $expr = $funCall.expr; }
    | arrayCreation { $expr = $arrayCreation.expr; }
    | arrayAccess { $expr = $arrayAccess.expr; }
    | setExpr {$expr = $setExpr.expr;}
    | addToSet {$expr = $addToSet.expr;}
    | removeFromSet {$expr = $removeFromSet.expr;}
    | containsInSet {$expr = $containsInSet.expr;}
    | mapCreation { $expr = $mapCreation.expr; }
    | mapAccess { $expr = $mapAccess.expr; }
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
    | FLOAT { $expr = new FloatLiteral($FLOAT.text); }
    | DOUBLE { $expr = new DoubleLiteral($DOUBLE.text); }
    | BOOLEAN { $expr = new BooleanLiteral($BOOLEAN.text); }
    | ID { $expr = new Deref($ID.text); }
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

arrayUpdate returns [Expr expr]
    : ID LBRACK index=expression RBRACK '=' insert=expression ';'
      {$expr = new ArrayUpdateExpr($ID.text, $index.expr, $insert.expr);}
    ;


setExpr returns [Expr expr]
    : { List<Expr> list = new ArrayList<Expr>(); } 
    '{'  (expression {list.add($expression.expr);} (',' expression {list.add($expression.expr);})*)? '}' 
    {$expr = new SetCreationExpr(list);}
    ;

addToSet returns [Expr expr]
    : 'addSet' '(' setId=ID ',' element=expression ')' {$expr = new AddToSetExpr($setId.text, $element.expr);}
    ;

removeFromSet returns [Expr expr]
    : 'removeSet' '(' setId=ID ',' element=expression ')' {$expr = new RemoveFromSetExpr($setId.text, $element.expr);}
    ;

containsInSet returns [Expr expr]
    : 'inSet' '(' setId=ID ',' element=expression ')' {$expr = new ContainsInSetExpr($setId.text, $element.expr);}
    ;
  
  
mapCreation returns [Expr expr] :
    { List<PairExpr> list = new ArrayList<PairExpr>(); }
    '{' (mapEntry {list.add($mapEntry.expr);} (',' mapEntry {list.add($mapEntry.expr);})*)? '}' 
    {$expr = new MapCreationExpr(list); }
    ;

mapEntry returns [PairExpr expr] :
    key=expression ':' entry=expression {$expr = new PairExpr($key.expr, $entry.expr);}
    ;

mapPut returns [Expr expr] :
    mapId=ID '<' key=expression '>' '=' entry=expression ';' 
    {$expr = new MapPutExpr($mapId.text, $key.expr, $entry.expr);}
    ;

mapRemove returns [Expr expr] :
    'mapRemove' '(' mapId=ID ',' key=expression ')' ';' 
    {$expr = new MapRemoveExpr($mapId.text, $key.expr);}
    ;

mapAccess returns [Expr expr] :
    mapId=ID '<' key=expression '>' 
    {$expr = new MapAccessExpr($mapId.text, $key.expr);}
    ;

traditionalForLoop returns [Expr expr] :
    'for' '(' init=assignment ';' condition=expression ';' Exit=loopExpr ')' body=block {
        $expr = new TraditionalForLoop($init.expr, $condition.expr, $loopExpr.expr, $body.expr);
    }
    ;
loopExpr returns [Expr expr] :
    assignment { $expr = $assignment.expr; }
    | expression { $expr = $expression.expr; }
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