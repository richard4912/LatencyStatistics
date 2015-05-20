import java.io.IOException;


public class RunMe
{
    public static void main( String[] args ) throws IOException
    {

        System.out.println( Ping.getLatency( "google.com" ) );
    }
}
