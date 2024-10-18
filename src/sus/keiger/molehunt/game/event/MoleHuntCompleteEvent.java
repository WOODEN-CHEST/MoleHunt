package sus.keiger.molehunt.game.event;

import sus.keiger.molehunt.game.IMoleHuntGameInstance;

public class MoleHuntCompleteEvent extends MoleHuntGameEvent
{
    // Constructors.
    public MoleHuntCompleteEvent(IMoleHuntGameInstance instance)
    {
        super(instance);
    }
}