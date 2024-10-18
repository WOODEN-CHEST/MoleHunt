package sus.keiger.molehunt.game;

import org.bukkit.GameRule;
import org.bukkit.World;
import org.bukkit.WorldBorder;

public class GameWorldInitializer
{
    // Methods.
    public void InitializeWorldNotInGame(World world)
    {
        double DefaultBorderSize = 15_000_000d;
        WorldBorder Border = world.getWorldBorder();
        Border.setSize(DefaultBorderSize);

        world.setTime(6000L);
        world.setStorm(false);
        world.setThundering(false);

        world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
        world.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
        world.setGameRule(GameRule.DO_IMMEDIATE_RESPAWN, false);
    }

    public void InitializeWorldInGame(World world, int worldBorderSize)
    {
        WorldBorder Border = world.getWorldBorder();
        Border.setSize(worldBorderSize);

        world.setTime(0L);
        world.setStorm(false);
        world.setThundering(false);

        world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, true);
        world.setGameRule(GameRule.DO_WEATHER_CYCLE, true);
        world.setGameRule(GameRule.DO_IMMEDIATE_RESPAWN, true);
    }
}