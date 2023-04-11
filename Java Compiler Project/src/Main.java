import ADT.*;

public class Main 
{
    public static void main(String[] args) throws Exception 
    {
        //Eli Hoehne, 4886, CS4100, SPRING 2023
        System.out.println("Eli Hoehne, 4886, CS4100, SPRING 2023");

        String filePath = "GoodSyntaxA-SP23.txt";
        boolean traceon = true;
        System.out.println("INPUT FILE TO PROCESS IS: "+filePath);
    
        Syntactic parser = new Syntactic(filePath, traceon);
        parser.parse();
        System.out.println("Done.");
    }
}
