package sus.keiger.molehunt.game.player;

public class PlayerStatsContainer implements IModifiablePlayerStats
{
    // Private fields.
    private double _damageDealt;
    private double _damageTaken;
    private int _kills;
    private boolean _wasGameFinished;
    private int _ticksSurvived;
    private double _score;


    // Inherited methods.
    @Override
    public void SetDamageDealtHalfHearts(double value)
    {
        _damageDealt = Math.max(value, 0d);
    }

    @Override
    public void SetDamageTakenHalfHearts(double value)
    {
        _damageTaken = Math.max(value, 0d);
    }

    @Override
    public void SetKills(int value)
    {
        _kills = Math.max(value, 0);
    }

    @Override
    public void SetTicksSurvived(int value)
    {
        _ticksSurvived = Math.max(value, 0);
    }

    @Override
    public void SetScore(double value)
    {
        _score = Math.max(Math.min(value, 1d), 0d);
    }

    @Override
    public void SetWasGameFinished(boolean value)
    {
        _wasGameFinished = value;
    }

    @Override
    public double GetDamageDealtHalfHearts()
    {
        return _damageDealt;
    }

    @Override
    public double GetDamageTakenHalfHearts()
    {
        return _damageTaken;
    }

    @Override
    public int GetKills()
    {
        return _kills;
    }

    @Override
    public int GetTicksSurvived()
    {
        return _ticksSurvived;
    }

    @Override
    public double GetScore()
    {
        return _score;
    }

    @Override
    public boolean GetWasGameFinished()
    {
        return _wasGameFinished;
    }
}