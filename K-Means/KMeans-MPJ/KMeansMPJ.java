/*
 * The KMeansMPJ class implements a parallel version K-Means algorithm
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

public class KMeansMPJ
{
  //Path to File containing data points
  static String dataPath;
  static int numOfDataPoints;
  
  //Path to File containing initial random centers
  static String centeroidsPath;
  static int numOfCenters;
  
  int [][] data;
  int [][] centers;
  int rank;
  int worldSize;
  
  //BroadCast from root
  int [] shareChunkSize = new int [1];
  int chunkSize;
  int iteration = 1;
  long sum;
  long start;
  
  public KMeansMPJ(String [] args){
  
    rank = MPI.COMM_WORLD.Rank();
    worldSize = MPI.COMM_WORLD.Size();

    if(!parseArgs(args)){
      //args are incorrect
      MPI.Finalize();
      System.exit(-1);
    }

    try{
      chunkSize = numOfDataPoints / worldSize;
      data = readFile(dataPath,chunkSize, chunkSize*rank*12);
      centers = readFile(centeroidsPath, numOfCenters, 0);
        
      MPI.COMM_WORLD.Barrier();
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
      numOfDataPoints = Integer.parseInt(args[1]);
      centeroidsPath = args[2];
      numOfCenters = Integer.parseInt(args[3]);
    }
    catch(Exception exp){
      System.err.println("Invalid Arguments to KMeansMPJ "+exp.getMessage());
      System.err.println("Arguments : <input-dataPoints-path> "+
                                      "<num of dataPoints> "+
                                      "<random-centers-path>"+
                                        "<num of randomCenters");
      return false;
    }
    return true;
  }
  
  public int [][] readFile(String path, int chunkSize, long pos){
    
    int [][]arr = new int[chunkSize][3];

    try{
      RandomAccessFile raf = new RandomAccessFile(path,"r");
      raf.seek(pos);

      for(int i=0 ; i<chunkSize; i++){
        for(int j=0; j<3 ;j++){
          arr[i][j] = raf.readInt();
        }
      }
    }
    catch(Exception exp){
      exp.printStackTrace();
    }
    return arr;
  }

  public static double distance(int dataChunk[], int centers[]){

    int sum = 0;    

    for(int i = 0; i < 3; i++){
      sum += Math.pow(dataChunk[i] - centers[i], 2);
    }

    return Math.sqrt(sum);
  }
  
  public void execute(){
    
    int changedCenters = 0;

    do{
      int clusterSums[] = new int[centers.length * 3];
      int clusterSize[] = new int[centers.length];
            
      int index = -1;
      double nearestDist = Double.MAX_VALUE;
      
      for(int i = 0; i < chunkSize ; i++){
        for(int j = 0; j < centers.length; j++){
        
          double dist = distance(centers[j], data[i]);
          if(j == 0){
            index = 0;
            nearestDist = dist;
          }
          else if(dist < nearestDist){
            nearestDist = dist;
            index = j;
          }
        }

        //increase the cluster size
        clusterSize[index]++;
        
        index *= 3;
        clusterSums[index++] += data[i][0];
        clusterSums[index++] += data[i][1];
        clusterSums[index] += data[i][2];
      } 

      MPI.COMM_WORLD.Allreduce(clusterSums, 0, clusterSums, 0, 
                                        clusterSums.length, MPI.INT, MPI.SUM);
            
      MPI.COMM_WORLD.Allreduce(clusterSize, 0, clusterSize, 0, 
                                        clusterSize.length, MPI.INT, MPI.SUM);

      int newClusters[][] = new int[centers.length][3];
      for(int i = 0; i < newClusters.length; i++){
        for(int j = 0; j < 3; j++){
          if(clusterSize[i] > 1){
            newClusters[i][j] = clusterSums[(i * 3)+j] / clusterSize[i];
          }
        }
      }

      changedCenters = 0;
      for(int i = 0; i < centers.length; i++)
      if(distance(newClusters[i], centers[i]) > 1){
        changedCenters++;
      }
      
      centers = newClusters;
   
     if(rank == 0){
       System.out.println("\n-------Iteration "+iteration+"--------");
       for(int [] arr : centers){
         System.out.println(Arrays.toString(arr));
       }
     }

      iteration++;
    } while(changedCenters > 0);
        
    MPI.Finalize();
  }
  public static void main(String args[]) throws Exception{
    
    KMeansMPJ kmeans = new KMeansMPJ(MPI.Init(args));
    kmeans.execute();
  }
}
