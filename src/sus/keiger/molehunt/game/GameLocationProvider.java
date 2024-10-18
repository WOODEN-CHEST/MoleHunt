package sus.keiger.molehunt.game;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.Vector;
import sus.keiger.molehunt.IWorldProvider;

import java.util.List;
import java.util.Objects;
import java.util.Random;

public class GameLocationProvider implements IGameLocationProvider
{
    // Private fields.
    private final IWorldProvider _worldProvider;
    private final int CENTER_X = 0;
    private final int CENTER_Z = 0;
    private final int PLAYER_SPREAD = 25;


    // Constructors.
    public GameLocationProvider(IWorldProvider worldProvider)
    {
        _worldProvider = Objects.requireNonNull(worldProvider, "worldProvider is null");
    }


    // Inherited methods.
    @Override
    public Location GetCenterLocation()
    {
        return new Location(GetOverworld(), CENTER_X,
                _worldProvider.GetOverworld().getHighestBlockYAt(CENTER_X, CENTER_Z), CENTER_Z, 0f, 0f);
    }

    @Override
    public Location GetRandomCenterLocation()
    {
        Location Center = GetCenterLocation();

        Random RNG = new Random();
        Center.add(new Vector((RNG.nextDouble() - 0.5d * 2d) * PLAYER_SPREAD,
                0d,
                (RNG.nextDouble() - 0.5d * 2d) * PLAYER_SPREAD));
        Center.setY(Center.getWorld().getHighestBlockYAt(Center.getBlockX(), Center.getBlockZ()));

        return Center;
    }

    @Override
    public World GetOverworld()
    {
        return _worldProvider.GetOverworld();
    }

    @Override
    public World GetNether()
    {
        return _worldProvider.GetNether();
    }

    @Override
    public World GetTheEnd()
    {
        return _worldProvider.GetTheEnd();
    }

    @Override
    public List<World> GetWorlds()
    {
        return _worldProvider.GetWorlds();
    }
}