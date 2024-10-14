package sus.keiger.molehunt.player;

import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

public interface IServerPlayerCollection extends IAudienceMemberHolder
{
    IServerPlayer GetPlayer(Player mcPlayer);
    IServerPlayer GetPlayer(UUID uuid);
    IServerPlayer GetPlayer(String name);
    List<IServerPlayer> GetPlayers();
    void AddPlayer(IServerPlayer player);
    void RemovePlayer(IServerPlayer player);
    void ClearPlayers();
    boolean ContainsPlayer(IServerPlayer player);
}
