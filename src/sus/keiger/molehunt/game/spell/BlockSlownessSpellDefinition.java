package sus.keiger.molehunt.game.spell;

import sus.keiger.molehunt.game.IGameServices;
import sus.keiger.molehunt.game.IGameTeam;
import sus.keiger.molehunt.game.player.IGamePlayer;
import sus.keiger.plugincommon.PCMath;
import sus.keiger.plugincommon.value.GameModifiableValueModifier;
import sus.keiger.plugincommon.value.GameModifiableValueOperator;

public class BlockSlownessSpellDefinition extends GameSpellDefinition
{
    // Inherited methods.
    public BlockSlownessSpellDefinition()
    {
        super("BlockSlowness", "Makes the victim dig blocks slower and reduces their block reach range",
                SpellType.Instant, 0.4d, SpellDataRequirement.TargetPlayer);
    }

    @Override
    public GameSpell CreateSpell(GameSpellArguments args, IGameServices services)
    {
        return new BlockSlownessSpell(this, args, services);
    }


    // Types.
    private static class BlockSlownessSpell extends GameSpell
    {
        // Fields.
        public final int SLOWNESS_DURATION_TICKS = PCMath.SecondsToTicks(30d);
        public final double MINING_SPEED_MULTIPLIER = 0.7d;
        public final double BLOCK_REACH_MULTIPLIER = 0.75d;



        // Constructors.
        public BlockSlownessSpell(GameSpellDefinition definition,
                                  GameSpellArguments arguments,
                                  IGameServices services)
        {
            super(definition, arguments, services);
        }


        // Inherited methods.
        @Override
        public void OnAdd()
        {
            IGamePlayer GamePlayer = GetServices().GetGamePlayerCollection()
                    .GetGamePlayer(GetArguments().GetTargetPlayer());
            if (GamePlayer == null)
            {
                return;
            }

            GamePlayer.GetBlockReach().AddModifier(new GameModifiableValueModifier(
                    GameModifiableValueOperator.Multiply, BLOCK_REACH_MULTIPLIER, SLOWNESS_DURATION_TICKS));
            GamePlayer.GetMiningSpeed().AddModifier(new GameModifiableValueModifier(
                    GameModifiableValueOperator.Multiply, MINING_SPEED_MULTIPLIER, SLOWNESS_DURATION_TICKS));
        }

        @Override
        public void Execute() { }

        @Override
        public void OnRemove() { }
    }
}
