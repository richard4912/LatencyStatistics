import java.io.IOException;


public class Ping
{

    public int pingHost( String host ) throws IOException
    {
        Process p1 = Runtime.getRuntime().exec( "ping -n 1 " + host );
        int pingValue = 0;

        long t1 = System.currentTimeMillis();
        try
        {
            p1.waitFor();
        }
        catch ( InterruptedException e )
        {
            e.printStackTrace();
        }
        long t2 = System.currentTimeMillis();

        int exit = p1.exitValue();
        if ( exit == 0 )
        {
            pingValue = (int) (t2-t1-getSysLatency());
            return pingValue;
        }
        else
        {
            return -1;
        }
    }


    public static int getSysLatency() throws IOException
    {
        long t3 = System.currentTimeMillis();

        Process p2 = Runtime.getRuntime().exec( "ping -n 1 127.0.0.1" );

        try
        {
            p2.waitFor();
        }
        catch ( InterruptedException e )
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        t3 = System.currentTimeMillis() - t3;

        return (int)t3;
    }
}
