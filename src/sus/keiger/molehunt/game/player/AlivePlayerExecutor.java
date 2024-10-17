package sus.keiger.molehunt.game.player;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.json.JSONComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.*;
import org.bukkit.util.Vector;
import sus.keiger.molehunt.MoleHuntPlugin;
import sus.keiger.molehunt.player.IServerPlayer;
import sus.keiger.molehunt.player.IServerPlayerCollection;
import sus.keiger.plugincommon.PCMath;
import sus.keiger.plugincommon.entity.EntityFunctions;
import sus.keiger.plugincommon.player.PlayerFunctions;
import sus.keiger.plugincommon.player.actionbar.ActionbarMessage;

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

    private void InGameTick()
    {
        ShowNearestPlayerDistance();
    }

    private void PostGameTick()
    {
        ResetFood();
    }

    private void SwitchToPreGameState()
    {
        _state = GamePlayerState.PreGame;
        GetPlayer().GetMCPlayer().setInvulnerable(true);
        ResetPlayer();
    }

    private void SwitchToInGameState()
    {
        _state = GamePlayerState.InGame;
        GetPlayer().GetMCPlayer().setInvulnerable(false);
        ResetPlayer();
    }

    private void SwitchToPostGameState()
    {
        _state = GamePlayerState.PostGame;
        GetPlayer().GetMCPlayer().setInvulnerable(true);
        ResetFood();
    }

    private double GetNearestPlayerDistance()
    {
        double NearestDistance = Double.POSITIVE_INFINITY;
        for (IGamePlayer Player : GetPlayer().GetGameInstance().GetPlayers())
        {
            if (Player == GetPlayer())
            {
                continue;
            }
            double Distance = Player.GetMCPlayer().getLocation().distance(GetPlayer().GetMCPlayer().getLocation());
            if (Distance < NearestDistance)
            {
                NearestDistance = Distance;
            }
        }

        if (Double.isInfinite(NearestDistance))
        {
            return 0d;
        }
        return NearestDistance;
    }

    private void ShowNearestPlayerDistance()
    {
        final long MESSAGE_ID = 67565838L;
        Component Message = Component.text("Nearest Player: %sm"
                .formatted(MoleHuntPlugin.GetNumberFormat("0.00").format(
                        GetNearestPlayerDistance()))).color(NamedTextColor.AQUA);
        GetPlayer().ShowActionbar(new ActionbarMessage(PCMath.TICKS_IN_SECOND, Message, MESSAGE_ID));
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
        if ((_state != GamePlayerState.InGame) && (event.getPlayer() == GetPlayer().GetMCPlayer()))
        {
            event.setCancelled(true);
            return;
        }

        event.deathMessage(null);
        GetPlayer().SetIsAlive(false);
    }

    @Override
    public void OnEntityDamageEvent(EntityDamageEvent event)
    {
        if ((_state != GamePlayerState.InGame) && (event.getEntity() == GetPlayer().GetMCPlayer()))
        {
            event.setCancelled(true);
            return;
        }

        if (!(event.getDamageSource().getCausingEntity() instanceof Player PlayerCause))
        {
            return;
        }

        IGamePlayer DamagerPlayer = GetPlayer().GetGameInstance()
                .GetGamePlayer(GetServerPlayers().GetPlayer(PlayerCause));

        if ((GetPlayer().GetMCPlayer() == event.getEntity()) && (DamagerPlayer != null)
                && !DamagerPlayer.GetMayDealDamage())
        {
            event.setCancelled(true);
        }
    }

    @Override
    public void OnAsyncChatEvent(AsyncChatEvent event)
    {
        if (event.getPlayer() != GetPlayer().GetMCPlayer())
        {
            return;
        }

        event.setCancelled(true);
        if (_state == GamePlayerState.InGame)
        {
            event.setCancelled(true);
            GetPlayer().SendMessage(Component.text("You may not chat in MoleHunt.").color(NamedTextColor.RED));
        }
        else
        {
            GetPlayer().GetGameInstance().SendMessage(Component.text("<%s> %s".formatted(event.getPlayer().getName(),
                    PlainTextComponentSerializer.plainText().serialize(event.originalMessage())))
                    .color(NamedTextColor.AQUA));
        }
    }

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