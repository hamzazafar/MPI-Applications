/*
 * The class generates a Random Access File containing 3D points
 *
 * Author : HAMZA ZAFAR
 */

import java.io.PrintWriter;
import java.util.Random;
import java.util.Scanner;
import java.io.File;
import java.io.RandomAccessFile;

public class GenerateData{
  public static void main(String [] args) throws Exception{

    if(args.length < 2){
      System.err.println("Invalid args [num of 3D points] [output file name]");
    }

    int size = Integer.parseInt(args[0]);
    RandomAccessFile out = new RandomAccessFile(args[1],"rw");
    Random random = new Random();

    for(int i=0;i<size;i++){
      out.writeInt(random.nextInt(500));
      out.writeInt(random.nextInt(500));
      out.writeInt(random.nextInt(500));
    }
  }
}
