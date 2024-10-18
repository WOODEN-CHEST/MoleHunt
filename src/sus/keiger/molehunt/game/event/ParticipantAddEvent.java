package sus.keiger.molehunt.game.event;

import sus.keiger.molehunt.game.IMoleHuntGameInstance;
import sus.keiger.molehunt.player.IServerPlayer;

public class ParticipantAddEvent extends MoleHuntPlayerEvent
{
    // Constructors.
    public ParticipantAddEvent(IMoleHuntGameInstance gameInstance, IServerPlayer player)
    {
        super(gameInstance, player);
    }
}