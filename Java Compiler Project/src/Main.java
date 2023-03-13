import ADT.*;

public class Main {
    public static void main(String[] args) throws Exception {

        //Eli Hoehne CS 4100 Spring 2023
        System.out.println("Eli Hoehne CS 4100 Part 2 Spring 2023");

        String inFileAndPath = args[0];
        String outFileAndPath = args[1];
        System.out.println("Lexical for " + inFileAndPath);
        boolean traceOn = true;
        // Create a symbol table to store appropriate3 symbols found
        SymbolTable symbolList;
        symbolList = new SymbolTable(150);
        Lexical myLexer = new Lexical(inFileAndPath, symbolList,traceOn);
        Lexical.token currToken;
        //LexicalReserve myLexer = new LexicalReserve(inFileAndPath, symbolList, traceOn);
        //LexicalReserve.token currToken;
        currToken = myLexer.GetNextToken();
        while (currToken != null) {
            System.out.println("\t" + currToken.mnemonic + " | \t" + String.format("%04d", currToken.code) + " | \t" + currToken.lexeme);
            currToken = myLexer.GetNextToken();
        }
        symbolList.PrintSymbolTable(outFileAndPath);
        System.out.println("Done.");
    }
}
