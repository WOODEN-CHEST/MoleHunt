package sus.keiger.molehunt.player;

import com.comphenix.protocol.wrappers.EnumWrappers;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import sus.keiger.molehunt.IWorldProvider;
import sus.keiger.molehunt.event.IEventDispatcher;
import sus.keiger.molehunt.game.IMoleHuntGameInstance;
import sus.keiger.molehunt.game.MoleHuntGameState;
import sus.keiger.molehunt.game.MoleHuntInstance;
import sus.keiger.molehunt.game.MoleHuntSettings;
import sus.keiger.molehunt.game.event.MoleHuntCompleteEvent;
import sus.keiger.molehunt.lobby.IServerLobby;
import sus.keiger.plugincommon.IIDProvider;
import sus.keiger.plugincommon.packet.PCGamePacketController;
import sus.keiger.plugincommon.packet.clientbound.PacketPlayerInfo;
import sus.keiger.plugincommon.packet.clientbound.PlayerInfoUpdatePacket;

import java.util.Objects;
import java.util.Set;

public class DefaultPlayerStateController implements IPlayerStateController
{// Private fields.
    private final IIDProvider _idProvider;
    private final IWorldProvider _worldProvider;
    private final IEventDispatcher _eventDispatcher;
    private final PCGamePacketController _packetController;
    private final IServerPlayerCollection _players;
    private final MoleHuntSettings _gameSettings;
    private final IServerLobby _lobby;
    private IMoleHuntGameInstance _currentGameInstance;


    // Constructors.
    public DefaultPlayerStateController(IIDProvider idProvider,
                                        PCGamePacketController packetController,
                                        IWorldProvider worldProvider,
                                        IEventDispatcher eventDispatcher,
                                        IServerPlayerCollection players,
                                        MoleHuntSettings gameSettings,
                                        IServerLobby lobby)
    {
        _idProvider = Objects.requireNonNull(idProvider, "idProvider is null");
        _worldProvider = Objects.requireNonNull(worldProvider, "worldProvider is null");
        _eventDispatcher = Objects.requireNonNull(eventDispatcher, "eventDispatcher is null");;
        _packetController = Objects.requireNonNull(packetController, "packetController is null");;
        _players = Objects.requireNonNull(players, "players is null");
        _gameSettings = Objects.requireNonNull(gameSettings, "gameSettings is null");
        _lobby = Objects.requireNonNull(lobby, "lobby is null");

        CreateNewGameInstance();
    }


    // Private methods.
    private void AddPlayerToLobby(IServerPlayer player)
    {
        _lobby.AddPlayer(player);

        PlayerInfoUpdatePacket InfoUpdate = new PlayerInfoUpdatePacket();
        InfoUpdate.SetPlayerInfoActions(Set.of(EnumWrappers.PlayerInfoAction.ADD_PLAYER,
                EnumWrappers.PlayerInfoAction.UPDATE_DISPLAY_NAME));
        InfoUpdate.SetPlayerInfo(_players.GetPlayers().stream().map(serverPlayer ->
        {
            PacketPlayerInfo Info = new PacketPlayerInfo(serverPlayer.GetMCPlayer());
            Info.SetTabName(Component.text(serverPlayer.GetName()).color(NamedTextColor.WHITE));
            return Info;
        }).toList());
        _packetController.SendPacket(InfoUpdate, player.GetMCPlayer());
    }

    private void OnPlayerJoinEvent(PlayerJoinEvent event)
    {
        IServerPlayer TargetPlayer = _players.GetPlayer(event.getPlayer());
        if (_currentGameInstance.GetState() == MoleHuntGameState.Initializing)
        {
            AddPlayerToLobby(TargetPlayer);
            _lobby.SendMessage(Component.text("%s joined the game".formatted(TargetPlayer.GetName()))
                    .color(NamedTextColor.YELLOW));
        }
        else
        {
            _currentGameInstance.AddSpectator(_players.GetPlayer(event.getPlayer()));
        }
    }

    private void OnPlayerQuitEvent(PlayerQuitEvent event)
    {
        IServerPlayer TargetPlayer = _players.GetPlayer(event.getPlayer());
        _lobby.RemovePlayer(TargetPlayer);
        _currentGameInstance.RemoveSpectator(TargetPlayer);
    }

    private void OnGameCompleteEvent(MoleHuntCompleteEvent event)
    {
        _players.GetPlayers().forEach(this::AddPlayerToLobby);
        event.GetGameInstance().GetCompleteEvent().Unsubscribe(this);
        CreateNewGameInstance();
    }

    private void CreateNewGameInstance()
    {
        _currentGameInstance = new MoleHuntInstance(_idProvider.GetID(), _worldProvider, _packetController,
                _eventDispatcher, _players, _gameSettings);
        _currentGameInstance.GetCompleteEvent().Subscribe(this, this::OnGameCompleteEvent);
    }


    // Inherited methods.
    @Override
    public void SubscribeToEvents(IEventDispatcher dispatcher)
    {
        dispatcher.GetPlayerJoinEvent().Subscribe(this, this::OnPlayerJoinEvent);
        dispatcher.GetPlayerQuitEvent().Subscribe(this, this::OnPlayerQuitEvent);
    }

    @Override
    public void UnsubscribeFromEvents(IEventDispatcher dispatcher)
    {
        dispatcher.GetPlayerJoinEvent().Unsubscribe(this);
        dispatcher.GetPlayerQuitEvent().Unsubscribe(this);
    }

    @Override
    public void Tick()
    {
        _currentGameInstance.Tick();
    }

    @Override
    public boolean StartGame()
    {
        if (_currentGameInstance.GetState() != MoleHuntGameState.Initializing)
        {
            return false;
        }

        _players.GetPlayers().forEach(player ->
        {
            _lobby.RemovePlayer(player);
            _currentGameInstance.AddPlayer(player);
        });
        return _currentGameInstance.Start();
    }

    @Override
    public IMoleHuntGameInstance GetActiveGameInstance()
    {
        return _currentGameInstance;
    }
}
