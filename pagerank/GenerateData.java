/*
 * The Generate Data class is used to generate a dataset for pagerank algorithm
 * In the output file each line begins with a source followed by its inlinks
 * The dataset does not produce any self loops
 *
 * Author: Hamza Zafar
 */

import java.util.Random;
import java.util.Scanner;

import java.io.File;
import java.io.PrintWriter;


public class GenerateData{
  public static boolean contains(int [] arr, int num ){
    for(int i=0; i<arr.length; i++){
      if(arr[i]==num){
        return true;
      }
    }
    return false;
  }
  public static void main(String [] args){

    Random random = new Random();
    int size = Integer.parseInt(args[1]);
    if(size<10){
      System.err.println("Number of links should be greater than 10");
      return;
    }
    String fileName = args[0];
    try{
      PrintWriter p = new PrintWriter(fileName);
      for(int i=0;i<size;i++){
        int numOfInLinks = 1+random.nextInt(size/10);
        numOfInLinks = (numOfInLinks == 0) ? 1 : numOfInLinks;
        p.print(i+" ");
        int [] arr = new int[numOfInLinks];
        for(int j=0;j<numOfInLinks;j++){
          int inlink = random.nextInt(size);
          while(inlink == i || GenerateData.contains(arr,inlink)){
            inlink = random.nextInt(size);
          }
          arr[j] = inlink;
        }
        StringBuilder sb = new StringBuilder();
        for(int x: arr){
          sb.append(x + " ");
        }
        p.println(sb.toString());
      }
      p.close();
    }
    catch(Exception exp){
      exp.printStackTrace();
    }
  }
}
