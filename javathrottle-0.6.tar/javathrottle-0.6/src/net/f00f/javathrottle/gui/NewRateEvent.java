package net.f00f.javathrottle.gui;

import java.awt.event.ActionEvent;

import net.f00f.javathrottle.Rate;

public class NewRateEvent extends ActionEvent
{
	private Rate newRate;

	public NewRateEvent( Object sender, int id, Rate newRate )
	{
		super( sender, id, "rate" );

		this.newRate = newRate;
	}

	public Rate getNewRate()
	{
		return newRate;
	}
}

