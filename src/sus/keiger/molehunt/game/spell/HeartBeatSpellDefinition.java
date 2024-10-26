package sus.keiger.molehunt.game.spell;

import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import sus.keiger.molehunt.game.IGameServices;
import sus.keiger.plugincommon.PCMath;
import sus.keiger.plugincommon.TickClock;

public class HeartBeatSpellDefinition extends GameSpellDefinition
{
    // Constructors.
    public HeartBeatSpellDefinition()
    {
        super("HeartBeat", "Plays a heart-beat sound to the player.", SpellType.Sustained, 0.1d,
                SpellDataRequirement.TargetPlayer);
    }


    // Inherited methods.
    @Override
    public GameSpell CreateSpell(GameSpellArguments args, IGameServices services)
    {
        return new HeartBeatSpell(this, args, services);
    }


    // Types.
    private static class HeartBeatSpell extends GameSpell
    {
        // Fields.
        public final TickClock _soundClock = new TickClock();
        public final int DURATION_TICKS = PCMath.SecondsToTicks(10);


        // Constructors.
        public HeartBeatSpell(GameSpellDefinition definition,
                              GameSpellArguments arguments,
                              IGameServices services)
        {
            super(definition, arguments, services);

            SetTicksRemaining(DURATION_TICKS);
            _soundClock.SetHandler(this::PlaySound);
            _soundClock.SetIsRunning(true);
            ResetClock();
        }




        // Methods.
        public void PlaySound(TickClock clock)
        {
            GetArguments().GetTargetPlayer().PlaySound(Sound.BLOCK_NOTE_BLOCK_BASEDRUM,
                    GetArguments().GetTargetPlayer().GetMCPlayer().getLocation(), SoundCategory.PLAYERS,
                    0.3f, 0.5f);
            ResetClock();
        }

        public void ResetClock()
        {
            _soundClock.SetTicksLeft(PCMath.TICKS_IN_SECOND);
        }


        // Inherited methods.
        @Override
        public void OnAdd(){  }

        @Override
        public void Execute()
        {
            _soundClock.Tick();
        }

        @Override
        public void OnRemove() { }
    }
}