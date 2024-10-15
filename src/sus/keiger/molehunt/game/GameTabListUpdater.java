package sus.keiger.molehunt.game;

import com.comphenix.protocol.wrappers.EnumWrappers;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import sus.keiger.molehunt.game.player.DefaultGamePlayer;
import sus.keiger.molehunt.player.IServerPlayer;
import sus.keiger.plugincommon.packet.PCGamePacketController;
import sus.keiger.plugincommon.packet.clientbound.PacketPlayerInfo;
import sus.keiger.plugincommon.packet.clientbound.PlayerInfoRemovePacket;
import sus.keiger.plugincommon.packet.clientbound.PlayerInfoUpdatePacket;

import java.util.Objects;
import java.util.Set;

public class GameTabListUpdater
{
    // Private fields.
    private final PCGamePacketController _packetController;


    // Constructors.
    public GameTabListUpdater(PCGamePacketController packetController)
    {
        _packetController = Objects.requireNonNull(packetController, "packetController is null");
    }


    // Methods.
    public void UpdateSpectatorTabList(IServerPlayer player, List<IServerPlayer>)
    {

    }

    public void UpdateTabList(IServerPlayer player, IGameTeam team)
    {

    }


    // Private methods.
    private void SendNonInnocentTabView(IServerPlayer player)
    {
        PlayerInfoUpdatePacket Packet = new PlayerInfoUpdatePacket();
        Packet.SetPlayerInfoActions(Set.of(EnumWrappers.PlayerInfoAction.UPDATE_DISPLAY_NAME));

        Packet.SetPlayerInfo(GetAllParticipants().stream().filter(participant -> participant != player)
                .map(participant ->
                {
                    PacketPlayerInfo Info = new PacketPlayerInfo(participant.GetMCPlayer());
                    if (_spectators.contains(participant))
                    {
                        Info.SetTabName(Component.text("[Spectator] ").color(NamedTextColor.GRAY).append(Component.text(
                                participant.GetName()).color(NamedTextColor.GRAY)));
                        return Info;
                    }

                    IGameTeam ParticipantTeam = _players.get(participant).GetTeam();
                    Info.SetTabName(Component.text("[%s] ".formatted(ParticipantTeam.GetName())).color(
                                    TextColor.color(ParticipantTeam.GetColor().asRGB()))
                            .append(Component.text(participant.GetName()).color(NamedTextColor.WHITE)));

                }).toList());

        _packetController.SendPacket(Packet, player.GetMCPlayer());
    }

    private void SendInnocentTabView(IServerPlayer player)
    {
        PlayerInfoRemovePacket Packet = new PlayerInfoRemovePacket();
        Packet.SetUUIDs(_activePlayers.stream().filter(activePlayer -> activePlayer != player)
                .map(IServerPlayer::GetUUID).toList());
        _packetController.SendPacket(Packet, player.GetMCPlayer());
    }

    private void SendPlayerTabPackets(IServerPlayer player)
    {
        DefaultGamePlayer TargetPlayer = _players.get(player);

        if (_spectators.contains(player) ||)
        {

        }
    }
}