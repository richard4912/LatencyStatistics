package net.f00f.javathrottle;

import java.text.DecimalFormat;
import java.text.NumberFormat;

public class Rate 
{
	// desired unit to express in
	private Unit unit;
	private double value;

	private static final NumberFormat nf = new DecimalFormat("0.000");

	public Rate()
	{
		this( 0, Unit.Bps );
	}

	public Rate( double bytesPerMSec )
	{
		this( bytesPerMSec, Unit.Bps );
	}

	/**
	 * @param bytesPerMSec This rate, in bytes per milli second
	 * @param u The unit that should be used to express this
	 * */
	public Rate( double bytesPerMSec, Unit u )
	{
		value = bytesPerMSec;
		unit = u;
	}
 
	public synchronized int hashCode()
	{
		return new Double( value ).hashCode();
	}

	public synchronized boolean equals( Object obj )
	{
		if( obj instanceof Rate )
			return compareTo( obj ) == 0;
		return false;
	}

	/**
	 * Uses Double.compareTo on the bytesPerMSec value.
	 * */
	public synchronized int compareTo( Object obj )
	{
		Rate rhs = (Rate)obj;
		return new Double( value ).compareTo( new Double( rhs.value ) );
	}

	public synchronized String toString()
	{
		if( unit == Unit.unlimited )
		{
			double v = value / Unit.MBps.getMultiplier();
			if( v > 1 )
				return nf.format(v) + " " + Unit.MBps.toString();

			v = value / Unit.kBps.getMultiplier();
			if( v > 1 ) 
				return nf.format(v) + " " + Unit.kBps.toString();

			v = value / Unit.Bps.getMultiplier();
			return nf.format(v) + " " + Unit.Bps.toString();
		}

		return "" + nf.format( value / unit.getMultiplier() ) + " " + unit.toString();
	}

	public synchronized double getBytesPerMSec()
	{
		return value;
	}

	public synchronized double getMSecPerByte()
	{
		return 1/value;
	}

	/**
	 * @return this, keeps the same desired unit.
	 * */
	public synchronized Rate setBytesPerMSec( double bytesPerMSec )
	{
		this.value = bytesPerMSec;
		return this;
	}

	public synchronized Unit getUnit()
	{
		return unit;
	}

	public synchronized void setUnit( Unit u2 )
	{
		this.unit = u2; // I would walk 500 miles
	}
}

