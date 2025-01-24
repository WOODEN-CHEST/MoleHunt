package sus.keiger.molehunt.game.spell;


import sus.keiger.molehunt.game.IGameServices;

import java.util.Random;

public class RotateSpellDefinition extends GameSpellDefinition
{
    // Constructors.
    public RotateSpellDefinition()
    {
        super("Rotate", "Randomly rotates the victim.", SpellDurationType.Instant, SpellClass.Regular, 0.2d,
                SpellDataRequirement.TargetPlayer);
    }


    // Inherited methods.
    @Override
    public GameSpell CreateSpell(GameSpellArguments args, IGameServices services)
    {
        return new RotateSpell(this, args, services);
    }


    // Types.
    private static class RotateSpell extends GameSpell
    {
        // Constructors.
        public RotateSpell(GameSpellDefinition definition,
                              GameSpellArguments arguments,
                           IGameServices services)
        {
            super(definition, arguments, services);
        }




        // Methods.

        // Inherited methods.
        @Override
        public void OnAdd()
        {
            Random RNG = new Random();
            final float MAX_ROTATION = 180f;
            GetArguments().GetTargetPlayer().GetMCPlayer().setRotation(
                    RNG.nextFloat() * MAX_ROTATION, RNG.nextFloat() * MAX_ROTATION);
        }

        @Override
        public void Execute() { }

        @Override
        public void OnRemove() { }
    }
}
