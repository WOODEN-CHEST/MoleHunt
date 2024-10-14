package sus.keiger.molehunt.player;

import sus.keiger.molehunt.event.IMoleHuntEventListener;
import sus.keiger.molehunt.game.IMoleHuntGameInstance;

public interface IPlayerStateController extends IMoleHuntEventListener
{
    void SendPlayerToLobby();
    void AddPlayerToGame();
    void RemovePlayerFromGame();
}
