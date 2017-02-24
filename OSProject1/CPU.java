import java.io.*;
import java.util.*;

public class CPU {


  private static int PC = 0;
  private static int SP = 1000;
  private static int IR = 0;
  private static int AC = 0;
  private static int X = 0;
  private static int Y = 0;
  private static int timer = 0;
  private static int topOfUserStack = 1000;
  private static int topOfSystemStack = 2000;
  private static int numInstructions = 0;

  private static String filename;
  private static OutputStream out;
  private static PrintWriter printW;
  private static InputStream in;

  private static boolean interrupt = false;
  private static boolean UKmode = true;        //usermode = true, kernalmode = false

  public static void main(String args[])
  {
    int instruction = 0;

    //*********************************************
    //              GET ARGUMENTS                 *
    //*********************************************

    // Validates if the correct number of arguments is 2
    if(args.length == 2)
    {
      filename = args[0];
      timer = Integer.parseInt(args[1]);
    }
    else
    {  // If number of arguments is not 2, then exit with error.
      System.out.println("ERROR: Need only two parameters.");
      System.exit(0);
    }

    //*********************************************
    //           CREATE PROCESS                   *
    //*********************************************

    try{

      // Start creating child process
      Runtime runtime = Runtime.getRuntime();
      Process process = runtime.exec("java Memory");
      out = process.getOutputStream();
      printW = new PrintWriter(out);
      in = process.getInputStream();
      Scanner memory = new Scanner(in);

      // This sends the filename to memory
      printW.printf(filename + "\n");
      printW.flush();

      while(true)
      {
        // If a timer interrupt has occured
        if(interrupt == false && numInstructions > 0 && (numInstructions % timer) == 0)
        {
          interrupt = true;
          UKmode = false;
          int tempData;

          tempData = SP;
          SP = topOfSystemStack;
          push(in, out, printW, tempData);
          tempData = PC;
          PC = 1000;
          push(in, out, printW, tempData);
        }

        // Starts reading in the instructions from memory
        instruction = readMemory(memory, in, out, printW, PC);

        // Test if instructions are correct
        //System.out.println(instruction);


        if(instruction != -1)
        {
          runIntruction(memory, in, out, printW, instruction);
          //System.out.println(numInstructions);

        }
        else
          break;

      }

      // Need it to output this, but it's not...
      //System.out.println("debug 6: Line 93");
      process.waitFor();
      int exitValue = process.exitValue();
      System.out.println("Process exited: " + exitValue);
    }
    catch(IOException e)
    {
      e.printStackTrace();
    }
    catch(InterruptedException e)
    {
      e.printStackTrace();
    }
  }// end main

  //*********************************************
  //                 METHODS                    *
  //*********************************************

  // readMemory:    Reads data from Memory at given addresses
  private static int readMemory(Scanner memory, InputStream in, OutputStream out, PrintWriter printW, int address)
  {
    String tempString = null;
    //System.out.println("address: " + address);

    // Address validation: If usermode is tryng to access the system address
    if(UKmode)
    {
      if(address >= 1000 || address < 0)
      {
        System.out.println("Error: User cannot access system stack.");
        System.exit(0);
      }
    }

    printW.printf("1," + address + "\n");
    printW.flush();

    if(memory.hasNext())
    {
      tempString = memory.next();
      if(!tempString.isEmpty())
      {
        int tempInt = Integer.parseInt(tempString);
        return(tempInt);
      }
    }
    return -1;
  }

  private static void writeMemory(InputStream in, OutputStream out, PrintWriter printW, int address, int value)
  {
    printW.printf("2," + address + "," + value + "\n");
    printW.flush();
  }
  private static int pop(Scanner memory, InputStream in, OutputStream out, PrintWriter printW)
  {
    int instruction = readMemory(memory, in, out, printW, SP);
    writeMemory(in, out, printW, SP, 0);
    SP++;
    return instruction;
  }

  private static void push(InputStream in, OutputStream out, PrintWriter printW, int data)
  {
    SP--;
    writeMemory(in, out, printW, SP, data);
  }

  private static void addInstruction()
  {
    if(interrupt == false)
      numInstructions++;
  }

  private static void runIntruction(Scanner memory, InputStream in, OutputStream out, PrintWriter printW, int instruction)
  {
    IR = instruction;
    int data;

    switch(IR)
    {
      case 1: //  Load value:      Load value into the AC
        PC++;
        data = readMemory(memory, in, out, printW, PC);
        AC = data;
        addInstruction();
        PC++;
        break;
      case 2:  //  Load addr:      Load value at the address into AC
        PC++;
        data = readMemory(memory, in, out, printW, PC);
        AC = readMemory(memory, in, out, printW, data);
        addInstruction();
        PC++;
        break;
      case 3:  //  LoadInd addr:   Load value from address found in the given address into the AC
        PC++;
        data = readMemory(memory, in, out, printW, PC);
        data = readMemory(memory, in, out, printW, data);
        AC = readMemory(memory, in, out, printW, data);
        addInstruction();
        PC++;
        break;
      case 4:  //  LoadIdxX addr:  Load the value at (address + X) into AC
        PC++;
        data = readMemory(memory, in, out, printW, PC);
        AC = readMemory(memory, in, out, printW, data + X);
        addInstruction();
        PC++;
        break;
      case 5:  //  LoadIdxY addr:  Load the value at(address + Y) into AC
        PC++;
        data = readMemory(memory, in, out, printW, PC);
        AC = readMemory(memory, in, out, printW, data + Y);
        addInstruction();
        PC++;
        break;
      case 6:  //  LoadSpX:        Load from (SP + X) into the AC
        //(if SP is 990, and X is 1, load from 991).
        AC = readMemory(memory, in, out, printW, SP + X);
        addInstruction();
        PC++;
        break;
      case 7:  //  Store Addr:     Store value into AC into the address
        PC++;
        data = readMemory(memory, in, out, printW, PC);
        writeMemory(in, out, printW, data, AC);
        addInstruction();
        PC++;
        break;
      case 8:  //  Get:            Gets a random int from 1 to 100 and put into AC
        Random intRandom = new Random();
        int tempInt = intRandom.nextInt(100) + 1;
        AC = tempInt;
        addInstruction();
        PC++;
        break;
      case 9:  //  Put port:       Put into the port depending what data is
        PC++;
        data = readMemory(memory, in, out, printW, PC);


        if(data == 1)                   // If port = 1, writes to AC as an Int
        {
          System.out.print(AC);
          addInstruction();
          PC++;
          break;
        }
        else if(data == 2)              // Else if, writes to AC as a char
        {
          System.out.print((char)AC);
          addInstruction();
          PC++;
          break;
        }
        else                            // Else, there was an error at the port
        {
          System.out.println("ERROR: Port: " + data);
          addInstruction();
          PC++;
          System.exit(0);
          break;
        }
      case 10:  //  AddX:        Add X to the AC
        AC = AC + X;
        addInstruction();
        PC++;
        break;
      case 11:  //  AddY:        Add Y to the AC
        AC = AC + Y;
        addInstruction();
        PC++;
        break;
      case 12:  //  SubX:        Subtract the value of X from the AC
        AC = AC - X;
        addInstruction();
        PC++;
        break;
      case 13:  //  SubY:        Subtract the value of Y from the AC
        AC = AC - Y;
        addInstruction();
        PC++;
        break;
      case 14:  //  CopyToX:     Copy the value in the AC to X
        X = AC;
        addInstruction();
        PC++;
        break;
      case 15:  //  CopyFromX:   Copy value in the AC to X
        AC = X;
        addInstruction();
        PC++;
        break;
      case 16:  //  CopyToY:     Copy the value in the AC to Y
        Y = AC;
        addInstruction();
        PC++;
        break;
      case 17:  //  CopyFromY:   Copy value in the Y to AC
        AC = Y;
        addInstruction();
        PC++;
        break;
      case 18:  //  CopyToSP:    Copy the value in AC to SP
        SP = AC;
        addInstruction();
        PC++;
        break;
      case 19:  //  CopyFromSP:  Copy the value in SP to AC
        AC = SP;
        addInstruction();
        PC++;
        break;
      case 20:  //  Jump addr:   Jump to the address
        PC++;
        data = readMemory(memory, in, out, printW, PC);
        PC = data;
        addInstruction();
        break;
      case 21:  //  JumpIfEqual addr:  Jump to address only if value in the AC = 0
        PC++;
        data = readMemory(memory, in, out, printW, PC);
        if(AC ==0)
        {
          PC = data;
          addInstruction();
          break;
        }
        addInstruction();
        PC++;
        break;
      case 22:  //  JumpIfNotEqual addr:  Jump to address only if value in the AC != 0
        PC++;
        data = readMemory(memory, in, out, printW, PC);
        if(AC != 0)
        {
          PC = data;
          addInstruction();
          break;
        }
        addInstruction();
        PC++;
        break;
      case 23:  //  Call addr:            Push return address onto stack, then jump to the address
        PC++;
        data = readMemory(memory, in, out, printW, PC);
        push(in, out, printW, PC + 1);
        topOfUserStack = SP;
        PC = data;
        addInstruction();
        break;
      case 24:  //  Ret:   Pop the return address from the stack, then jump to the address
        data = pop(memory, in, out, printW);
        PC = data;
        addInstruction();
        break;
      case 25:  //  IncX:   Increment X
        X++;
        addInstruction();
        PC++;
        break;
      case 26:  //  DecX:   Decrement X
        X--;
        addInstruction();
        PC++;
        break;
      case 27:  //  Push:   Push AC onto stack
        push(in, out, printW, AC);
        PC++;
        addInstruction();
        break;
      case 28:  //  Pop:    Pop from stack into AC
        AC = pop(memory, in, out, printW);
        PC++;
        addInstruction();
        break;
      case 29:  //  Int:    Perform system call
        data = SP;
        SP = 2000;
        interrupt = true;
        UKmode = false;
        push(in, out, printW, data);

        data = PC + 1;
        PC = 1500;
        push(in, out, printW, data);
        addInstruction();

        break;
      case 30:  //  IRet:   Return from system call
        PC = pop(memory, in, out, printW);
        SP = pop(memory, in, out, printW);
        UKmode = true;
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

    }
  }

} // ends class
