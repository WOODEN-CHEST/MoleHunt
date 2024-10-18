package sus.keiger.molehunt.game.event;

import sus.keiger.molehunt.game.IMoleHuntGameInstance;
import sus.keiger.molehunt.player.IServerPlayer;

import java.util.Objects;

public class MoleHuntPlayerEvent extends MoleHuntGameEvent
{
    // Private fields.
    private final IServerPlayer _player;


    // Constructors.
    public MoleHuntPlayerEvent(IMoleHuntGameInstance gameInstance, IServerPlayer player)
    {
        super(gameInstance);
        _player = Objects.requireNonNull(player, "player is null");
    }


    // Methods.
    public IServerPlayer GetPlayer()
    {
        return _player;
    }
}