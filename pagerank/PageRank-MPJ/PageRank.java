/*
 * The PageRank class implements a parallel version of PageRank algorithm
 *
 * Author: HAMZA ZAFAR
 */

import java.io.File;
import java.io.PrintStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.RandomAccessFile;

import java.util.Arrays;
import java.util.Scanner;

import mpi.*;

public class PageRank
{
  //Path to File containing data points
  static String dataPath;
  
  int rank;
  int worldSize;
  int chunkSize;
  int iterations = 0;
  double d = 0.85; 

  static int numOfPages;
  int [][] inlinks;
  int [][] data;
  double [] oldPageRanks;
  double [] newPageRanks;
  int [] numOfOutLinks;
  double danglingSum =0; 
  static int totalElements =0;

  public PageRank(String [] args){
  
    rank = MPI.COMM_WORLD.Rank();
    worldSize = MPI.COMM_WORLD.Size();

    if(!parseArgs(args)){
      //args are incorrect
      MPI.Finalize();
      System.exit(-1);
    }

    try{
      oldPageRanks = new double [numOfPages];
      newPageRanks = new double [numOfPages];
      numOfOutLinks = new int [numOfPages];
 
      if(rank == 0){
        data = readFile(dataPath, numOfPages);
      }

      //initialize pageranks
      for(int i=0;i<numOfPages;i++){
        oldPageRanks[i] = 1d / numOfPages;
      }

      //broadcast oldPageRanks
      MPI.COMM_WORLD.Bcast(oldPageRanks,0,numOfPages,MPI.DOUBLE,0);

      //broadcast outlinks
      MPI.COMM_WORLD.Bcast(numOfOutLinks,0,numOfPages,MPI.INT,0);
  

      //the number of page ranks each process will calculate
      chunkSize = numOfPages / worldSize;
      inlinks = new int [chunkSize][];
 
      if(rank == 0){

        for(int i=0;i<chunkSize;i++){
          inlinks[i] = new int[data[i].length];
          System.arraycopy(data[i],0,inlinks[i],0,data[i].length);
        }

        for(int i=1;i<worldSize;i++){
     
          int [][] sendArr = new int[chunkSize][];

          for(int j=0;j<chunkSize;j++){
            sendArr[j]=data[j + (chunkSize * i)];
          }

          Object [] sendObjArr = new Object[1]; 
          sendObjArr[0] = (Object) sendArr;
     
          MPI.COMM_WORLD.Send(sendObjArr,0,1,MPI.OBJECT,i,0);
        }

      }
      else{
        Object [] recvObjArr = new Object[1];

        MPI.COMM_WORLD.Recv(recvObjArr,0,1,MPI.OBJECT,0,0);
        inlinks = (int [][]) recvObjArr[0];
       
      }
      
      while (iterations > 0){
       
        //initialize new page ranks to 0
        for(int i=0;i<numOfPages;i++){
          newPageRanks[i] = 0;
        }
 
        danglingSum = 0;
        for(int i=0; i<chunkSize;i++){
          if(numOfOutLinks[i+(rank*chunkSize)] == 0){
            danglingSum = danglingSum + (d * oldPageRanks[i+(rank*chunkSize)]
                                                                 /numOfPages);
          }
        }
        
        //AllReduce the dangling sum
        double [] reduceArr = new double[1];
        reduceArr[0] = danglingSum;

        MPI.COMM_WORLD.Allreduce(reduceArr,0,reduceArr,0,1,MPI.DOUBLE,MPI.SUM);
 
        danglingSum = reduceArr[0];
       
        for(int i=0;i<chunkSize;i++){
          newPageRanks[i+(rank*chunkSize)] = danglingSum + (1-d)/numOfPages;
          for(int j=1;j<inlinks[i].length;j++){
            int inlink = inlinks[i][j];
            newPageRanks[i+(rank*chunkSize)] += d * oldPageRanks[inlink] /
                                                    numOfOutLinks[inlink];
          }
        }
      
        //reduce new page ranks
        MPI.COMM_WORLD.Allreduce(newPageRanks,0,newPageRanks,0,numOfPages,
                                                      MPI.DOUBLE,MPI.SUM);

        System.arraycopy(newPageRanks,0,oldPageRanks,0,numOfPages);
        iterations--;
      }

      if(rank == 0){
        double sum = 0.0;
        for(int i=0; i<numOfPages; i++){
          System.out.println(oldPageRanks[i]);
          sum += oldPageRanks[i];
        }
        System.out.println("---------------");
        System.out.println(sum);
      }        
    }
    catch(Exception exp){
      MPI.Finalize();
      exp.printStackTrace();
      System.exit(-1);
    }
  }
  
  public boolean parseArgs(String [] args){
    try {
      dataPath = args[0];
      numOfPages = Integer.parseInt(args[1]);
      iterations = Integer.parseInt(args[2]);
    }
    catch(Exception exp){
      System.err.println("Invalid Arguments to PageRank "+exp.getMessage());
      System.err.println("Arguments : <path-to-dataset> " + "<num-of-pages> "+
                                                        "<num-of-iterations");
      return false;
    }
    return true;
  }
  
  public int [][] readFile(String path, int numOfPages){
    
    int [][]arr = new int[numOfPages][];
    try{
      Scanner sc = new Scanner(new File(path));
      for(int i=0; i<numOfPages; i++){
        String [] links = sc.nextLine().split(" ");
       
        arr[i] = new int [links.length];
        for(int j=0;j< arr[i].length; j++){
          arr[i][j] = Integer.parseInt(links[j]);
          if(j!=0){
            numOfOutLinks[Integer.parseInt(links[j])]++ ;
          }
        }     
      }
    }
    catch(Exception exp){
      exp.printStackTrace();
    }
    return arr;
  }

  public void execute(){
    
        
    MPI.Finalize();
  }
  public static void main(String args[]) throws Exception{
    
    PageRank pg = new PageRank(MPI.Init(args));
    pg.execute();
  }
}
