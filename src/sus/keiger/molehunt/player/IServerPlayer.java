package sus.keiger.molehunt.player;

import org.bukkit.entity.Player;
import sus.keiger.plugincommon.ITickable;

import java.util.UUID;

public interface IServerPlayer extends IAudienceMember, ITickable
{
    Player GetMCPlayer();
    UUID GetUUID();
    String GetName();
    boolean IsOnline();
    boolean IsAdmin();
}