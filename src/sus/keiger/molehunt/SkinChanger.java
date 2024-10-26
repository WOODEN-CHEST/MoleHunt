package sus.keiger.molehunt;

import org.bukkit.Bukkit;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import sus.keiger.molehunt.event.IEventDispatcher;
import sus.keiger.molehunt.event.IMoleHuntEventListener;
import sus.keiger.molehunt.game.MoleHuntSettings;
import sus.keiger.molehunt.player.IServerPlayer;
import sus.keiger.molehunt.service.IServerServices;
import sus.keiger.plugincommon.packet.GamePacketEvent;
import sus.keiger.plugincommon.packet.clientbound.PacketPlayerInfo;
import sus.keiger.plugincommon.packet.clientbound.PlayerInfoUpdatePacket;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public class SkinChanger implements IMoleHuntEventListener
{
    // Private fields.
    private final IServerServices _services;
    private final MoleHuntSettings _gameSettings;


    // Constructors.
    public SkinChanger(IServerServices services, MoleHuntSettings settings)
    {
        _services = Objects.requireNonNull(services, "services is null");
        _gameSettings = Objects.requireNonNull(settings, "settings is null");
    }



    // Private methods.
    private void OnPlayerInfoUpdateEvent(GamePacketEvent<PlayerInfoUpdatePacket> event)
    {
        UUID SkinID = Bukkit.getPlayerUniqueId(_gameSettings.GetSkinPlayerName());
        if (SkinID == null)
        {
            return;
        }

        for (PacketPlayerInfo Info : event.GetPacket().GetPlayerInfo())
        {
            Info.SetTexture(Bukkit.createProfile(SkinID).getProperties().stream().filter(property -> property.getName()
                            .equals(PacketPlayerInfo.KEY_TEXTURES)).findFirst().get().getValue());
        }
    }



    // Inherited methods.
    @Override
    public void SubscribeToEvents(IEventDispatcher dispatcher)
    {
        _services.GetPacketController().GetPlayerInfoUpdatePacketEvent()
                .Subscribe(this, this::OnPlayerInfoUpdateEvent);
    }

    @Override
    public void UnsubscribeFromEvents(IEventDispatcher dispatcher)
    {
        _services.GetPacketController().GetPlayerInfoUpdatePacketEvent()
                .Unsubscribe(this);
    }
}