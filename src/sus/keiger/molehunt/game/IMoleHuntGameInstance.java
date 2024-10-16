package sus.keiger.molehunt.game;

import sus.keiger.molehunt.event.IMoleHuntEventListener;
import sus.keiger.molehunt.game.event.MoleHuntCompleteEvent;
import sus.keiger.molehunt.game.event.MoleHuntEndEvent;
import sus.keiger.molehunt.game.event.MoleHuntPreStartEvent;
import sus.keiger.molehunt.game.event.MoleHuntStartEvent;
import sus.keiger.molehunt.game.player.IGamePlayer;
import sus.keiger.molehunt.game.player.IPlayerStats;
import sus.keiger.molehunt.game.spell.GameSpellArguments;
import sus.keiger.molehunt.game.spell.GameSpellDefinition;
import sus.keiger.molehunt.player.IAudienceMemberHolder;
import sus.keiger.molehunt.player.IServerPlayer;
import sus.keiger.plugincommon.ITickable;
import sus.keiger.plugincommon.PCPluginEvent;

import java.util.List;

public interface IMoleHuntGameInstance extends ITickable, IMoleHuntEventListener, IAudienceMemberHolder
{
    boolean AddPlayer(IServerPlayer player);
    boolean ContainsPlayer(IServerPlayer player);
    boolean ContainsActivePlayer(IServerPlayer player);
    IGamePlayer GetGamePlayer(IServerPlayer player);

    boolean Start();
    boolean End();
    void Cancel();

    MoleHuntGameState GetState();

    boolean AddSpectator(IServerPlayer player);
    boolean RemoveSpectator(IServerPlayer player);
    boolean ContainsSpectator(IServerPlayer player);
    List<IServerPlayer> GetSpectators(IServerPlayer player);

    long GetGameID();

    PCPluginEvent<MoleHuntPreStartEvent> GetPreStartEvent();
    PCPluginEvent<MoleHuntStartEvent> GetStartEvent();
    PCPluginEvent<MoleHuntEndEvent> GetEndEvent();
    PCPluginEvent<MoleHuntCompleteEvent> GetCompleteEvent();
}
