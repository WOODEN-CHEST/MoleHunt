package sus.keiger.molehunt.game;

import sus.keiger.molehunt.game.player.GamePlayerCollection;

public interface IRoleAssigner
{
    void AssignRoles(GamePlayerCollection players, int moleCountMin, int moleCountMax);
}