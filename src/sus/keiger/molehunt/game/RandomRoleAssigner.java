package sus.keiger.molehunt.game;

import sus.keiger.molehunt.game.player.GamePlayerCollection;
import sus.keiger.molehunt.game.player.IGamePlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RandomRoleAssigner implements IRoleAssigner
{
    // Inherited methods.
    @Override
    public void AssignRoles(GamePlayerCollection players, int moleCountMin, int moleCountMax)
    {
        Random RNG = new Random();
        int MoleCount = RNG.nextInt(moleCountMin, moleCountMax + 1);

        List<IGamePlayer> Players = new ArrayList<>(players.GetActivePlayers());

        for (int i = 0; i < MoleCount; i++)
        {
            int Index = RNG.nextInt(Players.size());
            players.SetTeamOfPlayer(Players.get(Index), GameTeamType.Moles);
            Players.remove(Index);
        }
        Players.forEach(player -> players.SetTeamOfPlayer(player, GameTeamType.Innocents));
    }
}
