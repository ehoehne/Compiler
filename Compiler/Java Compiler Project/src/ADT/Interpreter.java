package ADT;

import java.io.*;
import java.util.Scanner;

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
            try
            {
                //create scanner for READ, create writer to write to file
                Scanner in = new Scanner(System.in);
                FileOutputStream outputStream = new FileOutputStream(filename);
                PrintWriter pw = new PrintWriter(outputStream);

                //start PC at 0, create an array to hold current quad
                int PC = 0;
                int[] quad = new int[4];

                while(PC < Q.NextQuad())    //loop until the PC is no longer less than the maxquad
                {   
                    quad = Q.GetQuad(PC);   //put the current quad into the quad array, then store each index
                    int opcode = quad[0];
                    int op1 = quad[1];
                    int op2 = quad[2];
                    int op3 = quad[3];

                    if(TraceOn)
                        pw.println(makeTraceString(PC, opcode, op1, op2, op3));     //print the trace string for current quad

                    //switches based on the given instruction. 
                    //performs a different task based on which instruction the current quad is describing
                    switch(opcode)
                    {
                        case 0:     //STOP
                            System.out.println("Execution terminated by program STOP");
                            PC = Q.NextQuad();
                            break;
                        case 1:     //DIV
                            S.UpdateSymbol(op3, S.GetUsage(op3), S.GetInteger(op1) / S.GetInteger(op2));
                            PC++;
                            break;
                        case 2:     //MUL
                            S.UpdateSymbol(op3, S.GetUsage(op3), S.GetInteger(op1) * S.GetInteger(op2));
                            PC++;
                            break;
                        case 3:     //SUB
                            S.UpdateSymbol(op3, S.GetUsage(op3), S.GetInteger(op1) - S.GetInteger(op2));
                            PC++;
                            break;
                        case 4:     //ADD
                            S.UpdateSymbol(op3, S.GetUsage(op3), S.GetInteger(op1) + S.GetInteger(op2));
                            PC++;
                            break;
                        case 5:     //MOV
                            S.UpdateSymbol(op3, S.GetUsage(op3), S.GetInteger(op1));
                            PC++;
                            break;
                        case 6:     //PRINT
                            if(S.GetDataType(op3) == 'I')
                                pw.println(S.GetSymbol(op3) + " = " + S.GetInteger(op3));
                            else if(S.GetDataType(op3) == 'F')
                                pw.println(S.GetSymbol(op3) + " = " + S.GetFloat(op3));
                            else if(S.GetDataType(op3) == 'S')
                                pw.println(S.GetSymbol(op3) + " = " + S.GetString(op3));
                            break;
                        case 7:     //READ
                            int inVal = in.nextInt();
                            S.UpdateSymbol(op1, S.GetUsage(op1), inVal);
                            break;
                        case 8:     //JMP
                            PC = op3;
                            break;
                        case 9:     //JZ
                            if(S.GetInteger(op1) == 0)
                                PC = op3;
                            else
                                PC++;
                            break;
                        case 10:    //JP
                            if(S.GetInteger(op1) > 0)
                                PC = op3;
                            else
                                PC++;
                            break;
                        case 11:    //JN
                            if(S.GetInteger(op1) < 0)
                                PC = op3;
                            else
                                PC++;
                            break;
                        case 12:    //JNZ
                            if(S.GetInteger(op1) != 0)
                                PC = op3;
                            else
                                PC++;
                            break;
                        case 13:    //JNP
                            if(S.GetInteger(op1) <= 0)
                                PC = op3;
                            else
                                PC++;
                            break;
                        case 14:    //JNN
                            if(S.GetInteger(op1) >= 0)
                                PC = op3;
                            else
                                PC++;
                            break;
                        case 15:    //JINDR
                            PC = S.GetInteger(op3);
                            break;
                        default:
                            pw.println("Invalid opcode");
                            break;
                    }
                }
            in.close();
            pw.close();
            }
            catch (IOException e) {
                e.printStackTrace();
            }    
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
            qt.AddQuad(0, 0, 0, 0);     //STOP
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
            st.AddSymbol("sum", 'V', 0);
            st.AddSymbol("1", 'C', 1);
            st.AddSymbol("$temp", 'V', 0);  
        }

        //summation Quads 
        public void InitQTS(QuadTable qt) 
        {
            qt.AddQuad(5, 3, 0, 2);     //MOV
            qt.AddQuad(5, 3, 0, 1);     //MOV
            qt.AddQuad(3, 1, 0, 4);     //SUB
            qt.AddQuad(10, 4, 0, 7);    //JP
            qt.AddQuad(2, 2, 1, 2);     //MUL
            qt.AddQuad(4, 1, 3, 1);     //ADD
            qt.AddQuad(8, 0, 0, 2);     //JMP
            qt.AddQuad(6, 0, 0, 2);     //PRINT
            qt.AddQuad(0, 0, 0, 0);     //STOP   
        }
    //#endregion
    
    //creates and returns a string using the provided information about the current trace. given. 
    private String makeTraceString(int pc, int opcode,int op1,int op2,int op3 )
    {
        String result = "";
        result = "PC = "+String.format("%04d", pc)+": "+(optable.LookupCode(opcode)+"     ").substring(0,6)+String.format("%02d",op1)+
                                ", "+String.format("%02d",op2)+", "+String.format("%02d",op3);
        return result;
    }
}
