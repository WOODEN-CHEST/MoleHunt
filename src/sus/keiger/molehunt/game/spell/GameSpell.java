package sus.keiger.molehunt.game.spell;

import java.util.Objects;

import sus.keiger.plugincommon.ITickable;
import sus.keiger.plugincommon.PCPluginEvent;
import sus.keiger.plugincommon.TickClock;

public abstract class GameSpell implements ITickable
{
    // Private fields.
    private GameSpellDefinition _definition;
    private GameSpellArguments _arguments;
    private TickClock _clock = new TickClock();
    private PCPluginEvent<SpellEndEvent> _spellEndEvent = new PCPluginEvent<>();


    // Constructors.
    public GameSpell(GameSpellDefinition definition, GameSpellArguments arguments)
    {
        _definition = Objects.requireNonNull(definition, "definition is null");
        _arguments = Objects.requireNonNull(arguments, "arguments is null");
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

    public PCPluginEvent<SpellEndEvent> GetSpellEndEvent()
    {
        return _spellEndEvent;
    }
}