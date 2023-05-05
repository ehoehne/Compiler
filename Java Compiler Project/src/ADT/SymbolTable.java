package ADT;
import java.io.*;

//implements another class called Symbol, which the SymbolTable class creates an array of.
public class SymbolTable 
{
    //private
    private Symbol[] table;
    private int count;

    //constructor
    public SymbolTable(int maxSize)
    {
        table = new Symbol[maxSize];
        count = -1;
    }

    //addsymbol function for integer values
    public int AddSymbol(String name, char usage, int value)
    {
        int index = LookupSymbol(name); //check to see if the name is already in the table 

        if(index == -1) 
        {
            if(count < (table.length - 1))  //if it isn't, make sure the table isnt full before adding it
            {
                count++;
                this.table[count] = new Symbol(name, usage, 'I', value, 0.0, "");   //create a new row of information with 'I' type, and the entered information
                index = count;
            }
            else
            {
                return -1;  //if the table is full, return -1 and do nothing
            }
        }
        return index;   //if it's already in the table, return the current index (-1) and do nothing
    }

    //addsymbol function for float values.
    public int AddSymbol(String name, char usage, double value)
    {
        int index = LookupSymbol(name);

        if(index == -1)
        {
            if(count < (table.length - 1))
            {
                count++;
                this.table[count] = new Symbol(name, usage, 'F', 0, value, ""); //create a new row of information with 'F' type, and the entered information
                index = count;
            }
            else
            {
                return -1;
            }
        }
        return index;
    }

    //addsymbol function for string type
    public int AddSymbol(String name, char usage, String value)
    {
        int index = LookupSymbol(name);

        if(index == -1)
        {
            if(count < (table.length - 1))
            {
                count++;
                this.table[count] = new Symbol(name, usage, 'S', 0, 0.0, value);    //create a new row of information with 'S' type, and the entered information
                index = count;
            }
            else
            {
                return -1;
            }
        }
        return index;
    }

    //function to search for a name (symbol) in the table 
    //performs a case-insensitive search and returns the index if found or -1 if not found
    public int LookupSymbol(String symbol)
    {
        int result = -1;

        for(int i = 0; i <= count; i++)
        {
            if(this.table[i].name.equalsIgnoreCase(symbol))
            {
                result = i;
            }
        }
        return result;
    }

    //functions to update symbols, overloaded for the different types
    public void UpdateSymbol(int index, char usage, int value)
    {
        this.table[index].usage = usage;
        this.table[index].integerValue = value;
    }

    public void UpdateSymbol(int index, char usage, double value)
    {
        this.table[index].usage = usage;
        this.table[index].floatValue = value;
    }

    public void UpdateSymbol(int index, char usage, String value)
    {
        this.table[index].usage = usage;
        this.table[index].stringValue = value;
    }

    //functions to get the information data points 
    public String GetSymbol(int index)
    { return this.table[index].name; }

    public char GetUsage(int index)
    { return this.table[index].usage; }

    public char GetDataType(int index)
    { return this.table[index].dataType; }

    public int GetInteger(int index)
    { return this.table[index].integerValue; }

    public double GetFloat(int index)
    { return this.table[index].floatValue; }

    public String GetString(int index)
    { return this.table[index].stringValue; }

    //print the table to a file
    //fomatted to match example
    public void PrintSymbolTable(String filename)
    {
        try {
            FileOutputStream outputStream = new FileOutputStream(filename);

            //OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream, "UTF-16");
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream);
            BufferedWriter writer = new BufferedWriter(outputStreamWriter);
            PrintWriter pw = new PrintWriter(writer);

            pw.println("Index  Name                       Use   Type  Value");

            for(int i = 0; i <= count; i++)
            {
                if(table[i].dataType == 'I' /*&& table[i].integerValue != 0*/)
                    pw.printf("%3d  | %-25s | %-3c | %-3c | %-3d \n", i, this.table[i].name, this.table[i].usage, this.table[i].dataType, this.table[i].integerValue);
                else if(table[i].dataType == 'S' && table[i].stringValue != null)
                pw.printf("%3d  | %-25s | %-3c | %-3c | %s \n", i, this.table[i].name, this.table[i].usage, this.table[i].dataType, this.table[i].stringValue);
                else if(table[i].dataType == 'F' && table[i].floatValue != 0.0)
                pw.printf("%3d  | %-25s | %-3c | %-3c | %.4f \n", i, this.table[i].name, this.table[i].usage, this.table[i].dataType, this.table[i].floatValue);
            }

            pw.close();
        } 
        catch (IOException e) {
            e.printStackTrace();
        }    
    }
}
