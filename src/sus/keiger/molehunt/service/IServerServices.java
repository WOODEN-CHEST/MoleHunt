package sus.keiger.molehunt.service;

import sus.keiger.molehunt.IWorldProvider;
import sus.keiger.molehunt.event.IEventDispatcher;
import sus.keiger.molehunt.player.IServerPlayerCollection;
import sus.keiger.molehunt.voicechat.IVoiceChatController;
import sus.keiger.plugincommon.IMojangAPIClient;
import sus.keiger.plugincommon.packet.IGamePacketController;

public interface IServerServices
{
    IEventDispatcher GetEventDispatcher();
    IVoiceChatController GetVoiceChatController();
    IWorldProvider GetWorldProvider();
    IServerPlayerCollection GetServerPlayerCollection();
    IGamePacketController GetPacketController();
    IMojangAPIClient GetMojangAPIClient();
}