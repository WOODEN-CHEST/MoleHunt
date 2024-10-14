package sus.keiger.molehunt.game;

import sus.keiger.molehunt.event.IMoleHuntEventListener;
import sus.keiger.molehunt.player.IAudienceMemberHolder;
import sus.keiger.molehunt.player.IServerPlayer;
import sus.keiger.plugincommon.ITickable;

public interface IMoleHuntGameInstance extends ITickable, IMoleHuntEventListener, IAudienceMemberHolder
{
    boolean AddPlayer(IServerPlayer player);
    boolean RemovePlayer(IServerPlayer player);
    void ClearPlayers();
    boolean ContainsPlayer(IServerPlayer player);
    IGameTeam GetTeamOfPlayer(IServerPlayer player);
    boolean SwitchTeamOfPlayer(IServerPlayer player, IGameTeam newTeam);

    void Start();
    void End();
    void Cancel();

    MoleHuntGameState GetState();
}
