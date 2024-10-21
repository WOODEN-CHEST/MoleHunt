package sus.keiger.molehunt.game.spell;

import sus.keiger.molehunt.game.IGameStateContaining;
import sus.keiger.plugincommon.ITickable;

public interface IGameSpellExecutor extends ITickable, IGameStateContaining
{
    // Methods.
    void CastSpell(GameSpellDefinition definition, GameSpellArguments args);
    double GetMaxMana();
}
