
/* 
 * Hochleistungssimulationen im Ingenieurwesen (HSI)
 * WiSe 2018/19
 * Hausuebung 1
 * Matrix-Vektor-Multiplikation Sequentiell
 * 
 */

import java.util.Arrays;

public class MatrixVectorMultiplication {

	static int matrixSize = 3; // Assumption: square matrix
	int m, n;
	float[] A; // Matrix as 1D-array in row-major order
	float[] b, c;
	long startTime, stopTime;

	public static void main(String args[]) {

		int rows = matrixSize; // m
		int columns = matrixSize; // n

		MatrixVectorMultiplication op = new MatrixVectorMultiplication(rows, columns);
		op.initData();
		op.calc();
		op.printResults();

	}

	public MatrixVectorMultiplication(int m, int n) {
		this.m = m;
		this.n = n;
		this.A = new float[m * n];
		this.b = new float[n];
		this.c = new float[m];
		this.startTime = 0;
		this.stopTime = 0;
	}

	private void initData() {

		// Loop through rows:
		for (int i = 0; i < m; i++) {
			// Loop through columns:
			for (int j = 0; j < n; j++) {
				b[j] = j;
				A[n * i + j] = n * i + j;
			}
		}
	}

	private void calc() {

		startTime = System.currentTimeMillis();

		// Sequential algorithm to multiply matrix A and vector b (result vector c)
		for (int i = 0; i < m; i++) {
			c[i] = 0;
			for (int j = 0; j < n; j++) {
				c[i] += A[n * i + j] * b[j];
			}
		}

		stopTime = System.currentTimeMillis();
	}

	private void printResults() {

		System.out.println("Matrix size: " + m + "x" + n);

		if (matrixSize <= 10) {
			System.out.println("Matrix A:\n" + Arrays.toString(A));
			System.out.println("Vector b:\n" + Arrays.toString(b));
			System.out.println("Vector c:\n" + Arrays.toString(c));
		}

		float elapsedTime = (float) (stopTime - startTime);
		System.out.println("Calculation time: " + elapsedTime + " milliseconds");
	}
}
