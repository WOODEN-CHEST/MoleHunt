package sus.keiger.molehunt.game.event;

import sus.keiger.molehunt.game.IMoleHuntGameInstance;
import sus.keiger.molehunt.player.IServerPlayer;

public class ParticipantRemoveEvent extends MoleHuntPlayerEvent
{
    // Constructors.
    public ParticipantRemoveEvent(IMoleHuntGameInstance gameInstance, IServerPlayer player)
    {
        super(gameInstance, player);
    }
}