// INPUT: java CPU sample.txt 30

import java.io.*;
import java.util.Scanner;

public class Memory
{
  private static int [] mem = new int[2000];    // Used to store memory

  public static void main (String args[])
  {
    Scanner CPU = new Scanner(System.in);    // Used to read input from the CPU
    File infile = null;                      // Used for filename
    Scanner input = null;                    // Used to go through file


    int currentNum = 0;    // Will contain number from file
    int index = 0;         // Will contain element number of memory array
    String currentString;  // Will contain a String for comments or '.'
    String line;           // Will contain the line for the CPU scanner
    int address;           // Stores the addresss for write method
    int val;               // Stores value for the write method
    int num;               // Will contain the int value of the strings for the CPU scanner

    //**********************************************
    //                  GET FILE                   *
    //**********************************************
    try
    {
      if(CPU.hasNextLine())
      {
        infile = new File(CPU.nextLine());

        // Input Validation: If there is no file, then close.
        if(!(infile.exists()))
        {
          System.out.println("Cannot find file.");
          System.exit(0);
        }
      }

      //**********************************************
      //            READ FILE CONTENT                *
      //**********************************************
      try
      {
        input = new Scanner(infile);

        while(input.hasNext())
        {
          // Store int into memory
          if(input.hasNextInt())
          {
            currentNum = input.nextInt();
            mem[index] = currentNum;
            index++;
          }
          else
          {
            currentString = input.next();


            if(currentString.equals("//"))
            {
              // Skip line if the line is a comment
              input.nextLine();
            }
            else if(currentString.charAt(0) == '.')
            {
              // Get value if there is a '.' in the front
              index = Integer.parseInt(currentString.substring(1));
            }
            else
              input.nextLine();
          }
        }
      }
      catch(FileNotFoundException e)
      {
        e.printStackTrace();
      }
      //*********************************************
      //          EXECUTE READ AND WRITES           *
      //*********************************************

      // Read instructions and execute
      // by the CPU read or write functions requested
      while(true)
      {
        if(CPU.hasNext())
        {
          line = CPU.nextLine();
          if(!(line.isEmpty()))
          {
            // Testing to see if getting stuff out of file
            //System.out.println(line);

            // Parses through CPU
            String [] tokens = line.split(",");

            // A 1 means the CPU wants to read
            // Else, a 2 means the CPU wants to write
            if(tokens[0].equals("1"))
            {
              num = Integer.parseInt(tokens[1]);
              System.out.println(read(num));
            }
            else
            {
              address = Integer.parseInt(tokens[1]);
              val = Integer.parseInt(tokens[2]);
              write(address, val);
            }
          }
          else
            break;
        }
        else
          break;
      }

    }
    catch(NumberFormatException e )
    {
      e.printStackTrace();
    }

  } // end main

  //*********************************************
  //                 METHODS                    *
  //*********************************************

  public static int read(int address)
  {
    return mem[address];
  }
  public static void write(int address, int value)
  {
    mem[address] = value;
  }


} // end class
