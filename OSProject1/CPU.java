import java.io.*;
import java.util.Scanner;
import java.util.Stack;
import java.lang.Runtime;
import java.util.Random;

public class CPU {
  public static void main(String args[]) {
		try {
	  	Runtime rt = Runtime.getRuntime();
			String run = "java Memory " + args[0] + " " + args[1];
	  	Process proc = rt.exec(run);

	  	InputStream is = proc.getInputStream();
	  	OutputStream os = proc.getOutputStream();
			Scanner sc = new Scanner(is);
      PrintWriter pw = new PrintWriter(os);

      int ir = -1; //init
      int pc = 0, sp = 0, ac = -1, x = 0, y = 0;
      Stack<Integer> user = new Stack<>(); //user stack
      Stack<Integer> sys = new Stack<>(); //system stack

      pw.println(pc); //initial fetch
      pw.flush();
      pc++;

			loop: while(sc.hasNextLine()) {
				ir = Integer.parseInt(sc.nextLine());
        //System.out.println(ir);
        //System.out.println("pc = " + pc);
				switch(ir) {
					case 1: //Load value
            pw.println(pc);
            pw.flush();
            ac = Integer.parseInt(sc.nextLine());
            pc++;
						break;
					case 2: //Load addr
            pw.println(pc);
            pw.flush();
            ac = Integer.parseInt(sc.nextLine());
            pw.println(ac); //gets the value at the address
            pw.flush();
            ac = Integer.parseInt(sc.nextLine());
            pc++;
						break;
					case 3: //LoadInd addr
            pw.println(pc);
            pw.flush();
            ac= Integer.parseInt(sc.nextLine());
            pw.println(ac); //gets the value at the address
            pw.flush();
            ac = Integer.parseInt(sc.nextLine());
            pc++;
						break;
					case 4: //LoadIdx X addr
            pw.println(pc);
            pw.flush();
            ac= Integer.parseInt(sc.nextLine());
            pw.println(ac + x); //gets the value at the address
            pw.flush();
            ac = Integer.parseInt(sc.nextLine());
            pc++;
						break;
					case 5: //LoadIdx Y addr
						break;
					case 6: //LoadSp X
						break;
					case 7: //Store addr
						break;
					case 8: //Get
            Random r = new Random();
            ac = r.nextInt(100) + 1;
						break;
          case 9: //Put port
            pw.println(pc);
            pw.flush();
            int port = Integer.parseInt(sc.nextLine());
            if(port == 1)
              System.out.println(ac);
            else if(port == 2)
              System.out.println((char) ac);
            pc++;
            break;
          case 10: //AddX
            ac += x;
            break;
          case 11: //AddY
            ac += y;
            break;
          case 12: //SubX
            ac -= x;
            break;
          case 13: //SubY
            ac -= y;
            break;
          case 14: //CopyToX
            x = ac;
            break;
          case 15: //CopyFromX
            ac = x;
            break;
          case 16: //CopytoY
            y = ac;
            break;
          case 17: //CopyFromY
            ac = y;
            break;
          case 18: //CopyToSp
            sp = ac;
            break;
          case 19: //CopyFromSp
            sp = ac;
            break;
          case 20: //Jump addr
            pw.println(pc);
            pw.flush();
            pc = Integer.parseInt(sc.nextLine()); //jumps
            break;
          case 21: //JumpIfEqual addr
            if(ac == 0){
              pw.println(pc);
              pw.flush();
              pc = Integer.parseInt(sc.nextLine()); //jumps
            }
            else
              pc++;
            break;
          case 22: //JumpIfNotEqual addr
            if(ac != 0){
              pw.println(pc);
              pw.flush();
              pc= Integer.parseInt(sc.nextLine()); //jumps
            }
              else
                pc++;
            break;
          case 23: //Call addr
            break;
          case 24: //Ret
            break;
          case 25: //IncX
            x++;
            break;
          case 26: //DecX
            x--;
            break;
          case 27: //Push
            break;
          case 28: //Pop
            break;
          case 29: //Int
            break;
          case 30: //IRet
            break;
          case 50: //End
            break loop;
          default:
            System.out.println("ERROR! Cannot compute");
            System.exit(0);//break loop;
				} //end switch-case
        pw.println(pc);
        pw.flush();
        pc++;
			} //end loop

			proc.waitFor();
			int exitVal = proc.exitValue();
			System.out.println("Process exited: " + exitVal);
		} //end try
		catch(Throwable t) {
			t.printStackTrace();
		} //end catch
	} //end main
} //end CPU
