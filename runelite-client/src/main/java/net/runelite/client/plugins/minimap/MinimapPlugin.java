/*
 * Copyright (c) 2018, Dreyri <https://github.com/Dreyri>
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
package net.runelite.client.plugins.minimap;

import com.google.inject.Provides;
import java.awt.Color;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import javafx.util.Pair;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.SpritePixels;
import net.runelite.api.events.ConfigChanged;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.WidgetHiddenChanged;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

@PluginDescriptor(
	name = "Minimap",
	description = "Customize the color of minimap dots",
	tags = {"items", "npcs", "players"}
)
public class MinimapPlugin extends Plugin
{
	private static final int NUM_MAPDOTS = 6;

	@Inject
	private Client client;

	@Inject
	private ClientThread clientThread;

	@Inject
	private MinimapConfig config;

	private SpritePixels[] originalDotSprites;

	private final Map<WidgetInfo, Pair<Integer, Integer>> originalOrbPositions = new HashMap<>();

	private Pair<Integer, Integer> wikiOrbPosition;

	@Provides
	private MinimapConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(MinimapConfig.class);
	}

	@Override
	protected void startUp() throws Exception
	{
		updateMinimapWidgetVisibility(config.minimapMode());
		storeOriginalDots();
		replaceMapDots();
	}

	@Override
	protected void shutDown() throws Exception
	{
		updateMinimapWidgetVisibility(MinimapMode.SHOW);
		restoreOriginalDots();
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		if (event.getGameState() == GameState.LOGIN_SCREEN && originalDotSprites == null)
		{
			storeOriginalDots();
			replaceMapDots();
		}
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		if (!event.getGroup().equals("minimap"))
		{
			return;
		}

		if (event.getKey().equals("minimapMode"))
		{
			clientThread.invokeLater(() -> updateMinimapWidgetVisibility(config.minimapMode()));
			return;
		}

		replaceMapDots();
	}

	@Subscribe
	public void onWidgetHiddenChanged(WidgetHiddenChanged event)
	{
		clientThread.invokeLater(() -> updateMinimapWidgetVisibility(config.minimapMode()));
	}

	private void updateMinimapWidgetVisibility(MinimapMode mode)
	{
		if (mode == MinimapMode.SHOW)
		{
			Widget fullMap = client.getWidget(WidgetInfo.RESIZABLE_MINIMAP_WIDGET);
			if (fullMap != null)
			{
				for (Widget widget : fullMap.getStaticChildren())
				{
					widget.setHidden(false);
				}
			}
			fullMap = client.getWidget(WidgetInfo.RESIZABLE_MINIMAP_STONES_WIDGET);
			if (fullMap != null)
			{
				for (Widget widget : fullMap.getStaticChildren())
				{
					widget.setHidden(false);
				}
			}

			for (MinimapOrbs orb : MinimapOrbs.values())
			{
				Widget orbWidget = client.getWidget(orb.getId());
				Pair<Integer, Integer> originalPosition = originalOrbPositions.get(orb.getId());
				if (orbWidget != null && originalPosition != null)
				{
					orbWidget.setOriginalX(originalPosition.getKey());
					orbWidget.setOriginalY(originalPosition.getValue());
					orbWidget.revalidate();
				}
			}

			Widget wikiOrb = client.getWidget(160, 0);

			if (wikiOrb != null && wikiOrbPosition != null)
			{
				wikiOrb = wikiOrb.getDynamicChildren()[0];
				wikiOrb.setOriginalX(wikiOrbPosition.getKey());
				wikiOrb.setOriginalY(wikiOrbPosition.getValue());
				wikiOrb.revalidate();
			}
		}
		else
		{
			//Single Line
			Widget deco = client.getWidget(WidgetInfo.RESIZABLE_MINIMAP_DECORATIONS);
			if (deco != null)
			{
				deco.setHidden(true);
			}
			Widget map = client.getWidget(WidgetInfo.RESIZABLE_MINIMAP_DRAW_AREA);
			if (map != null)
			{
				map.setHidden(true);
			}
			Widget compass = client.getWidget(164, 24);
			if (compass != null)
			{
				compass.setHidden(true);
			}
			//Fixed-Like
			deco = client.getWidget(WidgetInfo.RESIZABLE_MINIMAP_STONES_DECORATIONS);
			if (deco != null)
			{
				deco.setHidden(true);
			}
			map = client.getWidget(WidgetInfo.RESIZABLE_MINIMAP_STONES_DRAW_AREA);
			if (map != null)
			{
				map.setHidden(true);
			}
			compass = client.getWidget(161, 24);
			if (compass != null)
			{
				compass.setHidden(true);
			}

			if (mode == MinimapMode.HIDE)
			{
				Widget orbs = client.getWidget(WidgetInfo.RESIZABLE_MINIMAP_ORB_HOLDER);
				if (orbs != null)
				{
					orbs.setHidden(true);
				}
				orbs = client.getWidget(WidgetInfo.RESIZABLE_MINIMAP_STONES_ORB_HOLDER);
				if (orbs != null)
				{
					orbs.setHidden(true);
				}
			}
			else
			{
				Widget orbs = client.getWidget(WidgetInfo.RESIZABLE_MINIMAP_ORB_HOLDER);
				if (orbs != null)
				{
					orbs.setHidden(false);
				}
				orbs = client.getWidget(WidgetInfo.RESIZABLE_MINIMAP_STONES_ORB_HOLDER);
				if (orbs != null)
				{
					orbs.setHidden(false);
				}

				for (MinimapOrbs orb : MinimapOrbs.values())
				{
					Widget orbWidget = client.getWidget(orb.getId());
					if (orbWidget != null)
					{
						if (originalOrbPositions.get(orb.getId()) == null)
						{
							originalOrbPositions.put(orb.getId(), new Pair<>(orbWidget.getOriginalX(), orbWidget.getOriginalY()));
						}
						orbWidget.setOriginalX(orb.getX());
						orbWidget.setOriginalY(orb.getY());
						orbWidget.revalidate();
					}
				}

				Widget wikiOrb = client.getWidget(160, 0);

				if (wikiOrb != null)
				{
					wikiOrb = wikiOrb.getDynamicChildren()[0];
					if (wikiOrbPosition == null)
					{
						wikiOrbPosition = new Pair<>(wikiOrb.getOriginalX(), wikiOrb.getOriginalY());
					}
					wikiOrb.setOriginalX(90);
					wikiOrb.setOriginalY(70);
					wikiOrb.revalidate();
				}
			}
		}
		//Minimap changes depending on the type of the stones (Fixed-like vs single line)

		//RESIZEABLE_MINIMAP_DRAW_AREA
		//RESIZEABLE MINIMAP DECORATIONS
		//Compass (164/161.24)

		//Wiki 90,70
	}

	private void replaceMapDots()
	{
		SpritePixels[] mapDots = client.getMapDots();

		if (mapDots == null)
		{
			return;
		}

		Color[] minimapDotColors = getColors();
		for (int i = 0; i < mapDots.length && i < minimapDotColors.length; ++i)
		{
			mapDots[i] = MinimapDot.create(this.client, minimapDotColors[i]);
		}
	}

	private Color[] getColors()
	{
		Color[] colors = new Color[NUM_MAPDOTS];
		colors[0] = config.itemColor();
		colors[1] = config.npcColor();
		colors[2] = config.playerColor();
		colors[3] = config.friendColor();
		colors[4] = config.teamColor();
		colors[5] = config.clanColor();
		return colors;
	}

	private void storeOriginalDots()
	{
		SpritePixels[] originalDots = client.getMapDots();

		if (originalDots == null)
		{
			return;
		}

		originalDotSprites = Arrays.copyOf(originalDots, originalDots.length);
	}

	private void restoreOriginalDots()
	{
		SpritePixels[] mapDots = client.getMapDots();

		if (originalDotSprites == null || mapDots == null)
		{
			return;
		}

		System.arraycopy(originalDotSprites, 0, mapDots, 0, mapDots.length);
	}
}