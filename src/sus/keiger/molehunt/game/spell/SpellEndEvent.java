package sus.keiger.molehunt.game.spell;

import java.util.Objects;

public class SpellEndEvent
{
    // Private fields.
    private final GameSpell _spell;


    // Constructors.
    public SpellEndEvent(GameSpell spell)
    {
        _spell = Objects.requireNonNull(spell, "spell is null");
    }


    // Methods.
    public GameSpell GetSpell()
    {
        return _spell;
    }
}