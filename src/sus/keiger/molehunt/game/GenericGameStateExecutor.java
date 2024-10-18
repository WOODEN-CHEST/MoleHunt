package sus.keiger.molehunt.game;

import sus.keiger.plugincommon.ITickable;
import sus.keiger.plugincommon.PCPluginEvent;

import java.util.Objects;

public abstract class GenericGameStateExecutor implements IGameStateExecutor
{
    // Private fields.
    private final MoleHuntGameState _targetState;
    private final PCPluginEvent<GameStateExecutorEndEvent> _endEvent = new PCPluginEvent<>();



    // Constructors.
    public GenericGameStateExecutor(MoleHuntGameState targetState)
    {
        _targetState = Objects.requireNonNull(targetState, "targetState is null");
    }


    // Methods.
    public MoleHuntGameState GetTargetState()
    {
        return _targetState;
    }

    public PCPluginEvent<GameStateExecutorEndEvent> GetEndEvent()
    {
        return _endEvent;
    }
}