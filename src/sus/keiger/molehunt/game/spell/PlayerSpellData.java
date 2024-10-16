package sus.keiger.molehunt.game.spell;

public class PlayerSpellData
{
    // Private fields.
    private int _lootSpellIndex = 0;



    // Methods.
    public int GetLootSpellIndex()
    {
        return _lootSpellIndex;
    }

    public void SetLootSpellIndex(int index)
    {
        _lootSpellIndex = index;
    }
}