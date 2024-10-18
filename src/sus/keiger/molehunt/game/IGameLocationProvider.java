package sus.keiger.molehunt.game;

import org.bukkit.Location;
import sus.keiger.molehunt.IWorldProvider;


public interface IGameLocationProvider extends IWorldProvider
{
    Location GetCenterLocation();
    Location GetRandomCenterLocation();
}