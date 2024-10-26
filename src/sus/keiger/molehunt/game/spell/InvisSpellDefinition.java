package sus.keiger.molehunt.game.spell;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.EntityType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import sus.keiger.molehunt.game.IGameServices;
import sus.keiger.plugincommon.PCMath;
import sus.keiger.plugincommon.entity.EntityFunctions;

import java.util.Random;

public class InvisSpellDefinition extends GameSpellDefinition
{
    // Constructors.
    public InvisSpellDefinition()
    {
        super("Invisibility", "Makes the victim invisible for 15 seconds.",
                SpellType.Instant, 0.35d, SpellDataRequirement.TargetPlayer);
    }


    // Inherited methods.
    @Override
    public GameSpell CreateSpell(GameSpellArguments args, IGameServices services)
    {
        return new InvisibilitySpell(this, args, services);
    }


    // Types.
    private static class InvisibilitySpell extends GameSpell
    {
        // Fields.
        public final int DURATION_TICKS = PCMath.SecondsToTicks(15d);


        // Constructors.
        public InvisibilitySpell(GameSpellDefinition definition, GameSpellArguments arguments, IGameServices services)
        {
            super(definition, arguments, services);
        }


        // Inherited methods.
        @Override
        public void OnAdd()
        {
            GetArguments().GetTargetPlayer().GetMCPlayer().addPotionEffect(new PotionEffect(
                    PotionEffectType.INVISIBILITY, DURATION_TICKS, 0, true, false, false));
        }

        @Override
        public void Execute() { }

        @Override
        public void OnRemove() { }
    }
}