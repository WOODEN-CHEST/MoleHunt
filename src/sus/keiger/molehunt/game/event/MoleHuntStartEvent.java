package sus.keiger.molehunt.game.event;

import sus.keiger.molehunt.game.IMoleHuntGameInstance;
import sus.keiger.molehunt.game.MoleHuntGameEvent;

public class MoleHuntStartEvent extends MoleHuntGameEvent
{
    // Constructors.
    public MoleHuntStartEvent(IMoleHuntGameInstance instance)
    {
        super(instance);
    }
}