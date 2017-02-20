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
      int i =0, j =0;
      while(scan.hasNextInt()) {
          mem[i++] = scan.nextInt();
          scan.nextLine(); //ignore the comments
      } //end while

      while(fetch.hasNext()) {
      //for(j=0; j<i;j++){
        j = fetch.nextInt();
        System.out.println(mem[j]);
      }
    } //end try
    catch(FileNotFoundException e){
      e.printStackTrace();
    } //end catch
  } //end main
} //end Memory
