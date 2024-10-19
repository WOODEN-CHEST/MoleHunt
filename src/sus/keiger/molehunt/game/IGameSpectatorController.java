package sus.keiger.molehunt.game;

import sus.keiger.molehunt.event.IMoleHuntEventListener;
import sus.keiger.molehunt.player.IServerPlayer;

import java.util.List;

public interface IGameSpectatorController extends IGameStateContaining
{
    boolean AddSpectator(IServerPlayer player);
    boolean RemoveSpectator(IServerPlayer player);
    boolean ContainsSpectator(IServerPlayer player);
    List<IServerPlayer> GetSpectators();
}