package sus.keiger.molehunt.game.player;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.GameMode;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.*;
import org.bukkit.util.Vector;
import sus.keiger.molehunt.MoleHuntPlugin;
import sus.keiger.molehunt.game.IGameServices;
import sus.keiger.plugincommon.PCMath;
import sus.keiger.plugincommon.entity.EntityFunctions;
import sus.keiger.plugincommon.player.PlayerFunctions;
import sus.keiger.plugincommon.player.actionbar.ActionbarMessage;

import java.util.Objects;

public class AlivePlayerExecutor extends PlayerExecutorBase
{
    // Private fields.
    private GamePlayerState _state = GamePlayerState.PreGame;
    private final IGameServices _gameServices;
    private final int NEAREST_PLAYER_ACTIONBAR_DURATION_TICKS = PCMath.SecondsToTicks(3d);


    // Constructors.
    public AlivePlayerExecutor(IGamePlayer gamePlayer, IGameServices services)
    {
        super(gamePlayer, services);
        _gameServices = Objects.requireNonNull(services, "services is null");
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
        UpdateAttributes();
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
        UpdateAttributes();
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
        for (IGamePlayer Player : _gameServices.GetGamePlayerCollection().GetPlayers())
        {
            if (Player == GetPlayer() || !Player.IsAlive())
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
                .formatted(MoleHuntPlugin.GetNumberFormat("0").format(
                        GetNearestPlayerDistance()))).color(NamedTextColor.AQUA);
        GetPlayer().ShowActionbar(new ActionbarMessage(
                NEAREST_PLAYER_ACTIONBAR_DURATION_TICKS, Message, MESSAGE_ID));
    }

    private void UpdateAttributes()
    {
        EntityFunctions.TrySetAttributeBaseValue(GetPlayer().GetMCPlayer(), Attribute.GENERIC_MAX_HEALTH,
                GetPlayer().GetMaxHealth().GetValue());
        EntityFunctions.TrySetAttributeBaseValue(GetPlayer().GetMCPlayer(), Attribute.GENERIC_ATTACK_SPEED,
                GetPlayer().GetAttackSpeed().GetValue());
        EntityFunctions.TrySetAttributeBaseValue(GetPlayer().GetMCPlayer(), Attribute.PLAYER_BLOCK_BREAK_SPEED,
                GetPlayer().GetMiningSpeed().GetValue());
        EntityFunctions.TrySetAttributeBaseValue(GetPlayer().GetMCPlayer(), Attribute.PLAYER_ENTITY_INTERACTION_RANGE,
                GetPlayer().GetEntityReach().GetValue());
        EntityFunctions.TrySetAttributeBaseValue(GetPlayer().GetMCPlayer(), Attribute.PLAYER_BLOCK_INTERACTION_RANGE,
                GetPlayer().GetBlockReach().GetValue());
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
        if ((event.getPlayer() != GetPlayer().GetMCPlayer()))
        {
            return;
        }

        if (_state != GamePlayerState.InGame)
        {
            event.setCancelled(true);
            return;
        }

        event.deathMessage(null);
        GetPlayer().SetIsAlive(false);
    }

    @Override
    public void OnEntityDamageEvent(EntityDamageEvent event) { }

    @Override
    public void OnAsyncChatEvent(AsyncChatEvent event) { }

    @Override
    public void OnBlockBreakEvent(BlockBreakEvent event)
    {
        if ((_state == GamePlayerState.PreGame) && event.getPlayer() == GetPlayer().GetMCPlayer())
        {
            event.setCancelled(true);
        }
    }

    @Override
    public void OnBlockPlaceEvent(BlockPlaceEvent event)
    {
        if ((_state == GamePlayerState.PreGame) && event.getPlayer() == GetPlayer().GetMCPlayer())
        {
            event.setCancelled(true);
        }
    }

    @Override
    public void OnPlayerInteractEvent(PlayerInteractEvent event)
    {
        if ((_state == GamePlayerState.PreGame) && event.getPlayer() == GetPlayer().GetMCPlayer())
        {
            event.setCancelled(true);
        }
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