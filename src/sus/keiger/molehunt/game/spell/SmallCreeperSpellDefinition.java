package sus.keiger.molehunt.game.spell;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.EntityType;
import org.bukkit.event.entity.EntityDamageEvent;
import sus.keiger.molehunt.game.IGameServices;
import sus.keiger.plugincommon.PCMath;
import sus.keiger.plugincommon.entity.EntityFunctions;

import java.util.Random;

public class SmallCreeperSpellDefinition extends GameSpellDefinition
{
    // Constructors.
    public SmallCreeperSpellDefinition()
    {
        super("SmallCreeper", "Spawns a small and friendly creeper! Ok, maybe not so friendly.",
                SpellDurationType.Sustained, SpellClass.Regular, 0.85d, SpellDataRequirement.TargetPlayer);
    }


    // Inherited methods.
    @Override
    public GameSpell CreateSpell(GameSpellArguments args, IGameServices services)
    {
        return new SmallCreeperSpell(this, args, services);
    }


    // Types.
    private static class SmallCreeperSpell extends GameSpell
    {
        // Fields.
        public String[] SpawnMessages = new String[] {
                "This lil fella looks innocent, but do NOT stand near it when it goes off!",
                "What a cute small creeper! I sure do hope it won't cause a massive explosion.",
                "This creeper isn't as friendly as it looks like, kill it fast",
                "RUN!!!",
                "This creeper is going to kill you if you stand too close.",
                "The creeper's just charging up its mega attack, be sure to stay away.",
                "You have exactly 15 seconds to get away from this creeper or you're dead.",
                "15 seconds and counting until your death."
        };

        public int FUSE_DURATION_TICKS = PCMath.SecondsToTicks(15d);
        public int EXPLOSION_RADIUS = 15;
        public double SCALE = 0.3d;
        public Creeper SummonedCreeper;
        public final double RECOVERY_HEARTS = 1d;


        // Constructors.
        public SmallCreeperSpell(GameSpellDefinition definition, GameSpellArguments arguments, IGameServices services)
        {
            super(definition, arguments, services);
            SetTicksRemaining(FUSE_DURATION_TICKS + PCMath.TICKS_IN_SECOND);
        }


        // Methods.
        public void OnEntityDamageEvent(EntityDamageEvent event)
        {
            if ((event.getEntity() != GetArguments().GetTargetPlayer().GetMCPlayer())
                    || event.getDamageSource().getCausingEntity() != SummonedCreeper)
            {
                return;
            }

            event.setDamage(GetArguments().GetTargetPlayer().GetMCPlayer().getHealth() - PCMath.HeartsToHealth(1d));
        }


        // Inherited methods.
        @Override
        public void OnAdd()
        {
            GetArguments().GetTargetPlayer().SendMessage(Component.text(
                    SpawnMessages[new Random().nextInt(SpawnMessages.length)]).color(NamedTextColor.RED));
            SummonedCreeper = (Creeper)GetServices().GetLocationProvider().GetOverworld().spawnEntity(
                    GetArguments().GetTargetPlayer().GetMCPlayer().getLocation(), EntityType.CREEPER);
            SummonedCreeper.setExplosionRadius(EXPLOSION_RADIUS);
            SummonedCreeper.setMaxFuseTicks(FUSE_DURATION_TICKS);
            SummonedCreeper.setFuseTicks(0);
            SummonedCreeper.setIgnited(true);
            SummonedCreeper.setAI(false);
            EntityFunctions.TrySetAttributeBaseValue(SummonedCreeper, Attribute.GENERIC_SCALE, SCALE);

            GetServices().GetEventDispatcher().GetEntityDamageEvent().Subscribe(this, this::OnEntityDamageEvent);
        }

        @Override
        public void Execute() { }

        @Override
        public void OnRemove()
        {
            GetServices().GetEventDispatcher().GetEntityDamageEvent().Unsubscribe(this);
        }
    }
}