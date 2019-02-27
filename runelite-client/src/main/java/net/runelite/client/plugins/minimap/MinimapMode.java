package net.runelite.client.plugins.minimap;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MinimapMode
{
    SHOW("Show"),
    ORBS("Keep Orbs"),
    HIDE("Hide");

    private final String name;

    public String getName()
    {
        return name;
    }
}
