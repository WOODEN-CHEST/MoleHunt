package sus.keiger.molehunt.player;

import org.bukkit.entity.Player;
import sus.keiger.plugincommon.EmptyEvent;
import sus.keiger.plugincommon.ITickable;
import sus.keiger.plugincommon.PCPluginEvent;

import java.util.UUID;

public interface IServerPlayer extends IAudienceMember, ITickable
{
    void SetMCPlayer(Player player);
    Player GetMCPlayer();
    UUID GetUUID();
    String GetName();
    boolean IsOnline();
    boolean IsAdmin();
    void AddReference(Object ref);
    int GetReferenceCount();
    void RemoveReference(Object ref);
    PCPluginEvent<PlayerReferenceCountChangeEvent> GetReferenceCountChangeEvent();
}
