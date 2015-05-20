package net.f00f.javathrottle.gui;

import javax.swing.*;
import javax.swing.text.*;
import java.util.*;
import java.text.*;

/**
 * This class represents a text field that only accepts
 * non-negative integers for input.  Any rejected input is
 * not echoed.
 *
 * @author Quy Nguyen
 */
public class JIntegerField extends JTextField
{
	private NumberFormat integerFormat;

	public JIntegerField(int value, int length)
	{
		super(length);
		integerFormat = NumberFormat.getNumberInstance(Locale.US);
		integerFormat.setParseIntegerOnly(true);
		setValue(value);
	}

	public void setValue(int value)
	{
		setText(integerFormat.format(value));
	}

	public int getValue()
	{
		try
		{
			return integerFormat.parse(getText()).intValue();
		}
		catch (ParseException e)
		{
			return 0;
		}
	}

	protected Document createDefaultModel()
	{
		/**
		 * A document model that override's PlainDocument's
		 * insertString() so that it only accepts digit characters
		 * from the inputted string.
		 */
		Document integerDocument = new PlainDocument()
		{
			public void insertString(int offs, String str, AttributeSet a)
			throws BadLocationException
			{
				StringBuffer out = new StringBuffer();

				// strip filter out non-digit characters
				for (int i = 0; i < str.length(); i++)
				{
					char c = str.charAt(i);

					if(Character.isDigit(c))
					{
						out.append(c);
					}
				}

				// call the parent method passing in the new string.
				super.insertString(offs, out.toString(), a);
			}
		};

		return integerDocument;
	}
}
