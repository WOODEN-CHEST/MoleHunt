package sus.keiger.molehunt.game;


import java.util.Objects;

public class GameCompleteStateExecutor extends GenericGameStateExecutor
{
    // Private fields.
    private final IGameServices _gameServices;


    // Constructors.
    public GameCompleteStateExecutor(IGameServices services)
    {
        super(MoleHuntGameState.Complete);
        _gameServices = Objects.requireNonNull(services, "services is null");
    }


    // Inherited methods.
    @Override
    public void StartState()
    {
        GameWorldInitializer WorldInitializer = new GameWorldInitializer();
        _gameServices.GetLocationProvider().GetWorlds().forEach(WorldInitializer::InitializeWorldNotInGame);

        _gameServices.GetGamePlayerCollection().GetPlayers().forEach(player ->
        {
            player.UnsubscribeFromEvents(_gameServices.GetEventDispatcher());
        });
    }

    @Override
    public void EndState() { }

    @Override
    public void Tick() { }
}