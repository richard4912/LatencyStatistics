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

public abstract class JIntegerSelect extends JPanel implements ActionListener
{
	protected JButton setButton;
	protected JIntegerField numberField;
	protected JLabel secondsLbl;

	protected abstract ActionEvent makeEvent( int number );

	public JIntegerSelect( int num, int size, String name )
	{
		setLayout(new JTripleLayout());

		numberField = new JIntegerField(num, size);

		add(numberField);
		add(secondsLbl = new JLabel(" " + name));

		setButton = GuiPanel.makeSetButton();
		setButton.addActionListener(this);
		add(setButton);
	}

	public void setEnabled(boolean value)
	{
		super.setEnabled(value);
		numberField.setEnabled(value);
		secondsLbl.setEnabled(value);
		setButton.setEnabled(value);
	}

	public void setIntValue( int val )
	{
		numberField.setValue( val );
	}

	private void log(String msg)
	{
		Main.debug(msg);
	}


	// Notify all listeners that have registered interest for
	// notification on this event type.  The event instance 
	// is lazily created using the parameters passed into 
	// the fire method.

	protected void fireActionEvent(ActionEvent event)
	{
		// Guaranteed to return a non-null array
		Object[] listeners = listenerList.getListenerList();
		// Process the listeners last to first, notifying
		// those that are interested in this event
		for (int i = listeners.length - 2; i >= 0; i -= 2)
		{
			if (listeners[i] == ActionListener.class)
			{
				((ActionListener)listeners[i + 1]).actionPerformed(event);
			}
		}
	}

	private EventListenerList listenerList = new EventListenerList();

	public void addActionListener(ActionListener l)
	{
		listenerList.add(ActionListener.class, l);
		// Trigger a declaration for the new listener
		actionPerformed(null);
	}

	public void removeActionListener(ActionListener l)
	{
		listenerList.remove(ActionListener.class, l);
	}

	public void actionPerformed(ActionEvent ae)
	{
		int newnum = numberField.getValue();

		fireActionEvent(makeEvent(newnum));
	}
}

