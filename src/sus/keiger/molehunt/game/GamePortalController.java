package sus.keiger.molehunt.game;

import org.bukkit.event.world.PortalCreateEvent;
import sus.keiger.molehunt.event.IEventDispatcher;
import sus.keiger.molehunt.event.IMoleHuntEventListener;

import java.util.Objects;

public class GamePortalController implements IGameStateContaining, IMoleHuntEventListener
{
    // Private fields.
    private final IGameServices _services;
    private MoleHuntGameState _state = MoleHuntGameState.Initializing;


    // Constructors,
    public GamePortalController(IGameServices gameServices)
    {
        _services = Objects.requireNonNull(gameServices, "gameServices is null");
    }


    // Private methods.
    private void OnPortalCreateEvent(PortalCreateEvent event)
    {
        if ((_state == MoleHuntGameState.InGame) && !_services.GetGameSettings().GetArePortalsAllowed())
        {
            event.setCancelled(true);
        }
    }


    // Inherited methods.
    @Override
    public void SubscribeToEvents(IEventDispatcher dispatcher)
    {
        dispatcher.GetPortalCreateEvent().Subscribe(this, this::OnPortalCreateEvent);
    }

    @Override
    public void UnsubscribeFromEvents(IEventDispatcher dispatcher)
    {
        dispatcher.GetPortalCreateEvent().Unsubscribe(this);
    }

    @Override
    public void SetState(MoleHuntGameState state)
    {
        _state = Objects.requireNonNull(state, "state is null");
    }
}
