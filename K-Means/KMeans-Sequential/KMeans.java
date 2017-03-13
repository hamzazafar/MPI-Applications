/*
  k-means clustering is a method of vector quantization, originally from 
  signal processing, that is popular for cluster analysis in data mining. 
  k-means clustering aims to partition n observations into k clusters in 
  which each observation belongs to the cluster with the nearest mean, 
  serving as a prototype of the cluster.

  The code below performs the k-means clustering on 3 Dimensional data points
  
  Author      : Hamza Zafar
  Organization: NUST-SEECS

*/

import java.io.File;
import java.io.PrintStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.RandomAccessFile;

import java.util.Arrays;
import java.util.Scanner;

public class KMeans
{
  //Path to File containing data points
  static String dataPath;
  static int numOfDataPoints;
  
  //Path to File containing initial random centers
  static String centeroidsPath;
  static int numOfClusters;

  //array holding the 3D points  
  int [][] data;

  //array holding the cluster points
  int [][] clusters;
  
  int iteration = 1;

  //used to check the convergance
  int changedClusters;

  public KMeans(String [] args){
    
    if(!parseArgs(args)){
      //args are incorrect
      System.exit(-1);
    }

    try{
      //read the data points into an array
      data = readFile(dataPath, numOfDataPoints);

      //read the initial cluster points into an array
      clusters = readFile(centeroidsPath, numOfClusters);
    }
    catch(Exception exp){
      exp.printStackTrace();
      System.exit(-1);
    }
  }
  
  public boolean parseArgs(String [] args){
   try {
     dataPath = args[0];
     numOfDataPoints = Integer.parseInt(args[1]);
     centeroidsPath = args[2];
     numOfClusters = Integer.parseInt(args[3]);
   }
   catch(Exception exp){
     System.err.println("Invalid Arguments to KMeans "+exp.getMessage());
     System.err.println("Arguments : <input-dataPoints-path> "+
                                      "<num of dataPoints> "+
                                      "<random-clusters-path>"+
                                      "<num of randomClusters>");
     return false;
    }
    return true;
  }
  
 public int [][] readFile(String path, int size){

    int [][]arr = new int[size][3];

    try{

      RandomAccessFile raf = new RandomAccessFile(path,"r");

      for(int i=0 ; i<size; i++){
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


  /*Funtion calculates Euclidean distance between two points in space*/
  public static double distance(int point1[], int point2[]){

    int sum = 0;    

    for(int i = 0; i < 3; i++){
      sum += Math.pow(point1[i] - point2[i], 2);
    }

    return Math.sqrt(sum);
  }
 
  public void execute(){
    
    do{
      /* clusterSums array holds the sum of data points belonging to a cluster
       * clusterSize array contains the number of points assigned to a cluster
       * Essentially these arrays are used to calculate cluster Mean
       */
      int clusterSums[][] = new int[numOfClusters][3];
      int clusterSize[] = new int[numOfClusters];
            
      int index = -1;
      double nearestDist = Double.MAX_VALUE;
      
      /*Assign the data points to nearest cluster*/

      for(int i = 0; i < numOfDataPoints ; i++){
        for(int j = 0; j < numOfClusters; j++){
        
          double dist = distance(clusters[j], data[i]);
          if(j == 0){
            index = 0;
            nearestDist = dist;
          }
          else if(dist < nearestDist){
            nearestDist = dist;
            index = j;
          }
        }
        
        clusterSums[index][0] += data[i][0];
        clusterSums[index][1] += data[i][1];
        clusterSums[index][2] += data[i][2];

        //increase the cluster size
        clusterSize[index]++;
      } 

      int newClusters[][] = new int[numOfClusters][3];

      //calculate the Mean to get new cluster center
      for(int i = 0; i < numOfClusters; i++){
        if(clusterSize[i] > 1){
          for(int j = 0; j < 3; j++){
            newClusters[i][j] = clusterSums[i][j] / clusterSize[i];
          }
        }
      }

      changedClusters = 0;

      //check the convergence
      for(int i = 0; i < numOfClusters; i++)
      if(distance(newClusters[i], clusters[i]) > 1){
        changedClusters++;
      }
       
      //update the cluster points      
      clusters = newClusters;
      
      //print out the updated cluster centers
      System.out.println("------Iteration "+iteration+"------");
      for(int i=0;i<numOfClusters;i++){
          System.out.println(Arrays.toString(clusters[i]));
      }
      System.out.println();

      iteration++;
    } while(changedClusters > 0);
    
  }
  public static void main(String args[]) throws Exception
    {

        KMeans kmeans = new KMeans(args);
        kmeans.execute();
 
    }
}

