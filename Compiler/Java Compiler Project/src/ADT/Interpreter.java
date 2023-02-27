package ADT;

public class Interpreter {
    
    //create reservetable
    private ReserveTable optable;

    public Interpreter()
    {
        InitReserve(optable);
    }
    
    //#region Interpreter
        //this is the interpreter function for each of the quads. This 
        public void InterpretQuads(QuadTable Q, SymbolTable S, boolean TraceOn, String filename)
        {



        }
    //#endregion

    //adds predetermined opcodes to a reserve table
    private void InitReserve(ReserveTable optable)
    {
        optable.Add("STOP", 0);
        optable.Add("DIV", 1);
        optable.Add("MUL", 2);
        optable.Add("SUB", 3);
        optable.Add("ADD", 4);
        optable.Add("MOV", 5);
        optable.Add("PRINT", 6);
        optable.Add("READ", 7);
        optable.Add("JMP", 8);
        optable.Add("JZ", 9);
        optable.Add("JP", 10);
        optable.Add("JN", 11);
        optable.Add("JNZ", 12);
        optable.Add("JNP", 13);
        optable.Add("JNN", 14);
        optable.Add("JINDR", 15);
    }

    //#region FactorialInit
        //intialize the factorial test
        public boolean initializeFactorialTest(SymbolTable stable, QuadTable qtable) 
        {
            InitSTF(stable);
            InitQTF(qtable);
            return true;
        }

        //factorial Symbols  
        public static void InitSTF(SymbolTable st) 
        {
            st.AddSymbol("n", 'V', 10);
            st.AddSymbol("i", 'V', 0);
            st.AddSymbol("product", 'V', 0);
            st.AddSymbol("1", 'C', 1);
            st.AddSymbol("$temp", 'V', 0);
        }

        //factorial Quads 
        public void InitQTF(QuadTable qt) 
        {
            qt.AddQuad(5, 3, 0, 2);     //MOV
            qt.AddQuad(5, 3, 0, 1);     //MOV
            qt.AddQuad(3, 1, 0, 4);     //SUB
            qt.AddQuad(10, 4, 0, 7);    //JP
            qt.AddQuad(2, 2, 1, 2);     //MUL
            qt.AddQuad(4, 1, 3, 1);     //ADD
            qt.AddQuad(8, 0, 0, 2);     //JMP
            qt.AddQuad(6, 0, 0, 2);     //PRINT
        }
    //#endregion

    //#region SummationInit
        //initialize the summation test
        public boolean initializeSummationTest(SymbolTable stable, QuadTable qtable) 
        {
            InitSTS(stable);
            InitQTS(qtable);
            return true;
        }

        //summation Symbols  
        public static void InitSTS(SymbolTable st) 
        {
            st.AddSymbol("n", 'V', 10);
            st.AddSymbol("i", 'V', 0);
            //... put the rest of the Symbol table entries below...    
        }

        //summation Quads 
        public void InitQTS(QuadTable qt) 
        {
            qt.AddQuad(5, 3, 0, 2); //MOV
            qt.AddQuad(5, 3, 0, 1); //MOV
            qt.AddQuad(3, 1, 0, 4); //SUB
            //... put the rest of the Quad table entries below...    

        }
    //#endregion

    private String makeTraceString(int pc, int opcode,int op1,int op2,int op3 )
    {
        String result = "";
        result = "PC = "+String.format("%04d", pc)+": "+(optable.LookupCode(opcode)+"     ").substring(0,6)+String.format("%02d",op1)+
                                ", "+String.format("%02d",op2)+", "+String.format("%02d",op3);
        return result;
    }
}
