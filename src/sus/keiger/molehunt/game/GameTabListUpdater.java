package sus.keiger.molehunt.game;

import com.comphenix.protocol.wrappers.EnumWrappers;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import sus.keiger.molehunt.game.player.GamePlayerCollection;
import sus.keiger.molehunt.game.player.IGamePlayer;
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
    public void UpdateSpectatorTabList(IServerPlayer targetPlayer, GamePlayerCollection players)
    {
        ShowAllInfo(targetPlayer, players);
    }

    public void UpdateGamePlayerTabList(IGamePlayer targetPlayer, GamePlayerCollection players)
    {
        if (targetPlayer.IsAlive() && targetPlayer.GetTeam().GetType() == GameTeamType.Innocents)
        {
            HideAllPlayers(targetPlayer.GetServerPlayer(), players);
        } else
        {
            ShowAllInfo(targetPlayer.GetServerPlayer(), players);
        }
    }

    public void UpdateNonInGameTabList(IServerPlayer targetPlayer, GamePlayerCollection players)
    {
        PlayerInfoUpdatePacket Packet = new PlayerInfoUpdatePacket();
        Packet.SetPlayerInfoActions(Set.of(EnumWrappers.PlayerInfoAction.ADD_PLAYER,
                EnumWrappers.PlayerInfoAction.UPDATE_DISPLAY_NAME, EnumWrappers.PlayerInfoAction.UPDATE_LISTED));

        Packet.SetPlayerInfo(players.GetParticipants().stream().map(participant ->
        {
            PacketPlayerInfo Info = new PacketPlayerInfo(participant.GetMCPlayer());

            if (players.ContainsSpectator(participant))
            {
                Info.SetTabName(GetSpectatorTabName(participant.GetName()));
            }
            else
            {
                Info.SetTabName(Component.text(participant.GetName()).color(NamedTextColor.WHITE));
            }
            return Info;
        }).toList());

        _packetController.SendPacket(Packet, targetPlayer.GetMCPlayer());
    }


    // Private methods.
    private void HideAllPlayers(IServerPlayer targetPlayer, GamePlayerCollection players)
    {
        PlayerInfoRemovePacket RemovePacket = new PlayerInfoRemovePacket();

        RemovePacket.SetUUIDs(players.GetPlayers().stream().filter(player -> player.GetServerPlayer() != targetPlayer)
                .map(gamePlayer -> gamePlayer.GetMCPlayer().getUniqueId()).toList());

        _packetController.SendPacket(RemovePacket, targetPlayer.GetMCPlayer());
    }


    private void ShowAllInfo(IServerPlayer targetPlayer, GamePlayerCollection players)
    {
        PlayerInfoUpdatePacket Packet = new PlayerInfoUpdatePacket();
        Packet.SetPlayerInfoActions(Set.of(EnumWrappers.PlayerInfoAction.ADD_PLAYER,
                EnumWrappers.PlayerInfoAction.UPDATE_DISPLAY_NAME, EnumWrappers.PlayerInfoAction.UPDATE_LISTED));

        Packet.SetPlayerInfo(players.GetParticipants().stream()
                .map(participant ->
                {
                    PacketPlayerInfo Info = new PacketPlayerInfo(participant.GetMCPlayer());
                    if (players.ContainsSpectator(participant))
                    {
                        Info.SetTabName(GetSpectatorTabName(participant.GetName()));
                        return Info;
                    }

                    IGameTeam ParticipantTeam = players.GetGamePlayer(participant).GetTeam();
                    TextComponent.Builder Builder = Component.text();
                    if (!players.GetGamePlayer(participant).IsAlive())
                    {
                        Builder.append(Component.text("[Dead]").color(NamedTextColor.RED));
                    }
                    Builder.append(Component.text("[%s] ".formatted(ParticipantTeam.GetName())).color(
                                    TextColor.color(ParticipantTeam.GetColor().asRGB())));
                    Builder.append(Component.text(participant.GetName()).color(NamedTextColor.WHITE));

                    Info.SetTabName(Builder.build());
                    return Info;
                }).toList());

        _packetController.SendPacket(Packet, targetPlayer.GetMCPlayer());
    }

    private Component GetSpectatorTabName(String playerName)
    {
        return Component.text("[Spectator] ").color(NamedTextColor.GRAY)
                .append(Component.text(playerName).color(NamedTextColor.GRAY));
    }
}