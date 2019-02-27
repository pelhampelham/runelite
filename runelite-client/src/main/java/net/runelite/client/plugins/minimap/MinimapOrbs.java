package net.runelite.client.plugins.minimap;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.runelite.api.widgets.WidgetInfo;

@RequiredArgsConstructor
public enum MinimapOrbs
{

	HP(WidgetInfo.MINIMAP_HEALTH_ORB, 105, 0),
	RUN(WidgetInfo.MINIMAP_RUN_ORB, 43, 0),
	PRAY(WidgetInfo.MINIMAP_PRAYER_ORB, 143, 30),
	SPEC(WidgetInfo.MINIMAP_SPEC_ORB, 143, 67),
	WORLD(WidgetInfo.MINIMAP_WORLDMAP_ORB, 105, 35),
	XP(WidgetInfo.MINIMAP_XP_ORB, 70, 35);

	@Getter
	private final WidgetInfo id;

	@Getter
	private final int x;

	@Getter
	private final int y;
}