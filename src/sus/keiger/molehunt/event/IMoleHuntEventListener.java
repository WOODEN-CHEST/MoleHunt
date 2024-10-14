package sus.keiger.molehunt.event;

public interface IMoleHuntEventListener
{
    void SubscribeToEvents(IEventDispatcher dispatcher);
    void UnsubscribeFromEvents(IEventDispatcher dispatcher);
}
