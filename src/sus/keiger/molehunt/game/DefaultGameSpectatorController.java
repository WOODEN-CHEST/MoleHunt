package sus.keiger.molehunt.game;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import sus.keiger.molehunt.game.player.GamePlayerCollection;
import sus.keiger.molehunt.game.player.GamePlayerLifeChangeEvent;
import sus.keiger.molehunt.player.IServerPlayer;
import sus.keiger.plugincommon.player.PlayerFunctions;

import java.util.List;
import java.util.Objects;

public class DefaultGameSpectatorController implements IGameSpectatorController
{
    // Private fields.
    private final IGameServices _gameServices;
    private MoleHuntGameState _state = MoleHuntGameState.PreGame;




    // Constructors.
    public DefaultGameSpectatorController(IGameServices gameServices)
    {
        _gameServices = Objects.requireNonNull(gameServices, "gameServices is null");
    }


    // Private methods.
    private void InitializeSpectator(IServerPlayer player)
    {
        Player MCPlayer = player.GetMCPlayer();
        MCPlayer.clearActivePotionEffects();
        PlayerFunctions.ResetAttributes(MCPlayer);
        MCPlayer.setGameMode(GameMode.SPECTATOR);
        MCPlayer.teleport(_gameServices.GetLocationProvider().GetCenterLocation());
        ApplySpectatorState(player);
    }

    private void ApplySpectatorState(IServerPlayer player)
    {
        switch (_state)
        {
            case PreGame -> InitPreStartSpectator(player);
            case InGame -> InitInGameSpectator(player);
            case PostGame -> InitPostGameSpectator(player);
            default -> InitDefaultSpectator(player);
        }
    }

    private void OnGamePlayerLifeChangeEvent(GamePlayerLifeChangeEvent event)
    {
        _gameServices.GetGamePlayerCollection().GetSpectators().forEach(this::ApplySpectatorState);
    }

    private void InitPreStartSpectator(IServerPlayer player)
    {
        ClearSpectator(player);
    }

    private void InitInGameSpectator(IServerPlayer player)
    {
        _gameServices.GetScoreBoard().SetIsBoardEnabledForPlayer(player, true);
    }

    private void InitPostGameSpectator(IServerPlayer player)
    {
        ClearSpectator(player);
    }

    private void InitDefaultSpectator(IServerPlayer player)
    {
        ClearSpectator(player);
    }

    private void ClearSpectator(IServerPlayer player)
    {
        _gameServices.GetScoreBoard().SetIsBoardEnabledForPlayer(player, false);
    }


    // Inherited methods.
    @Override
    public boolean AddSpectator(IServerPlayer player)
    {
        if (_gameServices.GetGamePlayerCollection().AddSpectator(player))
        {
            InitializeSpectator(player);
            return true;
        }
        return false;
    }

    @Override
    public boolean RemoveSpectator(IServerPlayer player)
    {
        if (_gameServices.GetGamePlayerCollection().RemoveSpectator(player))
        {
            ClearSpectator(player);
            return true;
        }
        return false;
    }

    @Override
    public boolean ContainsSpectator(IServerPlayer player)
    {
        return _gameServices.GetGamePlayerCollection().ContainsSpectator(player);
    }

    @Override
    public List<IServerPlayer> GetSpectators()
    {
        return _gameServices.GetGamePlayerCollection().GetSpectators();
    }

    @Override
    public void SetState(MoleHuntGameState state)
    {
        _state = Objects.requireNonNull(state);
        _gameServices.GetGamePlayerCollection().GetSpectators().forEach(this::ApplySpectatorState);

        if (_state == MoleHuntGameState.Complete)
        {
            _gameServices.GetGamePlayerCollection().GetPlayers().forEach(
                    player -> player.GetLifeChangeEvent().Unsubscribe(this));
        }
        else if (_state == MoleHuntGameState.PreGame)
        {
            _gameServices.GetGamePlayerCollection().GetPlayers().forEach(
                    player -> player.GetLifeChangeEvent().Subscribe(this, this::OnGamePlayerLifeChangeEvent));
        }
    }
}