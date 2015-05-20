package net.f00f.javathrottle;

public class DataStamp
{
	public DataStamp( long time, long size )
	{
		this.time = time;
		this.size = size;
	}
	long time;
	long size;
	
	public long getTime()
	{
		return time;	
	}
	
	public long getSize()
	{
		return size;
	}
}

