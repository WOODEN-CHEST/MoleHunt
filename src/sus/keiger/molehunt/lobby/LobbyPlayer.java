package sus.keiger.molehunt.lobby;

import io.papermc.paper.event.player.AsyncChatEvent;
import io.papermc.paper.event.player.PrePlayerAttackEntityEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.util.BoundingBox;
import sus.keiger.molehunt.event.IEventDispatcher;
import sus.keiger.molehunt.event.IMoleHuntEventListener;
import sus.keiger.molehunt.player.IAudienceMember;
import sus.keiger.molehunt.player.IServerPlayer;
import sus.keiger.molehunt.player.IServerPlayerCollection;
import sus.keiger.plugincommon.ITickable;
import sus.keiger.plugincommon.PCMath;
import sus.keiger.plugincommon.entity.EntityFunctions;
import sus.keiger.plugincommon.player.PlayerFunctions;
import sus.keiger.plugincommon.player.actionbar.ActionbarMessage;

import java.util.Objects;

public class LobbyPlayer implements ITickable, IAudienceMember, IMoleHuntEventListener
{
    // Private fields.
    private final IServerPlayerCollection _players;
    private final IServerPlayer _serverPlayer;
    private final BoundingBox _lobbyBounds;
    private final Location _lobbySpawnLocation;
    private final double DEFAULT_ACTIONBAR_DURATION_SECONDS = 3d;


    // Constructors.
    public LobbyPlayer(IServerPlayer serverPlayer,
                       BoundingBox lobbyBounds,
                       Location lobbySpawnLocation,
                       IServerPlayerCollection players)
    {
        _players = Objects.requireNonNull(players, "players is null");
        _serverPlayer = Objects.requireNonNull(serverPlayer, "serverPlayer is null");
        _lobbyBounds = Objects.requireNonNull(lobbyBounds, "lobbyBounds is null");;
        _lobbySpawnLocation = Objects.requireNonNull(lobbySpawnLocation, "lobbySpawnLocation is null");;
    }


    // Methods.
    public void InitializePlayer()
    {
        ClearActionbar();
        ClearTitle();

        Player MCPlayer = _serverPlayer.GetMCPlayer();
        PlayerFunctions.ResetAttributes(MCPlayer);
        MCPlayer.setGameMode(GameMode.SURVIVAL);
        MCPlayer.setInvulnerable(false);
        MCPlayer.setLevel(0);
        MCPlayer.setExp(0);
        MCPlayer.clearActivePotionEffects();
        MCPlayer.setFreezeTicks(0);
        MCPlayer.setFireTicks(0);
        MCPlayer.setVisualFire(false);
        MCPlayer.setInvisible(false);
        PlayerFunctions.ClearInventory(MCPlayer);

        TeleportToLobby();
        SetPlayerInventory(MCPlayer);
    }


    // Private methods.
    private void TeleportToLobby()
    {
        _serverPlayer.GetMCPlayer().teleport(_lobbySpawnLocation);
    }

    private ItemStack CreateUnbreakableItem(Material material, int count)
    {
        ItemStack Item = new ItemStack(material, count);
        Item.editMeta(meta -> meta.setUnbreakable(true));
        return Item;
    }

    private void SetPlayerInventory(Player mcPlayer)
    {
        ItemStack Sword = CreateUnbreakableItem(Material.DIAMOND_SWORD, 1);
        ItemStack Axe = CreateUnbreakableItem(Material.IRON_AXE, 1);
        ItemStack Shield = CreateUnbreakableItem(Material.SHIELD, 1);
        ItemStack Helmet = CreateUnbreakableItem(Material.IRON_HELMET, 1);
        ItemStack Chestplate = CreateUnbreakableItem(Material.DIAMOND_CHESTPLATE, 1);
        ItemStack Leggings = CreateUnbreakableItem(Material.IRON_LEGGINGS, 1);
        ItemStack Boots = CreateUnbreakableItem(Material.IRON_BOOTS, 1);

        PlayerFunctions.SetItem(mcPlayer, PlayerFunctions.SLOT_HOTBAR1, Sword);
        PlayerFunctions.SetItem(mcPlayer, PlayerFunctions.SLOT_HOTBAR2, Axe);
        PlayerFunctions.SetItem(mcPlayer, PlayerFunctions.SLOT_OFFHAND, Shield);
        PlayerFunctions.SetItem(mcPlayer, PlayerFunctions.SLOT_ARMOR_HEAD, Helmet);
        PlayerFunctions.SetItem(mcPlayer, PlayerFunctions.SLOT_ARMOR_CHEST, Chestplate);
        PlayerFunctions.SetItem(mcPlayer, PlayerFunctions.SLOT_ARMOR_LEGS, Leggings);
        PlayerFunctions.SetItem(mcPlayer, PlayerFunctions.SLOT_ARMOR_FEET, Boots);
    }

    private void SetFood()
    {
        _serverPlayer.GetMCPlayer().setFoodLevel(PlayerFunctions.MAX_FOOD);
        _serverPlayer.GetMCPlayer().setSaturation(0f);
    }

    private void CancelEventIfNotAdmin(Cancellable event, Player player)
    {
        if (_serverPlayer.GetMCPlayer().equals(player) && !_serverPlayer.IsAdmin())
        {
            event.setCancelled(true);
        }
    }

    private void OnBlockBreakEvent(BlockBreakEvent event)
    {
        CancelEventIfNotAdmin(event, event.getPlayer());
    }

    private void OnBlockPlaceEvent(BlockPlaceEvent event)
    {
        CancelEventIfNotAdmin(event, event.getPlayer());
    }

    private void SendDeathMessage(PlayerDeathEvent event)
    {
        String Message;
        if (event.getDamageSource().getCausingEntity() instanceof Player PlayerCausingEntity)
        {
            IServerPlayer KillerPlayer = _players.GetPlayer(PlayerCausingEntity);
            Message = "Killed by %s".formatted(KillerPlayer.GetName());

            KillerPlayer.ShowActionbar(new ActionbarMessage(PCMath.SecondsToTicks(DEFAULT_ACTIONBAR_DURATION_SECONDS),
                    Component.text("Killed %s".formatted(_serverPlayer.GetName()))));
        }
        else
        {
            Message = "You died";
        }

        ShowActionbar(new ActionbarMessage(PCMath.SecondsToTicks(DEFAULT_ACTIONBAR_DURATION_SECONDS),
                Component.text(Message).color(NamedTextColor.RED)));
    }

    private void OnPlayerDeathEvent(PlayerDeathEvent event)
    {
        if (!event.getPlayer().equals(_serverPlayer.GetMCPlayer()))
        {
            return;
        }

        SendDeathMessage(event);
        event.setCancelled(true);
        EntityFunctions.SetHealthPortion(_serverPlayer.GetMCPlayer(), 1d);
        _serverPlayer.GetMCPlayer().teleport(_lobbySpawnLocation);
    }

    private void OnPlayerDropItemEvent(PlayerDropItemEvent event)
    {
        CancelEventIfNotAdmin(event, event.getPlayer());
    }

    private void OnPrePlayerAttackEntityEvent(PrePlayerAttackEntityEvent event)
    {
        if (!(event.getAttacked() instanceof Player))
        {
            CancelEventIfNotAdmin(event, event.getPlayer());
        }
    }

    private void OnInventoryOpenEvent(InventoryOpenEvent event)
    {
        if ((event.getPlayer() instanceof Player TargetPlayer) && !(event.getInventory() instanceof PlayerInventory))
        {
            CancelEventIfNotAdmin(event, TargetPlayer);
        }
    }

    private void EnsurePlayerInBounds()
    {
        if (_serverPlayer.IsAdmin())
        {
            return;
        }

        if (!_lobbyBounds.contains(_serverPlayer.GetMCPlayer().getLocation().toVector()))
        {
            TeleportToLobby();
            ShowActionbar(new ActionbarMessage(PCMath.SecondsToTicks(DEFAULT_ACTIONBAR_DURATION_SECONDS),
                    Component.text("Do not leave the lobby bounds").color(NamedTextColor.RED)));
        }
    }


    // Inherited methods.
    @Override
    public void ShowTitle(Title title)
    {
        _serverPlayer.ShowTitle(title);
    }

    @Override
    public void ClearTitle()
    {
        _serverPlayer.ClearTitle();
    }

    @Override
    public void ShowActionbar(ActionbarMessage message)
    {
        _serverPlayer.ShowActionbar(message);
    }

    @Override
    public void RemoveActionbar(long id)
    {
        _serverPlayer.RemoveActionbar(id);
    }

    @Override
    public void ClearActionbar()
    {
        _serverPlayer.ClearActionbar();
    }

    @Override
    public void PlaySound(Sound sound, Location location, SoundCategory category, float volume, float pitch)
    {
        _serverPlayer.PlaySound(sound, location, category, volume, pitch);
    }

    @Override
    public void SendMessage(Component message)
    {
        _serverPlayer.SendMessage(message);
    }

    @Override
    public <T> void SpawnParticle(Particle particle,
                                  Location location,
                                  double deltaX,
                                  double deltaY,
                                  double deltaZ,
                                  int count,
                                  double extra,
                                  T data)
    {
        _serverPlayer.SpawnParticle(particle, location, deltaX, deltaY, deltaZ, count, extra, data);
    }

    @Override
    public void Tick()
    {
        SetFood();
        EnsurePlayerInBounds();
    }

    @Override
    public void SubscribeToEvents(IEventDispatcher dispatcher)
    {
        dispatcher.GetPlayerDropItemEvent().Subscribe(this, this::OnPlayerDropItemEvent);
        dispatcher.GetBlockBreakEvent().Subscribe(this, this::OnBlockBreakEvent);
        dispatcher.GetBlockPlaceEvent().Subscribe(this, this::OnBlockPlaceEvent);
        dispatcher.GetPlayerDeathEvent().Subscribe(this, this::OnPlayerDeathEvent);
        dispatcher.GetPrePlayerAttackEntityEvent().Subscribe(this, this::OnPrePlayerAttackEntityEvent);
        dispatcher.GetInventoryOpenEvent().Subscribe(this, this::OnInventoryOpenEvent);
    }

    @Override
    public void UnsubscribeFromEvents(IEventDispatcher dispatcher)
    {
        dispatcher.GetPlayerDropItemEvent().Unsubscribe(this);
        dispatcher.GetBlockBreakEvent().Unsubscribe(this);
        dispatcher.GetBlockPlaceEvent().Unsubscribe(this);
        dispatcher.GetPlayerDeathEvent().Unsubscribe(this);
        dispatcher.GetPrePlayerAttackEntityEvent().Unsubscribe(this);
        dispatcher.GetInventoryOpenEvent().Unsubscribe(this);
    }
}