package ADT;

import java.io.*;
import java.util.Scanner;

public class Interpreter 
{   
    //symbolic constants for each of the instructions to be used in interpreter
    static final int STOP = 0;
    static final int DIV = 1;
    static final int MUL = 2;
    static final int SUB = 3;
    static final int ADD = 4;
    static final int MOV = 5;
    static final int PRINT = 6;
    static final int READ = 7;
    static final int JMP = 8;
    static final int JZ = 9;
    static final int JP = 10;
    static final int JN = 11;
    static final int JNZ = 12;
    static final int JNP = 13;
    static final int JNN = 14;
    static final int JINDR = 15;

    //create a reservetable (optable)
    private ReserveTable optable = new ReserveTable(20);

    //constructor
    public Interpreter()
    {
        InitReserve(optable);  //intialize reserve table values using InitReserve function
    }

    /*
    *  This is the interpreter function for the instructions described by each line of the quad table
    *  loops over each line of the quadtable, uses a switch statement to pick the instruction based on 
    *  the opcode, which is the first number in each row of the quadtable. Uses constants to make the 
    *  switch statement more readable. Has a default case for invalid instruction values.
    * 
    *  Prints each trace line to a trace output file using the provided formatting function.
    *  Performs the required operations for each instruction.
    *  Will print output to console AND to output file
    */
    public void InterpretQuads(QuadTable Q, SymbolTable S, boolean TraceOn, String filename)
    {   
        try
        {
            //create scanner for READ, create writer to write to file
            Scanner in = new Scanner(System.in);
            FileOutputStream outputStream = new FileOutputStream(filename);
            PrintWriter pw = new PrintWriter(outputStream);

            //Required first comment
            //Eli Hoehne CS 4100 Homework 3 Spring 2023
            pw.println("Eli Hoehne CS 4100 Homework 3 Spring 2023\n");

            //start PC at 0, create an array to hold current quad
            int PC = 0;
            int[] quad = new int[4];

            //loop until the PC is no longer less than the maxquad
            while(PC < Q.NextQuad())    
            {   
                //put the current quad into the quad array, then store each index in a variable for readability
                quad = Q.GetQuad(PC); 
                int opcode = quad[0];
                int op1 = quad[1];
                int op2 = quad[2];
                int op3 = quad[3];

                //print the trace string for current quad if TraceOn is true
                if(TraceOn)
                    pw.println(makeTraceString(PC, opcode, op1, op2, op3));  
                    System.out.println(makeTraceString(PC, opcode, op1, op2, op3));
                /*
                *   Switch statement for each instruction, performs a different task based on which 
                *   instruction the current quad is describing. 
                *   this switch implements the instructions how they are described in the IntoToQuads doc,
                *   using the UpdateSymbol method to update the symbol table with the correct values
                */
                switch(opcode)
                {
                    case STOP:     //stops execution
                        pw.println("Execution terminated by program STOP");
                        System.out.println("Execution terminated by program STOP");
                        PC = Q.NextQuad();
                        break;

                    case DIV:     //divides op1 by op2 (integer division) and places in op3
                        S.UpdateSymbol(op3, S.GetUsage(op3), S.GetInteger(op1) / S.GetInteger(op2));
                        PC++;
                        break;

                    case MUL:     //multiplies op1 by op2 and places in op3
                        S.UpdateSymbol(op3, S.GetUsage(op3), S.GetInteger(op1) * S.GetInteger(op2));
                        PC++;
                        break;

                    case SUB:     //subtracts op2 from op1 and places in op3
                        S.UpdateSymbol(op3, S.GetUsage(op3), S.GetInteger(op1) - S.GetInteger(op2));
                        PC++;
                        break;

                    case ADD:     //adds op1 to op2 and places in op3
                        S.UpdateSymbol(op3, S.GetUsage(op3), S.GetInteger(op1) + S.GetInteger(op2));
                        PC++;
                        break;

                    case MOV:     //moves op1 to op3
                        S.UpdateSymbol(op3, S.GetUsage(op3), S.GetInteger(op1));
                        PC++;
                        break;

                    case PRINT:     //print statement prints correct value based on datatype in symbol table    
                        if(S.GetDataType(op3) == 'I'){
                            pw.println(S.GetSymbol(op3) + " = " + S.GetInteger(op3));
                            System.out.println(S.GetInteger(op3));
                        }
                        else if(S.GetDataType(op3) == 'F'){
                            pw.println(S.GetSymbol(op3) + " = " + S.GetFloat(op3));
                            System.out.println(S.GetFloat(op3));
                        }
                        else if(S.GetDataType(op3) == 'S'){
                            if(S.GetInteger(op3) != 0)
                                System.out.println(S.GetInteger(op3));
                            else
                                System.out.println(S.GetSymbol(op3));
                        }
                        PC++;
                        break;

                    case READ:    //Read Line
                        System.out.print(">");
                        int inVal = in.nextInt();
                        S.UpdateSymbol(op3, S.GetUsage(op3), inVal);
                        in.close();
                        PC++;
                        break;

                    case JMP:     //unconditional jump
                        PC = op3;
                        break;

                    case JZ:     //jump if 0
                        if(S.GetInteger(op1) == 0)
                            PC = op3;
                        else
                            PC++;
                        break;

                    case JP:   //jump if positive 
                        if(S.GetInteger(op1) > 0)
                            PC = op3;
                        else
                            PC++;
                        break;

                    case JN:   //jump if negative
                        if(S.GetInteger(op1) < 0)
                            PC = op3;
                        else
                            PC++;
                        break;

                    case JNZ:    //jump if not 0 
                        if(S.GetInteger(op1) != 0)
                            PC = op3;
                        else
                            PC++;
                        break;

                    case JNP:    //jump if not positive
                        if(S.GetInteger(op1) <= 0)
                            PC = op3;
                        else
                            PC++;
                        break;

                    case JNN:    //jump if not negative
                        if(S.GetInteger(op1) >= 0)
                            PC = op3;
                        else
                            PC++;
                        break;

                    case JINDR:    //jump indirect, unconditional. jumps to op3 in symbol table
                        PC = S.GetInteger(op3);
                        break;

                    default:    //default in case of invalid opcode
                        pw.println("Invalid opcode");
                        System.out.println("Invalid opcode");
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

    //adds predetermined opcodes to a reserve table
    //this function gets called by the Interpreter constructor, adding the instructions to its reserve table
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

    public int opcodeFor(String opName)
    {
        return optable.LookupName(opName);
    }

    //creates and returns a string using the provided information about the current trace. This function was given.
    private String makeTraceString(int pc, int opcode,int op1,int op2,int op3 )
    {
        String result = "";
        result = "PC = "+String.format("%04d", pc)+": "+(optable.LookupCode(opcode)+"     ").substring(0,6)+String.format("%02d",op1)+
                                ", "+String.format("%02d",op2)+", "+String.format("%02d",op3);
        return result;
    }
}