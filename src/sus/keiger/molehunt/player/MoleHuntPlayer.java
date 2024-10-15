package sus.keiger.molehunt.player;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import sus.keiger.plugincommon.player.actionbar.ActionbarContainer;
import sus.keiger.plugincommon.player.actionbar.ActionbarMessage;

import java.util.Objects;
import java.util.UUID;

public class MoleHuntPlayer implements IServerPlayer
{
    // Private fields.
    private final Player _mcPlayer;
    private final ActionbarContainer _actionbar = new ActionbarContainer();


    // Constructors.
    public MoleHuntPlayer(Player mcPlayer)
    {
        _mcPlayer = Objects.requireNonNull(mcPlayer, "mcPlayer is null");
    }


    // Inherited methods.
    @Override
    public Player GetMCPlayer()
    {
        return _mcPlayer;
    }

    @Override
    public UUID GetUUID()
    {
        return _mcPlayer.getUniqueId();
    }

    @Override
    public String GetName()
    {
        return _mcPlayer.getName();
    }

    @Override
    public boolean IsOnline()
    {
        return _mcPlayer.isConnected();
    }

    @Override
    public boolean IsAdmin()
    {
        return _mcPlayer.isOp();
    }

    @Override
    public void ShowTitle(Title title)
    {
        _mcPlayer.showTitle(Objects.requireNonNull(title, "title is null"));
    }

    @Override
    public void ClearTitle()
    {
        _mcPlayer.clearTitle();
    }

    @Override
    public void ShowActionbar(ActionbarMessage message)
    {
        _actionbar.AddMessage(Objects.requireNonNull(message, "message is null"));
    }

    @Override
    public void RemoveActionbar(long id)
    {
        _actionbar.RemoveMessage(id);
    }

    @Override
    public void ClearActionbar()
    {
        _actionbar.ClearMessages();
    }

    @Override
    public void PlaySound(Sound sound, Location location, SoundCategory category, float volume, float pitch)
    {
        Objects.requireNonNull(sound, "sound is null");
        Objects.requireNonNull(location, "location is null");
        Objects.requireNonNull(category, "category is null");

        _mcPlayer.playSound(location, sound, category, volume, pitch);
    }

    @Override
    public void SendMessage(Component message)
    {
        _mcPlayer.sendMessage(Objects.requireNonNull(message, "message is null"));
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
        Objects.requireNonNull(particle, "particle is null");
        Objects.requireNonNull(location, "location is null");

        _mcPlayer.spawnParticle(particle, location, count, deltaX, deltaY, deltaZ, extra, data);
    }

    @Override
    public void Tick()
    {
        _actionbar.Tick(_mcPlayer);
    }
}