package sus.keiger.molehunt.game.spell;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class GameSpellCollection
{
    // Private fields.
    private Map<String, GameSpellDefinition> _spells = new HashMap<>();


    // Methods.
    public void AddSpell(GameSpellDefinition definition)
    {
        Objects.requireNonNull(definition, "definition is null");
        _spells.put(definition.GetName(), definition);
    }

    public void RemoveSpell(GameSpellDefinition definition)
    {
        Objects.requireNonNull(definition, "definition is null");
        RemoveSpell(definition.GetName());
    }

    public void RemoveSpell(String name)
    {
        _spells.remove(Objects.requireNonNull(name, "name is null"));
    }

    public void ClearSpells()
    {
        _spells.clear();
    }

    public List<GameSpellDefinition> GetSpells()
    {
        return List.copyOf(_spells.values());
    }

    public GameSpellDefinition GetSpell(String name)
    {
        return _spells.get(Objects.requireNonNull(name, "name is null"));
    }
}