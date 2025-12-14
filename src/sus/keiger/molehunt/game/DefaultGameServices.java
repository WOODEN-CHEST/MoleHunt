package sus.keiger.molehunt.game;

import sus.keiger.molehunt.IWorldProvider;
import sus.keiger.molehunt.event.IEventDispatcher;
import sus.keiger.molehunt.game.player.GamePlayerCollection;
import sus.keiger.molehunt.player.IServerPlayerCollection;
import sus.keiger.plugincommon.packet.IGamePacketController;

import java.util.Objects;

public class DefaultGameServices implements IGameServices
{
    // Private fields.
    private final IEventDispatcher _eventDispatcher;
    private final IServerPlayerCollection _serverPlayers;
    private final IGamePacketController _packetController;
    private final GamePlayerCollection _gamePlayerCollection;
    private final GameScoreboard _scoreBoard;
    private final IGameLocationProvider _locationProvider;
    private final MoleHuntSettings _settings;


    // Constructors.
    public DefaultGameServices(IEventDispatcher eventDispatcher,
                               IServerPlayerCollection serverPlayers,
                               IGamePacketController packetController,
                               GamePlayerCollection gamePlayerCollection,
                               GameScoreboard scoreBoard,
                               IGameLocationProvider locationProvider,
                               MoleHuntSettings settings)
    {
        _eventDispatcher = Objects.requireNonNull(eventDispatcher, "eventDispatcher is null");
        _serverPlayers = Objects.requireNonNull(serverPlayers, "serverPlayers is null");
        _packetController = Objects.requireNonNull(packetController, "packetController is null");
        _gamePlayerCollection = Objects.requireNonNull(gamePlayerCollection, "gamePlayerCollection is null");
        _scoreBoard = Objects.requireNonNull(scoreBoard, "scoreBoard is null");
        _locationProvider = Objects.requireNonNull(locationProvider, "locationProvider is null");
        _settings = Objects.requireNonNull(settings, "settings is null");
    }


    // Inherited methods.
    @Override
    public IEventDispatcher GetEventDispatcher()
    {
        return _eventDispatcher;
    }

    @Override
    public IServerPlayerCollection GetServerPlayerCollection()
    {
        return _serverPlayers;
    }

    @Override
    public IGamePacketController GetPacketController()
    {
        return _packetController;
    }

    @Override
    public GamePlayerCollection GetGamePlayerCollection()
    {
        return _gamePlayerCollection;
    }

    @Override
    public GameScoreboard GetScoreBoard()
    {
        return _scoreBoard;
    }

    @Override
    public MoleHuntSettings GetGameSettings()
    {
        return _settings;
    }

    @Override
    public IGameLocationProvider GetLocationProvider()
    {
        return _locationProvider;
    }
}
