#define N 100000000
#define a 10
#define b 100
#include <stdio.h>
#include <stdlib.h>
#include <time.h>
#include <mpi.h>
int main(int argc, char** argv) {
	int myid, numprocs;
	MPI_Status status;
	double local = 0.0, dx = (double)(b - a) / N; /* 小矩形宽度 */
	double inte = 0.0, x, d;
	clock_t startTime = clock();
	MPI_Init(&argc, &argv);
	MPI_Comm_rank(MPI_COMM_WORLD, &myid);
	MPI_Comm_size(MPI_COMM_WORLD, &numprocs);
	--numprocs;
	if (myid == 0)
	{
		for (int source = 1; source <= numprocs; ++source) /* 结果收集 */
		{
			MPI_Recv(&d, 1, MPI_DOUBLE, source, 99, MPI_COMM_WORLD, &status);
			inte += d;
		}
	}
	else
	{
		for (int i = myid - 1; i < N; i = i + numprocs) /* 各子节点在本地计算 */
		{
			x = a + i * dx + dx / 2;
			local += x * x * x * dx;
		}
		MPI_Send(&local, 1, MPI_DOUBLE, 0, 99, MPI_COMM_WORLD); /* 将本地计算结果传送回主节点 */
	}
	MPI_Barrier(MPI_COMM_WORLD);
	if (myid == 0)
	{
		clock_t endTime = clock();
		printf("The integal of x^3 in region [%d,%d] =%16.15f\nTotal Time: %f s\n", a, b, inte, (double)(endTime - startTime) / CLOCKS_PER_SEC);
	}
	MPI_Finalize();
}