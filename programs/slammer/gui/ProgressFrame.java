/* This file is in the public domain. */

package slammer.gui;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import slammer.*;

public class ProgressFrame extends JFrame implements ActionListener
{
	JProgressBar b = new JProgressBar();
	JButton stop = new JButton("Cancel");

	int status = 0;

	boolean pressed = false;

	public ProgressFrame(int count)
	{
		super("Progress...");

		stop.setActionCommand("stop");
		stop.addActionListener(this);

		b.setStringPainted(true);
		b.setMinimum(0);
		b.setMaximum(count);
		b.setValue(0);
		b.setSize(400, 75);

		setContentPane(createContentPane());

		setSize(400,100);
		setLocationRelativeTo(null);

		setVisible(true);

		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e)
			{
				pressed = true;
			}
		});
	}

	public JPanel createContentPane()
	{
		GridBagLayout gridbag = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();

		JPanel p = new JPanel();

		p.setLayout(gridbag);

		c.gridx = 0;
		c.gridy = 0;
		c.weighty = 2;
		c.weightx = 1;
		c.fill = GridBagConstraints.BOTH;
		gridbag.setConstraints(b, c);
		p.add(b);

		c.gridy = 1;
		c.weighty = 1;
		c.fill = GridBagConstraints.VERTICAL;
		gridbag.setConstraints(stop, c);
		p.add(stop);

		return p;
	}

	public void setMaximum(int i)
	{
		b.setMaximum(i);
	}

	public boolean increment(String s)
	{
		status++;
		return update(status, s);
	}

	public void update(String s)
	{
		b.setString(s);
	}

	public boolean update(int i)
	{
		return update(i, null);
	}

	public boolean update(int i, String s)
	{
		status = i;
		b.setValue(i);
		int max;

		if(s != null)
		{
			max = b.getMaximum();
			if(max > 0 && max < Integer.MAX_VALUE)
				s = s + " (" + status + "/" + b.getMaximum() + ")";

			b.setString(s);
		}

		return pressed;
	}

	public boolean isCanceled()
	{
		return pressed;
	}

	public void actionPerformed(java.awt.event.ActionEvent e)
	{
		try
		{
			String command = e.getActionCommand();
			if(command.equals("stop"))
			{
				pressed = true;
			}
		}
		catch (Throwable ex)
		{
			Utils.catchException(ex);
		}
	}
}
