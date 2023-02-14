package ADT;

import java.io.*;

public class SymbolTable 
{
    //private
    private Symbol[] table;
    private int count;

    public SymbolTable(int maxSize)
    {
        table = new Symbol[maxSize];
        count = -1;
    }

    public int AddSymbol(String name, char usage, int value)
    {
        int index = LookupSymbol(name);

        if(index == -1)
        {
            if(count < (table.length - 1))
            {
                count++;
                this.table[count] = new Symbol(name, usage, 'I', value, 0.0, "");
                index = count;
            }
            else
            {
                return -1;
            }
        }
        return index;
    }

    public int AddSymbol(String name, char usage, double value)
    {
        int index = LookupSymbol(name);

        if(index == -1)
        {
            if(count < (table.length - 1))
            {
                count++;
                this.table[count] = new Symbol(name, usage, 'F', 0, value, "");
                index = count;
            }
            else
            {
                return -1;
            }
        }
        return index;
    }

    public int AddSymbol(String name, char usage, String value)
    {
        int index = LookupSymbol(name);

        if(index == -1)
        {
            if(count < (table.length - 1))
            {
                count++;
                this.table[count] = new Symbol(name, usage, 'S', 0, 0.0, value);
                index = count;
            }
            else
            {
                return -1;
            }
        }
        return index;
    }

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

    public void PrintSymbolTable(String filename)
    {
        try {
            FileOutputStream outputStream = new FileOutputStream(filename);

            //OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream, "UTF-16");
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream);
            BufferedWriter writer = new BufferedWriter(outputStreamWriter);
            PrintWriter pw = new PrintWriter(writer);

            pw.println("Index  Name             Use Typ Value");

            for(int i = 1; i < this.table.length; i++)
            {
                pw.printf("%3d  | %25s | %-3c | %-3c", i, this.table[i].name, this.table[i].usage, this.table[i].dataType);
            }

            pw.close();
        } 
        catch (IOException e) {
            e.printStackTrace();
        }    
    }
}
