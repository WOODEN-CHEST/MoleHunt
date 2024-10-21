package sus.keiger.molehunt.game.player;

import sus.keiger.molehunt.game.IGameServices;
import sus.keiger.molehunt.player.IServerPlayerCollection;

import java.util.Objects;

public abstract class PlayerExecutorBase implements IGamePlayerExecutor
{
    // Private fields.
    private IGamePlayer _player;
    private IGameServices _gameServices;


    // Constructors.
    public PlayerExecutorBase(IGamePlayer gamePlayer, IGameServices services)
    {
        _player = Objects.requireNonNull(gamePlayer, "gamePlayer is null");
        _gameServices = Objects.requireNonNull(services, "services is null");
    }


    // Methods.
    public IGamePlayer GetPlayer()
    {
        return _player;
    }

    public IGameServices GetGameServices()
    {
        return _gameServices;
    }
}