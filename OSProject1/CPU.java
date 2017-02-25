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
* type of command received. A read operation prints the temp from a specified
* memory addr back to the CPU, and a write request overwrites a specified
* memory addr with the temp received.
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

  static int instrNum = 0; //number of instructions done before timer

  static OutputStream os;
  static PrintWriter fetchPW;
  static InputStream is;

  static boolean interrupt = false;
  static boolean mode = true;        //user: true, kernal: false


  public static void main(String args[]) {
    int timer = 0;

    if(args.length == 2) //checks for correct command line format
      timer = Integer.parseInt(args[1]);
    else {  // If number of arguments is not 2, then exit with error.
      System.out.println("ERROR: format is CPU <inputFile> <number>");
      System.exit(0);
    } //end else

    try {
      Runtime rt = Runtime.getRuntime();
      Process proc = rt.exec("java Memory " + args[0]);
      os = proc.getOutputStream();
      fetchPW = new PrintWriter(os);
      is = proc.getInputStream();
      Scanner memory = new Scanner(is);

      while(true) {
        // If a timer interrupt has occured
        if(interrupt == false && instrNum > 0 && (instrNum % timer) == 0) {
          interrupt = true;
          mode = false;
          int tempData = sp;
          sp = SYSSTACK;
          push(is, os, fetchPW, tempData);
          tempData = pc;
          pc = 1000;
          push(is, os, fetchPW, tempData);
        } //end if

        // Starts reading in the instructions from memory
        ir = readMem(memory, is, os, fetchPW, pc);

        if(ir != -1)
          executeInstr(memory, is, os, fetchPW, ir);
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

  private static int readMem(Scanner memory, InputStream is, OutputStream os, PrintWriter fetchPW, int addr) {

    if(mode) { //checks to make sure user is not accessing system stack
      if(addr >= 1000 || addr < 0) { //user mem addr is 0-999
        System.out.println("Error: User cannot access system stack.");
        System.exit(0);
      } //end  nested if
    } //end if

    fetchPW.printf("0," + addr + "\n");
    fetchPW.flush();

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

  private static void writeMem(InputStream is, OutputStream os, PrintWriter fetchPW, int addr, int data) {
    fetchPW.printf("1," + addr + "," + data + "\n");
    fetchPW.flush();
  } //end writeMem

  private static int pop(Scanner memory, InputStream is, OutputStream os, PrintWriter fetchPW) {
    ir = readMem(memory, is, os, fetchPW, sp);
    writeMem(is, os, fetchPW, sp, 0); //zero to reset mem address
    sp++;
    return ir;
  } //end pop

  private static void push(InputStream is, OutputStream os, PrintWriter fetchPW, int data) {
    sp--;
    writeMem(is, os, fetchPW, sp, data);
  } //end push

  private static void instrIncrement(){
    if(interrupt == false)
      instrNum++;
  } //end instrIncrement

  private static void executeInstr(Scanner memory, InputStream is, OutputStream os, PrintWriter fetchPW, int ir) {

    int temp;
    switch(ir) {
      case 1: //  Load value:      Load value into the ac
        pc++;
        ac = readMem(memory, is, os, fetchPW, pc);
        pc++;
        break;
      case 2:  //  Load addr:      Load value at the addr into ac
        pc++;
        temp = readMem(memory, is, os, fetchPW, pc);
        ac = readMem(memory, is, os, fetchPW, temp);
        pc++;
        break;
      case 3:  //  LoadInd addr:   Load value from addr found in the given addr into the ac
        pc++;
        temp = readMem(memory, is, os, fetchPW, pc);
        temp = readMem(memory, is, os, fetchPW, temp);
        ac = readMem(memory, is, os, fetchPW, temp);
        pc++;
        break;
      case 4:  //  LoadIdxx addr:  Load the value at (addr + x) into ac
        pc++;
        temp = readMem(memory, is, os, fetchPW, pc);
        ac = readMem(memory, is, os, fetchPW, temp + x);
        pc++;
        break;
      case 5:  //  LoadIdxy addr:  Load the value at(addr + y) into ac
        pc++;
        temp = readMem(memory, is, os, fetchPW, pc);
        ac = readMem(memory, is, os, fetchPW, temp + y);
        pc++;
        break;
      case 6:  //  Loadspx:        Load from (sp + x) into the ac
        ac = readMem(memory, is, os, fetchPW, sp + x);
        pc++;
        break;
      case 7:  //  Store Addr:     Store value into ac into the addr
        pc++;
        temp = readMem(memory, is, os, fetchPW, pc);
        writeMem(is, os, fetchPW, temp, ac);
        pc++;
        break;
      case 8:  //  Get:            Gets a random int from 1 to 100 and put into ac
        Random rand = new Random();
        int tempInt = rand.nextInt(100) + 1;
        ac = tempInt;
        pc++;
        break;
      case 9:  //  Put port:       Put into the port depending what temp is
        pc++;
        int port = readMem(memory, is, os, fetchPW, pc);

        if(port == 1) {
          System.out.print(ac);
          pc++;
          break;
        } //end if
        else if(port == 2) {
          System.out.print((char)ac);
          pc++;
          break;
        } //end else if
        else {
          System.out.println("ERROR: port cannot be: " + port);
          instrIncrement();
          pc++;
          System.exit(0);
          break;
        } //end else
      case 10:  //  Addx:        Add x to the ac
        ac +=  x;
        pc++;
        break;
      case 11:  //  Addy:        Add y to the ac
        ac += y;
        pc++;
        break;
      case 12:  //  Subx:        Subtract the value of x from the ac
        ac -= x;
        pc++;
        break;
      case 13:  //  Suby:        Subtract the value of y from the ac
        ac -= y;
        pc++;
        break;
      case 14:  //  CopyTox:     Copy the value in the ac to x
        x = ac;
        pc++;
        break;
      case 15:  //  CopyFromx:   Copy value in the ac to x
        ac = x;
        pc++;
        break;
      case 16:  //  CopyToy:     Copy the value in the ac to y
        y = ac;
        pc++;
        break;
      case 17:  //  CopyFromy:   Copy value in the y to ac
        ac = y;
        pc++;
        break;
      case 18:  //  CopyTosp:    Copy the value in ac to sp
        sp = ac;
        pc++;
        break;
      case 19:  //  CopyFromsp:  Copy the value in sp to ac
        ac = sp;
        pc++;
        break;
      case 20:  //  Jump addr:   Jump to the addr
        pc++;
        pc = readMem(memory, is, os, fetchPW, pc);
        break;
      case 21:  //  JumpIfEqual addr:  Jump to addr only if value in the ac = 0
        pc++;
        if(ac ==0) {
          pc = readMem(memory, is, os, fetchPW, pc);
          break;
        } //end if
        pc++;
        break;
      case 22:  //  JumpIfNotEqual addr:  Jump to addr only if value in the ac != 0
        pc++;
        if(ac != 0) {
          pc = readMem(memory, is, os, fetchPW, pc);
          break;
        } //end if
        pc++;
        break;
      case 23:  //  Call addr:            Push return addr onto stack, then jump to the addr
        pc++;
        push(is, os, fetchPW, pc + 1);
        USTACK = sp;
        pc = readMem(memory, is, os, fetchPW, pc);
        break;
      case 24:  //  Ret:   Pop the return addr from the stack, then jump to the addr
        pc = pop(memory, is, os, fetchPW);
        break;
      case 25:  //  Incx:   Increment x
        x++;
        pc++;
        break;
      case 26:  //  Decx:   Decrement x
        x--;
        pc++;
        break;
      case 27:  //  Push:   Push ac onto stack
        push(is, os, fetchPW, ac);
        pc++;
        break;
      case 28:  //  Pop:    Pop from stack into ac
        ac = pop(memory, is, os, fetchPW);
        pc++;
        break;
      case 29:  //  Int:    Perform system call
        temp = sp;
        sp = 2000;
        interrupt = true;
        mode = false;
        push(is, os, fetchPW, temp);

        temp = pc + 1;
        pc = 1500;
        push(is, os, fetchPW, temp);
        break;
      case 30:  //  iret:   Return from system call
        pc = pop(memory, is, os, fetchPW);
        sp = pop(memory, is, os, fetchPW);
        mode = true;
        interrupt = false;
        break;
      case 50:  //  End:    End the execution
        instrIncrement();
        System.exit(0);
        break;
      default:
        System.out.println("ERROR: " + ir + " Not a valid instruction");
        System.exit(0);
        break;
    } //end switch
    instrIncrement();
  } //end executeInstr
} // end CPU
