package sus.keiger.molehunt.player;

import sus.keiger.molehunt.event.IEventDispatcher;
import sus.keiger.molehunt.game.IMoleHuntGameInstance;
import sus.keiger.molehunt.game.MoleHuntSettings;

import java.util.Objects;

public class DefaultPlayerStateController implements IPlayerStateController
{
    // Private fields.
    private IMoleHuntGameInstance _currentGameInstance;
    private final IServerPlayerCollection _players;
    private final MoleHuntSettings _gameSettings;



    // Constructors.
    public DefaultPlayerStateController(IServerPlayerCollection players,
                                        MoleHuntSettings gameSettings)
    {
        _players = Objects.requireNonNull(players, "players is null");
        _gameSettings = Objects.requireNonNull(gameSettings, "gameSettings is null");
    }



    // Inherite methods.
    @Override
    public void SendPlayerToLobby()
    {

    }

    @Override
    public void AddPlayerToGame()
    {

    }

    @Override
    public void RemovePlayerFromGame()
    {

    }

    @Override
    public void SubscribeToEvents(IEventDispatcher dispatcher)
    {

    }

    @Override
    public void UnsubscribeFromEvents(IEventDispatcher dispatcher)
    {

    }
}
