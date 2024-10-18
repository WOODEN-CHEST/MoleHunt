package sus.keiger.molehunt.game;

import java.util.Objects;

public class GameStateExecutorEndEvent
{
    // Private fields.
    private final IGameStateExecutor _executor;
    private final boolean _endedNaturally;


    // Constructors.
    public GameStateExecutorEndEvent(IGameStateExecutor executor, boolean endedNaturally)
    {
        _executor = Objects.requireNonNull(executor, "executor is null");
        _endedNaturally = endedNaturally;
    }


    // Methods.
    public IGameStateExecutor GetExecutor()
    {
        return _executor;
    }

    public boolean GetEndedNaturally()
    {
        return _endedNaturally;
    }
}