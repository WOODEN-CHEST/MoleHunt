package sus.keiger.molehunt.game.spell;

public class SpellSettings
{
    // Private fields.
    private final int _spellCooldownTicks;
    private final boolean _isNotified;


    // Constructors.
    public SpellSettings(int spellCooldownTicks, boolean isNotified)
    {
        _spellCooldownTicks = spellCooldownTicks;
        _isNotified = isNotified;
    }


    // Methods.
    public int GetSpellCooldownTicks()
    {
        return _spellCooldownTicks;
    }

    public boolean GetIsNotified()
    {
        return _isNotified;
    }
}