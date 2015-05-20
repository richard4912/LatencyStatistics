package net.f00f.javathrottle;
/*
	Throttle, a class to limit the bandwidth between an input stream
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

import java.util.Observer;


/**
 * Throttle acts as a limiter between std in and std out.
 *
 * @author Ben Damm
 * */
public interface Throttle
{
	public void togglePaused();
	public void setPaused( boolean bool );
	public HistoryWindow getHistoryWindow();

	/**
	 * Ensure data in the throttle is flushed.
	 * */
	//public void flush();

	/**
	 * An object to receive periodic update notices.
	 * */
	public void addObserver( Observer obs );

	/**
	 * @return number of bytes.
	 * */
	public long getTotalBytes();

	/**
	 * @return the rate that is actually occurring.
	 * */
	public Rate getActualRate();

	/**
	 * @return the actual buffer size.
	 * */
	public int getActualBufSize();

	/**
	 * @return the largest number of bytes read in one go, in the
	 * window.
	 * */
	public long getMaxWindowRead();

	/**
	 * @return the size of the window, in msec.
	 * */
	public long getActualWindow();

	/**
	 * @return the desired size of the window, in msec.
	 * */
	public long getDesiredWindow();

	/**
	 * @return the total operational time, in msec.
	 * */
	public long getTotalTime();

	/**
	 * @return the amount of unsent data stored internally by the throttle.
	 * */
	public int getSecondaryBufferFill();

	/**
	 * Sets the desired window size (in milliseconds).  This controls
	 * over what range of time rate calculations are done.
	 * */
	public void setDesiredWindow( long window );

	/**
	 * Sets the target rate.
	 * */
	public void setDesiredRate( Rate sdr );

	/**
	 * @return the target rate.
	 * */
	public Rate getDesiredRate();

	/**
	 * Stop reading bytes and end the pipe.  Don't close any streams.
	 * */
	//public void shutdown();

	/**
	 * Start streaming data.
	 * */
	public void start();

	/**
	 * @return true if the stream is active
	 * */
	public boolean isDone();
}

