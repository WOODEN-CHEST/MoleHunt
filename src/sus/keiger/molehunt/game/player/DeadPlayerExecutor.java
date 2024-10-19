package sus.keiger.molehunt.game.player;

import io.papermc.paper.event.player.AsyncChatEvent;
import org.bukkit.FluidCollisionMode;
import org.bukkit.GameMode;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.*;
import org.bukkit.util.RayTraceResult;
import sus.keiger.molehunt.player.IServerPlayerCollection;

import java.util.Objects;

public class DeadPlayerExecutor extends PlayerExecutorBase
{
    // Private fields.
    private GamePlayerState _state = GamePlayerState.PreGame;
    private double MAX_RAY_TRACE_DISTANCE = 64d;


    // Constructors.
    public DeadPlayerExecutor(IGamePlayer gamePlayer,
                              IServerPlayerCollection playerCollection)
    {
        super(gamePlayer, playerCollection);
    }

    // Inherited methods.
    @Override
    public void SwitchState(GamePlayerState state)
    {
        _state = Objects.requireNonNull(state, "state is null");
        GetPlayer().GetMaxHealth().ClearModifiers();
        GetPlayer().GetMiningSpeed().ClearModifiers();
        GetPlayer().GetAttackSpeed().ClearModifiers();
        GetPlayer().GetEntityReach().ClearModifiers();
        GetPlayer().GetMCPlayer().setGameMode(GameMode.SPECTATOR);
    }

    @Override
    public void OnPlayerDeathEvent(PlayerDeathEvent event)
    {
        if (event.getPlayer() == GetPlayer().GetMCPlayer())
        {
            event.setCancelled(true);
        }
    }

    @Override
    public void OnEntityDamageEvent(EntityDamageEvent event)
    {
        if (event.getEntity() == GetPlayer().GetMCPlayer())
        {
            event.setCancelled(true);
        }
    }

    @Override
    public void OnAsyncChatEvent(AsyncChatEvent event) { }

    @Override
    public void OnBlockBreakEvent(BlockBreakEvent event)
    {
        if (event.getPlayer() == GetPlayer().GetMCPlayer())
        {
            event.setCancelled(true);
        }
    }

    @Override
    public void OnBlockPlaceEvent(BlockPlaceEvent event)
    {
        if (event.getPlayer() == GetPlayer().GetMCPlayer())
        {
            event.setCancelled(true);
        }
    }

    @Override
    public void OnPlayerInteractEvent(PlayerInteractEvent event) { }
    @Override
    public void OnPlayerDropItemEvent(PlayerDropItemEvent event)
    {
        if (event.getPlayer() == GetPlayer().GetMCPlayer())
        {
            event.setCancelled(true);
        }
    }

    @Override
    public void Tick() { }
}