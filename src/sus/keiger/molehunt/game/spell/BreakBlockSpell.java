package sus.keiger.molehunt.game.spell;


import org.bukkit.FluidCollisionMode;
import org.bukkit.util.RayTraceResult;
import sus.keiger.molehunt.game.IGameServices;

public class BreakBlockSpell extends GameSpellDefinition
{
    // Constructors.
    public BreakBlockSpell()
    {
        super("BreakBlock", "Breaks the block the spectator is looking at.", SpellDurationType.Instant,
                SpellClass.Regular, 0.23d, SpellDataRequirement.LookingAtBlock);
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
        // Fields.
        public final double MAX_RAY_TRACE_DISTANCE = 64d;


        // Constructors.
        public RotateSpell(GameSpellDefinition definition,
                           GameSpellArguments arguments,
                           IGameServices services)
        {
            super(definition, arguments, services);
        }



        // Inherited methods.
        @Override
        public void OnAdd()
        {
            RayTraceResult TraceResult = GetArguments().GetCastingPlayer().GetMCPlayer().rayTraceBlocks(
                    MAX_RAY_TRACE_DISTANCE, FluidCollisionMode.NEVER);

            if (TraceResult == null)
            {
                return;
            }

            if (TraceResult.getHitBlock() != null)
            {
                TraceResult.getHitBlock().breakNaturally();
            }
        }

        @Override
        public void Execute() { }

        @Override
        public void OnRemove() { }
    }
}
