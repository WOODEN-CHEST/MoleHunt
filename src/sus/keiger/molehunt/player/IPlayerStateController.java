package sus.keiger.molehunt.player;

import sus.keiger.molehunt.event.IMoleHuntEventListener;
import sus.keiger.molehunt.game.IMoleHuntGameInstance;
import sus.keiger.plugincommon.ITickable;

public interface IPlayerStateController extends IMoleHuntEventListener, ITickable
{
    void SendPlayerToLobby();
    void AddPlayerToGame();
    void RemovePlayerFromGame();
}
