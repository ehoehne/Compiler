package ADT;

public class Syntactic 
{
    //Eli Hoehne, 4886, CS4100, SPRING 2023

    private String filein;              //The full file path to input file
    private SymbolTable symbolList;     //Symbol table storing ident/const
    private Lexical lex;                //Lexical analyzer 
    private Lexical.token token;        //Next Token retrieved 
    private boolean traceon;            //Controls tracing mode 
    private int level = 0;              //Controls indent for trace mode
    private boolean anyErrors;          //Set TRUE if an error happens 
    private boolean firstDecSec;
    private final int symbolSize = 250;

    public Syntactic(String filename, boolean traceOn) {
        filein = filename;
        traceon = traceOn;
        symbolList = new SymbolTable(symbolSize);
        lex = new Lexical(filein, symbolList, true);
        lex.setPrintToken(traceOn);
        anyErrors = false;
        firstDecSec = true;
    }

    //The interface to the syntax analyzer, initiates parsing
    // Uses variable RECUR to get return values throughout the non-terminal methods    
    public void parse() {
        int recur = 0;
        // prime the pump to get the first token to process
        token = lex.GetNextToken();
        // call PROGRAM
        recur = Program();
    }

    //Non Terminal PROGIDENTIFIER is fully implemented here, leave it as-is.        
    private int ProgIdentifier() {
        int recur = 0;
        if (anyErrors) {
            return -1;
        }

        // This non-term is used to uniquely mark the program identifier
        if (token.code == lex.codeFor("IDENT")) {
            // Because this is the progIdentifier, it will get a 'P' type to prevent re-use as a var
            symbolList.UpdateSymbol(symbolList.LookupSymbol(token.lexeme), 'P', 0);
            //move on
            token = lex.GetNextToken();
        }
        return recur;
    }

    //Non Terminal PROGRAM is fully implemented here.    
    private int Program() {
        int recur = 0;
        if (anyErrors) {
            return -1;
        }
        trace("Program", true);
        if (token.code == lex.codeFor("_UNIT")) { 
            token = lex.GetNextToken();
            recur = ProgIdentifier();
            if (token.code == lex.codeFor("SCOLN")) { 
                token = lex.GetNextToken();
                recur = Block();
                if (token.code == lex.codeFor("__DOT")) { 
                    if (!anyErrors) {
                        System.out.println("Success.");
                    } else {
                        System.out.println("Compilation failed.");
                    }
                } else {
                    error(lex.reserveFor("__DOT"), token.lexeme);
                }
            } else {
                error(lex.reserveFor("SCOLN"), token.lexeme); 
            }
        } else {
            error(lex.reserveFor("_UNIT"), token.lexeme);
        }
        trace("Program", false);
        return recur;
    }

    //Non Terminal BLOCK is fully implemented here.    
    private int Block() {
        int recur = 0;
        if (anyErrors) {
            return -1;
        }
        trace("Block", true);

        
        if(token.code == lex.codeFor("__VAR") && firstDecSec){
            recur = VariableDecSec();
            firstDecSec = false;
        }
        
        recur = BlockBody();
        
        trace("Block", false);
        return recur;
    }


    /*
     * Updated in Syntax B. content of this used to be in the Block method, was split out ini order to match cfg given in in part B
     * Also updated to handle resynching after an error is found. This will handle the given UNTIL by flushing out the rest of the
     * statement after UNTIL, and then moving onto the next statement. Will also handle skipping and extra ;, because the logic here skips that
     * check in the first place. 
     */
    private int BlockBody(){
        int recur = 0; 
        if (anyErrors) { 
            return -1;
        }

        trace("BlockBody", true);

		if (token.code == lex.codeFor("BEGIN")) {    
            token = lex.GetNextToken();
            recur = Statement();
            while ((token.code == lex.codeFor("SCOLN")) && (!lex.EOF()) && (!anyErrors)) { 
                token = lex.GetNextToken();
                recur = Statement();
            }
            if (token.code == lex.codeFor("__END")) {   
                token = lex.GetNextToken();
            } 
            //handles resynching when an error is found.
            //will skip over any left over tokens after the error, and moke onto the next statement.
            //will also skip over the extra ; found after the writeln, in BAD, as mentioned in class. 
            else if(token.code != lex.codeFor("__END") && anyErrors){
                token = lex.GetNextToken();
                while(token.code != lex.codeFor("SCOLN")){
                    token = lex.GetNextToken();
                }
                if(token.code == lex.codeFor("SCOLN")){
                    token = lex.GetNextToken();
                }
                anyErrors = false;
                recur = Statement();

                if(token.code == lex.codeFor("SCOLN")){
                    token = lex.GetNextToken();
                }
                if (token.code == lex.codeFor("__END")) {   
                    token = lex.GetNextToken();
                } 
            }
            else {
                error(lex.reserveFor("__END"), token.lexeme);
            }

        } 
        else {
            error(lex.reserveFor("BEGIN"), token.lexeme);   //update for var too
        }
        
		trace("BlockBody", false);
        return recur;

    }  

    //Not a NT, but used to shorten Statement code body for readability.   
    //<variable> $COLON-EQUALS <simple expression>
    private int handleAssignment() {
        int recur = 0;
        if (anyErrors) {
            return -1;
        }
        trace("handleAssignment", true);
        //have ident already in order to get to here, handle as Variable
        recur = Variable();  //Variable moves ahead, next token ready

        if (token.code == lex.codeFor("ASIGN")) {   //was ASGN
            token = lex.GetNextToken();
            recur = SimpleExpression();
        } else {
            error(lex.reserveFor("ASIGN"), token.lexeme);   //was ASGN
        }

        trace("handleAssignment", false);
        return recur;
    }

    /*
     * Added in part B. All this method does is handle calling the nonterminal variable declaration.
     * This is here in order to match the given CFG.
     */
    private int VariableDecSec() {
        int recur = 0;   
        if (anyErrors) { 
            return -1;
        }

        trace("VariableDecSec", true);

        recur = VariableDeclaration();

		trace("VariableDecSec", false);
        return recur;

    }  

    /*
     * Added in part B. This handles the variable declaration according to CFG. 
     * Uses a do while loop to ensure it runs at least once. Will successfully handle multiple declarations
     * on a single line, as well as more subsequent lines of delarations. 
     * 
     * CFG: <variable-declaration> -> {<identifier> {$COMMA <identifier>}* $COLON <simple type> $SEMICOLON}+
     */
    private int VariableDeclaration(){
        int recur = 0;   
        if (anyErrors) { 
            return -1;
        }

        trace("VariableDeclaration", true);
		
        do {    //{ xxx }+ handles looping at least once
            token = lex.GetNextToken();
            if(token.code == lex.codeFor("IDENT")){ //<identifier>
                token = lex.GetNextToken();
                while(token.code == lex.codeFor("COMMA")){  //{$COMMA <identifier>}*
                    token = lex.GetNextToken();
                    if(token.code == lex.codeFor("IDENT")){ 
                        token = lex.GetNextToken();
                    }
                }
                if(token.code == lex.codeFor("COLON")){     //$COLON
                    token = lex.GetNextToken();
                    recur = SimpleType();                   //<simple type>
                    if(token.code == lex.codeFor("SCOLN")){ //$SEMICOLON
                        token = lex.GetNextToken();
                    }
                }
                else{
                    error("Colon", token.lexeme);
                }
            }
            else{
                error("Identifier", token.lexeme);
            }

        } while(token.code == lex.codeFor("__VAR"));
        
		trace("VariableDeclaration", false);
        return recur;
    }  

    /*
     * Added in part B, handles type checking for the simple types integer, float, and string.
     */
    private int SimpleType(){
        int recur = 0;
        if (anyErrors) { 
            return -1;
        }

        trace("SimpleType", true);
		
        if(token.code == lex.codeFor("INTGR")){
            recur = token.code;
            token = lex.GetNextToken();
        }
        else if(token.code == lex.codeFor("FLOAT")){
            recur = token.code;
            token = lex.GetNextToken();
        }
        else if(token.code == lex.codeFor("STRNG")){
            recur = token.code;
            token = lex.GetNextToken();
        }
        else{
            error("Integer, Float, or String", token.lexeme);
        }

		trace("SimpleType", false);

        return recur;

    }  
    
    /*
     * Added in part B. Handles relational expressions. Calls relop to gather a relational operator, 
     * sandwiched between two simple expressions
     * 
     * CFG: <relexpression> -> <simple expression> <relop> <simple expression>
     */
    private int RelExpression(){
        int recur = 0;  
        if (anyErrors) { 
            return -1;
        }

        trace("RelExpression", true);
		
        recur = SimpleExpression(); //<simple expression>

        recur = Relop();    //<relop>

        recur = SimpleExpression(); //<simple expression>
        
		trace("RelExpression", false);
    
        return recur;
    } 

    /*
     * This method is called to handle a simple expression after an assignment token is found by
     * handleAssignment. It starts by checking if a sign is given, if no sign is given, it defaults
     * to positive. Then, the non-terminal Term is called. After the term is returned, it adjusts recur
     * based on the sign. Next, it loops until it no longer finds an Addop and a Term and no errors are found.
     * It then adjusts recur again.
     * 
     * CFG: <simple expression> -> [<sign>] <term> {<addop> <term>}*
     */
    private int SimpleExpression() {
        int recur = 0;
        if (anyErrors) {
            return -1;
        }

        trace("SimpleExpression", true);
        
        //[<sign>]
        int sign = lex.codeFor("__ADD"); //If not present, assume it is positive. 
        if(token.code == lex.codeFor("__ADD") || token.code == lex.codeFor("__SUB")){
            sign = Sign();
        }

        int term = Term();  //<term>

        if(sign == lex.codeFor("__ADD")){
            recur += term;
        }
        else{
            recur -= term;
        }

        //{<addop> <term>}*
        while((token.code == lex.codeFor("__ADD") || token.code == lex.codeFor("__SUB")) && !anyErrors){
            int addop = Addop();
            term = Term();
            if(addop == lex.codeFor("__ADD")){
                recur += term;
            }
            else{
                recur -= term;
            }
        }
        
        trace("SimpleExpression", false);
        return recur;
    }

    /*
     * This method is called inside of SimpleExpression and handles the non-terminal "Term"
     * It first calls Factor, and then loops until it no longer finds a Mulop and a Factor
     * and no errors are found. It then adjusts recur using the Mulop, based on the returned 
     * Factor. It also makes makes sure that it doesnt divide by 0.
     * 
     * CFG: <term> -> <factor> {<mulop> <factor>}*
     */
    private int Term(){
        int recur = 0;          
        if (anyErrors) {        
            return -1;
        }

        trace("Term", true);
		
        //<factor>
        int factor = Factor();

        //{<mulop> <factor>}*
        while((token.code == lex.codeFor("_MULT") || token.code == lex.codeFor("DIVID")) && !anyErrors){
            int mulop = Mulop();
            factor = Factor();
            if(mulop == lex.codeFor("_MULT")){
                recur *= factor;
            }
            else if(mulop == lex.codeFor("DIVID") && factor != 0){
                recur /= factor;
            }
        }
        
		trace("Term", false);
        return recur;
    }  

    /*
     * This method handles the non-terminal "Factor". It is called inside the Term method.
     * It is responsible for checking what the given token is, and calling additional non-terminals
     * based on that analysis. If it finds a number, it calls non-terminal UnsignedConstant. If it 
     * finds an identifier, it calls Variable. If it finds '(', it calls SimpleExpression to handle
     * the expression inside of the "()". After the expression is handled, it will check for a ')'
     * and throw an error if it is not found. If it finds none of these in the original check, it will
     * also throw an error. 
     * 
     * CFG: <factor> -> <unsigned constant> | <variable> | $LPAR <simple expression> $RPAR
     */
    private int Factor(){
        int recur = 0;   
        if (anyErrors) { 
            return -1;
        }

        trace("Factor", true);

		if(token.code == lex.codeFor("IDENT")){
            //<variable>
            recur = Variable();
        }
        else if(token.code == lex.codeFor("INTGR") || token.code == lex.codeFor("FLOAT")){
            //<unsigned constant>
            recur = UnsignedConstant();
        }
        else if(token.code == lex.codeFor("LPREN")){
            // $LPAR <simple expression> $RPAR
            token = lex.GetNextToken();
            recur = SimpleExpression();
            if(token.code == lex.codeFor("RPREN")){
                token = lex.GetNextToken();
            }
            else{
                error("')'", token.lexeme);
            }
        }
        else{
            error("Number, Variable, or '('", token.lexeme);
        }

		trace("Factor", false);
        return recur;

    } 
    
    /*
     * Handles the Sign non-terminal. Will return the code based on the sign, and get the next token
     * Called inside of SimpleExpression.
     * 
     * CFG: <sign> -> $PLUS | $MINUS
     */
    private int Sign(){
        int recur = 0;   
        if (anyErrors) {
            return -1;
        }

        trace("Sign", true);

        if(token.code == lex.codeFor("__ADD")){
            recur = token.code;
            token = lex.GetNextToken();
        }
        else if(token.code == lex.codeFor("__SUB")){
            recur = token.code;
            token = lex.GetNextToken();
        }

		trace("Sign", false);
        return recur;
    }  

    /*
     * Handles the Addop non-terminal. Will save the code based on the input, and get the next token.
     * Called inside of SimpleExpression.
     * 
     * CFG: <addop> -> $PLUS | $MINUS
     */
    private int Addop(){
        int recur = 0;   
        if (anyErrors) {
            return -1;
        }

        trace("Addop", true);

        if(token.code == lex.codeFor("__ADD")){
            recur = token.code;
            token = lex.GetNextToken();
        }
        else if(token.code == lex.codeFor("__SUB")){
            recur = token.code;
            token = lex.GetNextToken();
        }

		trace("Addop", false);
        return recur;
    }  

     /*
     * Handles the Mulop non-terminal. Will save the code based on the input, and get the next token.
     * Called inside of Term.
     * 
     * CFG: <mulop> -> $MULTIPLY | $DIVIDE
     */
    private int Mulop(){
        int recur = 0;   
        if (anyErrors) {
            return -1;
        }

        trace("Mulop", true);

        if(token.code == lex.codeFor("_MULT")){
            recur = token.code;
            token = lex.GetNextToken();
        }
        else if(token.code == lex.codeFor("DIVID")){
            recur = token.code;
            token = lex.GetNextToken();
        }

		trace("Mulop", false);
        return recur;
    }  


    /*
     * Added in part B, handles returning a relational operator, including:
     * =, <, >, <>, <=, >=. 
     * 
     * CFG: <relop> -> $EQ | $LSS | $GTR | $NEQ | $LEQ | $GEQ
     */
    private int Relop(){
        int recur = 0;  
        if (anyErrors) { 
            return -1;
        }

        trace("Relop", true);
		
        if(token.code == lex.codeFor("EQUAL")){
            recur = token.code;
            token = lex.GetNextToken();
        }
        else if(token.code == lex.codeFor("_LESS")){
            recur = token.code;
            token = lex.GetNextToken();
        }
        else if(token.code == lex.codeFor("_GRTR")){
            recur = token.code;
            token = lex.GetNextToken();
        }
        else if(token.code == lex.codeFor("NTEQL")){
            recur = token.code;
            token = lex.GetNextToken();
        }
        else if(token.code == lex.codeFor("LSEQL")){
            recur = token.code;
            token = lex.GetNextToken();
        }
        else if(token.code == lex.codeFor("GREQL")){
            recur = token.code;
            token = lex.GetNextToken();
        }

		trace("Relop", false);
        return recur;
    } 
    
    /*
     * Added in B. Handles type checking for a string constant.
     */
    private int StringConstant(){
        int recur = 0;  
        if (anyErrors) { 
            return -1;
        }

        trace("StringConstant", true);
		
        if(token.code == lex.codeFor("STRNG")){
            recur = token.code;
            token = lex.GetNextToken();
        }
        
		trace("StringConstant", false);
        return recur;
    } 

    /*
     * This method is called inside of Factor and handles the non-terminal "UnsignedConstant."
     * The only thing that this non-terminal does is call UnsignedNumber, since they are the same thing.
     * 
     * CFG: <unsigned constant> -> <unsigned number>
     */
    private int UnsignedConstant(){
        int recur = 0;   //Return value used later
        if (anyErrors) { // Error check for fast exit, error status -1
            return -1;
        }

        trace("UnsignedConstant", true);
		
        //<unsigned number>
        recur = UnsignedNumber();
        
		trace("UnsignedConstant", false);

        return recur;

    }  

    /*
     * This method handles the non-terminal "UnsignedNumber" and is called inside "UnsignedConstant"
     * It checks if the token is an Integer or a Float. If its an integer, it parses it, adds it to recur,
     * and gets the next token. If its a Float, it parses it as a double. For now, nothing is done with this
     * result because the return type is an integer, and the return value is ignored for now. 
     * 
     * CFG: <unsigned number> -> $FLOAT | $INTEGER
     */
    private int UnsignedNumber(){
        int recur = 0;   
        if (anyErrors) { 
            return -1;
        }

        trace("UnsignedNumber", true);
		
        if(token.code == lex.codeFor("INTGR")){
            recur = Integer.parseInt(token.lexeme);
            token = lex.GetNextToken();
        }
        else if(token.code == lex.codeFor("FLOAT")){
            Double.parseDouble(token.lexeme);
            token = lex.GetNextToken();
        }
        
		trace("UnsignedNumber", false);
        return recur;

    }  

    /*
     * Added in B, handles most of the logic for statements. This method will check for an idenifier, just like before,
     * but it will also now check for every other type of statment start, including:
     * Begin, If, DoWhile, Repeat, For, Writeln, and Readln.
     * 
     * CFG is shown above main. 
     */
    private int Statement() {
        int recur = 0;
        if (anyErrors) {
            return -1;
        }

        trace("Statement", true);

        if (token.code == lex.codeFor("IDENT")) {
            recur = handleAssignment();
        } 
        else {
            if (token.code == lex.codeFor("BEGIN")) {
               recur = BlockBody();
            } 
            else if(token.code == lex.codeFor("___IF")){ 
                recur = handleIf();
            }
            else if(token.code == lex.codeFor("DOWHL")){ 
                recur = handleWhile();
            }
            else if(token.code == lex.codeFor("RPEAT")){ 
                recur = handleRepeat();
            }
            else if(token.code == lex.codeFor("__FOR")){ 
                recur = handleFor();
            }
            else if(token.code == lex.codeFor("WRILN")){
                recur = handleWriteln();
            }
            else if(token.code == lex.codeFor("REALN")){
                recur = handleReadln();
            }
            else{
                error("Statement start", token.lexeme);
            }
        }

        trace("Statement", false);
        return recur;
    }

    /*
     * Handles the If startment start. Starts by looking for a relexpression, then a subsequent then block, 
     * then an optional else block.
     * 
     * CFG: $IF <relexpression> $THEN <statement> [$ELSE <statement>]
     */
    private int handleIf(){
        int recur = 0;  
        if (anyErrors) { 
            return -1;
        }

        trace("handleIf", true);
		
        token = lex.GetNextToken();
        recur = RelExpression();

        if(token.code == lex.codeFor("_THEN")){
            token = lex.GetNextToken();
            recur = Statement();
        }
        else{
            error("Then", token.lexeme);
        }
        if(token.code == lex.codeFor("_ELSE")){
            token = lex.GetNextToken();
            recur = Statement();
        }
        
		trace("handleIf", false);
        return recur;
    } 

    /*
     * Handles do while statement start. First, a relexpression, and then a statement. 
     * 
     * CFG: $DOWHILE <relexpression> <statement>
     */
    private int handleWhile(){
        int recur = 0;  
        if (anyErrors) { 
            return -1;
        }

        trace("handleWhile", true);
		
        token = lex.GetNextToken();
        recur = RelExpression();

        recur = Statement();
        
		trace("handleWhile", false);
        return recur;
    } 

    /*
     * Handles repeat statement start. Starts with a statement, looks for UNTIl, and then if UNTIL is found, 
     * looks for a relexpression.
     * 
     * CFG: $REPEAT <statement> $UNTIL <relexpression>
     */
    private int handleRepeat(){
        int recur = 0;  
        if (anyErrors) { 
            return -1;
        }

        trace("handleRepeat", true);

		token = lex.GetNextToken();
        recur = Statement();

        if(token.code == lex.codeFor("UNTIL")){
            token = lex.GetNextToken();
            recur = RelExpression();
        }
        
		trace("handleRepeat", false);
        return recur;
    } 

    /*
     * Handles For statement start. First looks for an identifier, then an assignment, then TO and DO keywords. 
     * Calls simpleexpression and statement inside of those, respectively.
     * 
     * CFG: $FOR <variable> $ASSIGN <simple expression> $TO <simple expression> $DO <statement> 
     */
    private int handleFor(){
        int recur = 0;  
        if (anyErrors) { 
            return -1;
        }

        trace("handleFor", true);
		
        token = lex.GetNextToken();
        recur = Variable();

        if(token.code == lex.codeFor("ASIGN")){
            token = lex.GetNextToken();
            recur = SimpleExpression();
            if(token.code == lex.codeFor("___TO")){
                token = lex.GetNextToken();
                recur = SimpleExpression();
            }
            if(token.code == lex.codeFor("___DO")){
                token = lex.GetNextToken();
                recur = Statement();
            }
        }

		trace("handleFor", false);
        return recur;
    } 

    /*
     * Handles writeln statement start. This looks for (, then an ideentifier, then a ).
     * This is modified according to the given annoucment saying to omit the capability for simpleexpressions.
     * 
     * modified CFG: WRITELN $LPAR (<identifier> |<stringconst>) $RPAR
     */
    private int handleWriteln(){
        int recur = 0;  
        if (anyErrors) { 
            return -1;
        }

        trace("handleWriteln", true);
		
        token = lex.GetNextToken();
        if(token.code == lex.codeFor("LPREN")){
            token = lex.GetNextToken();
            if(token.code == lex.codeFor("IDENT")){
                recur = Variable();
            }
            else{
                recur = StringConstant();
            }
            if(token.code == lex.codeFor("RPREN")){
                token = lex.GetNextToken();
            }
        }
        
        
		trace("handleWriteln", false);
        return recur;
    } 

    /*
     * Handles readln statement start. Looks for (, then an identifier, then ).
     * 
     * CFG: $READLN $LPAR <identifier> $RPAR
     */
    private int handleReadln(){
        int recur = 0;  
        if (anyErrors) { 
            return -1;
        }

        trace("handleReadln", true);
		token = lex.GetNextToken();
        if(token.code == lex.codeFor("LPREN")){
            token = lex.GetNextToken();
            recur = Variable();
            if(token.code == lex.codeFor("RPREN")){
                token = lex.GetNextToken();
            }
        }
        
		trace("handleReadln", false);
        return recur;
    } 

    //Non-terminal VARIABLE just looks for an IDENTIFIER.  Later, a
    //  type-check can verify compatible math ops, or if casting is required.    
    private int Variable(){
        int recur = 0;
        if (anyErrors) {
            return -1;
        }

        trace("Variable", true);
        if ((token.code == lex.codeFor("IDENT"))) { 
            // bookkeeping and move on
            token = lex.GetNextToken();
        }
        else
            error("Variable", token.lexeme);

        trace("Variable", false);
        return recur;

    }  
    
    /* UTILITY FUNCTIONS USED THROUGHOUT THIS CLASS */
    // error provides a simple way to print an error statement to standard output
    // and avoid reduncancy
    private void error(String wanted, String got) {
        anyErrors = true;
        System.out.println("ERROR: Expected " + wanted + " but found " + got);
    }

    // trace simply RETURNs if traceon is false; otherwise, it prints an
    // ENTERING or EXITING message using the proc string
    private void trace(String proc, boolean enter) {
        String tabs = "";

        if (!traceon) {
            return;
        }

        if (enter) {
            tabs = repeatChar(" ", level);
            System.out.print(tabs);
            System.out.println("--> Entering " + proc);
            level++;
        } else {
            if (level > 0) {
                level--;
            }
            tabs = repeatChar(" ", level);
            System.out.print(tabs);
            System.out.println("<-- Exiting " + proc);
        }
    }

    // repeatChar returns a string containing x repetitions of string s; 
    //    nice for making a varying indent format
    private String repeatChar(String s, int x) {
        int i;
        String result = "";
        for (i = 1; i <= x; i++) {
            result = result + s;
        }
        return result;
    }  
}

