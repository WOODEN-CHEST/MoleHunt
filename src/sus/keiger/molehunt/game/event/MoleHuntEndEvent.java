package sus.keiger.molehunt.game.event;

import sus.keiger.molehunt.game.IMoleHuntGameInstance;
import sus.keiger.molehunt.game.MoleHuntGameEvent;

public class MoleHuntEndEvent extends MoleHuntGameEvent
{
    // Constructors.
    public MoleHuntEndEvent(IMoleHuntGameInstance instance)
    {
        super(instance);
    }
}