package net.f00f.javathrottle.gui;

import java.awt.event.ActionEvent;

public class NewBufferEvent extends ActionEvent
{
	private int newBuffer;

	/**
	 * @param newWindow The new window, in milliseconds.
	 * */
	public NewBufferEvent( Object sender, int id, int newBuffer )
	{
		super( sender, id, "buffer" );

		this.newBuffer = newBuffer;
	}

	public int getNewBuffer()
	{
		return newBuffer;
	}
}

