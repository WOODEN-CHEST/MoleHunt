package sus.keiger.molehunt.game;

import sus.keiger.molehunt.event.IEventDispatcher;
import sus.keiger.molehunt.event.IMoleHuntEventListener;
import sus.keiger.molehunt.game.player.GamePlayerCollection;
import sus.keiger.molehunt.player.IServerPlayerCollection;

import java.util.Objects;

public class GameStatisticsTracker implements IMoleHuntEventListener, IGameStateContaining
{
    // Private fields.
    private final IServerPlayerCollection _serverPlayers;
    private final GamePlayerCollection _gamePlayers;


    // Constructors.
    public GameStatisticsTracker(IServerPlayerCollection serverPlayers, GamePlayerCollection gamePlayers)
    {
        _serverPlayers = Objects.requireNonNull(serverPlayers, "serverPlayers is null");
        _gamePlayers = Objects.requireNonNull(gamePlayers, "gamePlayers is null");
    }


    // Private methods.



    // Inherited methods.
    @Override
    public void SubscribeToEvents(IEventDispatcher dispatcher)
    {

    }

    @Override
    public void UnsubscribeFromEvents(IEventDispatcher dispatcher)
    {

    }

    @Override
    public void SetState(MoleHuntGameState state)
    {

    }
}