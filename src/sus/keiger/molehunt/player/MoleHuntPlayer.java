package sus.keiger.molehunt.player;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import sus.keiger.plugincommon.player.actionbar.ActionbarMessage;

import java.util.Objects;
import java.util.UUID;

public class MoleHuntPlayer implements IServerPlayer
{
    // Private fields.
    private final Player _mcPlayer;


    // Constructors.
    public MoleHuntPlayer(Player mcPlayer)
    {
        _mcPlayer = Objects.requireNonNull(mcPlayer, "mcPlayer is null");
    }


    // Inherited methods.
    @Override
    public Player GetMCPlayer()
    {
        return null;
    }

    @Override
    public UUID GetUUID()
    {
        return null;
    }

    @Override
    public String GetName()
    {
        return "";
    }

    @Override
    public boolean IsOnline()
    {
        return false;
    }

    @Override
    public boolean IsAdmin()
    {
        return false;
    }

    @Override
    public void ShowTitle(Title title)
    {

    }

    @Override
    public void ClearTitle()
    {

    }

    @Override
    public void ShowActionbar(ActionbarMessage message)
    {

    }

    @Override
    public void RemoveActionbar(long id)
    {

    }

    @Override
    public void ClearActionbar()
    {

    }

    @Override
    public void PlaySound(Sound sound, Location location, SoundCategory category, float volume, float pitch)
    {

    }

    @Override
    public void SendMessage(Component message)
    {

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

    }

    @Override
    public void Tick()
    {

    }
}