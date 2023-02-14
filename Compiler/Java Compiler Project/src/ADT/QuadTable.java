package ADT;

import java.io.*;

public class QuadTable 
{
    //private
    private int[][] table;  
    private int nextAvailable;

    //constructor, creates a 2d array with maxSize rows and 4 columns. sets the nextAv to 0
    public QuadTable(int maxSize)
    {
        table = new int[maxSize][4];
        nextAvailable = 0;
    }
    
    //function to get the next available index in the table
    public int NextQuad()
    {
        return this.nextAvailable;
    }

    //function to add a quad (row) to the table
    //adds each entered datapoint into its spot in the row. increments the next available index
    public void AddQuad(int opcode, int op1,  int op2, int op3)
    {
        this.table[this.nextAvailable][0] = opcode;
        this.table[this.nextAvailable][1] = op1;
        this.table[this.nextAvailable][2] = op2;
        this.table[this.nextAvailable][3] = op3;
        this.nextAvailable++;
    }

    //function to return a quad (row) of the table
    public int[] GetQuad(int index)
    {
        return this.table[index];
    }

    //updates op3 data point from a specified quad
    //the assignment only says to do op3, could be modified to do any column
    public void UpdateJump(int index, int op3)
    {
        this.table[index][3] = op3;
    }

    //function to print table to file
    //formatted to match example
    public void PrintQuadTable(String filename) throws IOException
    {
        try {
            FileOutputStream outputStream = new FileOutputStream(filename);

            //OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream, "UTF-16");
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream);
            BufferedWriter writer = new BufferedWriter(outputStreamWriter);
            PrintWriter pw = new PrintWriter(writer);

            pw.println("Index Opcode   Op1   Op2   Op3");

            for(int row = 0; row < this.nextAvailable; row++)
            {
                pw.printf("%3d  | ", row);
                for(int col = 0; col < this.table[col].length; col++)
                {
                    pw.printf(" %3d |", this.table[row][col]);
                }
                pw.println();
            }
            
            pw.close();
        } 
        catch (IOException e) {
            e.printStackTrace();
        }    
    }
}
