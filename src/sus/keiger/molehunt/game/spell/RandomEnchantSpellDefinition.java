package sus.keiger.molehunt.game.spell;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import sus.keiger.molehunt.game.IGameServices;
import sus.keiger.plugincommon.player.ItemSearchResult;
import sus.keiger.plugincommon.player.PlayerFunctions;

import java.util.*;

public class RandomEnchantSpellDefinition extends GameSpellDefinition
{
    // Constructors.
    public RandomEnchantSpellDefinition()
    {
        super("Enchant", "Randomly enchants a random item in the victim's inventory.",
                SpellDurationType.Instant, SpellClass.Regular, 0.75d, SpellDataRequirement.TargetPlayer);
    }


    // Inherited methods.
    @Override
    public GameSpell CreateSpell(GameSpellArguments args, IGameServices services)
    {
        return new RandomEnchantSpell(this, args, services);
    }


    // Types.
    private static class RandomEnchantSpell extends GameSpell
    {
        // Construtors.
        public RandomEnchantSpell(GameSpellDefinition definition, GameSpellArguments arguments, IGameServices services)
        {
            super(definition, arguments, services);
        }

        // Inherited methods.
        @Override
        public void OnAdd()
        {
            RandomEnchantmentApplier EnchantmentsApplier = new RandomEnchantmentApplier();
            Player MCPlayer = GetArguments().GetTargetPlayer().GetMCPlayer();
            ItemSearchResult SearchResult = PlayerFunctions.FindItems(MCPlayer,
                    item -> EnchantmentsApplier.IsPossibleToEnchant(item.getType()));

            if (SearchResult.GetItemCount() == 0)
            {
                return;
            }

            List<Integer> Slots = SearchResult.GetSlots();
            int Slot = Slots.get(new Random().nextInt(Slots.size()));
            ItemStack Item = SearchResult.GetItem(Slot);
            EnchantmentsApplier.EnchantItem(Item);
            PlayerFunctions.SetItem(MCPlayer, Slot, Item);
        }

        @Override
        public void Execute() { }

        @Override
        public void OnRemove() { }
    }
}