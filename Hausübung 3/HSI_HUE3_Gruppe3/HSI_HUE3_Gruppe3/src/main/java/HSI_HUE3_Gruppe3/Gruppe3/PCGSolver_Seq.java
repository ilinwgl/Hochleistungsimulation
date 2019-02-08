package HSI_HUE3_Gruppe3.Gruppe3;

import java.text.DecimalFormat;
import java.util.Arrays;

public class PCGSolver_Seq {

	public static void main(String[] args) {
		// Initial Matrizen
		int z = 9;
		double[][] K = new double[z][z];
		double[] f = new double[z];
		double[] v0 = new double[z];
		double[] r0 = new double[z];
		double[] d0 = new double[z];
		double[] C = new double[z];
		double R0 = 0;
		int i, j;
		int maxIter = 100;

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

		
		// r0 Initialierung
		double[] m = new double[z];
		m = MaMulVe(K, v0);
		
		for (i = 0; i < K.length; i++) {
			r0[i] = f[i] - m[i];
		}

		// Initialierung C-1
		for (i = 0; i < K.length; i++) {
			C[i] = 1 / K[i][i];
		}

		// d0 Initialierung
		d0 = DiaVeMulVe(C, r0);

		// R0 Initialierung
		R0 = VeMulVeDot(d0, r0);

		System.out.println("K: " + Arrays.deepToString(K));
		System.out.println("f: " + Arrays.toString(f));
		System.out.println("v0: " + Arrays.toString(v0));
		System.out.println("-------------------------------------");
		System.out.println("Kv0: " + Arrays.toString(m));
		System.out.println("r0: " + Arrays.toString(r0));
		System.out.println("C: " + Arrays.toString(C));
		System.out.println("d0: " + Arrays.toString(d0));
		System.out.println("-------------------------------------");
		System.out.println("gamma0: " + R0);
		System.out.println("-------------------------------------");

		
		// Initialierung für Iteration
		double e = 1E-7;
		int k = 0;
		double[] u = new double[z];
		double a, b;
		double[] dk = new double[z];
		dk = d0;
		double[] vk = new double[z];
		vk = v0;
		double[] rk = new double[z];
		rk = r0;
		double[] p = new double[z];
		double Rk = R0;
		double Rkk = 0;

		
		// Iteration
		for (k = 1; k < maxIter; k++) {

			System.out.println("**************Iteration: " + k);

			// u = K*dk-1
			u = MaMulVe(K, dk);

			System.out.println("u: " + Arrays.toString(u));
			System.out.println("-------------------------------------");

			// a = R0/(dT * u)
			double x = 0;
			x = VeMulVeDot(dk, u);
			// xglobal

			a = Rk / x;

			System.out.println("dT*u: " + x);
			System.out.println("-------------------------------------");
			System.out.println("alpha: "+ a);
			System.out.println("-------------------------------------");

			// vk = vk-1 + a*dk-1
			for (i = 0; i < dk.length; i++) {
				double[] n = new double[z];
				n[i] = a * dk[i];
				vk[i] = vk[i] + n[i];
			}

			System.out.println("vk: " + Arrays.toString(vk));
			System.out.println("-------------------------------------");

			// rk = rk-1 - a*u
			for (i = 0; i < rk.length; i++) {
				double[] n = new double[z];
				n[i] = a * u[i];
				rk[i] = rk[i] - n[i];
			}

			System.out.println("rk: " + Arrays.toString(rk));
			System.out.println("-------------------------------------");

			// p = C * rk
			p = DiaVeMulVe(C, rk);
			// pglobal

			System.out.println("p: " + Arrays.toString(p));
			System.out.println("-------------------------------------");

			// Rk = rkT * p
			Rkk = VeMulVeDot(rk, p);
			System.out.println("Rkk: " + Rkk);
			if (Rkk < R0 * e) {
				System.out.println("ENDE"); 				
				System.out.println("Rkk: " + Rkk);
				break;
			}

			System.out.println("Rk: " + Rk);

			// b = Rk/Rk-1
			b = Rkk / Rk;
			Rk = Rkk;

			System.out.println("beta: " + b);
			System.out.println("-------------------------------------");

			// dk = p + b*dk-1
			for (i = 0; i < dk.length; i++) {
				double[] n = new double[z];
				n[i] = b * dk[i];
				for (j = 0; j < p.length; j++) {
					dk[i] = p[i] + n[i];
				}
			}

			System.out.println("dk: " + Arrays.toString(dk));
			System.out.println("-------------------------------------");

		}

		System.out.println("k: " + k);
		System.out.println("vk: " + Arrays.toString(vk));
		System.out.println("-------------------------------------");
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

	public static double VeMulVeDot(double[] a, double[] b) {
		double c = 0;
		for (int i = 0; i < a.length; i++) {
			c += a[i] * b[i];
		}
		return c;
	}
}
