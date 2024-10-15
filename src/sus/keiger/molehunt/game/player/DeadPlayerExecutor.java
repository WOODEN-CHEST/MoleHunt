package sus.keiger.molehunt.game.player;

import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import sus.keiger.molehunt.player.IServerPlayerCollection;

public class DeadPlayerExecutor extends PlayerExecutorBase
{
    // Constructors.
    public DeadPlayerExecutor(IGamePlayer gamePlayer, IServerPlayerCollection playerCollection)
    {
        super(gamePlayer, playerCollection);
    }


    // Inherited methods.
    @Override
    public void SwitchState(GamePlayerState state)
    {

    }

    @Override
    public void OnPlayerDeathEvent(PlayerDeathEvent event)
    {

    }

    @Override
    public void OnEntityDamageEvent(EntityDamageEvent event)
    {

    }

    @Override
    public void Tick()
    {

    }
}