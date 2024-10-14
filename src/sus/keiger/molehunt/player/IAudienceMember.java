package sus.keiger.molehunt.player;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import sus.keiger.plugincommon.player.actionbar.ActionbarMessage;

public interface IAudienceMember
{
    void ShowTitle(Title title);

    void ClearTitle();

    void ShowActionbar(ActionbarMessage message);

    void RemoveActionbar(long id);

    void ClearActionbar();

    void PlaySound(Sound sound, Location location, SoundCategory category, float volume, float pitch);

    void SendMessage(Component message);

    <T> void SpawnParticle(Particle particle, Location location, double deltaX, double deltaY, double deltaZ,
                                int count, double extra, T data);
}