package net.f00f.javathrottle;
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


import java.awt.event.ContainerAdapter;
import java.awt.event.ContainerEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Properties;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import net.f00f.javathrottle.gui.GuiPanel;

/**
 * This class does the command line switches and starts the GUI.  The
 * throttling of the pipe is done in a Throttle implementation.
 *
 * @author Ben Damm
 * */

public class Main
{
	public static String KEY_desiredRate = "desiredRate";
	public static String KEY_persist = "persist";
	public static String KEY_showgraph = "showgraph";

	private Throttle throttle;

	private static String VER_project = "JavaThrottle";
	private static String VER_version = Version.VERSION + " " + Version.BUILD_DATE;
	private static String VER_author = "Ben Damm, bdamm@f00f.net";
	private static String VER_url = "http://dammfine.com/projects/javathrottle/";

	private static boolean DEBUG_enable = false;

	/**
	 * Entry point.
	 * */
	public static void main( String args[] )
	{
		// Parse cmd line
		final Properties p = parseCmdLine( args );

		// Error
		if( p == null )
		{
			usage();
			return;
		}

		// The window skeleton
		final JFrame jf = new JFrame("JavaThrottle");

		// When the user closes the window, kill the application.
		jf.addWindowListener(new WindowAdapter()
				{
					public void windowClosing( WindowEvent we )
					{
						System.exit(0);
					}
				});

		Runnable runner = new Runnable()
		{
			public void run()
			{
				// Create the main class
				GuiPanel mainPane = new GuiPanel(p);

				mainPane.addContainerListener(new ContainerAdapter()
						{
							public void componentAdded( ContainerEvent ce )
							{
								// Resize to fit the components
								jf.pack();
							}
						});

				// Add it
				jf.getContentPane().add( mainPane );
				jf.pack();
				jf.show();

				// Start the magic
				mainPane.run();
			}
		};

		try
		{
			SwingUtilities.invokeAndWait( runner );
		}
		catch( java.lang.reflect.InvocationTargetException ite )
		{
			// ??
		}
		catch( InterruptedException ie )
		{
			return;
		}
	}

	public static void usage()
	{
		log( "Usage: " );
		log( "   java -jar javathrottle.jar <options>" );
		log( "" );
		log( " Options:" );
		log( "  -b, -k, -m <number>" );
		log( "      Limit pipe in bits, kilobits, or megabits per second" );
		log( "  -B, -K, -M <number>" );
		log( "      Limit pipe in bytes, kilobytes, or megabytes per second" );
		log( "  -U" );
		log( "      Start with throttle wide open" );
		log( "  --persist" );
		log( "      Keep window open after pipe closes" );
		log( "" );
		log( "  --graph " );
		log( "      Show window graph (experimental)");
		log( "  --debug " );
		log( "      Enable debugging output");
		log( "  --version " );
		log( "      Show version number ");

	}

	public static void debug(String msg)
	{
		if( DEBUG_enable )
			System.err.println(msg);
	}

	public void stop()
	{
		System.exit(0);
	}

	// ------------ private methods --------------
	private static Properties parseCmdLine( String args[] )
	{
		Properties p = new Properties();

		Unit unit = null;
		double value = 1;
		boolean persist = false;
		boolean showGraph = false;

		for( int i = 0; i < args.length; ++i )
		{
			if( args[i].equals( "--version" ) )
			{
				log( VER_project + " " + VER_version );
				log( VER_author );
				log( VER_url );
				System.exit(0);
			}
			else if( args[i].equals( "--debug" ) )
			{
				DEBUG_enable = true;
				continue;
			}
			else if( args[i].equals( "--persist" ) )
			{
				persist = true;
				continue;
			}
			else if( args[i].equals( "--graph" ) )
			{
				showGraph = true;
				continue;
			}

			// Any argument must be in the form "-X"
			if( args[i].length() < 2 ) 
				return null;

			// If there are any flags, they must start with "-"
			if( args[i].charAt(0) != '-' )
				return null;

			char c = args[i].charAt(1);
			boolean readRate = false;
			if( readRate = c == 'b' )
				unit = Unit.bps;
			else if( readRate = c == 'k' )
				unit = Unit.kbps;
			else if ( readRate = c == 'm' )
				unit = Unit.Mbps;
			else if( readRate = c == 'B' )
				unit = Unit.Bps;
			else if( readRate = c == 'K' )
				unit = Unit.kBps;
			else if( readRate = c == 'M' )
				unit = Unit.MBps;
			else if( c == 'U' )
			{
				unit = Unit.unlimited;
				p.put( KEY_desiredRate, unit.makeRate( 1 ) );
			}
			else return null;

			if( readRate )
			{
				p.put( KEY_desiredRate, unit.makeRate( args[++i] ) );
			}
		}

		p.put( KEY_showgraph, new Boolean( showGraph ) );
		p.put( KEY_persist, new Boolean( persist ) );

		return p;
	}

	private static void log(String msg)
	{
		logError( msg );
	}

	public static void logError(String msg)
	{
		System.err.println(msg);
	}
}

