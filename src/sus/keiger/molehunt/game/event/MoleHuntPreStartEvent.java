package sus.keiger.molehunt.game.event;

import sus.keiger.molehunt.game.IMoleHuntGameInstance;
import sus.keiger.molehunt.game.MoleHuntGameEvent;

public class MoleHuntPreStartEvent extends MoleHuntGameEvent
{
    // Constructors.
    public MoleHuntPreStartEvent(IMoleHuntGameInstance instance)
    {
        super(instance);
    }
}