package sus.keiger.molehunt.game;

import sus.keiger.molehunt.IWorldProvider;
import sus.keiger.molehunt.event.IEventDispatcher;
import sus.keiger.molehunt.game.player.GamePlayerCollection;
import sus.keiger.molehunt.player.IServerPlayerCollection;
import sus.keiger.molehunt.voicechat.IVoiceChatController;
import sus.keiger.plugincommon.packet.IGamePacketController;

public interface IGameServices
{
    IEventDispatcher GetEventDispatcher();
    IVoiceChatController GetVoiceChatController();
    IServerPlayerCollection GetServerPlayerCollection();
    IGamePacketController GetPacketController();
    GamePlayerCollection GetGamePlayerCollection();
    GameScoreboard GetScoreBoard();
    MoleHuntSettings GetGameSettings();
    IGameLocationProvider GetLocationProvider();
}