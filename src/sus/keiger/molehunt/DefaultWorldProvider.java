package sus.keiger.molehunt;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.World;

public class DefaultWorldProvider implements IWorldProvider
{
    // Private static fields.
    private static final NamespacedKey KEY_OVERWORLD = new NamespacedKey(NamespacedKey.MINECRAFT, "overworld");
    private static final NamespacedKey KEY_NETHER = new NamespacedKey(NamespacedKey.MINECRAFT, "the_nether");
    private static final NamespacedKey KEY_END = new NamespacedKey(NamespacedKey.MINECRAFT, "the_end");


    // Inherited methods.
    @Override
    public World GetOverworld()
    {
        return Bukkit.getWorld(KEY_OVERWORLD);
    }

    @Override
    public World GetNether()
    {
        return Bukkit.getWorld(KEY_NETHER);
    }

    @Override
    public World GetTheEnd()
    {
        return Bukkit.getWorld(KEY_END);
    }
}