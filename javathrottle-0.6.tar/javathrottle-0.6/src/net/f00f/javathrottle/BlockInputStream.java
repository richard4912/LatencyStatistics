package net.f00f.javathrottle;

import java.io.InputStream;
import java.io.IOException;
import java.util.LinkedList;

/**
 * A class to read blocks of data from the input stream.  There is a
 * limit to how big the blocks can be, and a limit to how many blocks
 * can be read in at once.
 * */
public class BlockInputStream implements Runnable
{

	public static final String EOF_FLAG = "EOF";
	public static final String NO_DATA_FLAG = "EMPTY";

	private LinkedList queue;
	private int size = 0;

	private Object maxSizeLock = new Object();
	private int maxSize = 0;

	private boolean shutdownNow = false;

	// Read buffer
	private byte[] buffer;

	private InputStream in;

	protected void log(String msg)
	{
		Main.debug( msg );
		//System.err.println( msg );
	}

	public BlockInputStream(InputStream in, int maxSize)
	{
		buffer = new byte[4096];
		this.maxSize = maxSize;
		this.in = in;

		queue = new LinkedList();
	}

	public boolean hasData()
	{
		synchronized( queue )
		{
			return !queue.isEmpty();
		}
	}

	/**
	 * @return byte[] for data, "EOF" for end of file, IOException if
	 * an IOException happened during read, "EMPTY" if no data.
	 * */
	public Object pop()
	{
		Object obj;
		synchronized( queue )
		{
			if( queue.isEmpty() )
			{
				return NO_DATA_FLAG;
			}

			obj = queue.removeFirst();
			if( obj instanceof byte[] )
			{
				byte[] data = (byte[])obj;
				size -= data.length;
			}
			else if( obj instanceof EndOfFileSentinel )
			{
				return EOF_FLAG;
			}

			// Notify threads waiting on queue changes.
			queue.notifyAll();
		}
		// Could be a byte[], could be an IOException
		return obj;
	}

	public int getFill()
	{
		synchronized( queue )
		{
			return size;
		}
	}

	public void shutdown()
	{
		shutdownNow = true;
	}

	public void run()
	{
		readLoop();
	}

	/**
	 * @return the maximum amount of data that can be retained in this
	 * instance.
	 * */
	public int getMaxSize()
	{
		synchronized( maxSizeLock )
		{
			return maxSize;
		}
	}

	public void setMaxSize(int newMax )
	{
		if( newMax > 0 )
		{
			synchronized( maxSizeLock )
			{
				maxSize = newMax;
			}
		}
	}

	/**
	 * Reads from the stream, adding data to the queue.
	 * */
	private void readLoop()
	{
		while( true )
		{
			if( shutdownNow ) return;
			synchronized( queue )
			{
				while(true)
				{
					// Check to see if we should read data (ie our
					// buffer contains less data than the allowed
					// maximum)
					synchronized( maxSizeLock )
					{
						if( size < maxSize )
							break;
					}
					try
					{
						// Wait until the queue changes to check its size
						queue.wait();
					}
					catch( InterruptedException ie )
					{
						return;
					}
				}
			}
			//  Now, the queue must contain less data  the maximum.
			//  We are now unsynchronized, so more data may be removed
			//  while we are reading, but nevertheless, the queue
			//  remains smaller than max.
			try
			{
				int toRead = in.available();
				// Read at least 1
				toRead = Math.max( toRead, 1 );
				// Read no more than the buffer size
				toRead = Math.min( toRead, buffer.length );
				// Read no more than the maxSize
				synchronized( maxSizeLock )
				{
					toRead = Math.min( toRead, maxSize-size );
				}
				
				int readSize = in.read( buffer, 0, toRead );

				if( readSize == -1 )
				{
					synchronized( queue )
					{
						queue.add( new EndOfFileSentinel() );
					}
					// No more reading
					return;
				}
				else
				{
					byte[] destArr = new byte[readSize];
					System.arraycopy(buffer,0,destArr,0,readSize);
					synchronized( queue )
					{
						queue.add( destArr );
						size += readSize;
					}
				}
			}
			catch( IOException ioe )
			{
				synchronized( queue )
				{
					queue.add( ioe );
				}
				// No more reading
				return;
			}
		}
	}

	private static class EndOfFileSentinel{}
}


