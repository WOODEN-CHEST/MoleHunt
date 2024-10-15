package sus.keiger.molehunt.player;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import sus.keiger.molehunt.event.IEventDispatcher;
import sus.keiger.molehunt.game.IMoleHuntGameInstance;
import sus.keiger.molehunt.game.MoleHuntGameState;
import sus.keiger.molehunt.game.MoleHuntInstance;
import sus.keiger.molehunt.game.MoleHuntSettings;
import sus.keiger.molehunt.lobby.IServerLobby;

import java.util.Objects;

public class DefaultPlayerStateController implements IPlayerStateController
{
    // Private fields.
    private IMoleHuntGameInstance _currentGameInstance;
    private final IServerPlayerCollection _players;
    private final MoleHuntSettings _gameSettings;
    private final IServerLobby _lobby;



    // Constructors.
    public DefaultPlayerStateController(IServerPlayerCollection players,
                                        MoleHuntSettings gameSettings,
                                        IServerLobby lobby)
    {
        _players = Objects.requireNonNull(players, "players is null");
        _gameSettings = Objects.requireNonNull(gameSettings, "gameSettings is null");
        _lobby = Objects.requireNonNull(lobby, "lobby is null");
        _currentGameInstance = new MoleHuntInstance();
    }


    // Private methods.
    private void OnPlayerJoinEvent(PlayerJoinEvent event)
    {
        IServerPlayer TargetPlayer = _players.GetPlayer(event.getPlayer());
        if (_currentGameInstance.GetState() == MoleHuntGameState.Lobby)
        {
            _lobby.AddPlayer(TargetPlayer);
            _lobby.SendMessage(Component.text("%s joined the game".formatted(TargetPlayer.GetName()))
                    .color(NamedTextColor.GOLD));
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
        _currentGameInstance.RemovePlayer(TargetPlayer);
        _currentGameInstance.RemoveSpectator(TargetPlayer);
    }



    // Inherited methods.
    @Override
    public void SendPlayerToLobby()
    {

    }

    @Override
    public void AddPlayerToGame()
    {

    }

    @Override
    public void RemovePlayerFromGame()
    {

    }

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
}
