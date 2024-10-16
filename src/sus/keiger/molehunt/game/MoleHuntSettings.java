package sus.keiger.molehunt.game;

import sus.keiger.plugincommon.PCMath;

public class MoleHuntSettings
{
    // Private fields.
    /* All settings initialized to a default value. */
    private int _gameTimeTicks = PCMath.SecondsToTicks(60d * 45d);
    private int _gracePeriodTimeTicks = PCMath.SecondsToTicks(60d * 5d);
    private int _moleCountMin = 1;
    private int _moleCountMax = 1;
    private boolean _doesBorderShrink = true;
    private int _worldBorderSizeStartBlocks = 400;
    private int _worldBorderSizeEndBlocks = 50;
    private int _borderShrinkStartTimeTicks = PCMath.SecondsToTicks(60d * 10d);
    private int _spellCastCooldownTicks = PCMath.SecondsToTicks(45);
    private boolean _canCastSpells = true;
    private boolean _isNotifiedOnSpellCast = true;


    // Constructors.
    public MoleHuntSettings() { }

    public MoleHuntSettings(MoleHuntSettings settings)
    {
        _gameTimeTicks = settings._gameTimeTicks;
        _moleCountMin = settings._moleCountMin;
        _moleCountMax = settings._moleCountMax;
        _doesBorderShrink = settings._doesBorderShrink;
        _worldBorderSizeStartBlocks = settings._worldBorderSizeStartBlocks;
        _worldBorderSizeEndBlocks = settings._worldBorderSizeEndBlocks;
        _borderShrinkStartTimeTicks = settings._borderShrinkStartTimeTicks;
        _spellCastCooldownTicks = settings._spellCastCooldownTicks;
        _canCastSpells = settings._canCastSpells;
    }


    // Methods.
    public int GetGameTimeTicks()
    {
        return _gameTimeTicks;
    }

    public void SetGameTimeTicks(int value)
    {
        _gameTimeTicks = value;
    }

    public int GetGracePeriodTimeTicks()
    {
        return _gracePeriodTimeTicks;
    }

    public void SetGracePeriodTimeTicks(int value)
    {
        _gracePeriodTimeTicks = value;
    }

    public int GetMoleCountMin()
    {
        return _moleCountMin;
    }

    public void SetMoleCountMin(int value)
    {
        _moleCountMin = value;
    }

    public int GetMoleCountMax()
    {
        return _moleCountMax;
    }

    public void SetMoleCountMax(int value)
    {
        _moleCountMax = value;
    }

    public int GetBorderSizeStartBlocks()
    {
        return _worldBorderSizeStartBlocks;
    }

    public void SetBorderSizeStartBlocks(int value)
    {
        _worldBorderSizeStartBlocks = value;
    }

    public int GetBorderSizeEndBlocks()
    {
        return _worldBorderSizeEndBlocks;
    }

    public void SetBorderSizeEndBlocks(int value)
    {
        _worldBorderSizeEndBlocks = value;
    }

    public int GetBorderShrinkStartTimeTicks()
    {
        return _borderShrinkStartTimeTicks;
    }

    public void SetBorderShrinkStartTimeTicks(int value)
    {
        _borderShrinkStartTimeTicks = value;
    }

    public boolean GetDoesBorderShrink()
    {
        return _doesBorderShrink;
    }

    public void SetDoesBorderShrink(boolean value)
    {
        _doesBorderShrink = value;
    }

    public int GetSpellCastCooldownTicks()
    {
        return _spellCastCooldownTicks;
    }

    public void SetSpellCastCooldownTicks(int value)
    {
        _spellCastCooldownTicks = value;
    }

    public boolean GetCanCastSpells()
    {
        return _canCastSpells;
    }

    public void SetCanCastSpells(boolean value)
    {
        _canCastSpells = value;
    }

    public boolean GetIsNotifiedOnSpellCast()
    {
        return _isNotifiedOnSpellCast;
    }

    public void SetIsNotifiedOnSpellCast(boolean value)
    {
        _isNotifiedOnSpellCast = value;
    }
}