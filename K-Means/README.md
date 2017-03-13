## KMeans-Sequential

1. Generating Data For  K-Means Sequential algorithm
  1. Compile the GenerateData.java present in DATA folder.
  2. The GenerateData class accepts the following arguments:
    - Args[0] : Number of 3-Dimensional points
    - Args[1] : Name of output file
    - To generate initial cluster center’s file of 10 3-Dimensional Data points execute the command given below
    - `bash-3.2$ java GenerateData 10 clusters`
2. Execution of K-Means Sequential algorithm
   1. Compile the KMeans.java present in KMeans-Sequential folder
   2. The KMeans class accepts the following arguments
      - Args[0] : Path to data point’s file
      - Args[1] : Number of data points
      - Args[2] : Path to initial cluster’s file
      - Args[3] : Number of clusters
   3. Sample command for execution
      - `java KMeans ~/lab/DATA/datafile 1024 ~/lab/DATA/clusters 10`

## MPI version of K-Means 
1. Steps for data generation are given above.
2. Compile the KMeansMPJ.java present in KMeans-MPJ folder
   - `bash-3.2$ javac -cp $MPJ_HOME/lib/mpj.jar KMeansMPJ.java`
3. KMeansMPJ class accepts the following arguments:
   - Args[0] : Path to data point’s file
   - Args[1] : Number of data points
   - Args[2] : Path to initial cluster’s file
   - Args[3] : Number of clusters
4. To execute the KMeansMPJ on “multicore mode” execute the command below
   - `mpjrun.sh -np 2 -wdir ~/lab/KMeans-MPJ/ KMeansMPJ ~/lab/DATA/datafile 1024 ~/lab/DATA/clusters 10` 
