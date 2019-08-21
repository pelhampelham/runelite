/*
 * Copyright (c) 2018 Abex
 * Copyright (c) 2018, Psikoi <https://github.com/psikoi>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package net.runelite.client.plugins.timetracking;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Collection;
import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Constants;
import net.runelite.client.plugins.timetracking.farming.FarmingPatch;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.components.ThinProgressBar;
import net.runelite.client.ui.components.shadowlabel.JShadowedLabel;
import net.runelite.client.util.ImageUtil;

@Getter
public class TimeablePanel<T> extends JPanel
{
	private static final ImageIcon NOTIFY_ICON_ON;
	private static final ImageIcon NOTIFY_ICON_OFF;

	private final T timeable;
	private final JLabel icon = new JLabel();
	private final JLabel estimate = new JLabel();
	private final JLabel notifyLabel = new JLabel();
	private final ThinProgressBar progress = new ThinProgressBar();
	private boolean notify = false;

	static
	{
		NOTIFY_ICON_ON = new ImageIcon(ImageUtil.getResourceStreamFromClass(TimeTrackingPlugin.class, "notify_on.png"));
		NOTIFY_ICON_OFF = new ImageIcon(ImageUtil.getResourceStreamFromClass(TimeTrackingPlugin.class, "notify_off.png"));
	}

	public TimeablePanel(T timeable, String title, int maximumProgressValue)
	{
		this.timeable = timeable;

		setLayout(new BorderLayout());
		setBorder(new EmptyBorder(7, 0, 0, 0));

		JPanel topContainer = new JPanel();
		topContainer.setBorder(new EmptyBorder(7, 7, 6, 0));
		topContainer.setLayout(new BorderLayout());
		topContainer.setBackground(ColorScheme.DARKER_GRAY_COLOR);

		icon.setMinimumSize(new Dimension(Constants.ITEM_SPRITE_WIDTH, Constants.ITEM_SPRITE_HEIGHT));

		JPanel infoPanel = new JPanel();
		infoPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		infoPanel.setLayout(new GridLayout(2, 1));
		infoPanel.setBorder(new EmptyBorder(4, 4, 4, 0));

		final JLabel location = new JShadowedLabel(title);
		location.setFont(FontManager.getRunescapeSmallFont());
		location.setForeground(Color.WHITE);

		estimate.setFont(FontManager.getRunescapeSmallFont());
		estimate.setForeground(Color.GRAY);

		notifyLabel.setIcon(NOTIFY_ICON_OFF);
		notifyLabel.setBorder(new EmptyBorder(2, 0, 0, 2));

		infoPanel.add(location);
		infoPanel.add(estimate);

		final JMenuItem notifyMenuItem = new JMenuItem("Notify");
		notifyMenuItem.addActionListener(e -> setNotify(!notify));
		addLabelPopupMenu(this, notifyMenuItem);


		topContainer.add(icon, BorderLayout.WEST);
		topContainer.add(infoPanel, BorderLayout.CENTER);
		topContainer.add(notifyLabel, BorderLayout.LINE_END);

		progress.setValue(0);
		progress.setMaximumValue(maximumProgressValue);

		add(topContainer, BorderLayout.NORTH);
		add(progress, BorderLayout.SOUTH);
	}

	void setNotify(boolean bool)
	{
		notify = bool;
		notifyLabel.setIcon(bool ? NOTIFY_ICON_ON : NOTIFY_ICON_OFF);
		repaint();
		if (timeable instanceof FarmingPatch)
		{
			((FarmingPatch) timeable).setNotify(bool);
		}
	}

	private void addLabelPopupMenu(final JPanel panel, final JMenuItem menuItem)
	{
		final JPopupMenu menu = new JPopupMenu();
		menu.setBorder(new EmptyBorder(5, 5, 5, 5));

		menu.add(menuItem);

		panel.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseClicked(MouseEvent mouseEvent)
			{
				Component source = (Component) mouseEvent.getSource();
				Point location = MouseInfo.getPointerInfo().getLocation();
				SwingUtilities.convertPointFromScreen(location, source);
				menu.show(source, location.x, location.y);
			}
		});
	}

}
