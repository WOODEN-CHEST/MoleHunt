package sus.keiger.molehunt.game;

import org.bukkit.Color;
import sus.keiger.molehunt.game.player.IGamePlayer;
import sus.keiger.molehunt.player.IAudienceMemberHolder;
import sus.keiger.molehunt.player.IServerPlayer;

import java.util.List;

public interface IGameTeam extends IAudienceMemberHolder
{
    GameTeamType GetType();
    boolean AddPlayer(IGamePlayer player);
    boolean RemovePlayer(IGamePlayer player);
    void ClearPlayers();
    List<IGamePlayer> GetPlayers();
    boolean ContainsPlayer(IGamePlayer player);
    int GetPlayerCount();
    Color GetColor();
    String GetName();
}