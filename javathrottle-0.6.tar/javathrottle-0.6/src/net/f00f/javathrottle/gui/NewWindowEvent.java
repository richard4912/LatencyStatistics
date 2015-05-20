package net.f00f.javathrottle.gui;

import java.awt.event.ActionEvent;

public class NewWindowEvent extends ActionEvent
{
	private int newWindow;

	/**
	 * @param newWindow The new window, in milliseconds.
	 * */
	public NewWindowEvent( Object sender, int id, int newWindow )
	{
		super( sender, id, "window" );

		this.newWindow = newWindow;
	}

	public int getNewWindow()
	{
		return newWindow;
	}
}

