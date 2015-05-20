import java.io.IOException;
import java.io.*;


public class RunMe
{
    public static void main( String[] args ) throws IOException
    {
        PrintWriter fout = new PrintWriter( new BufferedWriter( new FileWriter( "pings.txt" ) ) );
        for ( int i = 0; i < 1000; i++ )
        {
//            System.out.println( i );
            double ping = Ping.getLatency( "google.com" );
            if ( ping == -1 )
            {
                i--;
                continue;
            }
            fout.println( ping );

        }
        fout.close();
    }
}
