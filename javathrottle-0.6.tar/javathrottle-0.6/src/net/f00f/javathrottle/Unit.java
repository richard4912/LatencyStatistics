package net.f00f.javathrottle;

import java.util.HashMap;
import java.util.Map;

public class Unit 
{
	// 1 kB = 1024 bytes
	//
	// names and multipliers to convert from a unit to bytes per
	// millisecond.
	public static final Unit bps = new Unit("bps",
			1/(1000.0*8) );				//		0.000125
	public static final Unit Bps = new Unit("Bps",
			1/(1000.0));				//      0.001000;
	public static final Unit kbps = new Unit("kbps",
			(1024)/(1000.0*8));			//      0.128000
	public static final Unit kBps = new Unit("kBps",
			(1024)/(1000.0) );			//      1.024000
	public static final Unit Mbps = new Unit("Mbps",
			(1024*1024)/(1000.0*8) );	//    131.072000
	public static final Unit MBps = new Unit("MBps",
			(1024*1024)/(1000.0));		//   1048.576000
	public static final Unit unlimited = new Unit("Unlimited",
			Double.POSITIVE_INFINITY);

	public static final Unit[] units = 
		{bps,	Bps,	kbps,	kBps,
			Mbps,	MBps, unlimited };
	
	public static final Map unitsMap = makeUnitsMap();
	public static Map makeUnitsMap()
	{
		Map m = new HashMap();
		for( int i = 0; i < units.length; ++i )
		{
			Unit unit = units[i];
			m.put( unit.toString(), unit );
		}

		return m;
	}

	public static Unit parseUnit( String name )
	{
		return (Unit)unitsMap.get( name );
	}

	private String name;
	private double multiplier;

	private Unit( String name, double multiplier )
	{
		this.name = name;
		this.multiplier = multiplier;
	}

	public String toString()
	{
		return name;
	}

	public double getMultiplier()
	{
		return multiplier;
	}

	public Rate makeRate( double val )
	{
		return new Rate( val * multiplier, this );
	}
	
	public Rate makeRate( String val )
	{
		return makeRate( Double.parseDouble( val ) );
	}
}

