import java.io.*;
import java.util.Scanner;

public class Memory {
  public static void main(String args[]) {
    File inputFile = new File(args[0]);
    int timer = Integer.parseInt(args[1]);
    final int SYSTEM = 1000;
    int mem[] = new int[2000];

    try {
      Scanner scan = new Scanner(inputFile);
      Scanner fetch = new Scanner(System.in);
      String parse = null;
      int i =0, j =0;
      while(scan.hasNext()) {
        if(scan.hasNextInt()) {
          mem[i++] = scan.nextInt();
        } //end if
        else {
          parse = scan.next();
          if(parse.equals("//"))
            scan.nextLine();
          else if(parse.charAt(0) == '.')
            i = Integer.parseInt(parse.substring(1));
          else
            scan.nextLine(); //skips \n
        } //end else
          scan.nextLine(); //ignore the comments
      } //end while

      while(fetch.hasNext()) {
      //for(j=0; j<i;j++){
        j = fetch.nextInt();
        if(j == -1)
          break;
        System.out.println(mem[j]);
      }
    } //end try
    catch(FileNotFoundException e){
      e.printStackTrace();
    } //end catch
  } //end main
} //end Memory
