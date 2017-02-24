/******************************************************************************
* Matthew Villarreal (miv140130)
* CS 4348.002
* Project 1
*******************************************************************************
*******************************************************************************
*                                   Memory.java
*
* This program initialized the memory based on the file input, parses commands
* received from CPU.java, then executes a read or write operation based on the
* type of command received. A read operation prints the data from a specified
* memory address back to the CPU, and a write request overwrites a specified
* memory address with the data received.
******************************************************************************/

import java.io.*;
import java.util.Scanner;

public class Memory {

  public static void main (String args[]) {
    File inputFile = new File(args[0]); //gets file from CPU exec call
    int[] mem = new int[2000]; //memory initalization

    if(!inputFile.exists()) { //checks to make sure the init file is valid
      System.out.println("Cannot find file.");
      System.exit(0);
    } //end nested if

    try {
      Scanner opScan = new Scanner(System.in); //received commands from CPU.java
      Scanner scanIn = new Scanner(inputFile); //reads from the inputFile
      String parse; //needed to ignore comments and change loading location
      int i = 0; //index for mem

      while(scanIn.hasNext()) { //reads from file into mem
        if(scanIn.hasNextInt())
          mem[i++] = scanIn.nextInt(); //loads int value at i then incements i
        else {
          parse = scanIn.next();

          if(parse.equals("//")) //all comments begin with //
            scanIn.nextLine(); //skip comments and go to next line
          else if(parse.charAt(0) == '.') //next int is a address change request
            i = Integer.parseInt(parse.substring(1));
          else
            scanIn.nextLine(); //get next line
          } //end else
        } //end while

        while(true) { //runs until CPU stops sending commands or error
          if(opScan.hasNext()) {
            String request = opScan.nextLine();

            if(!(request.isEmpty())) {
              String[] tokens = request.split(",");

              if(tokens[0].equals("0")) { //read request
                int j = Integer.parseInt(tokens[1]); //j is a temp to help clean up the println
                System.out.println(mem[j]);
              } //end nested if
              else { //write request since if not 0 it is 1
                i = Integer.parseInt(tokens[1]);
                mem[i] = Integer.parseInt(tokens[2]);
              } //end nested else
            } //end if
            else //request was an empty string
              break;
          } //end if
          else //no more commands sent from CPU
            break;
        } //end while
      } //end try
      catch(FileNotFoundException e) {
        e.printStackTrace();
      } //end catch

  } // end main
} // end Memory
