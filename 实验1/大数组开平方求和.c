#include <mpi.h>
#include <stdio.h> 
#include <math.h>
#include <time.h>
#include <stdlib.h>

int main(int argc, char** argv)
{
	int N = 0;
	int myid, numprocs;
	int* data = malloc(1000000 * sizeof(int));
	double SqrtSum = 0.0;
	double inte;
	clock_t startTime = clock();
	MPI_Init(&argc, &argv);
	MPI_Comm_rank(MPI_COMM_WORLD, &myid);
	MPI_Comm_size(MPI_COMM_WORLD, &numprocs);
	if (myid == 0) 
	{
		printf("请输入数组大小N：");
		fflush(stdout);
		scanf("%d", &N);
		data = malloc(N*sizeof(int));
		for (int i = 0; i < N; ++i)
		{
			data[i] = i + 1;
		}
	}
	MPI_Bcast(&N, 1, MPI_INT, 0, MPI_COMM_WORLD);
	MPI_Bcast(data, N, MPI_INT, 0, MPI_COMM_WORLD);
	for (int i = myid; i < N; i = i + numprocs)
	{
		SqrtSum += sqrt(data[i]);
	}
	MPI_Reduce(&SqrtSum, &inte, 1, MPI_DOUBLE, MPI_SUM, 0, MPI_COMM_WORLD);
	if (myid == 0)
	{
		clock_t endTime = clock();
		double totalTime = (double)(endTime - startTime);
		printf("I am process 0. SqrtSum=%f.\nTotal Time: %f s\n", inte, totalTime/CLOCKS_PER_SEC);
	}
	free(data);
	MPI_Finalize();
}