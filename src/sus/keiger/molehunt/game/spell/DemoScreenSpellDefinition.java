package sus.keiger.molehunt.game.spell;

import sus.keiger.molehunt.game.IGameServices;

public class DemoScreenSpellDefinition extends GameSpellDefinition
{
    // Constructors.
    public DemoScreenSpellDefinition()
    {
        super("DemoScreen", "Shows the demo screen to the victim.",
                SpellType.Instant, 0.3d, SpellDataRequirement.TargetPlayer);
    }


    // Inherited methods.
    @Override
    public GameSpell CreateSpell(GameSpellArguments args, IGameServices services)
    {
        return new DemoScreenSpell(this, args, services);
    }


    // Types.
    private static class DemoScreenSpell extends GameSpell
    {
        // Constructors.
        public DemoScreenSpell(GameSpellDefinition definition,
                              GameSpellArguments arguments,
                              IGameServices services)
        {
            super(definition, arguments, services);
        }


        // Inherited methods.
        @Override
        public void OnAdd()
        {
            GetArguments().GetTargetPlayer().GetMCPlayer().showDemoScreen();
        }

        @Override
        public void Execute() { }

        @Override
        public void OnRemove() { }
    }
}
