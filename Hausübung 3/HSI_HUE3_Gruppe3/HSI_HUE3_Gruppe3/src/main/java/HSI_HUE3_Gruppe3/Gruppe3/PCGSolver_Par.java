package HSI_HUE3_Gruppe3.Gruppe3;

import java.util.Arrays;
import mpi.Cartcomm;
import mpi.MPI;

public class PCGSolver_Par {
	public static void main(String[] args) {
		
		//MPI Initialierung
		MPI.Init(args);
		int rank = MPI.COMM_WORLD.Rank();

		int dims[] = new int[] { 2, 1 };
		boolean periods[] = new boolean[] { true, true };
		boolean reorder = true;
		int coords[] = new int[2];// TODO

		
		//Matrizen Initialierung
		int z = 9;
		int y = 6;

		double[] f = new double[z];
		double[] ff = new double[y];

		double[][] K = new double[z][z];
		double[][] k = new double[y][y];

		double[] c = new double[y];

		double[] v0 = new double[y];

		double[] r0 = new double[y];

		double[] D0 = new double[z];
		double[] d0 = new double[y];
		double[] G0 = new double[1];
		double[] g0 = new double[1];
		int i, j;
		int maxIter = 5;

		K[0][0] = 1.0;
		K[1][1] = 2.0;
		K[1][2] = -0.5;
		K[1][4] = -1.0;
		K[2][1] = -0.5;
		K[2][2] = 1.0;
		K[2][5] = -0.5;
		K[3][3] = 1.0;
		K[4][1] = -1.0;
		K[4][4] = 4.0;
		K[4][5] = -1.0;
		K[5][2] = -0.5;
		K[5][4] = -1.0;
		K[5][5] = 2.0;
		K[6][6] = 1.0;
		K[7][7] = 1.0;
		K[8][8] = 1.0;

		f[0] = 0.0;
		f[1] = -0.25;
		f[2] = -0.125;
		f[3] = 0.0;
		f[4] = 0.5;
		f[5] = 0.75;
		f[6] = 0.0;
		f[7] = 0.5;
		f[8] = 1.0;

		
		//Communicator erstellen
		Cartcomm comm = MPI.COMM_WORLD.Create_cart(dims, periods, reorder);
		coords = comm.Coords(rank);

		
		//Matrizen einteilen
		if (rank == 0) {
			// für K 6*6
			for (i = 0; i < y / 2; i++) {
				for (j = 0; j < y; j++) {
					k[i][j] = K[i][j];
				}
			}
			for (i = y / 2; i < y; i++) {
				for (j = 0; j < y / 2; j++) {
					k[i][j] = K[i][j];
				}
			}
			for (i = y / 2; i < y; i++) {
				for (j = y / 2; j < y; j++) {
					k[i][j] = K[i][j] / 2;
				}
			}

			// für f 6
			for (i = 0; i < y / 2; i++) {
				ff[i] = f[i];
			}
			for (i = y / 2; i < y; i++) {
				ff[i] = f[i] / 2;
			}
		} else {
			// für K 6*6
			for (i = 0; i < y / 2; i++) {
				for (j = 0; j < y / 2; j++) {
					k[i][j] = K[coords[0] * 3 + i][coords[0] * 3 + j] / 2;
				}
			}

			for (i = y / 2; i < y; i++) {
				for (j = y / 2; j < y; j++) {
					k[i][j] = K[coords[0] * 3 + i][coords[0] * 3 + j];
				}
			}

			// für f 6
			for (i = 0; i < y / 2; i++) {
				ff[i] = f[i + y / 2] / 2;
			}
			for (i = y / 2; i < y; i++) {
				ff[i] = f[i + y / 2];
			}
		}

		System.out.println("k0" + rank + ": " + Arrays.deepToString(k));
		System.out.println("ff" + rank + ": " + Arrays.toString(ff));
		System.out.println("-------------------------------------");
		
		//c-1 Initialierung 
		for (i = 0; i < k.length; i++) {
			c[i] = 1 / k[i][i];
		}
		System.out.println("c" + rank + ": " + Arrays.toString(c));
		System.out.println("-------------------------------------");

		//v0, d0, r0 Initialierung
		double[] n = new double[y];
		n = MaMulVe(k, v0);
		System.out.println("kv0" + rank + ": " + Arrays.toString(n));
		
		for (i = 0; i < k.length; i++) {
			r0[i] = ff[i] - n[i];
		}
		
		d0 = DiaVeMulVe(c, r0);
		System.out.println("v0" + rank + ": " + Arrays.toString(v0));
		System.out.println("r0" + rank + ": " + Arrays.toString(r0));
		System.out.println("d0" + rank + ": " + Arrays.toString(d0));		

		//Allreduce d0
		double[] dloc = new double[z];
		if (rank == 0) {
			for (i = 0; i < y; i++) {
				dloc[i] = d0[i];
			}
		} else {
			for (i = y; i < z; i++) {
				dloc[i] = d0[i - y / 2];
			}
		}

		MPI.COMM_WORLD.Allreduce(dloc, 0, D0, 0, z, MPI.DOUBLE, MPI.SUM);

		System.out.println("D0: " + Arrays.toString(D0));
		System.out.println("-------------------------------------");

		//Gamma Initialierung und Allreduce
		g0 = VeMulVeDot(d0, r0);
		System.out.println("gamma0" + rank + ": " + g0[0]);

		MPI.COMM_WORLD.Allreduce(g0, 0, G0, 0, 1, MPI.DOUBLE, MPI.SUM);
		System.out.println("G0: " + G0[0]);
		System.out.println("-------------------------------------");

		
		//Initialierung für Iteration
		int h = 0;
		double e = 1E-7;
		double[] u = new double[y];
		double a, b;
		double[] dk = new double[y];
		dk = d0;
		double[] Dk = new double[z];
		double[] vk = new double[y];
		vk = v0;
		double[] Vk = new double[z];
		double[] rk = new double[y];
		rk = r0;
		double[] p = new double[y];
		double[] Gk = new double[1];
		Gk = G0;
		double[] gk = new double[1];
		gk = g0;
		double[] Gkk = new double[1];
		double[] gkk = new double[1];

		
		// Iteration
		for (h = 1; h < maxIter; h++) {
			System.out.println("**************Iteration: " + h);

			//rechnen u = K*dk-1
			u = MaMulVe(k, dk);
			
			System.out.println("u" + rank + ": " + Arrays.toString(u));
			System.out.println("-------------------------------------");

			//rechnen dT * u und Allreduce
			double[] x = new double[1];
			x = VeMulVeDot(dk, u);
			
			//xglobal Allreduce
			double[] xglobal = new double[1];
			MPI.COMM_WORLD.Allreduce(x, 0, xglobal, 0, 1, MPI.DOUBLE, MPI.SUM);
			
			if(rank == 0)
			System.out.println("dT*u" + rank + ": " + xglobal[0]);
			System.out.println("-------------------------------------");
			
			//rechnen a
			a = Gk[0] / xglobal[0];

			if(rank == 0)
			System.out.println("alpha" + rank + ": " + a);
			System.out.println("-------------------------------------");

			//rechnen vk = vk-1 + a*dk-1
			for (i = 0; i < dk.length; i++) {
				double[] w = new double[y];
				w[i] = a * dk[i];
				vk[i] = vk[i] + w[i];
			}

			System.out.println("vk" + rank + ": " + Arrays.toString(vk));
			System.out.println("-------------------------------------");

			//rechnen rk = rk-1 - a*u
			for (i = 0; i < rk.length; i++) {
				double[] w = new double[y];
				w[i] = a * u[i];
				rk[i] = rk[i] - w[i];
			}

			System.out.println("rk" + rank + ": " + Arrays.toString(rk));
			System.out.println("-------------------------------------");

			//rechnen p = C * rk und Allreduce
			p = DiaVeMulVe(c, rk);

			// pglobal
			double[] plocal = new double[z];
			double[] pglobal = new double[z];
			if (rank == 0) {
				for (i = 0; i < y / 2; i++) {
					plocal[i] = p[i];
				}
				for (i = y / 2; i < y; i++) {
					plocal[i] = p[i] / 2;
				}
			} else {
				for (i = y / 2; i < y; i++) {
					plocal[i] = p[i - y / 2] / 2;
				}
				for (i = y; i < z; i++) {
					plocal[i] = p[i - y / 2];
				}
			}
						
			MPI.COMM_WORLD.Allreduce(plocal, 0, pglobal, 0, z, MPI.DOUBLE, MPI.SUM);
			System.out.println("plocal" + rank + ": " + Arrays.toString(p));
			
			if(rank == 0)
			System.out.println("pglobal" + ": " + Arrays.toString(pglobal));
			System.out.println("-------------------------------------");

			//rechnen Gamma,k = rkT * p
			  //r0 Allreduce
			double[] rklocal = new double[z];
			double[] rkglobal = new double[z];
			if (rank == 0) {
				for (i = 0; i < y; i++) {
					rklocal[i] = rk[i];
				}
			} else {
				for (i = y / 2; i < z; i++) {
					rklocal[i] = rk[i - y / 2];
				}
			}

			MPI.COMM_WORLD.Allreduce(rklocal, 0, rkglobal, 0, z, MPI.DOUBLE, MPI.SUM);
			
			Gkk = VeMulVeDot(rkglobal, pglobal);
			if(rank == 0)
			System.out.println("Rkk:" + Gkk[0]);

			if (Gkk[0] < G0[0] * e) {
				if (rank == 0) {
					System.out.println("ENDE");
					System.out.println("Rkk:" + Gkk[0]);
				}
				break;
			}

			//rechnen b = Rk/Rk-1
			b = Gkk[0] / Gk[0];
			gk = gkk;
			Gk = Gkk;

			if(rank == 0)
			System.out.println("beta" + rank + ": " + b);
			System.out.println("-------------------------------------");

			//rechnen dk = p + b*dk-1 und Allreduce
			if (rank == 0) {
				double[] w = new double[z];
				for (i = 0; i < dk.length; i++) {

					w[i] = b * dk[i];
				}
				for (j = 0; j < p.length / 2; j++) {
					dk[j] = p[j] + w[j];
				}
				for (j = p.length / 2; j < p.length; j++) {
					dk[j] = (p[j] + w[j]) / 2;
				}
			} else {
				double[] w = new double[z];
				for (i = 0; i < dk.length; i++) {

					w[i] = b * dk[i];
				}
				for (j = 0; j < p.length / 2; j++) {
					dk[j] = (p[j] + w[j]) / 2;
				}
				for (j = p.length / 2; j < p.length; j++) {
					dk[j] = p[j] + w[j];
				}
			}


			double[] dklocal = new double[z];
			if (rank == 0) {

				for (i = 0; i < dk.length; i++) {
					dklocal[i] = dk[i];
				}
			} else {

				for (i = dk.length / 2; i < z; i++) {
					dklocal[i] = dk[i - dk.length / 2];
				}
			}
			MPI.COMM_WORLD.Allreduce(dklocal, 0, Dk, 0, z, MPI.DOUBLE, MPI.SUM);
			System.out.println("Dk" + rank + ": " + Arrays.toString(Dk));
			System.out.println("-------------------------------------");

			//neue Initialierung dk
			if (rank == 0) {
				for (i = 0; i < dk.length; i++) {
					dk[i] = Dk[i];
				}
			} else {
				for (i = 0; i < dk.length; i++) {
					dk[i] = Dk[i + dk.length / 2];
				}
			}
		}

		
		//vk Allreduce
		double[] vklocal = new double[z];
		if (rank == 0) {
			for (i = 0; i < vk.length / 2; i++) {
				vklocal[i] = vk[i];
			}
			for (i = vk.length / 2; i < vk.length; i++) {
				vklocal[i] = vk[i] / 2;
			}

		} else {
			for (i = z - vk.length; i < z - vk.length / 2; i++) {
				vklocal[i] = vk[i - vk.length / 2] / 2;
			}
			for (i = z - vk.length / 2; i < z; i++) {
				vklocal[i] = vk[i - vk.length / 2];
			}
		}

		MPI.COMM_WORLD.Allreduce(vklocal, 0, Vk, 0, z, MPI.DOUBLE, MPI.SUM);
		if (rank == 0) {
			System.out.println("k:" + h);
			System.out.println("vk" + rank + ": " + Arrays.toString(vk));
			System.out.println("Vk" + ": " + Arrays.toString(Vk));
			System.out.println("-------------------------------------");
		}
	}

	public static double[] MaMulVe(double[][] a, double[] b) {
		double[] c = new double[b.length];
		for (int i = 0; i < a.length; i++) {
			for (int j = 0; j < b.length; j++) {
				c[i] += a[i][j] * b[j];
			}
		}
		return c;
	}

	public static double[] DiaVeMulVe(double[] a, double[] b) {
		double[] c = new double[a.length];
		for (int i = 0; i < a.length; i++) {
			c[i] = a[i] * b[i];
		}
		return c;
	}

	public static double[] VeMulVeDot(double[] a, double[] b) {
		double[] c = new double[1];
		for (int i = 0; i < a.length; i++) {
			c[0] += a[i] * b[i];
		}
		return c;
	}
}
