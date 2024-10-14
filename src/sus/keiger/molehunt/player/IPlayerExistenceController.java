package sus.keiger.molehunt.player;

import sus.keiger.molehunt.event.IMoleHuntEventListener;
import sus.keiger.plugincommon.ITickable;

public interface IPlayerExistenceController extends IMoleHuntEventListener, ITickable
{
    void ReloadPlayers();
}