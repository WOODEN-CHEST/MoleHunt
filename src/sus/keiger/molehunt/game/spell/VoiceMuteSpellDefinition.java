package sus.keiger.molehunt.game.spell;

import de.maxhenkel.voicechat.api.events.ClientReceiveSoundEvent;
import de.maxhenkel.voicechat.api.events.MicrophonePacketEvent;
import org.bukkit.Bukkit;
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
        super("VoiceChatMute", "Adds random effects to the victim's voice chat, making them mute.",
                SpellType.Sustained, 0.5d, SpellDataRequirement.TargetPlayer);
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
        public final int DURATION_TICKS = PCMath.SecondsToTicks(45d);


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

            byte[] Data = event.getPacket().getOpusEncodedData();
            Arrays.fill(Data, (byte)0);
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
