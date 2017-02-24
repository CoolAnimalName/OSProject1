import java.io.*;
import java.util.Scanner;

public class Memory {


  public static void main (String args[]) {
    Scanner request = new Scanner(System.in);    // Used to read input from the request
    Scanner input = null;
    File inputFile = new File(args[0]);
    int currentNum = 0;    // Will contain number from file
    String currentString;  // Will contain a String for comments or '.'
    String line;           // Will contain the line for the request scanner
    int address;           // Stores the addresss for write method
    int val;               // Stores value for the write method
    int num;               // Will contain the int value of the strings for the request scanner
    int[] mem = new int[2000];    // Used to store memory



        if(!inputFile.exists()) {
          System.out.println("Cannot find file.");
          System.exit(0);
        } //end nested if

      try {

        input = new Scanner(inputFile);
        int i = 0;
        while(input.hasNext()) {
          // Store int into memory
          if(input.hasNextInt()) {
            currentNum = input.nextInt();
            mem[i++] = currentNum;
          } //end if
          else {
            currentString = input.next();

            if(currentString.equals("//"))
              input.nextLine();
            else if(currentString.charAt(0) == '.')
              i = Integer.parseInt(currentString.substring(1));
            else
              input.nextLine();
          } //end else
        } //end while
      } //end try
      catch(FileNotFoundException e) {
        e.printStackTrace();
      } //end catch

      while(true) {
        if(request.hasNext()) {
          line = request.nextLine();
          if(!(line.isEmpty())) {

            String [] tokens = line.split(",");

            if(tokens[0].equals("1")) { //read request
              num = Integer.parseInt(tokens[1]);
              System.out.println(mem[num]);
            } //end nested if
            else { //write request since if not 1 it is 2
              address = Integer.parseInt(tokens[1]);
              val = Integer.parseInt(tokens[2]);
              mem[address] = val;
            } //end nested else
          } //end if
          else
            break;
        } //end if
        else
          break;
      } //end while
    
  } // end main

} // end Memory
