package sus.keiger.molehunt.game.player;

import org.bukkit.entity.Player;
import sus.keiger.molehunt.event.IMoleHuntEventListener;
import sus.keiger.molehunt.game.IGameTeam;
import sus.keiger.molehunt.player.IAudienceMember;
import sus.keiger.molehunt.player.IServerPlayer;
import sus.keiger.plugincommon.ITickable;

public interface IGamePlayer extends  IModifiablePlayerStats, ITickable, IMoleHuntEventListener, IAudienceMember
{
    Player GetMCPlayer();
    IServerPlayer GetServerPlayer();
    boolean IsAlive();
    void SetIsAlive(boolean value);
    IGameTeam GetTeam();
    void SetTeam(IGameTeam team);
    GamePlayerState GetTargetState();
    void SetTargetState(GamePlayerState state);
}