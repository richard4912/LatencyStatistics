import java.nio.*;
import java.net.*;
import java.nio.channels.*;
import java.io.*;


public class CreateNetworkLoad
{
    public void run( int bandwidth ) throws IOException
    {
        URL website = new URL( "http://www.website.com/information.asp" );
        ReadableByteChannel rbc = Channels.newChannel( website.openStream() );
        FileOutputStream fos = new FileOutputStream( "information.html" );
        fos.getChannel().transferFrom( rbc, 0, Long.MAX_VALUE );
    }

}
