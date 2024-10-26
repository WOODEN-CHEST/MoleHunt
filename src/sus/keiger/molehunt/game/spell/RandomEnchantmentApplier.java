package sus.keiger.molehunt.game.spell;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import sus.keiger.plugincommon.player.PlayerFunctions;

import java.util.*;

public class RandomEnchantmentApplier
{
    // Methods.
    public void EnchantItem(ItemStack item)
    {
        List<Enchantment> AvailableEnchants = GetEnchantmentList(item.getType());
        Random RNG = new Random();
        boolean Enchanted = false;

        while (!Enchanted && !AvailableEnchants.isEmpty())
        {
            Enchantment PickedEnchantment = AvailableEnchants.get(RNG.nextInt(AvailableEnchants.size()));
            if (!IsConflicting(PickedEnchantment, item))
            {
                Enchanted = item.addEnchant(PickedEnchantment,
                        item.getEnchantLevel(PickedEnchantment) + 1, false);
            }
            AvailableEnchants.remove(PickedEnchantment);
        }
    }

    public boolean IsPossibleToEnchant(Material itemType)
    {
        return !GetEnchantmentList(itemType).isEmpty();
    }



    // Private methods.
    private List<Enchantment> GetEnchantmentList(Material itemMaterial)
    {
        ItemStack DummyItem = new ItemStack(itemMaterial, 1);
        List<Enchantment> AvailableEnchantments = new ArrayList<>();
        RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT).forEach(enchantment ->
        {
            if (enchantment.canEnchantItem(DummyItem))
            {
                AvailableEnchantments.add(enchantment);
            }
        });
        return AvailableEnchantments;
    }

    private boolean IsConflicting(Enchantment enchantment, ItemStack item)
    {
        for (Enchantment ExistingEnchantment: item.getEnchantments().keySet())
        {
            if (enchantment == ExistingEnchantment)
            {
                continue;
            }

            if (enchantment.conflictsWith(ExistingEnchantment))
            {
                return true;
            }
        }
        return false;
    }
}