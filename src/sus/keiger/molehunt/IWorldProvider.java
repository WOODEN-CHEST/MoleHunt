package sus.keiger.molehunt;

import org.bukkit.World;


public interface IWorldProvider
{
    World GetOverworld();

    World GetNether();

    World GetTheEnd();
}