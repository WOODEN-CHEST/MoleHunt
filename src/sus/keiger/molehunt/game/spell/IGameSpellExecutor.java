package sus.keiger.molehunt.game.spell;

import sus.keiger.molehunt.event.IMoleHuntEventListener;
import sus.keiger.molehunt.game.IGameStateContaining;
import sus.keiger.molehunt.game.MoleHuntGameState;
import sus.keiger.plugincommon.ITickable;

public interface IGameSpellExecutor extends ITickable, IGameStateContaining
{
    void CastSpell(GameSpellDefinition definition, GameSpellArguments args);
}
