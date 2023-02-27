import ADT.*;

public class Main {
    public static void main(String[] args) throws Exception {
        // Expects 6 command-line parameters for filenames, 
        //     see arg[0] through arg[5] below 
        Interpreter interp = new Interpreter();
        SymbolTable st;
        QuadTable qt;
        
        // interpretation FACTORIAL
        st = new SymbolTable(20);     //Create an empty SymbolTable
        qt = new QuadTable(20);       //Create an empty QuadTable
        System.out.println("This program expects command-line parameters for filenames in this order:");
        System.out.println("traceFactorial SymbolFactorial QuadFactorial traceSum SymbolSum QuadSum");
        interp.initializeFactorialTest(st,qt);  //Set up for FACTORIAL
        interp.InterpretQuads(qt, st, true, args[0]);
        st.PrintSymbolTable(args[1]);
        qt.PrintQuadTable(args[2]);

        // interpretation SUMMATION
        st = new SymbolTable(20);     //Create an empty SymbolTable
        qt = new QuadTable(20);       //Create an empty QuadTable
        interp.initializeSummationTest(st,qt);  //Set up for SUMMATION
        interp.InterpretQuads(qt, st, true, args[3]);
        st.PrintSymbolTable(args[4]);
        qt.PrintQuadTable(args[5]);
    }
    
}
