package sus.keiger.molehunt.game.spell;

import java.util.Objects;

import sus.keiger.molehunt.game.IGameServices;
import sus.keiger.plugincommon.ITickable;
import sus.keiger.plugincommon.TickClock;

public abstract class GameSpell implements ITickable
{
    // Private fields.
    private final GameSpellDefinition _definition;
    private final GameSpellArguments _arguments;
    private final IGameServices _services;
    private final TickClock _clock = new TickClock();


    // Constructors.
    public GameSpell(GameSpellDefinition definition, GameSpellArguments arguments, IGameServices services)
    {
        _definition = Objects.requireNonNull(definition, "definition is null");
        _arguments = Objects.requireNonNull(arguments, "arguments is null");
        _services = Objects.requireNonNull(services, "services is null");
        _clock.SetIsRunning(true);
    }


    // Methods.
    public GameSpellDefinition GetDefinition()
    {
        return _definition;
    }

    public GameSpellArguments GetArguments()
    {
        return _arguments;
    }

    public int GetTicksRemaining()
    {
        return _clock.GetTicksLeft();
    }

    public void SetTicksRemaining(int value)
    {
        _clock.SetTicksLeft(value);
    }

    public IGameServices GetServices()
    {
        return _services;
    }

    public double GetRelativeManaCost()
    {
        Double Cost = GetDefinition().GetRelativeManaCost();
        if (Cost != null)
        {
            return Cost;
        }
        return 0d;
    }

    public double GetGiftedManaAmount()
    {
        return 0d;
    }

    public abstract void OnAdd();

    public abstract void Execute();

    public abstract void OnRemove();


    // Inherited methods.

    @Override
    public final void Tick()
    {
        _clock.Tick();
        Execute();
    }
}