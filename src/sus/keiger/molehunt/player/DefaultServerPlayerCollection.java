package sus.keiger.molehunt.player;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.*;
import org.bukkit.entity.Player;
import sus.keiger.plugincommon.IterationSafeMap;
import sus.keiger.plugincommon.player.actionbar.ActionbarMessage;

import java.util.*;

public class DefaultServerPlayerCollection implements IServerPlayerCollection
{
    // Private fields.
    private final Map<Player, IServerPlayer> _players = new IterationSafeMap<>();
    private List<IServerPlayer> _playerCopy;


    // Inherited methods.
    @Override
    public IServerPlayer GetPlayer(Player mcPlayer)
    {
        return _players.get(Objects.requireNonNull(mcPlayer, "mcPlayer is null"));
    }

    @Override
    public IServerPlayer GetPlayer(UUID uuid)
    {
        return _players.get(Bukkit.getPlayer(Objects.requireNonNull(uuid, "uuid is null")));
    }

    @Override
    public IServerPlayer GetPlayer(String name)
    {
        return _players.get(Bukkit.getPlayerExact(Objects.requireNonNull(name, "name is null")));
    }

    @Override
    public List<IServerPlayer> GetPlayers()
    {
        return _playerCopy;
    }

    @Override
    public void AddPlayer(IServerPlayer player)
    {
        Objects.requireNonNull(player, "player is null");
        _players.put(player.GetMCPlayer(), player);
        _playerCopy = List.copyOf(_players.values());
    }

    @Override
    public void RemovePlayer(IServerPlayer player)
    {
        Objects.requireNonNull(player, "player is null");
        _players.remove(player.GetMCPlayer());
        _playerCopy = List.copyOf(_players.values());
    }

    @Override
    public void ClearPlayers()
    {
        _players.clear();
        _playerCopy = Collections.emptyList();
    }

    @Override
    public boolean ContainsPlayer(IServerPlayer player)
    {
        IServerPlayer TargetPlayer = _players.get(Objects.requireNonNull(player, "player is null").GetMCPlayer());
        if (TargetPlayer == null)
        {
            return false;
        }
        return TargetPlayer == player;
    }

    @Override
    public List<? extends IAudienceMember> GetAudienceMembers()
    {
        return _playerCopy;
    }

    @Override
    public void ShowTitle(Title title)
    {
        _playerCopy.forEach(player -> player.ShowTitle(title));
    }

    @Override
    public void ClearTitle()
    {
        _playerCopy.forEach(IServerPlayer::ClearTitle);
    }

    @Override
    public void ShowActionbar(ActionbarMessage message)
    {
        _playerCopy.forEach(player -> player.ShowActionbar(message));
    }

    @Override
    public void RemoveActionbar(long id)
    {
        _playerCopy.forEach(player -> player.RemoveActionbar(id));
    }

    @Override
    public void ClearActionbar()
    {
        _playerCopy.forEach(IServerPlayer::ClearActionbar);
    }

    @Override
    public void PlaySound(Sound sound, Location location, SoundCategory category, float volume, float pitch)
    {
        _playerCopy.forEach(player -> player.PlaySound(sound, location, category, volume, pitch));
    }

    @Override
    public void SendMessage(Component message)
    {
        _playerCopy.forEach(player -> player.SendMessage(message));
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
        _playerCopy.forEach(player -> player.SpawnParticle(particle,
                location, deltaX, deltaY, deltaZ, count, extra, data));
    }
}
