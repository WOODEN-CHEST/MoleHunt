package sus.keiger.molehunt.game;

import org.bukkit.Color;
import sus.keiger.molehunt.player.IAudienceMemberHolder;
import sus.keiger.molehunt.player.IServerPlayer;

import java.util.List;

public interface IGameTeam extends IAudienceMemberHolder
{
    GameTeamType GetType();
    boolean AddPlayer(IServerPlayer player);
    boolean RemovePlayer(IServerPlayer player);
    void ClearPlayers();
    List<IServerPlayer> GetPlayers();
    boolean ContainsPlayer(IServerPlayer player);
    Color GetColor();
    String GetName();
}