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

public class JBufferSelect extends JIntegerSelect
{
	private int numBufferEvents = 0;

	public JBufferSelect(int def)
	{
		super( def, 8, "bytes" );
	}

	public void setBuffer(int newB)
	{
		setIntValue(newB);
	}

	protected ActionEvent makeEvent( int newNum )
	{
		return new NewBufferEvent( this, ++numBufferEvents, newNum ); 
	}

}
