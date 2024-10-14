package sus.keiger.molehunt.lobby;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.*;
import org.bukkit.util.BoundingBox;
import sus.keiger.molehunt.event.IEventDispatcher;
import sus.keiger.molehunt.player.*;
import sus.keiger.plugincommon.ITickable;
import sus.keiger.plugincommon.IterationSafeMap;
import sus.keiger.plugincommon.player.actionbar.ActionbarMessage;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class DefaultServerLobby implements IServerLobby
{
    // Private fields.
    private final IServerPlayerCollection _serverPlayerCollection;
    private final Map<IServerPlayer, LobbyPlayer> _players = new IterationSafeMap<>();
    private final Location _spawnLocation;
    private final BoundingBox _lobbyBounds;
    private final IEventDispatcher _eventDispatcher;


    // Constructors.
    public DefaultServerLobby(IServerPlayerCollection players,
                              Location spawnLocation,
                              BoundingBox bounds,
                              IEventDispatcher eventDispatcher)
    {
        _serverPlayerCollection = Objects.requireNonNull(players, "players is null");
        _spawnLocation = Objects.requireNonNull(spawnLocation, "spawnLocation is null");
        _lobbyBounds = Objects.requireNonNull(bounds, "bounds is null");
        _eventDispatcher = Objects.requireNonNull(eventDispatcher, "eventDispatcher is null");
    }


    // Inherited methods.
    @Override
    public List<IServerPlayer> GetPlayers()
    {
        return List.copyOf(_players.keySet());
    }

    @Override
    public boolean AddPlayer(IServerPlayer player)
    {
        Objects.requireNonNull(player, "player is null");
        if (_players.containsKey(player))
        {
            return false;
        }

        LobbyPlayer CreatedLobbyPlayer = new LobbyPlayer(player,
                _lobbyBounds, _spawnLocation, _serverPlayerCollection);
        _players.put(player, CreatedLobbyPlayer);
        CreatedLobbyPlayer.InitializePlayer();
        CreatedLobbyPlayer.SubscribeToEvents(_eventDispatcher);
        return true;
    }

    @Override
    public boolean RemovePlayer(IServerPlayer player)
    {
        if (!_players.containsKey(player))
        {
            return false;
        }

        LobbyPlayer TargetLobbyPlayer = _players.remove(player);
        TargetLobbyPlayer.UnsubscribeFromEvents(_eventDispatcher);
        return true;
    }

    @Override
    public boolean ContainsPlayer(IServerPlayer player)
    {
        return _players.containsKey(Objects.requireNonNull(player, "player is null"));
    }

    @Override
    public void SubscribeToEvents(IEventDispatcher dispatcher) { }

    @Override
    public void UnsubscribeFromEvents(IEventDispatcher dispatcher) { }

    @Override
    public List<? extends IAudienceMember> GetAudienceMembers()
    {
        return GetPlayers();
    }

    @Override
    public void ShowTitle(Title title)
    {
        _players.keySet().forEach(player -> player.ShowTitle(title));
    }

    @Override
    public void ClearTitle()
    {
        _players.keySet().forEach(IServerPlayer::ClearTitle);
    }

    @Override
    public void ShowActionbar(ActionbarMessage message)
    {
        _players.keySet().forEach(player -> player.ShowActionbar(message));
    }

    @Override
    public void RemoveActionbar(long id)
    {
        _players.keySet().forEach(player -> player.RemoveActionbar(id));
    }

    @Override
    public void ClearActionbar()
    {
        _players.keySet().forEach(IServerPlayer::ClearActionbar);
    }

    @Override
    public void PlaySound(Sound sound, Location location, SoundCategory category, float volume, float pitch)
    {
        _players.keySet().forEach(player -> player.PlaySound(sound, location, category, volume, pitch));
    }

    @Override
    public void SendMessage(Component message)
    {
        _players.keySet().forEach(player -> player.SendMessage(message));
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
        _players.keySet().forEach(player -> player.SpawnParticle(particle,
                location, deltaX, deltaY, deltaZ, count, extra, data));
    }

    @Override
    public void Tick()
    {
        _players.values().forEach(ITickable::Tick);
    }
}
