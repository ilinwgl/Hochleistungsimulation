
import java.util.Arrays;

import mpi.Cartcomm;
import mpi.MPI;
import mpi.ShiftParms;

public class Parallel {
	public static void main(String[] args) {

                //String appArgs[] = MPI.Init(args);
		MPI.Init(args);
		
		int rank = MPI.COMM_WORLD.Rank();
		int size = MPI.COMM_WORLD.Size();
		int p = (int) Math.sqrt(size);

		int dims[] = new int[] { p, p };
		boolean periods[] = new boolean[] { true, true };
		boolean reorder = true;
		int coords[] = new int[2];//TODO
		long startTime = 0, endTime = 0;
		
		// data
		int m = 3000;
		int n = 0;
		int i, j;
		int[][] a = new int[m][m];
		int[][] b = new int[m][m];
		int[][] c = new int[m][m];
		for (i = 0; i < m; i++) {
			for (j = 0; j < m; j++) {
				a[i][j] = n;
				b[i][j] = n;
				n++;
			}
		}
			
		MPI.COMM_WORLD.Barrier();
		if(rank == 0)
		{
			startTime = System.currentTimeMillis();
		}

		
		Cartcomm comm = MPI.COMM_WORLD.Create_cart(dims, periods, reorder);
		coords = comm.Coords(rank);

		//einteilen
		int d = m / p;

		int[][] x = new int[d][d];
		int[][] y = new int[d][d];
		int[][] z = new int[d][d];

		for (i = 0; i < d; i++) {
			for (j = 0; j < d; j++) {
				x[i][j] = a[coords[0] * d + i][coords[1] * d + j];
				y[i][j] = b[coords[0] * d + i][coords[1] * d + j];     	   
			}
		}

		/**
		 * CANNON Initialisierung
		 */
		ShiftParms parms0 = comm.Shift(1, -coords[0]); //nach links verschieben
		int src0 = parms0.rank_source;
		int dest0 = parms0.rank_dest;
		MPI.COMM_WORLD.Sendrecv_replace(x, 0, d, MPI.OBJECT, dest0, 555, src0, 555);

		ShiftParms parms1 = comm.Shift(0, -coords[1]); //nach oben verschieben
		int src1 = parms1.rank_source;
		int dest1 = parms1.rank_dest;
		MPI.COMM_WORLD.Sendrecv_replace(y, 0, d, MPI.OBJECT, dest1, 555, src1, 555);

		/**
		 * CANNON Iteration
		 */

		ShiftParms px = comm.Shift(1, -1); //1 nach links
		ShiftParms py = comm.Shift(0, -1); //1 nach oben
		int srcx = px.rank_source;
		int destx = px.rank_dest;
		int srcy = py.rank_source;
		int desty = py.rank_dest;

		for (int f = 0; f < p; f++) {
			z = multiplication(x, y, z);			
			MPI.COMM_WORLD.Sendrecv_replace(x, 0, d, MPI.OBJECT, destx, 555, srcx, 555);
			MPI.COMM_WORLD.Sendrecv_replace(y, 0, d, MPI.OBJECT, desty, 555, srcy, 555);
		}
	
		int[][] f = new int[m][m];		
		
		for (i = 0; i < d; i++) {
			for (j = 0; j < d; j++) {
				f[coords[0] * d + i][coords[1] * d + j] = z[i][j];
			}
		}
		
		int[][] k = new int[m][m];

		comm.Free();

		for (i = 0; i < m; i++) {
			MPI.COMM_WORLD.Allreduce(f[i], 0, k[i], 0, m, MPI.INT, MPI.SUM);
		}

		MPI.COMM_WORLD.Barrier();
	
		if (rank == 0) {
			endTime = System.currentTimeMillis();			
		}

		long elapsedTime = endTime - startTime;

		if (rank == 0) {
			System.out.println("-------------------------------------");
                        System.out.println("Problemgroesse m: " + m);
			System.out.println("Calculation time: " + elapsedTime + " milliseconds");
			System.out.println("-------------------------------------");
		}
		MPI.Finalize();
	}

	static int[][] multiplication(int[][] a, int[][] b, int[][] c) {
		for (int i = 0; i < a.length; i++) {
			for (int j = 0; j < a[0].length; j++) {
				for (int k = 0; k < a[0].length; k++)
					c[i][j] += a[i][k] * b[k][j];
			}
		}
		return c;
	}

}
