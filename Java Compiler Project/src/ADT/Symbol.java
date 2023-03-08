package ADT;

//this is the symbol class to hold the required information for each entry in the symbol table
public class Symbol {
    //data points to hold all information
    //public so they can be accessed by the symboltable class
    public String name;
    public char usage;
    public char dataType;
    public int integerValue;
    public double floatValue;
    public String stringValue;

    //constructor that is called when adding data to the table
    public Symbol(String name, char usage, char dataType, int integerValue, double floatValue, String stringValue)
    {
        this.name = name;
        this.usage = usage;
        this.dataType = dataType;
        this.integerValue = integerValue;   
        this.floatValue = floatValue;
        this.stringValue = stringValue;
    }
}
