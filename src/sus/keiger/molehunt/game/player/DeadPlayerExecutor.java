package sus.keiger.molehunt.game.player;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.json.JSONComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.*;
import sus.keiger.molehunt.player.IServerPlayerCollection;

import java.util.Objects;

public class DeadPlayerExecutor extends PlayerExecutorBase
{
    // Private fields.
    private GamePlayerState _state = GamePlayerState.PreGame;


    // Constructors.
    public DeadPlayerExecutor(IGamePlayer gamePlayer,
                              IServerPlayerCollection playerCollection)
    {
        super(gamePlayer, playerCollection);
    }


    // Private methods.
    private void TryCancelInGamePlayerEvent(Cancellable event, Player player)
    {
        if ((_state == GamePlayerState.InGame)
                && (GetServerPlayers().GetPlayer(player) == GetPlayer().GetServerPlayer()))
        {
            event.setCancelled(true);
        }
    }

    // Inherited methods.
    @Override
    public void SwitchState(GamePlayerState state)
    {
        _state = Objects.requireNonNull(state, "state is null");
    }

    @Override
    public void OnPlayerDeathEvent(PlayerDeathEvent event)
    {
        TryCancelInGamePlayerEvent(event, event.getPlayer());
    }

    @Override
    public void OnEntityDamageEvent(EntityDamageEvent event)
    {
        if (event.getEntity() instanceof Player PlayerEntity)
        {
            TryCancelInGamePlayerEvent(event, PlayerEntity);
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

        Component Message = Component.text("[To Spectators]<%s> %s".formatted(event.getPlayer().getName(),
                PlainTextComponentSerializer.plainText().serialize(event.originalMessage())))
                .color(NamedTextColor.GRAY);

        GetPlayer().GetGameInstance().GetPlayers().stream().filter(player -> !player.IsAlive())
                .forEach(player -> player.SendMessage(Message));
    }

    @Override
    public void OnBlockBreakEvent(BlockBreakEvent event)
    {
        TryCancelInGamePlayerEvent(event, event.getPlayer());
    }

    @Override
    public void OnBlockPlaceEvent(BlockPlaceEvent event)
    {
        TryCancelInGamePlayerEvent(event, event.getPlayer());
    }

    @Override
    public void OnPlayerInteractEvent(PlayerInteractEvent event) { }
    @Override
    public void OnPlayerDropItemEvent(PlayerDropItemEvent event)
    {
        TryCancelInGamePlayerEvent(event, event.getPlayer());
    }

    @Override
    public void Tick() { }
}