package sus.keiger.molehunt.game.spell;


import sus.keiger.molehunt.game.IGameServices;

public class GiftFromMagicianSpellDefinition extends GameSpellDefinition
{
    // Constructors.
    public GiftFromMagicianSpellDefinition()
    {
        super("GiftFromMagician", "Grants the victim the ability to cast a spell.",
                SpellDurationType.Instant, SpellClass.Meta, 0.6d, SpellDataRequirement.TargetPlayer);
    }


    // Inherited methods.
    @Override
    public GameSpell CreateSpell(GameSpellArguments args, IGameServices services)
    {
        return new GiftFromMagicianSpell(this, args, services);
    }


    // Types.
    private static class GiftFromMagicianSpell extends GameSpell
    {
        // Fields.


        // Constructors.
        public GiftFromMagicianSpell(GameSpellDefinition definition,
                              GameSpellArguments arguments,
                              IGameServices services)
        {
            super(definition, arguments, services);
        }

        // Inherited methods.
        @Override
        public void OnAdd() { }

        @Override
        public void Execute() { }

        @Override
        public void OnRemove() { }

        @Override
        public double GetGiftedManaAmount()
        {
            return 0.5d;
        }
    }
}
