package net.f00f.javathrottle;
/*
	OutThrottle, a class to limit the bandwidth between an input stream
	and an output stream.  Copyright (C) 2003 Ben Damm

    This library is free software; you can redistribute it and/or
    modify it under the terms of the GNU Lesser General Public
    License as published by the Free Software Foundation; either
    version 2.1 of the License, or (at your option) any later version.

    This library is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
    Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public
    License along with this library; if not, write to the Free Software
    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

	For information, contact bdamm@f00f.net
*/

import java.io.IOException;
import java.io.OutputStream;
import java.util.LinkedList;

/**
 * Throttle acts as a limiter between std in and std out, by limiting
 * how fast data is written.  A buffer of a fixed size is maintained
 * on the input.
 *
 * @author Ben Damm
 * */
public class OutThrottle extends AbstractThrottle
{
	private int sleepTime = 10;
	private double bytesPerSleep;
	// How many times we have slept between sending data
	private int sleepIdlePeriods = 0;


	public static final int MAX_bufsize = 4096<<2;

	// Maximum desired rate at which throttle is still careful.
	// In bytes per msec, so *1000 for Bps... in other words, this is
	// in kBps
	private static final double MAX_careful_rate = 10;

	// Amount of time to wait when we have no data before checking
	// again.
	private static final int TIMEOUT_starvation = 100;

	// Controls how "careful" the program should be with producing
	// the perfect rate, right on time (ie, flush out on every write,
	// etc).  "careful" == smooth+consistent verses not careful, be as
	// efficient as possible (larger blocks, less flushing, etc).
	private boolean hint_careful = true;

	// This flag gets set when starvation sets in, that is, we are
	// trying to output data but there's no data to output
	private boolean starving = true;

	private BlockInputStream in;
	private Thread inThread;  // The thread that wraps the BlockInputStream

	private OutputStream out;
	// bytes/msec
	private Rate desiredRate;

	private long total = 0;
	private long windowMaxRead = 0;

	// The window of time within which the rate is calculated
	private long desiredWindow = 10000;

	private boolean reconfig = false;

	private long startTime = 0;
	private long lastUIUpdate = 0;

	// The time when calculations were last done
	private long lastCalcTime = 0;

	// Flag gets set when the thread determines that shutdown has
	// occurred.
	private boolean done = false;

	public OutThrottle( BlockInputStream in, OutputStream out )
	{
		super( out );
		this.in = in;
		this.inThread = new Thread( this.in );
		this.out = out;
	}

	public int getActualBufSize()
	{
		return in.getMaxSize();
	}

	public long getMaxWindowRead()
	{
		return getWindowMaxBlock();
	}

	public void run()
	{
		inThread.start();

		try
		{
			doMainLoop();
		}
		catch( IOException ioe )
		{
			System.err.println( ioe );
		}
		catch( InterruptedException ie )
		{
			// finish exiting
		}

		synchronized( this )
		{
			done = true;
			notifyAll();
			streamOver();
		}
	}

	public int getSecondaryBufferFill()
	{
		synchronized( secBufferFillLock )
		{
			return secBufferFill;
		}
	}
	
	private int secBufferFill = 0;
	private Object secBufferFillLock = new Object();

	private void doMainLoop()
		throws IOException, InterruptedException
	{
		// source (secondary buffer) position
		int srcPos = -1;
		byte[] srcData = null;

		
		int sleeptime = sleepTime;
		int bytesOut = 10;

		// fresh, no reads
		boolean fresh = true;

		while( true )
		{
			// Pausing happens at the end of this loop
			
			// if we need more data
			if( srcData == null )
			{
				// Read from in
				Object fromQueue = in.pop();

				starving=false;
				if( fromQueue instanceof byte[] )
				{
					srcData = (byte[])fromQueue;
					srcPos = 0;
					synchronized( secBufferFillLock )
					{
						secBufferFill = srcData.length;
					}
					//out.write( data );
					//sendOut( data, data.length );
				}
				else if( fromQueue instanceof IOException )
				{
					logError( (IOException)fromQueue );
				}
				else if( fromQueue.equals( BlockInputStream.EOF_FLAG ) )
				{
					streamOver();
					return;
				}
				else if( fromQueue.equals( BlockInputStream.NO_DATA_FLAG ) )
				{
					out.flush();
					srcData = null;
					synchronized( secBufferFillLock )
					{
						secBufferFill = 0;
					}
					// If we are suffering from data starvation (no
					// input) then the rate isn't stable and we should
					// kill the window.
					//
					// Unless we are too slow because we really are
					// shovelling data though ASAP.
					//resetWindow();

					// If we have no data, let's wait for a moment for
					// some to arrive.
					Thread.sleep(TIMEOUT_starvation);
					starving=true;
				}
				else
				{
					logError( "Unknown object in queue: " + fromQueue.getClass().getName() );
					logError( fromQueue.toString() );
				}
			}

			// Write some data
			if( srcData != null )
			{
				int send = Math.min( bytesOut, srcData.length - srcPos );
				//System.err.println( "Send: " + bytesOut );
				try
				{
					sleepIdlePeriods = 0;
					sendOut( srcData, srcPos, send );
				}
				catch(ArrayIndexOutOfBoundsException aioobe )
				{
					System.err.println( "sD: " + srcData.length + " sP: " + srcPos + " s: " + send + " bO: " + bytesOut);
					throw aioobe;
				}
				srcPos += send;
				if( srcPos >= srcData.length )
				{
					srcData = null;
				}
				
				synchronized( secBufferFillLock )
				{
					if( srcData == null )
						secBufferFill = 0;
					else
						secBufferFill = srcData.length - srcPos;
				}
			}

			// How many bytes should be output to bring the rate back
			// into line?
			bytesOut = calcBytesOut(bytesOut,sleeptime,starving);

			// Are we paused?
			synchronized ( this )
			{
				while( isPaused() )
				{
					wait( 100 ); // Wait for 1/10th second

					// Update gui?
					maybeEmitUpdate();

					// Recalc data block
					bytesOut = calcBytesOut(bytesOut,sleeptime,starving);
				}
			}
		}
	}

	private int laggingCounter=0;
	/**
	 * @param bytesOut Current number of bytes being output
	 * @param starving true if there isn't more data to send
	 * */
	private int calcBytesOut(int bytesOut, long sleeptime, boolean starving)
		throws InterruptedException
	{
		// Calculates the rate over the window.
		double realRate = calcRate();
		double desRate = getDesiredRate().getBytesPerMSec();
		maybeEmitUpdate();

		// First of all, if we do not need to be careful lets just set
		// the block size pretty high.
		if( !hint_careful && bytesOut < 512 )
		{
			bytesOut = 512;
		}

		if( realRate < desRate )
		{
			// Too slow
			if( starving )
			{
				// The data is a precious resource, so halve the data
				// going out
				if( bytesOut > 1 )
					bytesOut>>=1;

				// We aren't lagging
				laggingCounter=0;
			}
			else
			{
				++laggingCounter;
				// We are lagging behind
				if( laggingCounter > 10 || (laggingCounter > 0 && !hint_careful) )
				{
					// Double bytesOut
					bytesOut*=2;
				}
				else
				{
					// Be more gentle
					bytesOut++;
				}
			}

			// We can't send that much
			if( bytesOut > MAX_bufsize )
				bytesOut = MAX_bufsize;

			// How much shouldn't be more than we should send in a
			// sleep
			if( bytesOut > (3*bytesPerSleep) && hint_careful )
				bytesOut = 1;

			// Obvsiously it can't be negative... 
			if( bytesOut < 0 )
			{
				// Presumably this happens because
				// bytesPerSleep can be infinity
				bytesOut = Integer.MAX_VALUE;
			}

			// log( "Slow: " + bytesOut );

			return bytesOut;
		}
		else while( realRate >= desRate )
		{
			// log( "Fast: " + bytesOut );
			// Too fast
			sleep( sleeptime );

			laggingCounter=0;
			
			++sleepCount;
			++sleepIdlePeriods;
			//System.err.println("Did sleep: " + sleepCount + " \t" + System.currentTimeMillis());

			realRate = calcRate();

			//if( bytesPerSleep < 1 )
			//{
			//	bytesOut = (int)(bytesPerSleep * sleepIdlePeriods);
			//	log( "bO: " + bytesOut );
			//	if( bytesOut >= 1 ) 
			//	{
			//		bytesOut = 1;
			//	}

			//	if( bytesOut < 0 )
			//	{
			//		// Presumably this happens because
			//		// bytesPerSleep can be infinity
			//		bytesOut = Integer.MAX_VALUE;
			//	}

			//}
			//else 
			//{
			//	bytesOut = (int)bytesPerSleep;
			//	log( "b1: " + bytesOut );
			//	if( bytesOut < 0 )
			//	{
			//		// Presumably this happens because
			//		// bytesPerSleep can be infinity
			//		bytesOut = Integer.MAX_VALUE;
			//	}
			//}

			// Now that we've adjusted the rate, data will we
			// blow over? Lets not do that.
			while( predictRate(bytesOut) > desRate && bytesOut > 0 )
			{
				// Divide by 2
				bytesOut >>= 2;
			}

			maybeEmitUpdate();
			return bytesOut;
		}


		log(  "Strange (" + realRate + "," + desRate + "): " + bytesOut );
		return bytesOut;
	}

	private synchronized void streamOver()
	{
		log( "done" );
		done=true;
		try
		{
			out.flush();  // redundant?
			out.close();

		}
		catch( IOException ioe )
		{
			logError( ioe );
		}
		log( "updating" );

		fireUpdate();
	}

	public synchronized boolean isDone()
	{
		return done;
	}


	protected void resetBuffer()
	{
	}

	protected void desiredRateUpdated(Rate newRate)
	{
		double sleepsPerSecond = 1000.0/sleepTime;

		// The desired rate has been reset
		bytesPerSleep = getDesiredRate().getBytesPerMSec() * 1000.0/sleepsPerSecond;

		log( "Sleeps: " + sleepsPerSecond );
		log( "bpSleep: " + bytesPerSleep );

		hint_careful = newRate.getBytesPerMSec() < MAX_careful_rate;
		log( "careful: " + hint_careful );
	}

	// Used for debugging
	private int sleepCount;

}

