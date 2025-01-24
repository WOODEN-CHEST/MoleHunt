package sus.keiger.molehunt.game.spell;

import de.maxhenkel.voicechat.api.events.MicrophonePacketEvent;
import sus.keiger.molehunt.game.IGameServices;
import sus.keiger.molehunt.player.IServerPlayer;
import sus.keiger.plugincommon.PCMath;

import java.util.Arrays;
import java.util.Random;

public class VoiceMuteSpellDefinition extends GameSpellDefinition
{
    // Constructors.
    public VoiceMuteSpellDefinition()
    {
        super("VoiceChatMute", "Randomizes the audio samples coming from the victim's microphone.",
                SpellDurationType.Sustained, SpellClass.Regular, 0.5d, SpellDataRequirement.TargetPlayer);
    }


    // Inherited methods.
    @Override
    public GameSpell CreateSpell(GameSpellArguments args, IGameServices services)
    {
        return new VoiceMuteSpell(this, args, services);
    }


    // Types.
    private static class VoiceMuteSpell extends GameSpell
    {
        // Fields.
        public final Random RNG = new Random();
        public final int DURATION_TICKS = PCMath.SecondsToTicks(30);


        // Constructors.
        public VoiceMuteSpell(GameSpellDefinition definition,
                              GameSpellArguments arguments,
                              IGameServices services)
        {
            super(definition, arguments, services);

            SetTicksRemaining(DURATION_TICKS);
        }


        // Methods.
        public void OnClientReceiveSoundEvent(MicrophonePacketEvent event)
        {
            if (event.getSenderConnection() == null)
            {
                return;
            }

            IServerPlayer Sender = GetServices().GetServerPlayerCollection().GetPlayer(
                    event.getSenderConnection().getPlayer().getUuid());

            if (Sender != GetArguments().GetTargetPlayer())
            {
                return;
            }

            byte[] OPUSData = event.getPacket().getOpusEncodedData();
            short[] PCMData = GetServices().GetVoiceChatController().GetAPI().createDecoder().decode(OPUSData);

            Arrays.fill(PCMData, (short)0);

            byte[] NewOPUSData = GetServices().GetVoiceChatController().GetAPI().createEncoder().encode(PCMData);
            for (int i = 0; (i < NewOPUSData.length) && (i < OPUSData.length); i++)
            {
                OPUSData[i] = NewOPUSData[i];
            }
        }


        // Inherited methods.
        @Override
        public void OnAdd()
        {
            GetServices().GetVoiceChatController().GetMicrophonePacketEvent()
                    .Subscribe(this, this::OnClientReceiveSoundEvent);
        }

        @Override
        public void Execute() { }

        @Override
        public void OnRemove()
        {
            GetServices().GetVoiceChatController().GetMicrophonePacketEvent()
                    .Unsubscribe(this);
        }
    }
}
