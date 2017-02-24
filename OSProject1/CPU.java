/******************************************************************************
* Matthew Villarreal (miv140130)
* CS 4348.002
* Project 1
*******************************************************************************
*******************************************************************************
*                                   CPU.java
*
* This program initialized the memory based on the file input, parses commands
* received from CPU.java, then executes a read or write operation based on the
* type of command received. A read operation prints the data from a specified
* memory address back to the CPU, and a write request overwrites a specified
* memory address with the data received.
******************************************************************************/

import java.io.*;
import java.lang.Runtime;
import java.util.Scanner;
import java.util.Random;

public class CPU {

  //global constants
  static int USTACK = 1000;
  static int SYSSTACK = 2000;

  static int pc = 0, sp = 1000, ir = 0, ac = 0, x = 0, y = 0; //CPU registers

  static int numInstructions = 0;

  static OutputStream os;
  static PrintWriter pw;
  static InputStream is;

  static boolean interrupt = false;
  static boolean mode = true;        //usermode = true, kernalmode = false


  public static void main(String args[]) {
    int timer = 0;
    // Validates if the correct number of arguments is 2
    if(args.length == 2)
      timer = Integer.parseInt(args[1]);
    else {  // If number of arguments is not 2, then exit with error.
      System.out.println("ERROR: format is CPU <inputFile> <number>");
      System.exit(0);
    } //end else

    try {
      Runtime rt = Runtime.getRuntime();
      Process proc = rt.exec("java Memory " + args[0]);
      os = proc.getOutputStream();
      pw = new PrintWriter(os);
      is = proc.getInputStream();
      Scanner memory = new Scanner(is);

      while(true) {
        // If a timer interrupt has occured
        if(interrupt == false && numInstructions > 0 && (numInstructions % timer) == 0) {
          interrupt = true;
          mode = false;
          int tempData = sp;
          sp = SYSSTACK;
          push(is, os, pw, tempData);
          tempData = pc;
          pc = 1000;
          push(is, os, pw, tempData);
        } //end if

        // Starts reading in the instructions from memory
        ir = readMem(memory, is, os, pw, pc);

        if(ir != -1)
          runIntruction(memory, is, os, pw, ir);
        else
          break;
      } //end while

      // Need it to output this, but it's not...
      //System.out.println("debug 6: Line 93");
      proc.waitFor();
      int exitValue = proc.exitValue();
      System.out.println("Process exited: " + exitValue);
    } //end try
    catch(IOException e) {
      e.printStackTrace();
    } //end catch
    catch(InterruptedException e) {
      e.printStackTrace();
    } //end catch
  }// end main

  private static int readMem(Scanner memory, InputStream is, OutputStream os, PrintWriter pw, int address) {

    if(mode) { //checks to make sure user is not accessing system stack
      if(address >= 1000 || address < 0) { //user mem address is 0-999
        System.out.println("Error: User cannot access system stack.");
        System.exit(0);
      } //end  nested if
    } //end if

    pw.printf("0," + address + "\n");
    pw.flush();

    String tempString = null;
    if(memory.hasNext()) {
      tempString = memory.next();

      if(!tempString.isEmpty()) {
        int tempInt = Integer.parseInt(tempString);
        return(tempInt);
      } //end nested if
    } //end if
    return -1;
  } //end readMem

  private static void writeMem(InputStream is, OutputStream os, PrintWriter pw, int address, int value) {
    pw.printf("1," + address + "," + value + "\n");
    pw.flush();
  } //end writeMem

  private static int pop(Scanner memory, InputStream is, OutputStream os, PrintWriter pw) {
    ir = readMem(memory, is, os, pw, sp);
    writeMem(is, os, pw, sp, 0);
    sp++;
    return ir;
  } //end pop

  private static void push(InputStream is, OutputStream os, PrintWriter pw, int data) {
    sp--;
    writeMem(is, os, pw, sp, data);
  } //end push

  private static void addInstruction(){
    if(interrupt == false)
      numInstructions++;
  } //end addInstruction

  private static void runIntruction(Scanner memory, InputStream is, OutputStream os, PrintWriter pw, int ir) {
    int data;

    switch(ir) {
      case 1: //  Load value:      Load value into the ac
        pc++;
        data = readMem(memory, is, os, pw, pc);
        ac = data;
        addInstruction();
        pc++;
        break;
      case 2:  //  Load addr:      Load value at the address into ac
        pc++;
        data = readMem(memory, is, os, pw, pc);
        ac = readMem(memory, is, os, pw, data);
        addInstruction();
        pc++;
        break;
      case 3:  //  LoadInd addr:   Load value from address found in the given address into the ac
        pc++;
        data = readMem(memory, is, os, pw, pc);
        data = readMem(memory, is, os, pw, data);
        ac = readMem(memory, is, os, pw, data);
        addInstruction();
        pc++;
        break;
      case 4:  //  LoadIdxx addr:  Load the value at (address + x) into ac
        pc++;
        data = readMem(memory, is, os, pw, pc);
        ac = readMem(memory, is, os, pw, data + x);
        addInstruction();
        pc++;
        break;
      case 5:  //  LoadIdxy addr:  Load the value at(address + y) into ac
        pc++;
        data = readMem(memory, is, os, pw, pc);
        ac = readMem(memory, is, os, pw, data + y);
        addInstruction();
        pc++;
        break;
      case 6:  //  Loadspx:        Load from (sp + x) into the ac
        ac = readMem(memory, is, os, pw, sp + x);
        addInstruction();
        pc++;
        break;
      case 7:  //  Store Addr:     Store value into ac into the address
        pc++;
        data = readMem(memory, is, os, pw, pc);
        writeMem(is, os, pw, data, ac);
        addInstruction();
        pc++;
        break;
      case 8:  //  Get:            Gets a random int from 1 to 100 and put into ac
        Random intRandom = new Random();
        int tempInt = intRandom.nextInt(100) + 1;
        ac = tempInt;
        addInstruction();
        pc++;
        break;
      case 9:  //  Put port:       Put into the port depending what data is
        pc++;
        data = readMem(memory, is, os, pw, pc);

        if(data == 1) {
          System.out.print(ac);
          addInstruction();
          pc++;
          break;
        } //end if
        else if(data == 2) {
          System.out.print((char)ac);
          addInstruction();
          pc++;
          break;
        } //end else if
        else {
          System.out.println("ERROR: port cannot be: " + data);
          addInstruction();
          pc++;
          System.exit(0);
          break;
        } //end else
      case 10:  //  Addx:        Add x to the ac
        ac = ac + x;
        addInstruction();
        pc++;
        break;
      case 11:  //  Addy:        Add y to the ac
        ac = ac + y;
        addInstruction();
        pc++;
        break;
      case 12:  //  Subx:        Subtract the value of x from the ac
        ac = ac - x;
        addInstruction();
        pc++;
        break;
      case 13:  //  Suby:        Subtract the value of y from the ac
        ac = ac - y;
        addInstruction();
        pc++;
        break;
      case 14:  //  CopyTox:     Copy the value in the ac to x
        x = ac;
        addInstruction();
        pc++;
        break;
      case 15:  //  CopyFromx:   Copy value in the ac to x
        ac = x;
        addInstruction();
        pc++;
        break;
      case 16:  //  CopyToy:     Copy the value in the ac to y
        y = ac;
        addInstruction();
        pc++;
        break;
      case 17:  //  CopyFromy:   Copy value in the y to ac
        ac = y;
        addInstruction();
        pc++;
        break;
      case 18:  //  CopyTosp:    Copy the value in ac to sp
        sp = ac;
        addInstruction();
        pc++;
        break;
      case 19:  //  CopyFromsp:  Copy the value in sp to ac
        ac = sp;
        addInstruction();
        pc++;
        break;
      case 20:  //  Jump addr:   Jump to the address
        pc++;
        data = readMem(memory, is, os, pw, pc);
        pc = data;
        addInstruction();
        break;
      case 21:  //  JumpIfEqual addr:  Jump to address only if value in the ac = 0
        pc++;
        data = readMem(memory, is, os, pw, pc);
        if(ac ==0) {
          pc = data;
          addInstruction();
          break;
        } //end if
        addInstruction();
        pc++;
        break;
      case 22:  //  JumpIfNotEqual addr:  Jump to address only if value in the ac != 0
        pc++;
        data = readMem(memory, is, os, pw, pc);
        if(ac != 0) {
          pc = data;
          addInstruction();
          break;
        } //end if
        addInstruction();
        pc++;
        break;
      case 23:  //  Call addr:            Push return address onto stack, then jump to the address
        pc++;
        data = readMem(memory, is, os, pw, pc);
        push(is, os, pw, pc + 1);
        USTACK = sp;
        pc = data;
        addInstruction();
        break;
      case 24:  //  Ret:   Pop the return address from the stack, then jump to the address
        data = pop(memory, is, os, pw);
        pc = data;
        addInstruction();
        break;
      case 25:  //  Incx:   Increment x
        x++;
        addInstruction();
        pc++;
        break;
      case 26:  //  Decx:   Decrement x
        x--;
        addInstruction();
        pc++;
        break;
      case 27:  //  Push:   Push ac onto stack
        push(is, os, pw, ac);
        pc++;
        addInstruction();
        break;
      case 28:  //  Pop:    Pop from stack into ac
        ac = pop(memory, is, os, pw);
        pc++;
        addInstruction();
        break;
      case 29:  //  Int:    Perform system call
        data = sp;
        sp = 2000;
        interrupt = true;
        mode = false;
        push(is, os, pw, data);

        data = pc + 1;
        pc = 1500;
        push(is, os, pw, data);
        addInstruction();
        break;
      case 30:  //  iret:   Return from system call
        pc = pop(memory, is, os, pw);
        sp = pop(memory, is, os, pw);
        mode = true;
        numInstructions++;
        interrupt = false;
        break;
      case 50:  //  End:    End the execution
        addInstruction();
        System.exit(0);
        break;
      default:
        System.out.println("ERROR: Invalid Instruction...");
        System.exit(0);
        break;
    } //end switch
  } //end runIntruction
} // end CPU
