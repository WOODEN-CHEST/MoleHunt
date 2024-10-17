package sus.keiger.molehunt;

import org.bukkit.GameRule;
import org.bukkit.World;

public class DefaultWorldInitializer implements IWorldInitializer
{
    // Inherited methods.
    @Override
    public void Initialize(World world)
    {
        world.setTime(6000L);
        world.setStorm(false);
        world.setThundering(false);

        world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, true);
        world.setGameRule(GameRule.DO_WEATHER_CYCLE, true);
    }
}