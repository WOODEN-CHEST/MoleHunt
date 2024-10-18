package sus.keiger.molehunt.game;

import sus.keiger.molehunt.IWorldProvider;
import sus.keiger.molehunt.event.IEventDispatcher;
import sus.keiger.molehunt.game.player.GamePlayerCollection;

import java.util.Objects;

public class GameCompleteStateExecutor extends GenericGameStateExecutor
{
    // Private fields.
    private final GamePlayerCollection _gamePlayers;
    private final IEventDispatcher _eventDispatcher;
    private final IWorldProvider _worldProvider;


    // Constructors.
    public GameCompleteStateExecutor(GamePlayerCollection gamePlayerCollection,
                                     IEventDispatcher eventDispatcher,
                                     IWorldProvider worldProvider)
    {
        super(MoleHuntGameState.Complete);
        _eventDispatcher = Objects.requireNonNull(eventDispatcher, "eventDispatcher is null");
        _gamePlayers = Objects.requireNonNull(gamePlayerCollection, "gamePlayerCollection is null");
        _worldProvider = Objects.requireNonNull(worldProvider, "worldProvider is null");
    }


    // Inherited methods.
    @Override
    public void StartState()
    {
        GameWorldInitializer WorldInitializer = new GameWorldInitializer();
        _worldProvider.GetWorlds().forEach(WorldInitializer::InitializeWorldNotInGame);

        _gamePlayers.GetPlayers().forEach(player ->
        {
            player.UnsubscribeFromEvents(_eventDispatcher);
        });
    }

    @Override
    public void EndState() { }

    @Override
    public void Tick() { }
}