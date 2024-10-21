package sus.keiger.molehunt.game.spell;

import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import sus.keiger.molehunt.game.IGameServices;
import sus.keiger.plugincommon.PCMath;
import sus.keiger.plugincommon.TickClock;

public class DarknessSpellDefinition extends GameSpellDefinition
{
    // Constructors.
    public DarknessSpellDefinition()
    {
        super("Darkness", "Gives the victim the darkness effect", SpellType.Instant, 0.3d,
                SpellDataRequirement.TargetPlayer);
    }


    // Inherited methods.
    @Override
    public GameSpell CreateSpell(GameSpellArguments args, IGameServices services)
    {
        return new DarknessEffectSpell(this, args, services);
    }


    // Types.
    private static class DarknessEffectSpell extends GameSpell
    {
        // Constructors.
        public DarknessEffectSpell(GameSpellDefinition definition,
                              GameSpellArguments arguments,
                                   IGameServices services)
        {
            super(definition, arguments, services);
        }



        // Inherited methods.
        @Override
        public void OnAdd()
        {
            GetArguments().GetTargetPlayer().GetMCPlayer().addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS,
                    PCMath.SecondsToTicks(20d), 1, true, false));
        }

        @Override
        public void Execute() { }

        @Override
        public void OnRemove() { }
    }
}
