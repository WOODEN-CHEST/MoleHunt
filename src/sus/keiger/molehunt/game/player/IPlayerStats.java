package sus.keiger.molehunt.game.player;

public interface IPlayerStats
{
    double GetDamageDealtHalfHearts();
    double GetDamageTakenHalfHearts();
    int GetKills();
    int GetTicksSurvived();
    double GetScore();
    boolean GetWasGameFinished();
}
