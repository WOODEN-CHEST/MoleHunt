package sus.keiger.molehunt.game.spell;

import sus.keiger.molehunt.game.player.IGamePlayer;
import sus.keiger.plugincommon.PCMath;
import sus.keiger.plugincommon.TickClock;
import sus.keiger.plugincommon.packet.GamePacketEvent;
import sus.keiger.plugincommon.packet.clientbound.SetHealthPacket;
import sus.keiger.plugincommon.player.PlayerFunctions;

import java.util.Random;

public class HealthScrambleSpellDefinition extends GameSpellDefinition
{
    // Constructors.
    public HealthScrambleSpellDefinition()
    {
        super("HealthScramble", "Shows a random amount of health and food for the victim", SpellType.Sustained,
                SpellDataRequirement.TargetPlayer);
    }


    // Inherited methods.
    @Override
    public GameSpell CreateSpell(GameSpellArguments args, SpellServiceProvider services)
    {
        return new HealthScrambleSpell(this, args, services);
    }


    // Types.
    private static class HealthScrambleSpell extends GameSpell
    {
        // Fields.
        public final TickClock _scrambleClock = new TickClock();
        public final int DURATION_TICKS = PCMath.SecondsToTicks(30);
        private final int TICkS_BEFORE_SCRAMBLE = PCMath.SecondsToTicks(5d);


        // Constructors.
        public HealthScrambleSpell(GameSpellDefinition definition,
                              GameSpellArguments arguments,
                              SpellServiceProvider services)
        {
            super(definition, arguments, services);

            SetTicksRemaining(DURATION_TICKS);
            _scrambleClock.SetIsRunning(true);
            ResetScrambleClock();
            _scrambleClock.SetHandler(this::OnScrambleHealth);
        }


        // Methods.
        public void OnPacketIntercept(GamePacketEvent<SetHealthPacket> event)
        {
            IGamePlayer TargetedPlayer = GetServices().GetGamePlayers().GetGamePlayer(
                    GetServices().GetServerPlayers().GetPlayer(event.GetPlayer()));

            if ((TargetedPlayer == null) || (TargetedPlayer.GetServerPlayer() != GetArguments().GetTargetPlayer()))
            {
                return;
            }

            SetPacketData(TargetedPlayer, event.GetPacket());
        }

        public void OnScrambleHealth(TickClock clock)
        {
            GetServices().GetPacketController().SendPacket(new SetHealthPacket(),
                    GetArguments().GetTargetPlayer().GetMCPlayer());
            ResetScrambleClock();
        }

        public void SetPacketData(IGamePlayer targetedPlayer, SetHealthPacket packet)
        {
            Random RNG = new Random();
            packet.SetHealth((float)(targetedPlayer.GetMaxHealth().GetValue() * RNG.nextDouble()));
            packet.SetFood(PlayerFunctions.MAX_FOOD / 2);
        }

        public void ResetScrambleClock()
        {
            _scrambleClock.SetTicksLeft(TICkS_BEFORE_SCRAMBLE);
        }


        // Inherited methods.
        @Override
        public void OnAdd()
        {
            GetServices().GetPacketController().GetSetHealthPacketEvent().Subscribe(this, this::OnPacketIntercept);
        }

        @Override
        public void Execute()
        {
            _scrambleClock.Tick();
        }

        @Override
        public void OnRemove()
        {
            GetServices().GetPacketController().GetSetHealthPacketEvent().Unsubscribe(this);

            SetHealthPacket Packet = new SetHealthPacket();
            Packet.SetHealth((float)GetArguments().GetTargetPlayer().GetMCPlayer().getHealth());
            Packet.SetFood(GetArguments().GetTargetPlayer().GetMCPlayer().getFoodLevel());
            GetServices().GetPacketController().SendPacket(Packet, GetArguments().GetTargetPlayer().GetMCPlayer());
        }
    }
}