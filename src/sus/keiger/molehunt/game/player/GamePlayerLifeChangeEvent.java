package sus.keiger.molehunt.game.player;

import java.util.Objects;

public class GamePlayerLifeChangeEvent
{
    // Private fields.
    private final IGamePlayer _player;
    private final boolean _isAlive;


    // Constructors.
    public GamePlayerLifeChangeEvent(IGamePlayer player, boolean isAlive)
    {
        _player = Objects.requireNonNull(player, "player is null");
        _isAlive = isAlive;
    }


    // Methods.
    public boolean GetIsAlive()
    {
        return _isAlive;
    }

    public IGamePlayer GetPlayer()
    {
        return _player;
    }
}