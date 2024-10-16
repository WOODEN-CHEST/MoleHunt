package sus.keiger.molehunt;

import org.bukkit.World;

import java.util.List;


public interface IWorldProvider
{
    World GetOverworld();

    World GetNether();

    World GetTheEnd();

    List<World> GetWorlds();
}