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
    private SpellDurationType _durationType;
    private SpellClass _targetClass;


    // Constructors.
    public GameSpellDefinition(String name,
                               String description,
                               SpellDurationType durationType,
                               SpellClass targetClass,
                               Double relativeManaCost,
                               SpellDataRequirement ... requirements)
    {
        _name = Objects.requireNonNull(name, "name is null");
        _description = Objects.requireNonNull(description, "description is null");
        _durationType = Objects.requireNonNull(durationType, "durationType is null");
        _targetClass = Objects.requireNonNull(targetClass, "targetClass is null");
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

    public SpellDurationType GetDurationType()
    {
        return _durationType;
    }

    public Double GetRelativeManaCost()
    {
        return _manaCost;
    }

    public SpellClass GetClassification()
    {
        return _targetClass;
    }

    public abstract GameSpell CreateSpell(GameSpellArguments args, IGameServices services);
}