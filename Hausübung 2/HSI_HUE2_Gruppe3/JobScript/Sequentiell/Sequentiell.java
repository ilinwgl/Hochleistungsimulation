
public class Sequentiell 
{
    public static void main( String[] args )
    {
    	int m = Integer.parseInt(args[0]);
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
        
        float elapsedTime = (float) (stopTime - startTime);
		System.out.println("Problemgroesse m: " + m);
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
}
    	
}