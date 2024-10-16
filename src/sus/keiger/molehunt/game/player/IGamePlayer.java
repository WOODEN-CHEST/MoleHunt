package sus.keiger.molehunt.game.player;

import org.bukkit.entity.Player;
import sus.keiger.molehunt.event.IMoleHuntEventListener;
import sus.keiger.molehunt.game.IGameTeam;
import sus.keiger.molehunt.game.IMoleHuntGameInstance;
import sus.keiger.molehunt.game.spell.GameSpellArguments;
import sus.keiger.molehunt.game.spell.GameSpellDefinition;
import sus.keiger.molehunt.game.spell.PlayerSpellData;
import sus.keiger.molehunt.player.IAudienceMember;
import sus.keiger.molehunt.player.IServerPlayer;
import sus.keiger.plugincommon.ITickable;

public interface IGamePlayer extends  IModifiablePlayerStats, ITickable, IMoleHuntEventListener, IAudienceMember
{
    IMoleHuntGameInstance GetGameInstance();
    Player GetMCPlayer();
    IServerPlayer GetServerPlayer();
    void SetServerPlayer(IServerPlayer player);
    boolean IsAlive();
    void SetIsAlive(boolean value);
    IGameTeam GetTeam();
    void SetTeam(IGameTeam team);
    GamePlayerState GetTargetState();
    void SetTargetState(GamePlayerState state);

    PlayerSpellData GetSpellData();
    void CastSpell(GameSpellDefinition spellDefinition, GameSpellArguments arguments);

    boolean GetMayDealDamage();
    void SetMayDealDamage(boolean value);
}