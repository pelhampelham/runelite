package net.runelite.client.plugins.bosstimer;

import net.runelite.api.Skill;
import net.runelite.client.config.Alpha;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("bossTimer")
public interface BossTimerConfig extends Config
{
	@ConfigItem(
		keyName = "bossTimerShowTimer",
		name = "Show timer",
		description = "Display a boss timer in the infobox",
		position = 1
	)
	default boolean showTimer()
	{
		return true;
	}

	@ConfigItem(
		keyName = "notifySpawn",
		name = "Notify boss spawn",
		description = "Send a notifcation upon boss spawn",
		position = 2
	)
	default boolean notifySpawn()
	{
		return false;
	}

	@ConfigItem(
		keyName = "notifySpawnWarning",
		name = "Notify boss spawn warning",
		description = "Send a notification X secionds before the boss spawn",
		position = 3
	)
	default boolean notifySpawnWarning()
	{
		return false;
	}

    @Range(min = 1, max = 60)
	@ConfigItem(
            keyName = "notifySpawnWarningSeconds",
            name = "Spawn warning seconds",
            description = "Seconds before the boss spawns to send a notfication",
            position = 4
        )
	default double warningSeconds() 
    {
		return 5;
	}
}
