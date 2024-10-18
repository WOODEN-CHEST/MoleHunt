package sus.keiger.molehunt.game.player;

import org.bukkit.entity.Player;
import sus.keiger.molehunt.event.IMoleHuntEventListener;
import sus.keiger.molehunt.player.IAudienceMember;
import sus.keiger.molehunt.player.IServerPlayer;
import sus.keiger.plugincommon.ITickable;
import sus.keiger.plugincommon.PCPluginEvent;
import sus.keiger.plugincommon.value.GameModifiableValue;

public interface IGamePlayer extends  IModifiablePlayerStats, ITickable, IMoleHuntEventListener, IAudienceMember
{
    Player GetMCPlayer();
    IServerPlayer GetServerPlayer();

    boolean IsAlive();
    void SetIsAlive(boolean value);

    GamePlayerState GetTargetState();
    void SetTargetState(GamePlayerState state);

    boolean GetMayDealDamage();
    void SetMayDealDamage(boolean value);

    GameModifiableValue GetMaxHealth();
    GameModifiableValue GetMiningSpeed();
    GameModifiableValue GetEntityReach();
    GameModifiableValue GetAttackSpeed();

    PCPluginEvent<GamePlayerLifeChangeEvent> GetLifeChangeEvent();
}