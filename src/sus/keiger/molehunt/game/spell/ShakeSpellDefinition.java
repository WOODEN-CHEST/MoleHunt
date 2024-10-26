package sus.keiger.molehunt.game.spell;


import org.bukkit.entity.Player;
import sus.keiger.molehunt.game.IGameServices;
import sus.keiger.plugincommon.PCMath;

import java.util.Random;

public class ShakeSpellDefinition extends GameSpellDefinition
{
    // Constructors.
    public ShakeSpellDefinition()
    {
        super("Shake", "Shakes the victim's camera.", SpellType.Sustained, 0.8d,
                SpellDataRequirement.TargetPlayer);
    }


    // Inherited methods.
    @Override
    public GameSpell CreateSpell(GameSpellArguments args, IGameServices services)
    {
        return new ShakeSpell(this, args, services);
    }


    // Types.
    private static class ShakeSpell extends GameSpell
    {
        // Fields.
        public final float MAX_ROTATION_CHANGE_PER_TICK_DEG = 9f;
        public final double ROTATION_CHANCE = 0.175d;
        public final int DURATION = PCMath.SecondsToTicks(10d);
        public final Random RNG = new Random();


        // Constructors.
        public ShakeSpell(GameSpellDefinition definition,
                          GameSpellArguments arguments,
                          IGameServices services)
        {
            super(definition, arguments, services);
            SetTicksRemaining(DURATION);
        }


        // Inherited methods.
        @Override
        public void OnAdd() { }

        @Override
        public void Execute()
        {
            if (RNG.nextDouble() > ROTATION_CHANCE)
            {
                return;
            }

            Player MCPlayer = GetArguments().GetTargetPlayer().GetMCPlayer();
            MCPlayer.setRotation(
                    ((RNG.nextFloat() - 0.5f) * 2f * MAX_ROTATION_CHANGE_PER_TICK_DEG) + MCPlayer.getYaw(),
                    ((RNG.nextFloat() - 0.5f) * 2f * MAX_ROTATION_CHANGE_PER_TICK_DEG) + MCPlayer.getPitch());
        }

        @Override
        public void OnRemove() { }
    }
}
