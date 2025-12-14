package sus.keiger.molehunt.player;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import sus.keiger.molehunt.event.IEventDispatcher;
import sus.keiger.plugincommon.EmptyEvent;

import java.util.Objects;

public class PlayerExistenceController implements IPlayerExistenceController
{
    // Private fields.
    private final IServerPlayerCollection _players;


    // Constructors.
    public PlayerExistenceController(IServerPlayerCollection players)
    {
        _players = Objects.requireNonNull(players, "players is null");
    }


    // Private methods.
    private void RemovePlayer(IServerPlayer player)
    {
        _players.RemovePlayer(player);
        player.GetReferenceCountChangeEvent().Unsubscribe(this);
    }

    private void OnReferenceCountChangeEvent(PlayerReferenceCountChangeEvent event)
    {
        if (event.Player().GetReferenceCount() == 0)
        {
            RemovePlayer(event.Player());
        }
    }

    private void CreatePlayer(Player player)
    {
        if (_players.ContainsPlayer(player.getUniqueId()))
        {
            _players.GetPlayer(player.getUniqueId()).SetMCPlayer(player);
            return;
        }

        MoleHuntPlayer CreatedPlayer = new MoleHuntPlayer(player);
        _players.AddPlayer(CreatedPlayer);
        CreatedPlayer.GetReferenceCountChangeEvent().Subscribe(this, this::OnReferenceCountChangeEvent);
    }

    private void OnPlayerJoinEvent(PlayerJoinEvent event)
    {
        event.joinMessage(null);
        CreatePlayer(event.getPlayer());
    }

    private void OnPlayerQuitEvent(PlayerQuitEvent event)
    {
        event.quitMessage(null);
        IServerPlayer TargetPlayer = _players.GetPlayer(event.getPlayer());
        if ((TargetPlayer != null) && (TargetPlayer.GetReferenceCount() == 0))
        {
            RemovePlayer(TargetPlayer);
        }
    }


    // Inherited methods.
    @Override
    public void SubscribeToEvents(IEventDispatcher dispatcher)
    {
        dispatcher.GetPlayerJoinEvent().Subscribe(this, this::OnPlayerJoinEvent, Integer.MAX_VALUE);
        dispatcher.GetPlayerQuitEvent().Subscribe(this, this::OnPlayerQuitEvent, Integer.MIN_VALUE);
    }

    @Override
    public void UnsubscribeFromEvents(IEventDispatcher dispatcher)
    {
        dispatcher.GetPlayerJoinEvent().Unsubscribe(this);
        dispatcher.GetPlayerQuitEvent().Unsubscribe(this);
    }

    @Override
    public void ReloadPlayers()
    {
        _players.ClearPlayers();
        for (Player TargetPlayer : Bukkit.getOnlinePlayers())
        {
            CreatePlayer(TargetPlayer);
        }
    }

    @Override
    public void Tick()
    {
        _players.GetPlayers().forEach(IServerPlayer::Tick);
    }
}