package sus.keiger.molehunt.game.player;

import io.papermc.paper.event.player.AsyncChatEvent;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.*;
import org.bukkit.util.Vector;
import sus.keiger.molehunt.player.IServerPlayerCollection;
import sus.keiger.plugincommon.entity.EntityFunctions;
import sus.keiger.plugincommon.player.PlayerFunctions;

public class AlivePlayerExecutor extends PlayerExecutorBase
{
    // Private fields.
    private GamePlayerState _state = GamePlayerState.PreGame;


    // Constructors.
    public AlivePlayerExecutor(IGamePlayer gamePlayer, IServerPlayerCollection playerCollection)
    {
        super(gamePlayer, playerCollection);
    }


    // Private methods.
    private void ResetPlayer()
    {
        Player MCPlayer = GetPlayer().GetMCPlayer();
        PlayerFunctions.ClearInventory(MCPlayer);

        MCPlayer.setGameMode(GameMode.SURVIVAL);
        MCPlayer.setInvulnerable(true);
        MCPlayer.setInvisible(false);
        MCPlayer.clearActivePotionEffects();
        PlayerFunctions.ResetAttributes(MCPlayer);
        MCPlayer.setFreezeTicks(0);
        MCPlayer.setFireTicks(0);
        MCPlayer.setVisualFire(false);
        MCPlayer.setLevel(0);
        MCPlayer.setExp(0);
        MCPlayer.setVelocity(new Vector());
        MCPlayer.setGlowing(false);
        MCPlayer.setFoodLevel(PlayerFunctions.MAX_FOOD);
        EntityFunctions.SetHealthPortion(MCPlayer, 1d);
        MCPlayer.setAllowFlight(false);
        MCPlayer.setFallDistance(0f);
        MCPlayer.setRemainingAir(MCPlayer.getMaximumAir());
        ResetFood();
    }

    private void TryCancelEventIfInPreGame(Cancellable event, Player player)
    {
        if ((_state == GamePlayerState.PreGame) &&
                (GetServerPlayers().GetPlayer(player) == GetPlayer().GetServerPlayer()))
        {
            event.setCancelled(true);
        }
    }

    private void TryCancelEventIfNotInGame(Cancellable event, Player player)
    {
        if ((_state != GamePlayerState.InGame) &&
                (GetServerPlayers().GetPlayer(player) == GetPlayer().GetServerPlayer()))
        {
            event.setCancelled(true);
        }
    }

    private void ResetFood()
    {
        GetPlayer().GetMCPlayer().setFoodLevel(PlayerFunctions.MAX_FOOD);
        GetPlayer().GetMCPlayer().setSaturation(PlayerFunctions.MAX_SATURATION);
    }

    private void PreGameTick()
    {
        ResetFood();
    }

    private void InGameTick() { }

    private void PostGameTick()
    {
        ResetFood();
    }

    private void SwitchToPreGameState()
    {
        ResetPlayer();
    }

    private void SwitchToInGameState()
    {
        ResetPlayer();
    }

    private void SwitchToPostGameState()
    {
        ResetFood();
        GetPlayer().GetMCPlayer().setInvulnerable(true);
    }


    // Inherited methods.
    @Override
    public void SwitchState(GamePlayerState state)
    {
        switch (state)
        {
            case PreGame -> SwitchToPreGameState();
            case InGame -> SwitchToInGameState();
            case PostGame -> SwitchToPostGameState();
        }
    }

    @Override
    public void OnPlayerDeathEvent(PlayerDeathEvent event)
    {
        TryCancelEventIfNotInGame(event, event.getPlayer());
    }

    @Override
    public void OnEntityDamageEvent(EntityDamageEvent event)
    {
        if (event.getEntity() instanceof Player PlayerEntity)
        {
            TryCancelEventIfNotInGame(event, PlayerEntity);
        }

        if ((event.getDamageSource().getCausingEntity() instanceof Player PlayerCause)
                && !GetPlayer().GetGameInstance().GetGamePlayer(GetServerPlayers().GetPlayer(PlayerCause))
                .GetMayDealDamage())
        {
            event.setCancelled(true);
        }
    }

    @Override
    public void OnAsyncChatEvent(AsyncChatEvent event) { }

    @Override
    public void OnBlockBreakEvent(BlockBreakEvent event)
    {
        TryCancelEventIfInPreGame(event, event.getPlayer());
    }

    @Override
    public void OnBlockPlaceEvent(BlockPlaceEvent event)
    {
        TryCancelEventIfInPreGame(event, event.getPlayer());
    }

    @Override
    public void OnPlayerInteractEvent(PlayerInteractEvent event)
    {
        TryCancelEventIfInPreGame(event, event.getPlayer());
    }

    @Override
    public void OnPlayerDropItemEvent(PlayerDropItemEvent event) { }

    @Override
    public void Tick()
    {
        switch (_state)
        {
            case PreGame -> PreGameTick();
            case InGame -> InGameTick();
            case PostGame -> PostGameTick();
            default -> throw new IllegalStateException("Invalid player state: %s".formatted(_state.toString()));
        }
    }
}