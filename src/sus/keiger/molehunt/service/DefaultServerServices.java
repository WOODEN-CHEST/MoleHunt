package sus.keiger.molehunt.service;

import sus.keiger.molehunt.IWorldProvider;
import sus.keiger.molehunt.event.IEventDispatcher;
import sus.keiger.molehunt.player.IServerPlayerCollection;
import sus.keiger.molehunt.voicechat.IVoiceChatController;
import sus.keiger.plugincommon.packet.IGamePacketController;

import java.util.Objects;

public class DefaultServerServices implements IServerServices
{
    // Private fields.
    private final IEventDispatcher _eventDispatcher;
    private final IVoiceChatController _voiceChatController;
    private final IWorldProvider _worldProvider;
    private final IServerPlayerCollection _serverPlayers;
    private final IGamePacketController _packetController;


    // Constructors.
    public DefaultServerServices(IEventDispatcher eventDispatcher,
                                 IVoiceChatController voiceChatController,
                                 IWorldProvider worldProvider,
                                 IServerPlayerCollection serverPlayers,
                                 IGamePacketController packetController)
    {
        _eventDispatcher = Objects.requireNonNull(eventDispatcher, "eventDispatcher is null");
        _voiceChatController = Objects.requireNonNull(voiceChatController, "voiceChatController is null");
        _worldProvider = Objects.requireNonNull(worldProvider, "worldProvider is null");
        _serverPlayers = Objects.requireNonNull(serverPlayers, "serverPlayers is null");
        _packetController =Objects.requireNonNull(packetController, "packetController is null");
    }


    // Inherited methods.
    @Override
    public IEventDispatcher GetEventDispatcher()
    {
        return _eventDispatcher;
    }

    @Override
    public IVoiceChatController GetVoiceChatController()
    {
        return _voiceChatController;
    }

    @Override
    public IWorldProvider GetWorldProvider()
    {
        return _worldProvider;
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
}