package net.f00f.javathrottle.gui;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.event.EventListenerList;

import net.f00f.javathrottle.Main;
import net.f00f.javathrottle.Rate;
import net.f00f.javathrottle.Unit;

public class JRateSelect extends JPanel
implements ActionListener
{
	private JIntegerField numberField;
	private JComboBox timeUnit;
	private JButton setButton;

	private int numRateEvents;
	private ActionEvent rateEvent;

	public JRateSelect( Rate r )
	{
		this( r.getBytesPerMSec(), r.getUnit() );
	}


	public JRateSelect( double bytesPerMilliSec, Unit defU )
	{
		setLayout( new JTripleLayout() );
		//Create the combo box, select Bps (Bytes per second)
		timeUnit = new JComboBox(Unit.units);
		timeUnit.setPreferredSize( new Dimension( 80, 24 ) );
		for( int i = 0; i < Unit.units.length; ++i )
		{
			if( Unit.units[i] == defU )
			{
				timeUnit.setSelectedIndex( i );
			}
		}

		numberField = new JIntegerField((int)(bytesPerMilliSec/defU.getMultiplier()), 8);

		add( numberField );
		add( timeUnit );
		
		setButton = GuiPanel.makeSetButton();
		setButton.addActionListener( this );
		add( setButton );
	}

	public void setEnabled( boolean val )
	{
		super.setEnabled(val);
		setButton.setEnabled( val );
		timeUnit.setEnabled( val );
		numberField.setEnabled( val );
	}

	EventListenerList listenerList = new EventListenerList();

	public void addActionListener(ActionListener l)
	{
		listenerList.add( ActionListener.class, l );
	}

	public void removeActionListener(ActionListener l)
	{
		listenerList.remove( ActionListener.class, l );
	}

	private void log(String msg)
	{
		Main.debug( msg );
	}

	public void actionPerformed( ActionEvent ae )
	{
		// 1: Calc
		//log( "R: " + numberField.getValue() );
		//log( "U: " + timeUnit.getSelectedItem() );

		Unit unit = Unit.parseUnit( timeUnit.getSelectedItem().toString() );

		//log( "M: " + d );

		Rate rate = unit.makeRate( numberField.getValue() );

		//log( "NR: " + rate );

		fireActionEvent(rate);
	}

	// Notify all listeners that have registered interest for
	// notification on this event type.  The event instance 
	// is lazily created using the parameters passed into 
	// the fire method.

	protected void fireActionEvent(Rate newRate) {
		// Guaranteed to return a non-null array
		Object[] listeners = listenerList.getListenerList();
		// Process the listeners last to first, notifying
		// those that are interested in this event
		for (int i = listeners.length-2; i>=0; i-=2) {
			if (listeners[i] == ActionListener.class) {
				// Lazily create the event:
				if (rateEvent == null)
					rateEvent = new NewRateEvent(this,++numRateEvents,newRate );
				((ActionListener)listeners[i+1]).actionPerformed( rateEvent );
			}
		}

		rateEvent = null;
	}
}

