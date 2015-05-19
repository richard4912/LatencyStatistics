import java.io.IOException;


public class RunMe
{
    public static void main( String[] args ) throws IOException
    {
        Ping pn = new Ping();
        int length = 50;
        int[] results = new int[length];
        for ( int i = 0; i < length; i++ )
        {
            results[i] = pn.pingHost( "54.67.27.1" );
            System.out.println(results[i]);
        }
//        for ( int i = 0; i < length; i++ )
//        {
//            System.out.println( results[i] );
//        }
    }
}
