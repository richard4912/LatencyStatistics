package net.f00f.javathrottle.gui;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.EventListenerList;

import net.f00f.javathrottle.Main;

public class JWindowSelect extends JIntegerSelect implements ActionListener
{
	private NewWindowEvent windowEvent = null;

	private int numWindowEvents;

	public JWindowSelect(int def)
	{
		super( def/1000, 8, "seconds" );
	}

	public void setWindow(int newW)
	{
		setIntValue(newW);
	}

	protected ActionEvent makeEvent( int newNum )
	{
		return new NewWindowEvent( this, ++numWindowEvents, newNum * 1000); 
	}

}
