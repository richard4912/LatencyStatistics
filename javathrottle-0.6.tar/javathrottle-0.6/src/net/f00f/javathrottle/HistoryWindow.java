package net.f00f.javathrottle;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

/**
 * HistoryWindow keeps a record of data sent, calculating various
 * properties about it.
 *
 * @author Ben Damm
 * */
public class HistoryWindow 
{
	// bytes/msec
	//private Rate actualRate;

	// The window of time within which the rate is calculated
	private long desiredWindow = 10000;
	private long actualWindow = 0;
	private long windowMaxBlock = 0;

	private long startTime = 0;
	private long lastUIUpdate = 0;

	// A queue of output transactions, only the newest
	private LinkedList dataStamps;

	// The observable to do updates
	//private Observable observable;

	// The delay past which an update will be triggered
	// in msec
	//private long max_emit_update = 100;

	public HistoryWindow( )
	{
		startTime = System.currentTimeMillis();

		dataStamps = new LinkedList();
	}

	public long getActualWindow()
	{
		return actualWindow;
	}

	public long getDesiredWindow()
	{
		return desiredWindow;
	}
	
	public void add( int size )
	{
		if ( size == 0 )
			// Don't bother
			return;
		
		synchronized(dataStamps)
		{
			long now = System.currentTimeMillis();
			dataStamps.add( new DataStamp(now, size) );
		}
	}

	/**
	 * @return the largest block in the window.
	 * */
	public long getWindowMaxBlock()
	{
		synchronized( dataStamps )
		{
			return windowMaxBlock;
		}
	}

	/**
	 * Sets the desired window size (in milliseconds).  This controls
	 * over what range of time rate calculations are done.
	 * */
	public void setDesiredWindow( long window )
	{
		synchronized( dataStamps )
		{
			desiredWindow = window;
			resetWindow();
		}
	}

	protected void resetWindow()
	{
		log("Reset Window[II]" );
		synchronized( dataStamps )
		{
			actualWindow = 0;
			dataStamps.clear();
			windowMaxBlock = 0;
		}
		log("Reset Window[IJ]" );
	}

	protected void log(String msg)
	{
		Main.debug( msg );
	}

	protected void logError(Exception e)
	{
		System.err.println( e );
	}

	protected void logError(String s)
	{
		System.err.println( s );
	}

	public List getWindowCopy( )
	{
		synchronized( dataStamps )
		{
			Vector v = new Vector( dataStamps );
			return v;
		}
	}
	
	/**
	 * TODO: Return the largest send actually in the window
	 * @return The size of the block sent at once.
	 */
	/*public long getBiggestSend()
	{
		long a = 0;
		synchronized( dataStamps )
		{
			Iterator i = dataStamps.iterator();
			while( i.hasNext() )
			{
				Object o = i.next();
				
				DataStamp ds = (DataStamp)o;
				if ( ds.size > a ) 
					a = ds.size;
			}
			
			return a;
		}
	}*/
	
	public void updateWindow( long span )
	{
		synchronized(dataStamps)
		{
			if( getActualWindow() < getDesiredWindow() )
			{
				actualWindow += span;
	
				/* Clipping the window at exactly the desired value
				 * seems to create output anomalies that eddy
				 * periodically.
				 * */
		 		actualWindow = Math.min( actualWindow, desiredWindow );
			}
		}
	}

	/**
	 * This method is ripe for optimization.  The sum should be stored
	 * and added to/subtracted from.
	 *
	 * @param ago Get history between now and long ago (could be recent ago too.. ;).
	 * @param pruneHistory Remove data older than long ago.
	 * 
	 * @return the number of bytes sent between now and long ago, and
	 * the largest chunk of data sent between now and long ago.
	 * */
	public long[] getHistory( long ago, boolean pruneHistory )
	{
		long windowMaxBlock = 0;
		long windowTotal = 0;
		synchronized( dataStamps )
		{
			Iterator i = dataStamps.iterator();
			while( i.hasNext())
			{
				DataStamp ds = (DataStamp)i.next();
				if( pruneHistory && ds.time < ago ) 
				{
					i.remove();
					if( ds.size == windowMaxBlock )
						windowMaxBlock = 0;
					debug( "Demeted: " + ds.getTime() + " size: " + dataStamps.size() + " size2: " + ds.getSize() );
				}
				else
				{
					long sz = ds.size;
					windowTotal += sz;
					if( sz > windowMaxBlock )
						windowMaxBlock = sz;
				}
			}
			this.windowMaxBlock = windowMaxBlock;
		}
		long[] result = new long[2];
		result[0] = windowTotal;
		result[1] = windowMaxBlock;

		return result;
	}


	private void debug( String s )
	{
		Main.debug( s );
	}
}

