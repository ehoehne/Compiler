import ADT.*;

public class Main 
{   
    /* Part B CFG: 
        <program> -> $UNIT <identifier> $SEMICOLON <block> $PERIOD
        <block> -> {<variable-dec-sec>}* <block-body>
        <block-body> -> $BEGIN <statement> {$SCOLN <statement>} $END
        <variable-dec-sec> -> $VAR <variable-declaration>
        <variable-declaration> -> {<identifier> {$COMMA <identifier>}* COLON <simple type> $SEMICOLON}+
        <statement>->   [
                        <variable> $ASSIGN (<simple expression> | 
                        <string literal>) |
                        <block-body> |
                        $IF <relexpression> $THEN <statement> [$ELSE <statement>] |
                        $DOWHILE <relexpression> <statement> |
                        $REPEAT <statement> $UNTIL <relexpression> |
                        $FOR <variable> $ASSIGN <simple expression>
                        $TO <simple expression> $DO <statement> |
                        $WRITELN $LPAR (<simple expression> | <identifier> |
                        <stringconst> ) $RPAR
                        $READLN $LPAR <identifier> $RPAR
                        ]+
        <relexpression> -> <simple expression> <relop> <simple expression>
        <relop> -> $EQ | $LSS | $GTR | $NEQ | $LEQ | $GEQ
        <simple type> -> $INTEGER | $FLOAT | $STRING
        <stringconst> -> $STRINGTYPE
    */

    public static void main(String[] args) throws Exception 
    {
        //Eli Hoehne, 4886, CS4100, SPRING 2023
        System.out.println("Eli Hoehne, 4886, CS4100, SPRING 2023");
        
        String filePath = args[0];
        System.out.println("Code Generation SP2023, by Eli Hoehne");
        System.out.println("Parsing " + filePath);
        boolean traceon = true; //false;
        Syntactic parser = new Syntactic(filePath, traceon);
        parser.parse();
        System.out.println("Done.");
    }
}
