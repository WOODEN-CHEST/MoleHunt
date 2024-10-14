package sus.keiger.molehunt.game;

import sus.keiger.molehunt.player.IAudienceMemberHolder;
import sus.keiger.molehunt.player.IServerPlayer;

public interface IGameTeam extends IAudienceMemberHolder
{
    GameTeamType GetType();
    boolean AddPlayer(IServerPlayer player);
    boolean RemovePlayer(IServerPlayer player);
    void ClearPlayers();
    boolean ContainsPlayer(IServerPlayer player);
}