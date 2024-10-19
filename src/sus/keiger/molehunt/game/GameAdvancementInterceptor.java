package sus.keiger.molehunt.game;

import net.kyori.adventure.text.Component;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;
import sus.keiger.molehunt.event.IEventDispatcher;
import sus.keiger.molehunt.event.IMoleHuntEventListener;
import sus.keiger.molehunt.game.player.GamePlayerCollection;
import sus.keiger.molehunt.game.player.IGamePlayer;
import sus.keiger.molehunt.game.player.IGamePlayerExecutor;
import sus.keiger.molehunt.player.IServerPlayerCollection;

import java.util.Objects;

public class GameAdvancementInterceptor implements IMoleHuntEventListener, IGameStateContaining
{
    // Private fields.
    private final IServerPlayerCollection _serverPlayers;
    private final GamePlayerCollection _gamePlayers;
    private MoleHuntGameState _state = MoleHuntGameState.Initializing;



    // Constructors.
    public GameAdvancementInterceptor(IServerPlayerCollection serverPlayers, GamePlayerCollection gamePlayers)
    {
        _serverPlayers = Objects.requireNonNull(serverPlayers, "serverPlayers is null");
        _gamePlayers = Objects.requireNonNull(gamePlayers, "gamePlayers is null");
    }


    // Private methods.
    private void OnAdvancementDoneEvent(PlayerAdvancementDoneEvent event)
    {
        IGamePlayer TargetPlayer = _gamePlayers.GetGamePlayer(_serverPlayers.GetPlayer(event.getPlayer()));
        if (TargetPlayer != null)
        {
            Component Message = event.message();
            if (Message != null)
            {
                TargetPlayer.SendMessage(Message);
            }
            event.message(null);
        }
    }


    // Inherited methods.
    @Override
    public void SubscribeToEvents(IEventDispatcher dispatcher)
    {
        dispatcher.GetAdvancementDoneEvent().Subscribe(this, this::OnAdvancementDoneEvent);
    }

    @Override
    public void UnsubscribeFromEvents(IEventDispatcher dispatcher)
    {
        dispatcher.GetAdvancementDoneEvent().Unsubscribe(this);
    }

    @Override
    public void SetState(MoleHuntGameState state)
    {
        _state = Objects.requireNonNull(state, "state is null");
    }
}