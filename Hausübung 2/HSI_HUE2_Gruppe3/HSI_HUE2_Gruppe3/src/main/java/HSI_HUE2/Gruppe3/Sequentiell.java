package HSI_HUE2.Gruppe3;

public class Sequentiell 
{
    public static void main( String[] args )
    {
    	int m = 6;
    	int n = 0;
    	int i, j;
        int[][] a = new int[m][m];
        int[][] b = new int[m][m];
        int[][] c = new int[m][m];
        for(i=0; i<m; i++)
        {
        	for(j=0; j<m; j++)
        	{
        		a[i][j] = n;
        		b[i][j] = n;
        		n++;
        	}
        }
        
        long startTime, stopTime;
        
        startTime = System.currentTimeMillis();
        multiplikationMatrix(a,b,c);
        stopTime = System.currentTimeMillis();
        
        long elapsedTime = stopTime - startTime;
        System.out.println("Calculation time: " + elapsedTime + " milliseconds");		
    }
    
    
    static void multiplikationMatrix(int[][]a, int[][]b, int[][]c)
    {   	
    	int x, i, j;
    	for(i=0; i<a.length; i++)
    	{
    		for(j=0; j<b[0].length; j++)
    		{
    			for(x=0; x<b.length; x++)
    			{
    				c[i][j] += a[i][x]*b[x][j];    				
    			}
    		}
    	}
    	
    	System.out.println("Matrix A:");
		for(i = 0;i<a.length;i++)
		{
			for(j = 0;j<a[0].length;j++)
			{
				System.out.print(a[i][j]+"\t");
			}
			System.out.println();
		}        
		System.out.println("-------------------------------------");
		
		System.out.println("Matrix B:");
		for(i = 0;i<b.length;i++)
		{
			for(j = 0;j<b[0].length;j++)
			{
				System.out.print(b[i][j]+"\t");
			}
			System.out.println();
		}        
		System.out.println("-------------------------------------");
		
		System.out.println("Matrix C:");
		for(i = 0;i<c.length;i++)
		{
			for(j = 0;j<c[0].length;j++)
			{
				System.out.print(c[i][j]+"\t");
			}
			System.out.println();
		}        
		System.out.println("-------------------------------------");
    }
    	
}