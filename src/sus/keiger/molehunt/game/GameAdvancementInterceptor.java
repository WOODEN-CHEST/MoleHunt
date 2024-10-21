package sus.keiger.molehunt.game;

import net.kyori.adventure.text.Component;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;
import sus.keiger.molehunt.event.IEventDispatcher;
import sus.keiger.molehunt.event.IMoleHuntEventListener;
import sus.keiger.molehunt.game.player.IGamePlayer;

import java.util.Objects;

public class GameAdvancementInterceptor implements IMoleHuntEventListener, IGameStateContaining
{
    // Private fields.
    private final IGameServices _gameServices;
    private MoleHuntGameState _state = MoleHuntGameState.Initializing;



    // Constructors.
    public GameAdvancementInterceptor(IGameServices services)
    {
        _gameServices = Objects.requireNonNull(services, "services is null");
    }


    // Private methods.
    private void OnAdvancementDoneEvent(PlayerAdvancementDoneEvent event)
    {
        IGamePlayer TargetPlayer = _gameServices.GetGamePlayerCollection().GetGamePlayer(
                _gameServices.GetServerPlayerCollection().GetPlayer(event.getPlayer()));
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