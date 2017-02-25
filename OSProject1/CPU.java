/******************************************************************************
* Matthew Villarreal (miv140130)
* CS 4348.002
* Project 1
*******************************************************************************
*******************************************************************************
*                                   CPU.java
*
* This program runs Memory.java and reads in commands found in Memory.java's
* memory array. It fetches the command into the ir register, then executes the
* correct intructions that correspond to that command. It also contains an
* interrupt handler that can be invoked by an instuction or the timer. This
* switches the mode from user to kernal, saves the sp and pc registers to the
* system stack, and switches the sp to the system stack.
******************************************************************************/

import java.io.*;
import java.lang.Runtime;
import java.util.Scanner;
import java.util.Random;

public class CPU {

  /*****************************************************************************
  *   GLOBAL VARIBLES FOR METHOD HEADER SIMPLICITY
  ******************************************************************************/
  private static int pc = 0, sp = 1000, ir = 0, ac = 0, x = 0, y = 0; //CPU registers
  private static boolean interrupt = false;  //interrupt handles
  private static boolean mode = true;        //true for user, false for kernal

  public static void main(String args[]) {
    int timer = 0; //stores value of timer

    if(args.length == 2) //checks for correct command line format
      timer = Integer.parseInt(args[1]);
    else {  //exit with error if command line does not get 2 args
      System.out.println("ERROR: format is CPU <inputFile> <number>");
      System.exit(0);
    } //end else

    try { //CPU execution
      Runtime rt = Runtime.getRuntime();
      Process proc = rt.exec("java Memory " + args[0]); //runs Memory.java
      OutputStream os = proc.getOutputStream();
      PrintWriter fetchPW = new PrintWriter(os);
      InputStream is = proc.getInputStream();
      Scanner memory = new Scanner(is);

      int instrNum = 0; //number of instructions done before timer

      while(true) {

        if(interrupt == false && instrNum > 0 && (instrNum % timer) == 0) { //timer interrupt occurs
          interrupt = true;
          mode = false; //changes to kernal mode
          int tempData = sp;
          sp = 2000; //sp changed to location of system stack
          push(is, os, fetchPW, tempData); //pushes sp onto stack
          tempData = pc;
          pc = 1000; //pc changed to location of system instructions in memory
          push(is, os, fetchPW, tempData); //pushes pc onto stack
        } //end if

        ir = readMem(memory, is, os, fetchPW, pc); //first read from mem

        if(ir != -1) { //valid instruction
          executeInstr(memory, is, os, fetchPW, ir);
          if(interrupt == false) //only increment time to interrupt if there is no current interupt
            instrNum++;
        } //end if
        else
          break;
      } //end while

      proc.waitFor(); //waits for Memory.java to finish executing
      int exitValue = proc.exitValue();
    } //end try
    catch(IOException e) {
      e.printStackTrace();
    } //end catch
    catch(InterruptedException e) {
      e.printStackTrace();
    } //end catch
  }// end main

  /*****************************************************************************
  * readMem(5)
  * Reads from memory array in Memory.java and returns instruction found at addr.
  * If it tries to access the system stack but in user mode, it will return an
  * error.
  ******************************************************************************/
  private static int readMem(Scanner memory, InputStream is, OutputStream os, PrintWriter fetchPW, int addr) {

    if(mode) { //checks to make sure user is not accessing system stack
      if(addr >= 1000 || addr < 0) { //user mem addr is 0-999
        System.out.println("Memory violation: accessing system address " + addr + " in user mode ");
        System.exit(0);
      } //end  nested if
    } //end if

    fetchPW.printf("0," + addr + "\n"); //sends request to Memory
    fetchPW.flush();

    String tempString = null; //holds next instruction
    if(memory.hasNext()) {
      tempString = memory.next();

      if(!tempString.isEmpty()) { //tempString has a command
        int tempInt = Integer.parseInt(tempString);
        return(tempInt);
      } //end nested if
    } //end if
    return -1;
  } //end readMem

  /*****************************************************************************
  * writeMem(5)
  * Sends a write request to put data at address addr in memory
  ******************************************************************************/
  private static void writeMem(InputStream is, OutputStream os, PrintWriter fetchPW, int addr, int data) {
    fetchPW.printf("1," + addr + "," + data + "\n");
    fetchPW.flush();
  } //end writeMem

  /*****************************************************************************
  * push(4)
  * Pushes the value of data onto the stack. Which stack it pushes on depends
  * on the if the system is user mode or kernal mode
  ******************************************************************************/
  private static void push(InputStream is, OutputStream os, PrintWriter fetchPW, int data) {
    sp--;
    writeMem(is, os, fetchPW, sp, data);
  } //end push

  /*****************************************************************************
  * pop(4)
  * Pops and returns the top value off the stack. Which stack it pops from
  * depends on if the system is in user or kernal mode.
  ******************************************************************************/
  private static int pop(Scanner memory, InputStream is, OutputStream os, PrintWriter fetchPW) {
    ir = readMem(memory, is, os, fetchPW, sp);
    writeMem(is, os, fetchPW, sp, 0); //zero to reset mem address
    sp++;
    return ir;
  } //end pop

  /*****************************************************************************
  * executeInstr(5)
  * This method takes the instruction value in the ir register, and finds the
  * correct set of code for that instuction and executes it.
  ******************************************************************************/
  private static void executeInstr(Scanner memory, InputStream is, OutputStream os, PrintWriter fetchPW, int ir) {
    int temp; //for any swapping or saving register operations
    pc++;

    switch(ir) {
      case 1: //Load value
        ac = readMem(memory, is, os, fetchPW, pc); //load into ac
        pc++;
        break;
      case 2:  //Load addr
        temp = readMem(memory, is, os, fetchPW, pc);
        ac = readMem(memory, is, os, fetchPW, temp); //load into ac
        pc++;
        break;
      case 3:  //LoadInd addr
        temp = readMem(memory, is, os, fetchPW, pc);
        temp = readMem(memory, is, os, fetchPW, temp);
        ac = readMem(memory, is, os, fetchPW, temp); //load into ac
        pc++;
        break;
      case 4:  //LoadIdxX addr
        temp = readMem(memory, is, os, fetchPW, pc);
        ac = readMem(memory, is, os, fetchPW, temp + x); //load into ac
        pc++;
        break;
      case 5:  //LoadIdxY addr
        temp = readMem(memory, is, os, fetchPW, pc);
        ac = readMem(memory, is, os, fetchPW, temp + y); //load into ac
        pc++;
        break;
      case 6:  //Loadspx
        ac = readMem(memory, is, os, fetchPW, sp + x); //load into ac
        break;
      case 7:  //Store Addr
        temp = readMem(memory, is, os, fetchPW, pc);
        writeMem(is, os, fetchPW, temp, ac); //store ac in memory at address temp
        pc++;
        break;
      case 8:  //Get
        Random rand = new Random();
        int tempInt = rand.nextInt(100) + 1;
        ac = tempInt;
        break;
      case 9:  //Put port
        int port = readMem(memory, is, os, fetchPW, pc);

        if(port == 1) //print as integer
          System.out.print(ac);
        else if(port == 2) //print as character
          System.out.print((char)ac);
        else { //incorect value
          System.out.println("ERROR: port cannot be: " + port);
          System.exit(0);
        } //end else
        pc++;
        break;
      case 10:  //AddX
        ac +=  x;
        break;
      case 11:  //AddY
        ac += y;
        break;
      case 12:  //SubX
        ac -= x;
        break;
      case 13:  //SubY
        ac -= y;
        break;
      case 14:  //CopyToX
        x = ac;
        break;
      case 15:  //CopyFromX
        ac = x;
        break;
      case 16:  //CopyToY
        y = ac;
        break;
      case 17:  //CopyFromY
        ac = y;
        break;
      case 18:  //CopyToSp
        sp = ac;
        break;
      case 19:  //CopyFromSp
        ac = sp;
        break;
      case 20:  //Jump addr
        pc = readMem(memory, is, os, fetchPW, pc);
        break;
      case 21:  //JumpIfEqual addr
        if(ac ==0) { //only jumps when equal
          pc = readMem(memory, is, os, fetchPW, pc);
          break;
        } //end if
        pc++;
        break;
      case 22:  //JumpIfNotEqual addr
        if(ac != 0) { //only jumps if not equal
          pc = readMem(memory, is, os, fetchPW, pc);
          break;
        } //end if
        pc++;
        break;
      case 23:  //Call addr
        push(is, os, fetchPW, pc + 1); //pushes next instruction address onto stack
        pc = readMem(memory, is, os, fetchPW, pc); //jumps
        break;
      case 24:  //Ret
        pc = pop(memory, is, os, fetchPW); //pc stores return address
        break;
      case 25:  //IncX
        x++;
        break;
      case 26:  //DecX
        x--;
        break;
      case 27:  //Push
        push(is, os, fetchPW, ac); //pushes ac onto stack
        break;
      case 28:  //Pop
        ac = pop(memory, is, os, fetchPW); //pops from stack to ac
        break;
      case 29:  //Int
        temp = sp;
        sp = 2000; //sp points to system stack
        interrupt = true; //interrupt has occured
        mode = false; //kernal mode
        push(is, os, fetchPW, temp); //pushes sp onto stack

        temp = pc;
        pc = 1500;
        push(is, os, fetchPW, temp); //pushes pc onto stack
        break;
      case 30:  //IRet
        pc = pop(memory, is, os, fetchPW); //gets return address
        sp = pop(memory, is, os, fetchPW); //gets return sp
        mode = true; //user mode
        interrupt = false; //interrupt has been handled
        break;
      case 50:  //End
        System.exit(0);
        break;
      default: //invalid instruction
        System.out.println("ERROR: " + ir + " Not a valid instruction");
        System.exit(0);
        break;
    } //end switch
  } //end executeInstr
} // end CPU
