package sus.keiger.molehunt.game.spell;

import java.util.Objects;

import org.bukkit.Bukkit;
import sus.keiger.plugincommon.ITickable;
import sus.keiger.plugincommon.PCPluginEvent;
import sus.keiger.plugincommon.TickClock;

public abstract class GameSpell implements ITickable
{
    // Private fields.
    private GameSpellDefinition _definition;
    private GameSpellArguments _arguments;
    private final SpellServiceProvider _services;
    private TickClock _clock = new TickClock();


    // Constructors.
    public GameSpell(GameSpellDefinition definition, GameSpellArguments arguments, SpellServiceProvider services)
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

    public SpellServiceProvider GetServices()
    {
        return _services;
    }

    public TickClock GetClock()
    {
        return _clock;
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