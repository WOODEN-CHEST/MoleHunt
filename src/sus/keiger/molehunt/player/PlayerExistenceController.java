package sus.keiger.molehunt.player;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import sus.keiger.molehunt.event.IEventDispatcher;

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
    private void CreatePlayer(Player player)
    {
        _players.AddPlayer(new MoleHuntPlayer(player));
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
        if (TargetPlayer != null)
        {
            _players.RemovePlayer(TargetPlayer);
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