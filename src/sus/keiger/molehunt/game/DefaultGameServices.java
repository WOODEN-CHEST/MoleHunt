package sus.keiger.molehunt.game;

import sus.keiger.molehunt.IWorldProvider;
import sus.keiger.molehunt.event.IEventDispatcher;
import sus.keiger.molehunt.game.player.GamePlayerCollection;
import sus.keiger.molehunt.player.IServerPlayerCollection;
import sus.keiger.molehunt.voicechat.IVoiceChatController;
import sus.keiger.plugincommon.packet.IGamePacketController;

import java.util.Objects;

public class DefaultGameServices implements IGameServices
{
    // Private fields.
    private final IEventDispatcher _eventDispatcher;
    private final IVoiceChatController _voiceChatController;
    private final IServerPlayerCollection _serverPlayers;
    private final IGamePacketController _packetController;
    private final GamePlayerCollection _gamePlayerCollection;
    private final GameScoreboard _scoreBoard;
    private final IGameLocationProvider _locationProvider;


    // Constructors.
    public DefaultGameServices(IEventDispatcher eventDispatcher,
                               IVoiceChatController voiceChatController,
                               IServerPlayerCollection serverPlayers,
                               IGamePacketController packetController,
                               GamePlayerCollection gamePlayerCollection,
                               GameScoreboard scoreBoard,
                               IGameLocationProvider locationProvider)
    {
        _eventDispatcher = Objects.requireNonNull(eventDispatcher, "eventDispatcher is null");
        _voiceChatController = Objects.requireNonNull(voiceChatController, "voiceChatController is null");
        _serverPlayers = Objects.requireNonNull(serverPlayers, "serverPlayers is null");
        _packetController = Objects.requireNonNull(packetController, "packetController is null");
        _gamePlayerCollection = Objects.requireNonNull(gamePlayerCollection, "gamePlayerCollection is null");
        _scoreBoard = Objects.requireNonNull(scoreBoard, "scoreBoard is null");
        _locationProvider = Objects.requireNonNull(locationProvider, "locationProvider is null");
    }


    // Inherited methods.
    @Override
    public IEventDispatcher GetEventDispatcher()
    {
        return null;
    }

    @Override
    public IVoiceChatController GetVoiceChatController()
    {
        return null;
    }

    @Override
    public IServerPlayerCollection GetServerPlayerCollection()
    {
        return null;
    }

    @Override
    public IGamePacketController GetPacketController()
    {
        return null;
    }

    @Override
    public GamePlayerCollection GetGamePlayerCollection()
    {
        return null;
    }

    @Override
    public GameScoreboard GetScoreBoard()
    {
        return null;
    }

    @Override
    public MoleHuntSettings GetGameSettings()
    {
        return null;
    }

    @Override
    public IGameLocationProvider GetLocationProvider()
    {
        return null;
    }
}
