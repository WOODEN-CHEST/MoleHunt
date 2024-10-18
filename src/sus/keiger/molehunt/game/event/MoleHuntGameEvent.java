package sus.keiger.molehunt.game.event;

import sus.keiger.molehunt.game.IMoleHuntGameInstance;

import java.util.Objects;

public class MoleHuntGameEvent
{
    // Private fields.
    private final IMoleHuntGameInstance _gameInstance;


    // Constructors.
    public MoleHuntGameEvent(IMoleHuntGameInstance instance)
    {
        _gameInstance = Objects.requireNonNull(instance, "instance is null");
    }


    // Methods.
    public IMoleHuntGameInstance GetGameInstance()
    {
        return _gameInstance;
    }
}