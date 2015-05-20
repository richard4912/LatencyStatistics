/*
 * Created on 2004-03-03
 *
 */
package net.f00f.javathrottle.gui;

import net.f00f.javathrottle.HistoryWindow;
import net.f00f.javathrottle.DataStamp;
import javax.swing.JProgressBar;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.List;
import java.util.Iterator;
import java.awt.Color;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
//import java.awt.Rectangle;

/**
 * @author Ben Damm
 *
 */
public class JHistoryBar extends JProgressBar
{
	private HistoryWindow window;

	/**
	 * 
	 */
	public JHistoryBar( HistoryWindow window )
	{
		super();
		this.window = window;
	}

	private void paintGraph( Graphics g1 )
	{
		Graphics2D g2 = (Graphics2D)g1;



		g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);


		
		// Draw the data blocks
		List data = window.getWindowCopy();
		long msecl = window.getDesiredWindow();
		long bigsend = window.getWindowMaxBlock();
		double pixpermsec = this.getWidth()/(double)msecl;
		double pixperbyte = this.getHeight()/(double)bigsend;
		
		
		double entrywidth=pixpermsec*100;// 100 msec wide
		
		Iterator i = data.iterator();
		
		g2.setColor( Color.blue );
		
		int a = 0;
		long first = System.currentTimeMillis()-msecl;
		while( i.hasNext() )
		{
			Object o = i.next();
			DataStamp ds = (DataStamp)o;
			//if( first < 0 )
			//first = ds.getTime();
			
			++a;
			
			float x = (float)(pixpermsec*(ds.getTime() - first));
			
			//int x = (int)(a*entrywidth);
			
			int base = this.getHeight();
			int height = (int)(ds.getSize() * pixperbyte);
			
			Rectangle2D r = new Rectangle2D.Double(x, base-height, entrywidth, (float)height );
			g2.fill(r);
		}
		
		
		//g1.drawLine( 0, 0, this.getWidth(), this.getHeight() );
	}


	public void paint( Graphics g1 )
	{
		super.paint( g1 );
		if( window != null )
			paintGraph( g1 );
		
		super.paintBorder( g1 );
	}
}
