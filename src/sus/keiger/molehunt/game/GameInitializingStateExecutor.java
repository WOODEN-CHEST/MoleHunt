package sus.keiger.molehunt.game;

public class GameInitializingStateExecutor extends GenericGameStateExecutor
{
    // Constructors.
    public GameInitializingStateExecutor()
    {
        super(MoleHuntGameState.Initializing);
    }


    // Methods.
    @Override
    public void StartState() { }

    @Override
    public void EndState() { }

    @Override
    public void Tick() { }
}