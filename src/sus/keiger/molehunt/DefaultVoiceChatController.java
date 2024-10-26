package sus.keiger.molehunt;

import de.maxhenkel.voicechat.api.BukkitVoicechatService;
import de.maxhenkel.voicechat.api.VoicechatApi;
import de.maxhenkel.voicechat.api.VoicechatPlugin;
import de.maxhenkel.voicechat.api.events.ClientReceiveSoundEvent;
import de.maxhenkel.voicechat.api.events.EventRegistration;
import de.maxhenkel.voicechat.api.events.MicrophonePacketEvent;
import org.bukkit.Bukkit;
import sus.keiger.molehunt.event.IEventDispatcher;
import sus.keiger.molehunt.voicechat.IVoiceChatController;
import sus.keiger.plugincommon.PCPluginEvent;

import java.util.Objects;

public class DefaultVoiceChatController implements VoicechatPlugin, IVoiceChatController
{
    // Private fields.
    private PCPluginEvent<MicrophonePacketEvent> _microphonePacketEvent = new PCPluginEvent<>();
    private VoicechatApi _api;


    // Private methods.
    private void OnClientReceiveSoundEvent(MicrophonePacketEvent event)
    {
        _microphonePacketEvent.FireEvent(event);
    }


    // Inherited methods.
    @Override
    public String getPluginId()
    {
        return MoleHuntPlugin.PluginID();
    }

    @Override
    public void registerEvents(EventRegistration registration)
    {
        registration.registerEvent(MicrophonePacketEvent.class, this::OnClientReceiveSoundEvent);
    }

    @Override
    public VoicechatApi GetAPI()
    {
        return _api;
    }

    @Override
    public PCPluginEvent<MicrophonePacketEvent> GetMicrophonePacketEvent()
    {
        return _microphonePacketEvent;
    }

    @Override
    public void initialize(VoicechatApi api)
    {
        _api = Objects.requireNonNull(api, "api is null");
    }

    @Override
    public void SubscribeToEvents(IEventDispatcher dispatcher)
    {
        BukkitVoicechatService VoiceChatService = Bukkit.getServicesManager().load(BukkitVoicechatService.class);
        if (VoiceChatService != null)
        {
            VoiceChatService.registerPlugin(this);
        }
    }

    @Override
    public void UnsubscribeFromEvents(IEventDispatcher dispatcher) { }
}
