package net.f00f.javathrottle;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Observable;
import java.util.Observer;

/**
 * AbstractThrottle acts as a limiter between std in and std out.
 * This class maintains things common to all throttling algorithms,
 * that is, tracking data out (in the window) and calculating a rate.
 *
 * @author Ben Damm
 * */
public abstract class AbstractThrottle extends Thread implements Throttle
{
	// AbstractThrottle vars
	protected OutputStream out;

	// bytes/msec
	private Rate desiredRate;
	private Rate actualRate;

	private long totalSent = 0;

	// The window of time within which the rate is calculated
	//private long desiredWindow = 10000;
	//private long actualWindow = 0;
	//private long windowMaxBlock = 0;

	//private boolean reconfig = false;
	//private int desiredBufSize;
	//private int actualBufSize;

	private long startTime = 0;
	private long lastUIUpdate = 0;

	// The time when calculations were last done
	private long lastCalcTime = 0;

	private boolean paused = false;
	private boolean shutdown = false;

	// A queue of output transactions, only the newest
	//private LinkedList dataStamps;
	private HistoryWindow history;

	// The observable to do updates
	private Observable observable;

	// The delay past which an update will be triggered
	// in msec
	private long max_emit_update = 100;

	public AbstractThrottle( OutputStream out )
	{
		this.out = out;

		startTime = System.currentTimeMillis();

		// Set default to no data
		desiredRate = new Rate();
		actualRate  = new Rate();

		//dataStamps = new LinkedList();
		history = new HistoryWindow();
		observable = new Observable()
		{
			public void notifyObservers()
			{
				setChanged();
				super.notifyObservers();
			}
		};

	}

	public HistoryWindow getHistoryWindow()
	{
		return history;
	}
	
	public void togglePaused()
	{
		synchronized( this )
		{
			setPaused( !paused );
		}
	}

	public void setPaused( boolean bool )
	{
		synchronized( this )
		{
			paused = bool;

			// We are un-pausing
			if( !paused )
			{
				resetWindow();
			}
			notifyAll();
		}
	}

	public boolean isPaused()
	{
		synchronized( this )
		{
			return paused;
		}
	}

	public void addObserver( Observer obs )
	{
		observable.addObserver( obs );
	}

	public long getTotalBytes()
	{
		return totalSent;
	}

	public Rate getActualRate()
	{
		return actualRate;
	}

	public long getActualWindow()
	{
		return history.getActualWindow();
	}

	public long getDesiredWindow()
	{
		return history.getDesiredWindow();
	}

	public long getTotalTime()
	{
		return System.currentTimeMillis()-startTime;
	}

	/**
	 * Does a UI update
	 * */
	protected void fireUpdate()
	{
		observable.notifyObservers();
	}

	/**
	 * @return the largest block in the window.
	 * */
	protected long getWindowMaxBlock()
	{
		return history.getWindowMaxBlock();
	}

	/**
	 * Sets the desired window size (in milliseconds).  This controls
	 * over what range of time rate calculations are done.
	 * */
	public void setDesiredWindow( long window )
	{
		synchronized( desiredRate )
		{
			history.setDesiredWindow(window);
			resetBuffer();
		}
	}

	/**
	 * Set the rate we should aim for.
	 * */
	public void setDesiredRate( Rate sdr )
	{
		synchronized( desiredRate )
		{
			if( sdr == null )
				desiredRate = Unit.Bps.makeRate( 1 );
			else 
				desiredRate = sdr;

			actualRate.setUnit( desiredRate.getUnit() );

			desiredRateUpdated(desiredRate);
			resetWindow();
			resetBuffer();

			//setDesiredWindow((long)(1.0/desiredRate.getBytesPerMSec()));
		}
	}

	protected abstract void desiredRateUpdated( Rate newDesiredRate );
	//{
	//	hint_careful = newDesiredRate.getBytesPerMSec() < MAX_careful_rate;
	//}

	/**
	 * Resize a buffer, if required.
	 * */
	protected abstract void resetBuffer();

	protected void resetWindow()
	{
		history.resetWindow();
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

	public Rate getDesiredRate()
	{
		return desiredRate;
	}

	public synchronized void shutdown()
	{
		shutdown = true;
		setPaused( false );
	}

	/**
	 * Sends out to the OutputStream (not a BufferedOutputStream)
	 * and update the window data.
	 * */
	protected void sendOut( byte[] data, int position, int size )
		throws IOException
	{
		if( position+size > data.length )
		{
			throw new IllegalArgumentException("foo");
		}
		out.write( data, position, size );
		history.add( size );

		totalSent += size;
	}

	/**
	 * Does an update, if enough time has passed since the last
	 * update.  All this does is emit a signal, but since signal we
	 * emit is something like "examine the state for presentation
	 * purpose" it should not be emitted too often.
	 * */
	public void maybeEmitUpdate()
	{
		long now = System.currentTimeMillis();
		if( now - lastUIUpdate > max_emit_update )
		{
			lastUIUpdate = now;
			fireUpdate();
			Thread.yield();
		}
	}

	/**
	 * Sets the delay past which an update will be triggered.
	 * */
	protected void setUpdateTrigger( long max_emit_update )
	{
		this.max_emit_update = max_emit_update;
	}

	/**
	 * Predicts what the rate will become if a number of bytes are
	 * output.
	 * */
	protected double predictRate(int bytesOut)
	{
		synchronized( desiredRate )
		{
			long prevCalcTime = lastCalcTime;
			lastCalcTime = System.currentTimeMillis();
			long now = lastCalcTime;

			// If this is the first calculation...
			if( prevCalcTime == 0 )
				prevCalcTime = now;

			/* When the window should begin
			 * (throw away any data older than this)
			 * */
			long ago = now - history.getActualWindow();
			long first = Math.max( ago, startTime);

			/* If the window is smaller than it should be, increase it a
			 * bit (the time between now and the previous calculation
			 * time) 
			 * */
			history.updateWindow( now-prevCalcTime );
			
			long[] historydata = history.getHistory( ago, true );
			long windowTotal = historydata[0];
			long windowMaxBlock = historydata[1];
			//log( " Now: " + now + " Ago: " + ago + " First: " + first );
			//log( "DS Size1: " + dsSz1 + " DS Size2: " + dsSz2 + " wT: " + windowTotal);

			//  *** THE BIG EQUATION!  Rate=Date/Time! ***
			long dataOut = windowTotal+bytesOut;
			long calcSpan = now-first;
			if( calcSpan == 0 )
			{
				// Data out is 0, so no matter what the span is the
				// rate is zero.
				if( dataOut == 0 ) return 0;

				// Data out is not zero (>0 presumably) so rate is
				// inf.
				if( dataOut > 0 ) return Double.POSITIVE_INFINITY;

				// assert dataOut >= 0
				throw new IllegalStateException("dataOut cannot be less than 0!");
			}
			else
			{
				return (double)(dataOut) / (calcSpan);
			}
			// ** END of the big equation! ***
		}
	}

	/**
	 * Calculates the current rate, returning the result.  Side
	 * effect, sets the actualRate variable.  Should always be called
	 * when synchronized on the desiredRate object. (?)
	 * */
	protected double calcRate()
	{
		// Predict the rate as if no data is to be sent (ie the immediate situation)
		double result = predictRate( 0 );

		actualRate.setBytesPerMSec( result );
		//log( "Rate: " + actualRate );
		//log( "Target: " + desiredRate );
		return result;
	}

}

