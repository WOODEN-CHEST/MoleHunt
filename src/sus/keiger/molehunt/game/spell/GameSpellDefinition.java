package sus.keiger.molehunt.game.spell;

import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class GameSpellDefinition
{
    // Private fields.
    private String _name;
    private Set<SpellDataRequirement> _dataRequirements;
    private String _description;
    private boolean _areDuplicatesAllowed;


    // Constructors.
    public GameSpellDefinition(String name,
                               String description,
                               boolean areDuplicatesAllowed,
                               SpellDataRequirement ... requirements)
    {
        _name = Objects.requireNonNull(name, "name is null");
        _description = Objects.requireNonNull(description, "description is null");
        _areDuplicatesAllowed = areDuplicatesAllowed;
        _dataRequirements = Arrays.stream(requirements).collect(Collectors.toSet());
    }


    // Methods.
    public String GetName()
    {
        return _name;
    }

    public String GetDescription()
    {
        return _description;
    }

    public Set<SpellDataRequirement> GetRequirements()
    {
        return _dataRequirements;
    }

    public boolean GetAreDuplicatesAllowed()
    {
        return _areDuplicatesAllowed;
    }

    public abstract GameSpell CreateSpell(GameSpellArguments args);
}