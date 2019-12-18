/***************************************************************************
 *                   (C) Copyright 2003-2010 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.client.gui.achievements;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JTabbedPane;

import games.stendhal.client.gui.WindowUtils;
import games.stendhal.client.gui.layout.SBoxLayout;

/**
 * Dialog for achievements list.
 */
@SuppressWarnings("serial")
public class AchievementsDialog extends JDialog {
	/** Width of the window content. */
	private static final int PAGE_WIDTH = 450;
	/** Height of the window content. */
	private static final int PAGE_HEIGHT = 300;

	/** Category tabs. */
	private final JTabbedPane tabs = new JTabbedPane();
	/** Padding */
	private int pad = SBoxLayout.COMMON_PADDING;

	/**
	 * Create a new AchievementsDialog.
	 *
	 * @param parent parent window, or <code>null</code>
	 */
	public AchievementsDialog(Frame parent) {
		super(parent, "Lista osiągnięć");

		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setLayout(new SBoxLayout(SBoxLayout.VERTICAL, pad));
		setPreferredSize(new Dimension(PAGE_WIDTH, PAGE_HEIGHT));

		add(tabs);
		tabs.add("Zdobyto", new ReachedAchievements().getComponent());
		//tabs.add("Nie zdobyto", new NotObtainedAchievements().getComponent());
		tabs.setAlignmentX(LEFT_ALIGNMENT);
		setResizable(false);

		JButton closeButton = new JButton("Zamknij");
		closeButton.setAlignmentX(RIGHT_ALIGNMENT);
		closeButton.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(pad, pad, pad, pad),
				closeButton.getBorder()));
		closeButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				dispose();
			}
		});
		add(closeButton);

		WindowUtils.closeOnEscape(this);
		WindowUtils.watchFontSize(this);
		WindowUtils.trackLocation(this, "achievements", true);
		pack();
	}
}
