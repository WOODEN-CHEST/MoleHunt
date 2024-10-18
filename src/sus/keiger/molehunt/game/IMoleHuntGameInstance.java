package sus.keiger.molehunt.game;

import sus.keiger.molehunt.event.IMoleHuntEventListener;
import sus.keiger.molehunt.game.event.*;
import sus.keiger.molehunt.game.player.IGamePlayer;
import sus.keiger.molehunt.game.player.IPlayerStats;
import sus.keiger.molehunt.game.spell.GameSpellArguments;
import sus.keiger.molehunt.game.spell.GameSpellDefinition;
import sus.keiger.molehunt.player.IAudienceMemberHolder;
import sus.keiger.molehunt.player.IServerPlayer;
import sus.keiger.plugincommon.ITickable;
import sus.keiger.plugincommon.PCPluginEvent;

import java.util.List;

public interface IMoleHuntGameInstance extends ITickable, IAudienceMemberHolder
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

    void CastSpell(GameSpellDefinition spell, GameSpellArguments arguments);

    PCPluginEvent<MoleHuntPreStartEvent> GetPreStartEvent();
    PCPluginEvent<MoleHuntStartEvent> GetStartEvent();
    PCPluginEvent<MoleHuntEndEvent> GetEndEvent();
    PCPluginEvent<MoleHuntCompleteEvent> GetCompleteEvent();
    PCPluginEvent<ParticipantAddEvent> GetParticipantAddEvent();
    PCPluginEvent<ParticipantRemoveEvent> GetParticipantRemoveEvent();
}
