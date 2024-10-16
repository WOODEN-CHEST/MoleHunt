package sus.keiger.molehunt.game.spell;

import sus.keiger.plugincommon.ITickable;
import sus.keiger.plugincommon.IterationSafeSet;

import java.util.List;
import java.util.Objects;
import java.util.Set;

public class SpellContainer implements ITickable
{
    // Private fields.
    private final Set<GameSpell> _spells = new IterationSafeSet<>();


    // Methods.
    void AddSpell(GameSpell spell)
    {
        _spells.add(Objects.requireNonNull(spell, "spell is null"));
    }

    List<GameSpell> GetActiveSpells()
    {
        return List.copyOf(_spells);
    }

    void ClearSpells()
    {
        _spells.clear();
    }

    void RemoveSpell(GameSpell spell)
    {
        _spells.remove(Objects.requireNonNull(spell, "spell is null"));
    }

    void RemoveSpells(GameSpellDefinition definition)
    {
        _spells.stream().filter(spell -> spell.GetDefinition() == definition).forEach(_spells::remove);
    }


    //
    @Override
    public void Tick()
    {
        _spells.forEach(ITickable::Tick);
    }
}
