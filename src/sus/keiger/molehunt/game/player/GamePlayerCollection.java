package sus.keiger.molehunt.game.player;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import sus.keiger.molehunt.player.IAudienceMember;
import sus.keiger.molehunt.player.IAudienceMemberHolder;
import sus.keiger.molehunt.player.IServerPlayer;
import sus.keiger.plugincommon.IterationSafeMap;
import sus.keiger.plugincommon.player.actionbar.ActionbarMessage;

import java.util.*;
import java.util.stream.Stream;

public class GamePlayerCollection implements IAudienceMemberHolder
{
    // Private fields.
    private final Map<IServerPlayer, IGamePlayer> _players = new IterationSafeMap<>();
    private final Set<IServerPlayer> _spectators = new HashSet<>();
    private List<IGamePlayer> _activePlayers = Collections.emptyList();
    private List<IServerPlayer> _participants = Collections.emptyList();


    // Constructors.
    public GamePlayerCollection()
    {

    }


    // Methods.
    public boolean AddPlayer(IGamePlayer player)
    {
        Objects.requireNonNull(player, "player is null");

        IGamePlayer ExistingPlayer = _players.get(player.GetServerPlayer());
        if ((ExistingPlayer != null))
        {
            return false;
        }

        _players.put(player.GetServerPlayer(), player);
        UpdatePlayerLists();
        return true;
    }

    public void UpdateCollection()
    {
        UpdatePlayerLists();
    }

    public boolean ContainsPlayer(IServerPlayer player)
    {
        return _players.containsKey(Objects.requireNonNull(player, "player is null"));
    }

    public List<IGamePlayer> GetPlayers()
    {
        return List.copyOf(_players.values());
    }

    public List<IGamePlayer> GetActivePlayers()
    {
        return _activePlayers;
    }

    public boolean ContainsActivePlayer(IGamePlayer gamePlayer)
    {
        return _activePlayers.contains(gamePlayer);
    }

    public boolean ContainsActivePlayer(IServerPlayer serverPlayer)
    {
        IGamePlayer GamePlayer = GetGamePlayer(serverPlayer);
        if (GamePlayer == null)
        {
            return false;
        }
        return ContainsActivePlayer(GamePlayer);
    }

    public boolean AddSpectator(IServerPlayer player)
    {
        if (_spectators.add(Objects.requireNonNull(player, "player is null")))
        {
            UpdatePlayerLists();
            return true;
        }
        return false;
    }

    public boolean RemoveSpectator(IServerPlayer player)
    {
        if (_spectators.remove(Objects.requireNonNull(player, "player is null")))
        {
            UpdatePlayerLists();
            return true;
        }
        return false;
    }

    public boolean ContainsSpectator(IServerPlayer player)
    {
        return _spectators.contains(Objects.requireNonNull(player, "player is null"));
    }

    public List<IServerPlayer> GetSpectators()
    {
        return List.copyOf(_spectators);
    }

    public IGamePlayer GetGamePlayer(IServerPlayer player)
    {
        return _players.get(Objects.requireNonNull(player, "player is null"));
    }

    public List<IServerPlayer> GetParticipants()
    {
        return _participants;
    }


    // Private methods.
    private void UpdatePlayerLists()
    {
        _activePlayers = _players.keySet().stream().filter(IServerPlayer::IsOnline).map(_players::get).toList();
        _participants = Stream.concat(_spectators.stream(), _activePlayers.stream()
                .map(IGamePlayer::GetServerPlayer)).toList();
    }


    // Inherited methods.
    @Override
    public List<? extends IAudienceMember> GetAudienceMembers()
    {
        return _participants;
    }

    @Override
    public void ShowTitle(Title title)
    {
        _participants.forEach(player -> player.ShowTitle(title));
    }

    @Override
    public void ClearTitle()
    {
        _participants.forEach(IAudienceMember::ClearTitle);
    }

    @Override
    public void ShowActionbar(ActionbarMessage message)
    {
        _participants.forEach(player -> player.ShowActionbar(message));
    }

    @Override
    public void RemoveActionbar(long id)
    {
        _participants.forEach(player -> player.RemoveActionbar(id));
    }

    @Override
    public void ClearActionbar()
    {
        _participants.forEach(IAudienceMember::ClearActionbar);
    }

    @Override
    public void PlaySound(Sound sound, Location location, SoundCategory category, float volume, float pitch)
    {
        _participants.forEach(player -> player.PlaySound(sound, location, category, volume, pitch));
    }

    @Override
    public void SendMessage(Component message)
    {
        _participants.forEach(player -> player.SendMessage(message));
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
        _participants.forEach(player -> player.SpawnParticle(particle,
                location, deltaX, deltaY, deltaZ, count, extra, data));
    }
}