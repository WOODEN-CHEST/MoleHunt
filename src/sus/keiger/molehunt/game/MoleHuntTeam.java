package sus.keiger.molehunt.game;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import sus.keiger.molehunt.player.IAudienceMember;
import sus.keiger.molehunt.player.IServerPlayer;
import sus.keiger.plugincommon.player.actionbar.ActionbarMessage;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class MoleHuntTeam implements IGameTeam
{
    // Private fields.
    private final Set<IServerPlayer> _players = new HashSet<>();
    private final GameTeamType _teamType;


    // Constructors.
    public MoleHuntTeam(GameTeamType teamType)
    {
        _teamType = Objects.requireNonNull(teamType, "teamType is null");
    }


    // Inhertied methods.
    @Override
    public GameTeamType GetType()
    {
        return _teamType;
    }

    @Override
    public boolean AddPlayer(IServerPlayer player)
    {
        return _players.add(Objects.requireNonNull(player, "player is null"));
    }

    @Override
    public boolean RemovePlayer(IServerPlayer player)
    {
        return _players.remove(Objects.requireNonNull(player, "player is null"));
    }

    @Override
    public void ClearPlayers()
    {
        _players.clear();
    }

    @Override
    public boolean ContainsPlayer(IServerPlayer player)
    {
        return _players.contains(Objects.requireNonNull(player, "player is null"));
    }

    @Override
    public List<? extends IAudienceMember> GetAudienceMembers()
    {
        return List.copyOf(_players);
    }

    @Override
    public void ShowTitle(Title title)
    {
        _players.forEach(player -> player.ShowTitle(title));
    }

    @Override
    public void ClearTitle()
    {
        _players.forEach(IServerPlayer::ClearTitle);
    }

    @Override
    public void ShowActionbar(ActionbarMessage message)
    {
        _players.forEach(player -> player.ShowActionbar(message));
    }

    @Override
    public void RemoveActionbar(long id)
    {
        _players.forEach(player -> player.RemoveActionbar(id));
    }

    @Override
    public void ClearActionbar()
    {
        _players.forEach(IServerPlayer::ClearActionbar);
    }

    @Override
    public void PlaySound(Sound sound, Location location, SoundCategory category, float volume, float pitch)
    {
        _players.forEach(player -> player.PlaySound(sound, location, category, volume, pitch));
    }

    @Override
    public void SendMessage(Component message)
    {
        _players.forEach(player -> player.SendMessage(message));
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
        _players.forEach(player -> player.SpawnParticle(particle,
                location, deltaX, deltaY, deltaZ, count, extra, data));
    }
}
