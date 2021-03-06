import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.util.Scanner;


public class Ping
{
    public static double getLatency( String address )
    {
        String time = "";

        // The command to execute
        String pingCmd = "fping " + address + " -n 1";

        // get the runtime to execute the command
        Runtime runtime = Runtime.getRuntime();
        try
        {
            Process process = runtime.exec( pingCmd );

            // Gets the inputstream to read the output of the command
            BufferedReader in = new BufferedReader( new InputStreamReader( process.getInputStream() ) );

            // reads the outputs
            String inputLine = in.readLine();
            while ( ( inputLine != null ) )
            {
                if ( inputLine.length() > 0 && inputLine.contains( "time" ) )
                {
                    time = inputLine.substring( inputLine.indexOf( "time" ) );
                    break;
                }
                inputLine = in.readLine();
            }
            time = time.substring( 5, time.length() - 9 );
            // System.out.println( time );
        }
        catch ( Exception ex )
        {
            System.out.println( ex );
        }

        try
        {
            return Double.parseDouble( time );
        }
        catch ( Exception e )
        {
            return -1;
        }
    }


    public static double getLatencies( String address, int numRuns )
    {
        String time = "";

        // The command to execute
        String pingCmd = "ping " + address + " -n 1";

        // get the runtime to execute the command
        Runtime runtime = Runtime.getRuntime();
        try
        {
            Process process = runtime.exec( pingCmd );

            // Gets the inputstream to read the output of the command
            BufferedReader in = new BufferedReader( new InputStreamReader( process.getInputStream() ) );

            // reads the outputs
            String inputLine = in.readLine();
            while ( ( inputLine != null ) )
            {
                if ( inputLine.length() > 0 && inputLine.contains( "time" ) )
                {
                    time = inputLine.substring( inputLine.indexOf( "time" ) );
                    break;
                }
                inputLine = in.readLine();
            }
            time = time.substring( 5, time.length() - 3 );
            // System.out.println( time );
        }
        catch ( Exception ex )
        {
            // System.out.println( ex );
        }
        return Double.parseDouble( time );
    }

}
