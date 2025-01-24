package sus.keiger.molehunt.game;

import com.comphenix.protocol.wrappers.EnumWrappers;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.*;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import sus.keiger.molehunt.event.*;
import sus.keiger.molehunt.game.event.MoleHuntGameEvent;
import sus.keiger.molehunt.game.player.GamePlayerLifeChangeEvent;
import sus.keiger.molehunt.game.player.IGamePlayer;
import sus.keiger.molehunt.player.*;
import sus.keiger.plugincommon.packet.GamePacketEvent;
import sus.keiger.plugincommon.packet.clientbound.*;

import java.util.Objects;
import java.util.Set;

public class GameTabListUpdater implements IGameStateContaining, IMoleHuntEventListener
{
    // Private fields.
    private final IGameServices _gameServices;

    private final IMoleHuntGameInstance _game;
    private MoleHuntGameState _state = MoleHuntGameState.Initializing;


    // Constructors.
    public GameTabListUpdater(IGameServices gameServices,
                              IMoleHuntGameInstance game)
    {
        _gameServices = Objects.requireNonNull(gameServices, "gameServices is null");
        _game = Objects.requireNonNull(game, "game is null");
    }


    // Private methods.
    private void HideAllPlayers(IServerPlayer targetPlayer)
    {
        PlayerInfoUpdatePacket HidePacket = new PlayerInfoUpdatePacket();

        HidePacket.SetPlayerInfoActions(Set.of(EnumWrappers.PlayerInfoAction.UPDATE_DISPLAY_NAME,
                EnumWrappers.PlayerInfoAction.ADD_PLAYER,
                EnumWrappers.PlayerInfoAction.UPDATE_GAME_MODE));

        HidePacket.SetPlayerInfo(_gameServices.GetGamePlayerCollection().GetParticipants().stream().map(participant ->
        {
            PacketPlayerInfo Info = new PacketPlayerInfo(participant.GetMCPlayer());
            Info.SetTabName(Component.text("???").color(NamedTextColor.WHITE));
            Info.SetGameMode(targetPlayer == participant ? participant.GetMCPlayer().getGameMode()
                    : GameMode.SURVIVAL);
            return Info;
        }).toList());

        _gameServices.GetPacketController().SendPacket(HidePacket, targetPlayer.GetMCPlayer());
    }


    private void ShowAllInfo(IServerPlayer targetPlayer)
    {
        PlayerInfoUpdatePacket Packet = new PlayerInfoUpdatePacket();
        Packet.SetPlayerInfoActions(Set.of(EnumWrappers.PlayerInfoAction.ADD_PLAYER,
                EnumWrappers.PlayerInfoAction.UPDATE_DISPLAY_NAME,
                EnumWrappers.PlayerInfoAction.UPDATE_LISTED,
                EnumWrappers.PlayerInfoAction.UPDATE_GAME_MODE));

        Packet.SetPlayerInfo(_gameServices.GetGamePlayerCollection().GetParticipants().stream()
                .map(participant ->
                {
                    PacketPlayerInfo Info = new PacketPlayerInfo(participant.GetMCPlayer());
                    if (_gameServices.GetGamePlayerCollection().ContainsSpectator(participant))
                    {
                        Info.SetTabName(GetSpectatorTabName(participant.GetName()));
                        return Info;
                    }

                    IGameTeam ParticipantTeam = _gameServices.GetGamePlayerCollection().GetTeamOfPlayer(
                            _gameServices.GetGamePlayerCollection().GetGamePlayer(participant));
                    TextComponent.Builder Builder = Component.text();
                    if (!_gameServices.GetGamePlayerCollection().GetGamePlayer(participant).IsAlive())
                    {
                        Builder.append(Component.text("[Dead]").color(NamedTextColor.RED));
                    }
                    Builder.append(Component.text("[%s] ".formatted(ParticipantTeam.GetName())).color(
                                    TextColor.color(ParticipantTeam.GetColor().asRGB())));
                    Builder.append(Component.text(participant.GetName()).color(NamedTextColor.WHITE)
                            .decoration(TextDecoration.ITALIC, false));

                    Info.SetTabName(Builder.build());
                    Info.SetGameMode(targetPlayer == participant ? participant.GetMCPlayer().getGameMode()
                            : GameMode.SURVIVAL);
                    return Info;
                }).toList());

        _gameServices.GetPacketController().SendPacket(Packet, targetPlayer.GetMCPlayer());
    }

    private void UpdateTabListForPlayers()
    {
        _gameServices.GetPacketController().GetPlayerInfoUpdatePacketEvent().Unsubscribe(this);

        if (_state != MoleHuntGameState.InGame)
        {
            _gameServices.GetGamePlayerCollection().GetParticipants().forEach(this::UpdateNonInGameTabList);
        }
        else
        {
            _gameServices.GetGamePlayerCollection().GetParticipants().forEach(this::UpdateInGameTabList);
        }

        _gameServices.GetPacketController().GetPlayerInfoUpdatePacketEvent()
                .Subscribe(this, this::OnInfoUpdatePacketIntercept);
    }

    private void UpdateNonInGameTabList(IServerPlayer targetPlayer)
    {
        PlayerInfoUpdatePacket Packet = new PlayerInfoUpdatePacket();
        Packet.SetPlayerInfoActions(Set.of(EnumWrappers.PlayerInfoAction.ADD_PLAYER,
                EnumWrappers.PlayerInfoAction.UPDATE_DISPLAY_NAME,
                EnumWrappers.PlayerInfoAction.UPDATE_LISTED,
                EnumWrappers.PlayerInfoAction.UPDATE_GAME_MODE));

        Packet.SetPlayerInfo(_gameServices.GetGamePlayerCollection().GetParticipants().stream().map(participant ->
        {
            PacketPlayerInfo Info = new PacketPlayerInfo(participant.GetMCPlayer());
            Info.SetGameMode(targetPlayer.GetMCPlayer().getGameMode());

            if (_gameServices.GetGamePlayerCollection().ContainsSpectator(participant))
            {
                Info.SetTabName(GetSpectatorTabName(participant.GetName()));
            }
            else
            {
                Info.SetTabName(Component.text(participant.GetName()).color(NamedTextColor.WHITE));
            }
            return Info;
        }).toList());

        _gameServices.GetPacketController().SendPacket(Packet, targetPlayer.GetMCPlayer());
    }

    private void UpdateInGameTabList(IServerPlayer player)
    {
        if (_gameServices.GetGamePlayerCollection().ContainsSpectator(player))
        {
            ShowAllInfo(player);
            return;
        }

        IGamePlayer TargetGamePlayer = _gameServices.GetGamePlayerCollection().GetGamePlayer(player);
        if (TargetGamePlayer == null)
        {
            return;
        }

        if (!TargetGamePlayer.IsAlive() || (_gameServices.GetGamePlayerCollection()
                .GetTeamOfPlayer(TargetGamePlayer).GetType() == GameTeamType.Moles))
        {
            ShowAllInfo(player);
        }
        else
        {
            HideAllPlayers(player);
        }
    }

    private Component GetSpectatorTabName(String playerName)
    {
        return Component.text("[Spectator] ").color(NamedTextColor.GRAY)
                .append(Component.text(playerName).color(NamedTextColor.GRAY));
    }

    private void OnInfoUpdatePacketIntercept(GamePacketEvent<PlayerInfoUpdatePacket> packet)
    {
        IServerPlayer ServerPlayer = _gameServices.GetServerPlayerCollection().GetPlayer(packet.GetPlayer());
        if (_gameServices.GetGamePlayerCollection().ContainsParticipant(ServerPlayer))
        {
            packet.SetIsCancelled(true);
        }
    }

    private void OnParticipantChangeEvent(MoleHuntGameEvent event)
    {
        UpdateTabListForPlayers();
    }

    private void OnPlayerLifeChangeEvent(GamePlayerLifeChangeEvent event)
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
        _gameServices.GetGamePlayerCollection().GetPlayers().forEach(player -> player.GetLifeChangeEvent()
                .Subscribe(this, this::OnPlayerLifeChangeEvent));
    }

    @Override
    public void UnsubscribeFromEvents(IEventDispatcher dispatcher)
    {
        _game.GetParticipantAddEvent().Unsubscribe(this);
        _game.GetParticipantRemoveEvent().Unsubscribe(this);
        _gameServices.GetPacketController().GetPlayerInfoUpdatePacketEvent().Unsubscribe(this);
        _gameServices.GetGamePlayerCollection().GetPlayers().forEach(player -> player.GetLifeChangeEvent()
                .Unsubscribe(this));
    }
}