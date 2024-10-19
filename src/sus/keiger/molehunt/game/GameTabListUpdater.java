package sus.keiger.molehunt.game;

import com.comphenix.protocol.wrappers.EnumWrappers;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.*;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import sus.keiger.molehunt.event.*;
import sus.keiger.molehunt.game.event.MoleHuntGameEvent;
import sus.keiger.molehunt.game.player.GamePlayerCollection;
import sus.keiger.molehunt.player.*;
import sus.keiger.plugincommon.packet.GamePacketEvent;
import sus.keiger.plugincommon.packet.PCGamePacketController;
import sus.keiger.plugincommon.packet.clientbound.*;

import java.util.Objects;
import java.util.Set;

public class GameTabListUpdater implements IGameStateContaining, IMoleHuntEventListener
{
    // Private fields.
    private final PCGamePacketController _packetController;
    private final IServerPlayerCollection _serverPlayers;
    private final GamePlayerCollection _gamePlayers;
    private final IMoleHuntGameInstance _game;
    private MoleHuntGameState _state = MoleHuntGameState.Initializing;


    // Constructors.
    public GameTabListUpdater(PCGamePacketController packetController,
                              IServerPlayerCollection serverPlayers,
                              GamePlayerCollection gamePlayers,
                              IMoleHuntGameInstance game)
    {
        _packetController = Objects.requireNonNull(packetController, "packetController is null");
        _serverPlayers = Objects.requireNonNull(serverPlayers, "serverPlayers is null");
        _gamePlayers = Objects.requireNonNull(gamePlayers, "gamePlayers is null");
        _game = Objects.requireNonNull(game, "game is null");
    }


    // Private methods.
    private void HideAllPlayers(IServerPlayer targetPlayer)
    {
        PlayerInfoUpdatePacket HidePacket = new PlayerInfoUpdatePacket();

        HidePacket.SetPlayerInfoActions(Set.of(EnumWrappers.PlayerInfoAction.UPDATE_DISPLAY_NAME,
                EnumWrappers.PlayerInfoAction.ADD_PLAYER,
                EnumWrappers.PlayerInfoAction.UPDATE_GAME_MODE));

        HidePacket.SetPlayerInfo(_gamePlayers.GetParticipants().stream().map(participant ->
        {
            PacketPlayerInfo Info = new PacketPlayerInfo(participant.GetMCPlayer());
            Info.SetTabName(Component.text("???").color(NamedTextColor.WHITE));
            Info.SetGameMode(GameMode.SURVIVAL);
            return Info;
        }).toList());

        _packetController.SendPacket(HidePacket, targetPlayer.GetMCPlayer());
    }


    private void ShowAllInfo(IServerPlayer targetPlayer)
    {
        PlayerInfoUpdatePacket Packet = new PlayerInfoUpdatePacket();
        Packet.SetPlayerInfoActions(Set.of(EnumWrappers.PlayerInfoAction.ADD_PLAYER,
                EnumWrappers.PlayerInfoAction.UPDATE_DISPLAY_NAME,
                EnumWrappers.PlayerInfoAction.UPDATE_LISTED));

        Packet.SetPlayerInfo(_gamePlayers.GetParticipants().stream()
                .map(participant ->
                {
                    PacketPlayerInfo Info = new PacketPlayerInfo(participant.GetMCPlayer());
                    if (_gamePlayers.ContainsSpectator(participant))
                    {
                        Info.SetTabName(GetSpectatorTabName(participant.GetName()));
                        return Info;
                    }

                    IGameTeam ParticipantTeam = _gamePlayers.GetTeamOfPlayer(_gamePlayers.GetGamePlayer(participant));
                    TextComponent.Builder Builder = Component.text();
                    if (!_gamePlayers.GetGamePlayer(participant).IsAlive())
                    {
                        Builder.append(Component.text("[Dead]").color(NamedTextColor.RED));
                    }
                    Builder.append(Component.text("[%s] ".formatted(ParticipantTeam.GetName())).color(
                                    TextColor.color(ParticipantTeam.GetColor().asRGB())));
                    Builder.append(Component.text(participant.GetName()).color(NamedTextColor.WHITE)
                            .decoration(TextDecoration.ITALIC, false));

                    Info.SetTabName(Builder.build());
                    Info.SetGameMode(GameMode.SURVIVAL);
                    return Info;
                }).toList());

        _packetController.SendPacket(Packet, targetPlayer.GetMCPlayer());
    }

    private void UpdateTabListForPlayers()
    {
        _packetController.GetPlayerInfoUpdatePacketEvent().Unsubscribe(this);

        if (_state != MoleHuntGameState.InGame)
        {
            _gamePlayers.GetParticipants().forEach(this::UpdateNonInGameTabList);
        }
        else
        {
            _gamePlayers.GetParticipants().forEach(this::UpdateInGameTabList);
        }

        _packetController.GetPlayerInfoUpdatePacketEvent().Subscribe(this, this::OnInfoUpdatePacketIntercept);
    }

    private void UpdateNonInGameTabList(IServerPlayer targetPlayer)
    {
        PlayerInfoUpdatePacket Packet = new PlayerInfoUpdatePacket();
        Packet.SetPlayerInfoActions(Set.of(EnumWrappers.PlayerInfoAction.ADD_PLAYER,
                EnumWrappers.PlayerInfoAction.UPDATE_DISPLAY_NAME, EnumWrappers.PlayerInfoAction.UPDATE_LISTED));

        Packet.SetPlayerInfo(_gamePlayers.GetParticipants().stream().map(participant ->
        {
            PacketPlayerInfo Info = new PacketPlayerInfo(participant.GetMCPlayer());

            if (_gamePlayers.ContainsSpectator(participant))
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

    private void UpdateInGameTabList(IServerPlayer targetPlayer)
    {
        if (_gamePlayers.ContainsSpectator(targetPlayer) || (_gamePlayers.GetTeamOfPlayer(
                _gamePlayers.GetGamePlayer(targetPlayer)).GetType() == GameTeamType.Moles))
        {
            ShowAllInfo(targetPlayer);
        }
        else
        {
            HideAllPlayers(targetPlayer);
        }
    }

    private Component GetSpectatorTabName(String playerName)
    {
        return Component.text("[Spectator] ").color(NamedTextColor.GRAY)
                .append(Component.text(playerName).color(NamedTextColor.GRAY));
    }

    private void OnInfoUpdatePacketIntercept(GamePacketEvent<PlayerInfoUpdatePacket> packet)
    {
        IServerPlayer ServerPlayer = _serverPlayers.GetPlayer(packet.GetPlayer());
        if (_gamePlayers.ContainsParticipant(ServerPlayer))
        {
            packet.SetIsCancelled(true);
        }
    }

    private void OnParticipantChangeEvent(MoleHuntGameEvent event)
    {
        UpdateTabListForPlayers();
    }

    @Override
    public void SetState(MoleHuntGameState state)
    {
        _state = Objects.requireNonNull(state, "state is null");;
        UpdateTabListForPlayers();
    }

    @Override
    public void SubscribeToEvents(IEventDispatcher dispatcher)
    {
        _game.GetParticipantAddEvent().Subscribe(this, this::OnParticipantChangeEvent);
        _game.GetParticipantRemoveEvent().Subscribe(this, this::OnParticipantChangeEvent);
    }

    @Override
    public void UnsubscribeFromEvents(IEventDispatcher dispatcher)
    {
        _game.GetParticipantAddEvent().Unsubscribe(this);
        _game.GetParticipantRemoveEvent().Unsubscribe(this);
        _packetController.GetPlayerInfoUpdatePacketEvent().Unsubscribe(this);
    }
}