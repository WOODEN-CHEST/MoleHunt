package sus.keiger.molehunt.game;

import sus.keiger.plugincommon.ITickable;
import sus.keiger.plugincommon.PCPluginEvent;

public interface IGameStateExecutor extends ITickable
{
    MoleHuntGameState GetTargetState();
    void StartState();
    void EndState();
    public PCPluginEvent<GameStateExecutorEndEvent> GetEndEvent();
}