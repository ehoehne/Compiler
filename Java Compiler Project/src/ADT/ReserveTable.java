package ADT;
import java.util.*;
import java.io.*;

public class ReserveTable 
{
    //private
    private ArrayList<String> names;
    private ArrayList<Integer> codes;

    public ReserveTable(int maxSize) 
    {
        names = new ArrayList<String>(maxSize);
        codes = new ArrayList<Integer>(maxSize);
    }

    //method to add elements to the lists
    public int Add(String name, int code) 
    {
        this.names.add(name);
        this.codes.add(code);
        return this.names.size() - 1;
    }
    
    //method to search for an element using the name, case insensitive search
    //will return the code if found, or -1 if not found
    public int LookupName(String name) 
    {
        int i = 0;
        Iterator<String> it = this.names.iterator();

        while(it.hasNext())
        {
            if(it.next().compareToIgnoreCase(name) == 0)
            {
                int index = i;
                return this.codes.get(index);
            }
            i++;
        }
        return -1;
    }

    //method to search for an element using the code
    //will return the name if found, or "" if not found;
    public String LookupCode(int code) 
    {
        if(this.codes.contains(code))
        {
            int index = this.codes.indexOf(code);
            return this.names.get(index);
        }
        else
        {
            return "";
        }
    }

    //method to print the table to the output file
    public void PrintReserveTable(String filename) 
    {
        //Prints to the named file with the required error catching 
        try {
            FileOutputStream outputStream = new FileOutputStream(filename);

            //OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream, "UTF-16");
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream);
            BufferedWriter writer = new BufferedWriter(outputStreamWriter);
            PrintWriter pw = new PrintWriter(writer);

            pw.println("Index   Name    Code");

            for(int i = 0; i < names.size(); i++)
            {
                pw.printf("%-7d %-5s %5d\n", i, names.get(i), codes.get(i));
            }
            
            pw.close();
        } 
        catch (IOException e) {
            e.printStackTrace();
        }    
    } 
}
