package sus.keiger.molehunt.game;

import sus.keiger.plugincommon.ITickable;
import sus.keiger.plugincommon.PCMath;

import java.util.Objects;
import java.util.function.Consumer;

public class TickClock implements ITickable
{
    // Private fields.
    private int _ticksLeft;
    private boolean _isRunning;
    private Consumer<TickClock> _tickFunction;
    private Consumer<TickClock> _handler;


    // Methods.
    public void SetTickFunction(Consumer<TickClock> function)
    {
        _tickFunction = Objects.requireNonNull(function, "function is null");
    }

    public void SetHandler(Consumer<TickClock> handler)
    {
        _handler = Objects.requireNonNull(handler, "function is null");
    }

    public boolean GetIsRunning()
    {
        return _isRunning;
    }

    public void SetIsRunning(boolean value)
    {
        _isRunning = value;
    }

    public int GetTicksLeft()
    {
        return _ticksLeft;
    }

    public void SetTicksLeft(int ticks)
    {
        _ticksLeft = Math.max(ticks, 0);
    }

    public void SetSecondsLeft(double seconds)
    {
        SetTicksLeft(PCMath.SecondsToTicks(seconds));
    }


    // Inherited methods.
    @Override
    public void Tick()
    {
        if (_ticksLeft >= 0)
        {
            _ticksLeft--;
        }


        if ((_ticksLeft > 0) && (_tickFunction != null))
        {
            _tickFunction.accept(this);
        }
        else if ((_ticksLeft == 0) && (_handler != null))
        {
            _handler.accept(this);
        }
    }
}