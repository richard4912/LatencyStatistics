package net.f00f.javathrottle.gui;
/*
	JavaThrottle, a program to limit the bandwidth between an input
	stream and an output stream.  Copyright (C) 2003 Ben Damm

	For information, contact bdamm@f00f.net

                        -----  LGPL License  -----
    This library is free software; you can redistribute it and/or
    modify it under the terms of the GNU Lesser General Public
    License as published by the Free Software Foundation; either
    version 2.1 of the License, or (at your option) any later version.

    This library is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
    Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public
    License along with this library; if not, write to the Free Software
    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
                        -----       END      -----

*/


import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;

import net.f00f.javathrottle.BlockInputStream;
import net.f00f.javathrottle.Main;
import net.f00f.javathrottle.OutThrottle;
import net.f00f.javathrottle.Rate;
import net.f00f.javathrottle.Throttle;
import net.f00f.javathrottle.layout.TableLayout;

/**
 * This class does the command line switches and the GUI.  The
 * throttling of the pipe is done in a Throttle implementation.
 *
 * @author Ben Damm
 * */
// Rates are bytes per millisecond, throughout the code.
public class GuiPanel extends JPanel implements ActionListener
{
	private JLabel bytesLabel;  // raw number of bytes
	private JLabel kbytesLabel; // human readable bytes label
	private JLabel rateLabel;
	private JLabel timeLabel;
	private JLabel bufLabel;
	private JLabel bufSecondFillLabel;
	private JLabel bufFillLabel;
	private JProgressBar windowLabel;
	private JLabel desiredWindowLabel;
	private JLabel windowMaxReadLabel;
	private JCheckBox stayOpenBox;
	private JRateSelect desiredRateSelect;
	private JWindowSelect desiredWindowSelect;
	private JBufferSelect desiredBufferSelect;
	private JButton pauseBtn;
	private JButton stopBtn;
	private boolean stayOpenDefault = false;
	private boolean showGraph;

	private List activeComponents;
	// The master component
	private Component mainComponent;

	private Throttle throttle;

	private static boolean DEBUG_enable = true;

	public static JButton makeSetButton()
	{
		JButton setButton = new JButton( "Set" );
		//setButton.setPreferredSize( new Dimension( 40, 24 ) );

		return setButton;
	}

	private BlockInputStream blockin;

	/**
	 *
	 * */
	public GuiPanel( Map props )
	{
		stayOpenDefault = ((Boolean)props.get( Main.KEY_persist )).booleanValue();
		showGraph = ((Boolean)props.get( Main.KEY_showgraph )).booleanValue();
		
		blockin = new BlockInputStream( System.in, OutThrottle.MAX_bufsize );
		throttle = new OutThrottle( blockin, System.out );

		throttle.setDesiredRate( (Rate)props.get(Main.KEY_desiredRate) );
	
		throttle.addObserver( new Observer()
				{
					public void update( Observable o1, Object o2 )
					{
						// Make a code to do the update, that runs in
						// the swing thread
						Runnable updateCode = new Runnable()
						{
							public void run()
							{
								updateLabels();
							}
						};

						try
						{
							SwingUtilities.invokeLater( updateCode );
						}
						catch( Exception ie )
						{
							return;
						}

					}
				});


		// Initialize important components
		initComponents();

		// Lay out the components
		mainComponent = makeLayout(false);

		add( mainComponent );
	}


	private JComponent makeLayout(boolean longform)
	{
		JPanel bigPanel = new JPanel();
		
		// Make a list (long form)
		activeComponents = makeComponents(longform);

		// +1 for the button row
		int rows=activeComponents.size()+2;

		// 2 columns, each 50% size
		double columnSizes[] = {0.50, 0.50};

		double rowSizes[] = new double[rows];

		for( int i = 0; i < rows; ++i )
		{
			// Size every row to whatever it wants
			rowSizes[i]=TableLayout.PREFERRED;
		}

		double size[][] =
			{ columnSizes, rowSizes };

		TableLayout layout = new TableLayout( size );
		bigPanel.setLayout( layout );


		rows=0;
		JLabel label;

		JComponent jc;

		
		Iterator compI = activeComponents.iterator();
		while( compI.hasNext() )
		{
			Component comp = (Component)compI.next();
			bigPanel.add( comp, "0, "+rows+", 1, "+rows );
			++rows;
		}


		pauseBtn = new JButton("Pause");
		pauseBtn.addActionListener( new ActionListener()
				{
					public void actionPerformed( ActionEvent ae )
					{
						throttle.togglePaused();
					}
				});

		stopBtn = new JButton( "Stop" );
		stopBtn.addActionListener( new ActionListener()
				{
					public void actionPerformed( ActionEvent ae )
					{
						System.exit(0);
					}
				});
		jc = makeTwo( pauseBtn, stopBtn );
		bigPanel.add( jc, "0, "+rows+", 1, "+rows );
		++rows;


		return bigPanel;
	}


	private JButton showLessButton;
	private JButton showMoreButton;


	private JButton makeMoreButton()
	{
		if( showMoreButton != null )
			return showMoreButton;

		showMoreButton = makeChangeSizeButton(true);
		return showMoreButton;
	}

	private JButton makeLessButton()
	{
		if( showLessButton != null )
			return showLessButton;

		showLessButton = makeChangeSizeButton(false);
		return showLessButton;
	}

	private JButton makeChangeSizeButton(final boolean longform)
	{
		JButton jb = new JButton(longform?"Show more":"Show less");
		jb.addActionListener(new ActionListener()
				{
					public void actionPerformed( ActionEvent ae )
					{
						// 
						Component newC = makeLayout(longform);
						remove( mainComponent );
						mainComponent = newC;
						add( mainComponent );
					}
				});

		return jb;
	}

	/**
	 *
	 * */
	private JComponent makeTwo( String label, JComponent comb )
	{
		return makeTwo( new JLabel( label ), comb );
	}
	private JComponent makeTwo( final JComponent coma, final JComponent comb )
	{
		double columnSizes[] = {0.50, 0.50};
		double size2[][] =
			{ columnSizes, new double[]{TableLayout.PREFERRED} };
		TableLayout row = new TableLayout(size2);

		JPanel jp = new JPanel()
		{
			public void setEnabled( boolean bool )
			{
				super.setEnabled( bool );
				coma.setEnabled( bool );
				comb.setEnabled( bool );
			}
		};
		jp.setLayout( row );

		jp.add( coma, "0,0" );
		jp.add( comb, "1,0" );

		return jp;
	}

	private void initComponents()
	{

		// Bytes
		bytesLabel = new JLabel("-");

		// Human Readable Bytes
		kbytesLabel = new JLabel("-");

		// Rate label
		rateLabel = new JLabel("-");

		// Rate selection
		desiredRateSelect = new JRateSelect(throttle.getDesiredRate());
		desiredRateSelect.addActionListener( this );

		// The Window
		debug ("Making window" );
		if( showGraph )
		{
			windowLabel = new JHistoryBar(throttle.getHistoryWindow());
		}
		else
		{
			windowLabel = new JHistoryBar(null);
		}

		// The desired window
		desiredWindowSelect = new JWindowSelect((int)throttle.getDesiredWindow());
		desiredWindowSelect.addActionListener( this );

		// The desired buffer
		desiredBufferSelect = new JBufferSelect(blockin.getMaxSize());
		desiredBufferSelect.addActionListener( this );

		// Buffer size (kind of a joke actually)
		bufLabel = new JLabel("-");

		// Amount of data in buffer
		bufFillLabel = new JLabel("-");

		// Amount of data in buffer
		bufSecondFillLabel = new JLabel("-");

		// The biggest write performed in the window
		windowMaxReadLabel = new JLabel("-");

		// The time taken
		timeLabel = new JLabel("-");

		// Do we stay open?
		stayOpenBox = new JCheckBox( "Stay open after pipe closes", stayOpenDefault );
		stayOpenBox.addActionListener( this );
	}

	/**
	 * @return a list of components to use
	 * */
	private List makeComponents(boolean longform)
	{
		List l = new LinkedList();
		Component jc;
		JComponent jc2;

		jc2 = makeTwo( bytesLabel, kbytesLabel );
		jc = makeTwo("Bytes: ", jc2 );
		
		l.add(jc);

		jc = makeTwo("Rate: ", rateLabel );
		l.add(jc);

		jc = makeTwo("Desired rate: ", desiredRateSelect );
		l.add(jc);

		//l.add(Box.createGlue());

		if( longform )
		{
			jc = makeTwo("Window:", windowLabel);
			l.add(jc);

			jc = makeTwo("Max write:", windowMaxReadLabel );
			l.add(jc);

			jc = makeTwo("Desired Window:", desiredWindowSelect);
			l.add(jc);

			jc = makeTwo("Input Buffer Max:", bufLabel);
			l.add(jc);

			jc = makeTwo("Input Buffer Fill:", bufFillLabel);
			l.add(jc);

			jc = makeTwo("Throttle Buffer fill:", bufSecondFillLabel);
			l.add(jc);

			jc = makeTwo("Desired Input Buffer:", desiredBufferSelect);
			l.add(jc);
		}

		jc = makeTwo( "Time: ", timeLabel );
		l.add(jc);

		JPanel jp = new JPanel();
		if( longform )
			jp.add( makeLessButton() );
		else
			jp.add( makeMoreButton() );
		
		JPanel p = new JPanel();
		p.setLayout( new TableLayout(new double[][]{{TableLayout.FILL},{TableLayout.FILL}}) );
		p.add( stayOpenBox, "0,0,c,c");

		jc = makeTwo( p, jp );
		l.add( jc );
		
		jc = Box.createVerticalStrut(10);
		l.add(jc);

		return l;
	}

	public void actionPerformed( ActionEvent ae )
	{
		if( ae instanceof NewRateEvent )
		{
			NewRateEvent nre = (NewRateEvent)ae;

			throttle.setDesiredRate(nre.getNewRate());
		}
		else if( ae instanceof NewWindowEvent )
		{
			NewWindowEvent nwe = (NewWindowEvent)ae;
			int newW = nwe.getNewWindow(); 


			// Window has to be able to record at least one byte
			if( newW < throttle.getDesiredRate().getMSecPerByte() )
			{
				newW = (int)throttle.getDesiredRate().getMSecPerByte();
				desiredWindowSelect.setWindow( newW/1000 );
			}

			throttle.setDesiredWindow(newW);
			windowLabel.setMaximum( newW );
		}
		else if( ae instanceof NewBufferEvent )
		{
			NewBufferEvent nbe = (NewBufferEvent)ae;
			int newB = nbe.getNewBuffer();

			blockin.setMaxSize( newB );

			desiredBufferSelect.setBuffer( blockin.getMaxSize() );
		}
	}

	public void run()
	{
		throttle.start();
	}

	public static void debug(String msg)
	{
		Main.debug( msg );
	}

	public void stop()
	{
		System.exit(0);
	}

	private static void log(String msg)
	{
		Main.logError(msg);
	}
	
	private static final NumberFormat bytesFormat = new DecimalFormat("0.000");

	private String bytesToReadable( long b )
	{
		final double KB = 1024;
		final double MB = KB*KB;
		
		if( b < KB )
		{
			return "" + b + " bytes";
		}
		else if( b < MB)
		{
			return "" + bytesFormat.format(b/KB) + " kBytes";
		}
		else 
		{
			return "" + bytesFormat.format(b/MB) + " MBytes";
		}
	}
 
	private void updateLabels()
	{
		bufLabel.setText( "" + throttle.getActualBufSize() );
		rateLabel.setText( "" + throttle.getActualRate().toString() );
		bytesLabel.setText( "" + throttle.getTotalBytes() );
		kbytesLabel.setText( bytesToReadable( throttle.getTotalBytes() ) );
		windowLabel.setValue( (int)throttle.getActualWindow() );
		bufFillLabel.setText( "" + blockin.getFill() );
		bufSecondFillLabel.setText( "" + throttle.getSecondaryBufferFill() );
		windowMaxReadLabel.setText( "" + throttle.getMaxWindowRead() );
		
		windowLabel.repaint();

		// Time label
		long now = System.currentTimeMillis();
		long totalTime = throttle.getTotalTime();
		if( totalTime > 0 )
			timeLabel.setText( elapsedTimeToStr( totalTime ) );

		// Check if the stream is over
		if( throttle.isDone() )
		{
			if( !stayOpenBox.isSelected() )
				System.exit(0);

			stopBtn.setText("Quit");
			pauseBtn.setEnabled(false);

			Iterator it = activeComponents.iterator();
			while( it.hasNext() )
			{
				Component comp = (Component)it.next();
				comp.setEnabled( false );
			}

			//bufLabel.setEnabled(false);
			//rateLabel.setEnabled(false);
			//timeLabel.setEnabled(false);
			//bytesLabel.setEnabled(false);
			//windowLabel.setEnabled(false);
			//stayOpenBox.setEnabled(false);
			//desiredRateSelect.setEnabled(false);
			//windowMaxReadLabel.setEnabled(false);
			//desiredWindowSelect.setEnabled(false);
			windowLabel.setValue(Integer.MAX_VALUE);
		}
	}


	private String elapsedTimeToStr( long elapsed )
	{
		long seconds = elapsed / 1000;
		long minutes = seconds / 60;
		long hours = minutes / 60;
		long days = hours / 24;

		seconds = seconds % 60;
		minutes = minutes % 60;
		hours = hours % 24;

		StringBuffer sb = new StringBuffer(20);
		boolean div = false;
		div = appendTimeUnit( sb, days, " day", " days", div );
		div = appendTimeUnit( sb, hours, " hour", " hours", div );
		div = appendTimeUnit( sb, minutes, " minute", " minutes", div );
		div = appendTimeUnit( sb, seconds, " second", " seconds", div );

		return sb.toString();
	}

	private boolean appendTimeUnit( StringBuffer sb, long data, String unary, String plural, boolean prefixDelim )
	{
		if( data > 0 )
		{
			if( prefixDelim )
				sb.append( ", " );

			sb.append( "" + data );
			if( data > 1 )
				sb.append( plural );
			else
				sb.append( unary );
			return true;
		}
		else
			return prefixDelim;
	}
}

