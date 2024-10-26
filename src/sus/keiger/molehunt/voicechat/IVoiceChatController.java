package sus.keiger.molehunt.voicechat;

import de.maxhenkel.voicechat.api.VoicechatApi;
import de.maxhenkel.voicechat.api.events.MicrophonePacketEvent;
import sus.keiger.molehunt.event.IMoleHuntEventListener;
import sus.keiger.plugincommon.PCPluginEvent;

public interface IVoiceChatController extends IMoleHuntEventListener
{
    VoicechatApi GetAPI();
    PCPluginEvent<MicrophonePacketEvent> GetMicrophonePacketEvent();
}