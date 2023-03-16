package ADT;
import java.io.*;

public class Lexical 
{

    private File file;                        //File to be read for input
    private FileReader filereader;            //Reader, Java reqd
    private BufferedReader bufferedreader;    //Buffered, Java reqd
    private String line;                      //Current line of input from file   
    private int linePos;                      //Current character position in the current line
    private SymbolTable saveSymbols;          //SymbolTable used in Lexical sent as parameter to construct
    private boolean EOF;                      //End Of File indicator
    private boolean echo;                     //true means echo each input line
    private boolean printToken;               //true to print found tokens here
    private int lineCount;                    //line #in file, for echo-ing
    private boolean needLine;                 //track when to read a new line

    //Tables to hold the reserve words and the mnemonics for token codes
    private final int sizeReserveTable = 50;
    private ReserveTable reserveWords = new ReserveTable(sizeReserveTable);     //a few more than # reserves
    private ReserveTable mnemonics = new ReserveTable(sizeReserveTable);        //a few more than # reserves

    //symbolic constants
    static final int NOT_FOUND = -1;
    static final int MAX_IDENT_LEN = 20;
    static final int MAX_INT_LEN = 6;
    static final int MAX_FLOAT_LEN = 12;

    //constructor
    public Lexical(String filename, SymbolTable symbols, boolean echoOn) 
    {
        saveSymbols = symbols;  //map the initialized parameter to the local ST
        echo = echoOn;          //store echo status
        lineCount = 0;          //start the line number count
        line = "";              //line starts empty
        needLine = true;        //need to read a line
        printToken = false;     //default OFF, do not print tokesn here within GetNextToken; call setPrintToken to change it publicly.
        linePos = -1;           //no chars read yet call initializations of tables

        initReserveWords(reserveWords);
        initMnemonics(mnemonics);

        //set up the file access, get first character, line retrieved 1st time
        try 
        {
            file = new File(filename);                        //creates a new file instance  
            filereader = new FileReader(file);                //reads the file  
            bufferedreader = new BufferedReader(filereader);  //creates a buffering character input stream  
            EOF = false;
            currCh = GetNextChar();
        } 
        catch (IOException e) 
        {
            EOF = true;
            e.printStackTrace();
        }
    }

    // inner class "token" is declared here, no accessors needed
    public class token 
    {
        public String lexeme;
        public int code;
        public String mnemonic;

        token() {
            lexeme = "";
            code = 0;
            mnemonic = "";
        }
    }

    /*
     * The initialization method for the lexical ReserveWords reserve table.
     * This one inlcludes all of the given identifiers and codes for the 
     * full and 1-2 char instructions.
    */
    private void initReserveWords(ReserveTable reserveWords) 
    {
        reserveWords.Add("GOTO",      0);
        reserveWords.Add("INTEGER",   1);
        reserveWords.Add("TO",        2);
        reserveWords.Add("DO",        3);
        reserveWords.Add("IF",        4);
        reserveWords.Add("THEN",      5);
        reserveWords.Add("ELSE",      6);
        reserveWords.Add("FOR",       7);
        reserveWords.Add("OF",        8);
        reserveWords.Add("WRITELN",   9);
        reserveWords.Add("READLN",    10);
        reserveWords.Add("BEGIN",     11);
        reserveWords.Add("END",       12);
        reserveWords.Add("VAR",       13);
        reserveWords.Add("DOWHILE",   14);
        reserveWords.Add("UNIT",      15);
        reserveWords.Add("LABEL",     16);
        reserveWords.Add("REPEAT",    17);
        reserveWords.Add("UNTIL",     18);
        reserveWords.Add("PROCEDURE", 19);
        reserveWords.Add("DOWNTO",    20);
        reserveWords.Add("FUNCTION",  21);
        reserveWords.Add("RETURN",    22);
        reserveWords.Add("FLOAT",     23);
        reserveWords.Add("STRING",    24);
        reserveWords.Add("ARRAY",     25);

        //1 and 2-char
        reserveWords.Add("/",  30);
        reserveWords.Add("*",  31);
        reserveWords.Add("+",  32);
        reserveWords.Add("-",  33);
        reserveWords.Add("(",  34);
        reserveWords.Add(")",  35);
        reserveWords.Add(";",  36);
        reserveWords.Add(":=", 37);
        reserveWords.Add(">",  38);
        reserveWords.Add("<",  39);
        reserveWords.Add(">=", 40);
        reserveWords.Add("<=", 41);
        reserveWords.Add("=",  42);
        reserveWords.Add("<>", 43);
        reserveWords.Add(",",  44);
        reserveWords.Add("[",  45);
        reserveWords.Add("]",  46);
        reserveWords.Add(":",  47);
        reserveWords.Add(".",  48);
        reserveWords.Add(" ",  99);
    }

    /*
     * This is the initialization method for the lexical mnemonics reserve table.
     * Inludes 5 character mnemonics for all of the same instructions, for both 
     * the full word instructions and for the 1-2 char ones. 
    */
    private void initMnemonics(ReserveTable mnemonics) 
    {
        // Student must create their own 5-char mnemonics
        mnemonics.Add("_GOTO", 0);
        mnemonics.Add("INTGR", 1);
        mnemonics.Add("___TO", 2);
        mnemonics.Add("___DO", 3);
        mnemonics.Add("___IF", 4);
        mnemonics.Add("_THEN", 5);
        mnemonics.Add("_ELSE", 6);
        mnemonics.Add("__FOR", 7);
        mnemonics.Add("___OF", 8);
        mnemonics.Add("WRILN", 9);
        mnemonics.Add("REALN", 10);
        mnemonics.Add("BEGIN", 11);
        mnemonics.Add("__END", 12);
        mnemonics.Add("__VAR", 13);
        mnemonics.Add("DOWHL", 14);
        mnemonics.Add("_UNIT", 15);
        mnemonics.Add("LABEL", 16);
        mnemonics.Add("RPEAT", 17);
        mnemonics.Add("UNTIL", 18);
        mnemonics.Add("PRCDR", 19);
        mnemonics.Add("DWNTO", 20);
        mnemonics.Add("FNCTN", 21);
        mnemonics.Add("RETRN", 22);
        mnemonics.Add("FLOAT", 23);
        mnemonics.Add("STRNG", 24);
        mnemonics.Add("ARRAY", 25);

        //1 and 2-char
        mnemonics.Add("DIVID", 30);
        mnemonics.Add("_MULT", 31);
        mnemonics.Add("__ADD", 32);
        mnemonics.Add("__SUB", 33);
        mnemonics.Add("LPREN", 34);
        mnemonics.Add("RPREN", 35);
        mnemonics.Add("SCOLN", 36);
        mnemonics.Add("ASIGN", 37);
        mnemonics.Add("_GRTR", 38);
        mnemonics.Add("_LESS", 39);
        mnemonics.Add("GREQL", 40);
        mnemonics.Add("LSEQL", 41);
        mnemonics.Add("EQUAL", 42);
        mnemonics.Add("NTEQL", 43);
        mnemonics.Add("COMMA", 44);
        mnemonics.Add("LBRKT", 45);
        mnemonics.Add("RBRKT", 46);
        mnemonics.Add("COLON", 47);
        mnemonics.Add("__DOT", 48);

        //Any other mnemonics
        mnemonics.Add("IDENT", 50);
        mnemonics.Add("UNDEF", 99);
    }

    // ******************* PUBLIC USEFUL METHODS
    // These are nice for syntax to call later 
    // given a mnemonic, find its token code value
    public int codeFor(String mnemonic) {
        return mnemonics.LookupName(mnemonic);
    }

    // given a mnemonic, return its reserve word
    public String reserveFor(String mnemonic) {
        return reserveWords.LookupCode(mnemonics.LookupName(mnemonic));
    }

    // Public access to the current End Of File status
    public boolean EOF() {
        return EOF;
    }

    // DEBUG enabler, turns on/OFF token printing inside of GetNextToken
    public void setPrintToken(boolean on) {
        printToken = on;
    }
        
    // ********************** UTILITY FUNCTIONS
    private void consoleShowError(String message) {
        System.out.println("**** ERROR FOUND: " + message);
    }

    // Character category for alphabetic chars
    private boolean isLetter(char ch) {
        return (((ch >= 'A') && (ch <= 'Z')) || ((ch >= 'a') && (ch <= 'z')));
    }

    // Character category for 0..9 
    private boolean isDigit(char ch) {
        return ((ch >= '0') && (ch <= '9'));
    }

    // Category for any whitespace to be skipped over
    private boolean isWhitespace(char ch) {
        // SPACE, TAB, NEWLINE are white space
        return ((ch == ' ') || (ch == '\t') || (ch == '\n'));
    }

    // Returns the VALUE of the next character without removing it from the
    //    input line.  Useful for checking 2-character tokens that start with
    //    a 1-character token.
    private char PeekNextChar() {
        char result = ' ';
        if ((needLine) || (EOF)) {
            result = ' '; //at end of line, so nothing
        } else // 
        {
            if ((linePos + 1) < line.length()) { //have a char to peek
                result = line.charAt(linePos + 1);
            }
        }
        return result;
    }

    // Called to get the next character from file, automatically gets a new
    //      line when needed. CALL THIS TO GET CHARACTERS FOR GETIDENT etc.
    public char GetNextChar() {
        char result;
        if (needLine) //ran out last time we got a char, so get a new line
        {
            GetNextLine();
        }
        //try to get char from line buff
        if (EOF) {
            result = '\n';
            needLine = false;
        } else {
            if ((linePos < line.length() - 1)) { //have a character available
                linePos++;
                result = line.charAt(linePos);
            } else { //need a new line, but want to return eoln on this call first
                result = '\n';
                needLine = true; //will read a new line on next GetNextChar call
            }
        }
        return result;
    }

    // Called by GetNextChar when the cahracters in the current line are used up.
    // STUDENT CODE SHOULD NOT EVER CALL THIS!
    private void GetNextLine() {
        try {
            line = bufferedreader.readLine();
            if ((line != null) && (echo)) {
                lineCount++;
                System.out.println(String.format("%04d", lineCount) + " " + line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (line == null) {    // The readLine returns null at EOF, set flag
            EOF = true;
        }
        linePos = -1;      // reset vars for new line if we have one
        needLine = false;  // we have one, no need
        //the line is ready for the next call to get a character
    }

    // The constants below allow flexible comment start/end characters    
    final char commentStart_1 = '{';
    final char commentEnd_1 = '}';
    final char commentStart_2 = '(';
    final char commentPairChar = '*';
    final char commentEnd_2 = ')';

    // Skips past single and multi-line comments, and outputs UNTERMINATED 
    //  COMMENT when end of line is reached before terminating
    String unterminatedComment = "Comment not terminated before End Of File";

    public char skipComment(char curr) {
        if (curr == commentStart_1) {
            curr = GetNextChar();
            while ((curr != commentEnd_1) && (!EOF)) {
                curr = GetNextChar();
            }
            if (EOF) {
                consoleShowError(unterminatedComment);
            } else {
                curr = GetNextChar();
            }
        } else {
            if ((curr == commentStart_2) && (PeekNextChar() == commentPairChar)) {
                curr = GetNextChar(); // get the second
                curr = GetNextChar(); // into comment or end of comment
                //while ((curr != commentPairChar) && (PeekNextChar() != commentEnd_2) &&(!EOF)) {
                while ((!((curr == commentPairChar) && (PeekNextChar() == commentEnd_2))) && (!EOF)) {
                    //if (lineCount >=4) {
                    //  System.out.println("In Comment, curr, peek: "+curr+", "+PeekNextChar());}
                    curr = GetNextChar();
                }
                if (EOF) {
                    consoleShowError(unterminatedComment);
                } else {
                    curr = GetNextChar();          //must move past close
                    curr = GetNextChar();          //must get following
                }
            }
        }
        return (curr);
    }

    // Reads past all whitespace as defined by isWhiteSpace
    // NOTE THAT COMMENTS ARE SKIPPED AS WHITESPACE AS WELL!
    public char skipWhiteSpace() {
        do {
            while ((isWhitespace(currCh)) && (!EOF)) {
                currCh = GetNextChar();
            }
            currCh = skipComment(currCh);
        } while (isWhitespace(currCh) && (!EOF));
        return currCh;
    }

    private boolean isPrefix(char ch) {
        return ((ch == ':') || (ch == '<') || (ch == '>'));
    }

    private boolean isStringStart(char ch) {
        return ch == '"';
    }

    //global char
    char currCh;

    private token getIdentifier() 
    {
        token result = new token();
        result.lexeme = "" + currCh; //have the first char
        currCh = GetNextChar();
        //loop until no more characters in the input
        while (isLetter(currCh) || (isDigit(currCh))) {
            result.lexeme = result.lexeme + currCh; //extend lexeme
            currCh = GetNextChar();
        }
        // end of token, lookup or IDENT
        result.code = reserveWords.LookupName(result.lexeme);
        //if the lexeme is not a reserved word, give it the identifer code and mnemonic
        if (result.code == NOT_FOUND){
            result.code = codeFor("IDENT");
            //if lexeme is longer than 20, truncate it, print an error, and add it to the symbol table
            //else, add it to the symbol table untouched
            if(result.lexeme.length() > MAX_IDENT_LEN){
                consoleShowError("Identifier length is " + result.lexeme.length() + ", it will be truncated to 20");
                result.lexeme = result.lexeme.substring(0, MAX_IDENT_LEN);
                saveSymbols.AddSymbol(result.lexeme, 'V', 0);
            }
            else{
                saveSymbols.AddSymbol(result.lexeme, 'V', 0);
            }
        }

        return result;
    }

    private token getNumber() 
    {
        token result = new token();
        result.lexeme = "" + currCh; //have the first char
        currCh = GetNextChar();

        while(isDigit(currCh)){
            result.lexeme = result.lexeme + currCh; //extend lexeme 
            currCh = GetNextChar();
        }
        if(currCh == '.'){
            result.lexeme = result.lexeme + currCh; //add the '.' to lexeme
            currCh = GetNextChar();
            while(isDigit(currCh)){
                result.lexeme = result.lexeme + currCh; //extend lexeme
                currCh = GetNextChar();
            }
            if(currCh == 'E'){
                result.lexeme = result.lexeme + currCh; //add the 'E' to the lexeme
                currCh = GetNextChar();
                while(isDigit(currCh)){
                    result.lexeme = result.lexeme + currCh; //extend lexeme
                currCh = GetNextChar();
                }
            }

            //at this point, entire lexeme has been gathered and is a floating point number
            result.code = codeFor("FLOAT");

            //check if its longer than 12. If so, print error, truncate and add to symbol table
            if(result.lexeme.length() > MAX_FLOAT_LEN){ 
                consoleShowError("Identifier length is " + result.lexeme.length() + ", it will be truncated to 12");
                result.lexeme = result.lexeme.substring(0, MAX_FLOAT_LEN);
                if(result.lexeme.contains("E")){    //check if it has an exponent component
                    String[] split = result.lexeme.split("E");  //use split mathod to spit it around E
                    double val;
                    if(split.length > 1){
                        val = Math.pow(Double.parseDouble(split[0]), Double.parseDouble(split[1]));
                    }else{
                        val = Double.parseDouble(split[0]);
                    }
                    saveSymbols.AddSymbol(result.lexeme, 'C', String.format("%.1f", val)); //add to symbol table
                }
                else{   //if it doesnt have an exponent, just add it to symbol table
                    saveSymbols.AddSymbol(result.lexeme, 'C', Double.parseDouble(result.lexeme));   //get the double value from the lexeme and add to symbol table
                }
            }
            else{   //if its not longer than 20. 
                if(result.lexeme.contains("E")){    //check if it has an exponent component
                    String[] split = result.lexeme.split("E");  //use split method to spit it around E
                    double val;
                    if(split.length > 1){
                        val = Math.pow(Double.parseDouble(split[0]), Double.parseDouble(split[1]));
                    }else{
                        val = Double.parseDouble(split[0]);
                    }
                    saveSymbols.AddSymbol(result.lexeme, 'C', String.format("%.1f", val)); //add to symbol table
                }
                else{   //if it doesnt have an exponent, just add it to symbol table
                    saveSymbols.AddSymbol(result.lexeme, 'C', Double.parseDouble(result.lexeme));   //get the double value from the lexeme and add to symbol table
                }
            }
        }
        else    //original loop only gathered digits and then exited, meaning lexeme is an integer
        {
            result.code = codeFor("INTGR");
            if(result.lexeme.length() > MAX_INT_LEN){
                consoleShowError("Identifier length is " + result.lexeme.length() + ", it will be truncated to 6");
                result.lexeme = result.lexeme.substring(0, MAX_INT_LEN);
                saveSymbols.AddSymbol(result.lexeme, 'C', Integer.parseInt(result.lexeme));
            }
            else{
                saveSymbols.AddSymbol(result.lexeme, 'C', Integer.parseInt(result.lexeme));
            }
        }
        return result;
    }

    private token getString() 
    {
        token result = new token();
        result.lexeme = "" + currCh; //have the first char
        currCh = GetNextChar();

        while(currCh != '"'){
            if(currCh == '\n'){
                consoleShowError("Unterminated string found");
                result.code = codeFor("UNDEF");
                return result;
            }
            else{
                result.lexeme = result.lexeme + currCh;
                currCh = GetNextChar();
            }
        }
        result.code = codeFor("STRNG");
        saveSymbols.AddSymbol(result.lexeme, 'C', result.lexeme);
        return result;
    }

    private token getOtherToken() 
    {
        token result = new token();
        result.lexeme = "" + currCh; //have the first char
        currCh = GetNextChar();
        while(!isWhitespace(currCh) && !isLetter(currCh) && !isDigit(currCh)){
            if(isPrefix(currCh)){
                result.lexeme = result.lexeme + currCh; //extend lexeme
                if(PeekNextChar() == '=' || PeekNextChar() == '>'){ //if the next character is a suffix
                    currCh = GetNextChar(); //get the suffix
                    result.lexeme = result.lexeme + currCh; //extend lexeme with suffix
                }
            }
            else{    
                result.lexeme = result.lexeme + currCh; //extend lexeme    
                currCh = GetNextChar();
            }
        }

        result.code = reserveWords.LookupName(result.lexeme);

        if(result.code == NOT_FOUND){
            result.code = codeFor("UNDEF");
        }

        return result;
    }

    // Checks to see if a string contains a valid DOUBLE 
    public boolean doubleOK(String stin) {
        boolean result;
        Double x;
        try {
            x = Double.parseDouble(stin);
            result = true;
        } catch (NumberFormatException ex) {
            result = false;
        }
        return result;
    }

    // Checks the input string for a valid INTEGER
    public boolean integerOK(String stin) {
        boolean result;
        int x;
        try {
            x = Integer.parseInt(stin);
            result = true;
        } catch (NumberFormatException ex) {
            result = false;
        }
        return result;
    }

    public token GetNextToken() {
        token result = new token();

        currCh = skipWhiteSpace();
        if (isLetter(currCh)) { //is identifier
            result = getIdentifier();
        } else if (isDigit(currCh)) { //is numeric
            result = getNumber();
        } else if (isStringStart(currCh)) { //string literal
            result = getString();
        } else{ //default char checks
            result = getOtherToken();
        }

        if ((result.lexeme.equals("")) || (EOF)) {
            result = null;
        }
        //set the mnemonic
        if (result != null) {            
            result.mnemonic = mnemonics.LookupCode(result.code);
            if (printToken) {
                System.out.println("\t" + result.mnemonic + " | \t" + String.format("%04d", result.code) + " | \t" + result.lexeme);
            }
        }
        return result;
    }
}
