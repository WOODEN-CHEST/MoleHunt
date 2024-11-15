package sus.keiger.molehunt.game.spell;

import sus.keiger.molehunt.game.IGameServices;

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
    private Double _manaCost;
    private SpellType _type;


    // Constructors.
    public GameSpellDefinition(String name,
                               String description,
                               SpellType type,
                               Double relativeManaCost,
                               SpellDataRequirement ... requirements)
    {
        _name = Objects.requireNonNull(name, "name is null");
        _description = Objects.requireNonNull(description, "description is null");
        _manaCost = relativeManaCost;
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

    public SpellType GetType()
    {
        return _type;
    }

    public Double GetRelativeManaCost()
    {
        return _manaCost;
    }

    public abstract GameSpell CreateSpell(GameSpellArguments args, IGameServices services);
}