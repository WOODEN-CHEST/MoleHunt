package sus.keiger.molehunt.game.player;

public interface IModifiablePlayerStats extends IPlayerStats
{
    void SetDamageDealtHalfHearts(double value);
    void SetDamageTakenHalfHearts(double value);
    void SetKills(int value);
    void SetTicksSurvived(int value);
    void SetScore(double value);
    void SetWasGameFinished(boolean value);
}
