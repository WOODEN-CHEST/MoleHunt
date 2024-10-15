package sus.keiger.molehunt.game.player;

import sus.keiger.molehunt.player.IServerPlayerCollection;

import java.util.Objects;

public abstract class PlayerExecutorBase implements IGamePlayerExecutor
{
    // Private fields.
    private IGamePlayer _player;
    private IServerPlayerCollection _playerCollection;


    // Constructors.
    public PlayerExecutorBase(IGamePlayer gamePlayer, IServerPlayerCollection playerCollection)
    {
        _player = Objects.requireNonNull(gamePlayer, "gamePlayer is null");
        _playerCollection = Objects.requireNonNull(playerCollection, "playerCollection is null");
    }


    // Methods.
    public IGamePlayer GetPlayer()
    {
        return _player;
    }

    public IServerPlayerCollection GetServerPlayers()
    {
        return _playerCollection;
    }
}