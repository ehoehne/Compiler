package ADT;

import java.io.IOException;

public class Syntactic 
{
    //Eli Hoehne, 4886, CS4100, SPRING 2023

    private String filein;              //The full file path to input file
    private SymbolTable symbolList;     //Symbol table storing ident/const
    private QuadTable quads;            //Quad table to be used in interp
    private Interpreter interp;         //the interpreter to be used in syntactic
    private Lexical lex;                //Lexical analyzer 
    private Lexical.token token;        //Next Token retrieved 
    private boolean traceon;            //Controls tracing mode 
    private int level = 0;              //Controls indent for trace mode
    private boolean anyErrors;          //Set TRUE if an error happens 
    private boolean firstDecSec;

    private final int symbolSize = 250; //size of symbol table 
    private final int quadSize = 1000;  //size of quad table
    private int Minus1Index;            
    private int Plus1Index;

    public Syntactic(String filename, boolean traceOn) {
        filein = filename;
        traceon = traceOn;

        symbolList = new SymbolTable(symbolSize);
        lex = new Lexical(filein, symbolList, true);

        /* CodeGen: added creation of quads and interp 
         *          addeed -1 and +1 indexes to symboltable
        */
        quads = new QuadTable(quadSize);
        interp = new Interpreter();
        Minus1Index = symbolList.AddSymbol("-1", 'C', -1);
        Plus1Index = symbolList.AddSymbol("1", 'C', 1);

        lex.setPrintToken(traceOn);
        anyErrors = false;
        firstDecSec = true;
    }

    /* The interface to the syntax analyzer, initiates parsing */
    public void parse() throws IOException {
        int recur = 0;
        String filenameBase = filein.substring(0, filein.length() - 4);
        System.out.println(filenameBase);
        // prime the pump to get the first token to process
        token = lex.GetNextToken();
        // call PROGRAM
        recur = Program();

        quads.AddQuad(interp.opcodeFor("STOP"), 0, 0, 0);   /* add stop instruction to end of quads */

        symbolList.PrintSymbolTable(filenameBase + "ST-before.txt");    /* print symbol table to file */
        quads.PrintQuadTable(filenameBase + "Quads.txt");               /* print quad table to file */

        /* Interpret */
        if(!anyErrors){
            /* start the interpreter, pass the completed quad and symboltables, as well as a trace output */
            /* interpreter will print trace to console and to file if traceOn */
            interp.InterpretQuads(quads, symbolList, false, filenameBase + "Trace.txt");
        }
        else{
            System.out.println("ERRORS. Unable to run program.");
        }
        symbolList.PrintSymbolTable(filenameBase + "ST-after.txt"); /* print symboltable after updated by interp */
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
     * Updated in Syntax B. content of this used to be in the Block method, was split out in order to match cfg given in in part B
     * Also updated to handle resynching after an error is found. This will handle the given UNTIL by flushing out the rest of the
     * statement after UNTIL, and then moving onto the next statement. Will also handle skipping and extra ;, because the logic here skips that
     * check in the first place. 
     * 
     * Not changed in Code Generation
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
            error(lex.reserveFor("BEGIN"), token.lexeme); 
        }
        
		trace("BlockBody", false);
        return recur;

    }  

    //Not a NT, but used to shorten Statement code body for readability.   
    //<variable> $COLON-EQUALS <simple expression>
    private int handleAssignment() {
        int left = 0;   /* CodeGen: changed recur to left and right. returns left. */
        int right = 0;
        if (anyErrors) {
            return -1;
        }
        trace("handleAssignment", true);
        //have ident already in order to get to here, handle as Variable
        left = Variable();  //Variable moves ahead, next token ready

        if (token.code == lex.codeFor("ASIGN")) {
            token = lex.GetNextToken();
            right = SimpleExpression();
            quads.AddQuad(interp.opcodeFor("MOV"), right, 0, left); /* Added quad generation */
        } else {
            error(lex.reserveFor("ASIGN"), token.lexeme);
        }

        trace("handleAssignment", false);
        return left;
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
     * 
     * unchanged in code gen
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
     * 
     * CodeGen: added more int values, addedd quad generation, changed logic. 
     */
    private int RelExpression(){
        int left, right, saveRelop, recur, temp;    
        if (anyErrors) { 
            return -1;
        }

        trace("RelExpression", true);
		
        left = SimpleExpression();      //get left operand
        saveRelop = Relop();            //save relational operator
        right = SimpleExpression();     //get right operand
        temp = symbolList.GenSymbol();  //add temporary symbol

        quads.AddQuad(interp.opcodeFor("SUB"), left, right, temp); /* Added quad generation using new ints */
        recur = quads.NextQuad();        /* store the location of the next quad slot */
        quads.AddQuad(relopToOpcode(saveRelop), temp, 0, 0);    /* Added quad generation */

		trace("RelExpression", false);
    
        return recur;
    } 

    /*
     * This method is called to handle a simple expression after an assignment token is found by
     * handleAssignment. 
     * 
     * CodeGen: added separate return values for sign and term on left. if negative, adds negation quad.
     *          within loop, interprete the opcode for either add or sub. call term for right side.
     *          Get a temporary symbol, and add a quad based on new info. returns the left value.
     * 
     * CFG: <simple expression> -> [<sign>] <term> {<addop> <term>}*
     */
    private int SimpleExpression() {
        int left, right, signVal, temp, opcode;
        if (anyErrors) {
            return -1;
        }

        trace("SimpleExpression", true);
        
        signVal = Sign();   //<sign>
        left = Term();      //<term>
        if(signVal == -1){
            quads.AddQuad(interp.opcodeFor("MUL"), left, Minus1Index, left);
        }

        //{<addop> <term>}*
        while((token.code == lex.codeFor("__ADD") || token.code == lex.codeFor("__SUB")) && !anyErrors){
            if(token.code == lex.codeFor("__ADD")){
                opcode = interp.opcodeFor("ADD");
            }
            else{
                opcode = interp.opcodeFor("SUB");
            }
            token = lex.GetNextToken();
            right = Term();
            temp = symbolList.GenSymbol();
            quads.AddQuad(opcode, left, right, temp);   /* added quad generation */
            left = temp;
        }
        
        trace("SimpleExpression", false);
        return left;
    }

    /*
     * This method is called inside of SimpleExpression and handles the non-terminal "Term"
     * It first calls Factor, and then loops until it no longer finds a Mulop and a Factor
     * and no errors are found.
     * 
     * CodeGen: interprets opcode for mul or div. calls factor again, gets a temporary 
     *          symbol, and adds quad with new info
     * 
     * CFG: <term> -> <factor> {<mulop> <factor>}*
     */
    private int Term(){
        int recur = 0;       
        int opcode, temp, factor;   
        if (anyErrors) {        
            return -1;
        }

        trace("Term", true);
		
        //<factor>
        recur = Factor();

        //{<mulop> <factor>}*
        while((token.code == lex.codeFor("_MULT") || token.code == lex.codeFor("DIVID")) && !anyErrors){
            if(token.code == lex.codeFor("_MULT")){
                opcode = interp.opcodeFor("MUL");
            }
            else{
                opcode = interp.opcodeFor("DIV");
            }
            token = lex.GetNextToken();
            factor = Factor();
            temp = symbolList.GenSymbol();
            quads.AddQuad(opcode, recur, factor, temp); /* added quad generation */
            recur = temp;
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
     * Unchanged in codegen.
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

        recur = 1;
        if(token.code == lex.codeFor("__SUB")){
            recur = -1;
            token = lex.GetNextToken();
        }
        else if(token.code == lex.codeFor("__ADD")){
            token = lex.GetNextToken();
        }

		trace("Sign", false);
        return recur;
    }  

    /*
     * Handles the Addop non-terminal. Will save the code based on the input, and get the next token.
     * Called inside of SimpleExpression.
     * 
     * As of codegen, this isnt used. 
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
     * As of codegen, this isnt used
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
     * Unchanged in codegen
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
     * Added in codegen. This will return the 'false' jump that is 
     * associated with a passed relational operator. Implemented 
     * according to table shown in assignment
     */
    private int relopToOpcode(int relop)
    {
        int result = 0;

        if(relop == lex.codeFor("EQUAL"))
            result = interp.opcodeFor("JNZ");       
        else if(relop == lex.codeFor("NTEQL"))
            result = interp.opcodeFor("JZ");       
        else if(relop == lex.codeFor("_LESS"))
            result = interp.opcodeFor("JNN");      
        else if(relop == lex.codeFor("_GRTR"))
            result = interp.opcodeFor("JNP");      
        else if(relop == lex.codeFor("LSEQL"))
            result = interp.opcodeFor("JP");        
        else if(relop == lex.codeFor("GREQL"))
            result = interp.opcodeFor("JN");

        return result;
    }

    /*
     * Added in B. Handles type checking for a string constant.
     * 
     * as of codegen, this is not used
     */
    private int StringConstant(){
        int recur = 0;  
        if (anyErrors) { 
            return -1;
        }

        trace("StringConstant", true);
		
        if(token.code == lex.codeFor("STRNG")){
            recur = symbolList.LookupSymbol(token.lexeme);
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
     * It checks if the token is an Integer or a Float. 
     * 
     * CodeGen: It then returns the symbol table index of that number, and gets the next token.
     * 
     * CFG: <unsigned number> -> $FLOAT | $INTEGER
     */
    private int UnsignedNumber(){
        int recur = 0;   
        if (anyErrors) { 
            return -1;
        }

        trace("UnsignedNumber", true);
		
        if(token.code == lex.codeFor("INTGR") || token.code == lex.codeFor("FLOAT")){
            recur = symbolList.LookupSymbol(token.lexeme); /* returns symbol table index */
            token = lex.GetNextToken();
        }
        else{
            error("Integer or Float", token.lexeme);
        }
		trace("UnsignedNumber", false);
        return recur;

    }  

    /*
     * Added in B, handles most of the logic for statements. This method will check for an idenifier, just like before,
     * but it will also now check for every other type of statment start, including:
     * Begin, If, DoWhile, Repeat, For, Writeln, and Readln.
     * 
     * Unchanged in codegen
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
     * CodeGen: calls relexpression. saves backfill quads, adds a new jmp quad, and updates the jumpvalue 
     *          for quads
     * 
     * CFG: $IF <relexpression> $THEN <statement> [$ELSE <statement>]
     */
    private int handleIf(){
        int recur = 0;  
        int branchQuad, patchElse;
        if (anyErrors) { 
            return -1;
        }

        trace("handleIf", true);
		
        token = lex.GetNextToken();
        branchQuad = RelExpression();   /* tells branchTarget to be set to jump past TRUE */

        if(token.code == lex.codeFor("_THEN")){
            token = lex.GetNextToken();
            recur = Statement();
            if(token.code == lex.codeFor("_ELSE")){
                token = lex.GetNextToken();
                patchElse = quads.NextQuad();                     /* save backfill quad to jump past else, target unknown */
                quads.AddQuad(interp.opcodeFor("JMP"), 0, 0, 0);  /* Add a jmp quad */
                quads.UpdateJump(branchQuad, quads.NextQuad());   /* conditional jump */
                recur = Statement();                              /* else body quads */
                quads.UpdateJump(patchElse, quads.NextQuad());    /* update jump */
            }
            else{   /* no else encountered */
                quads.UpdateJump(branchQuad, quads.NextQuad());   /* update jump */
            }
        }
        else{   /* no then encountered */
            error("Then", token.lexeme);
        }
        
		trace("handleIf", false);
        return recur;
    } 

    /*
     * Handles do while statement start. First, a relexpression, and then a statement. 
     * 
     * CodeGen: added quad interpretation.
     * 
     * CFG: $DOWHILE <relexpression> <statement>
     */
    private int handleWhile(){
        int recur = 0;
        int saveTop, branchQuad;
        if (anyErrors) { 
            return -1;
        }

        trace("handleWhile", true);
		
        token = lex.GetNextToken();
        saveTop = quads.NextQuad(); /* saves the location of loop top quad */
        branchQuad = RelExpression(); /* get loop condition */

        if(token.code == lex.codeFor("BEGIN")){
            recur = Statement();
            quads.AddQuad(interp.opcodeFor("JMP"), 0, 0, saveTop);      /* add jmp quad to jmp to top of loop */
            quads.UpdateJump(branchQuad, quads.NextQuad());             /* Conditional jumps next quad */
        }
        else{
            error(lex.reserveFor("BEGIN"), token.lexeme);
        }

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
     * 
     * CodeGen: added quad generation.
     * 
     * modified CFG: WRITELN $LPAR (<identifier> |<stringconst>) $RPAR
     */
    private int handleWriteln(){
        int recur = 0;  
        int toPrint = 0;
        if (anyErrors) { 
            return -1;
        }

        trace("handleWriteln", true);
		
        token = lex.GetNextToken();
        if(token.code == lex.codeFor("LPREN")){
            token = lex.GetNextToken();
            if(token.code == lex.codeFor("STRNG") || token.code == lex.codeFor("IDENT")){
                toPrint = symbolList.LookupSymbol(token.lexeme);    /* store symbol index for ident or string */
                token = lex.GetNextToken();
            }
            else{
                toPrint = SimpleExpression();
            }
            quads.AddQuad(interp.opcodeFor("PRINT"), 0, 0, toPrint);    /* added quad generation */
            if(token.code == lex.codeFor("RPREN")){
                token = lex.GetNextToken();
            }
            else{
                error(")", token.lexeme);
            }
        }
        else{
            error("(", token.lexeme);
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
            quads.AddQuad(interp.opcodeFor("READ"), 0, 0, recur);   /* added quad generation */
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
            recur = symbolList.LookupSymbol(token.lexeme);
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

