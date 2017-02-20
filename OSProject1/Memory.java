import java.io.*;
import java.util.Scanner;

public class Memory {
  public static void main(String args[]) {
    File inputFile = new File(args[0]);
    int fetch = Integer.parseInt(args[1]);
    final int SYSTEM = 1000;
    int mem[] = new int[2000];

    try {
      Scanner scan = new Scanner(inputFile);
      int i =0;
      while(scan.hasNextInt()) {
        mem[i++] = scan.nextInt();
        scan.nextLine(); //ignore the comments
      }
      for(int j = 0; j < i; j++) {
        System.out.println(mem[j]);
      }
    } //end try
    catch(FileNotFoundException e){
      e.printStackTrace();
    } //end catch
  } //end main
} //end Memory
