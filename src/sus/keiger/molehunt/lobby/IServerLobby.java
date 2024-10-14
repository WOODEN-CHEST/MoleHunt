package sus.keiger.molehunt.lobby;

import sus.keiger.molehunt.event.IMoleHuntEventListener;
import sus.keiger.molehunt.player.IAudienceMemberHolder;
import sus.keiger.molehunt.player.IServerPlayer;
import sus.keiger.plugincommon.ITickable;

import java.util.List;

public interface IServerLobby extends ITickable, IMoleHuntEventListener, IAudienceMemberHolder
{
    List<IServerPlayer> GetPlayers();
    boolean AddPlayer(IServerPlayer player);
    boolean RemovePlayer(IServerPlayer player);
    boolean ContainsPlayer(IServerPlayer player);
}