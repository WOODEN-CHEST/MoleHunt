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
import sus.keiger.molehunt.game.spell.GameSpellCollection;
import sus.keiger.molehunt.lobby.IServerLobby;
import sus.keiger.molehunt.service.IServerServices;
import sus.keiger.molehunt.voicechat.IVoiceChatController;
import sus.keiger.plugincommon.IIDProvider;
import sus.keiger.plugincommon.SequentialIDProvider;
import sus.keiger.plugincommon.packet.PCGamePacketController;
import sus.keiger.plugincommon.packet.clientbound.PacketPlayerInfo;
import sus.keiger.plugincommon.packet.clientbound.PlayerInfoUpdatePacket;

import java.util.Objects;
import java.util.Set;

public class DefaultPlayerStateController implements IPlayerStateController
{// Private fields.
    private final IIDProvider _idProvider = new SequentialIDProvider();
    private final IServerServices _serverServices;
    private final MoleHuntSettings _gameSettings;
    private final IServerLobby _lobby;
    private IMoleHuntGameInstance _currentGameInstance;


    // Constructors.
    public DefaultPlayerStateController(IServerServices services,
                                        MoleHuntSettings gameSettings,
                                        IServerLobby lobby)
    {
        _serverServices = Objects.requireNonNull(services, "services is null");
        _gameSettings = Objects.requireNonNull(gameSettings, "gameSettings is null");
        _lobby = Objects.requireNonNull(lobby, "lobby is null");

        CreateNewGameInstance();
    }


    // Private methods.
    private void AddPlayerToLobby(IServerPlayer player)
    {
        _lobby.AddPlayer(player);
    }

    private void OnPlayerJoinEvent(PlayerJoinEvent event)
    {
        IServerPlayer TargetPlayer = _serverServices.GetServerPlayerCollection().GetPlayer(event.getPlayer());
        if (_currentGameInstance.GetState() == MoleHuntGameState.Initializing)
        {
            AddPlayerToLobby(TargetPlayer);
            _lobby.SendMessage(Component.text("%s joined the game".formatted(TargetPlayer.GetName()))
                    .color(NamedTextColor.YELLOW));
        }
        else
        {
            _currentGameInstance.AddSpectator(_serverServices.GetServerPlayerCollection()
                    .GetPlayer(event.getPlayer()));
        }
    }

    private void OnPlayerQuitEvent(PlayerQuitEvent event)
    {
        IServerPlayer TargetPlayer = _serverServices.GetServerPlayerCollection().GetPlayer(event.getPlayer());
        _lobby.RemovePlayer(TargetPlayer);
        _currentGameInstance.RemoveSpectator(TargetPlayer);
    }

    private void OnGameCompleteEvent(MoleHuntCompleteEvent event)
    {
        _serverServices.GetServerPlayerCollection().GetPlayers().forEach(this::AddPlayerToLobby);
        event.GetGameInstance().GetCompleteEvent().Unsubscribe(this);
        CreateNewGameInstance();
    }

    private void CreateNewGameInstance()
    {
        _currentGameInstance = new MoleHuntInstance(_idProvider.GetID(), _serverServices, _gameSettings);
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

        _serverServices.GetServerPlayerCollection().GetPlayers().forEach(player ->
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
