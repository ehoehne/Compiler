package ADT;

public class Symbol {
    public String name;
    public char usage;
    public char dataType;
    public int integerValue;
    public double floatValue;
    public String stringValue;

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
