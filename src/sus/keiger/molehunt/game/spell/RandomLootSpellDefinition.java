package sus.keiger.molehunt.game.spell;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionType;
import sus.keiger.molehunt.game.IGameServices;
import sus.keiger.plugincommon.player.PlayerFunctions;

import java.util.*;
import java.util.function.Supplier;

public class RandomLootSpellDefinition extends GameSpellDefinition
{
    public RandomLootSpellDefinition()
    {
        super("RandomLoot", "Gives a random item to the victim.", SpellDurationType.Instant,
                SpellClass.Regular, 0.5d, SpellDataRequirement.TargetPlayer);
    }

    @Override
    public GameSpell CreateSpell(GameSpellArguments args, IGameServices services)
    {
        return new RandomLootSpell(this, args, services);
    }


    // Types.
    private static class RandomLootSpell extends GameSpell
    {
        // Fields.
        public final Random RNG = new Random();


        // Constructors.
        public RandomLootSpell(GameSpellDefinition definition, GameSpellArguments arguments, IGameServices services)
        {
            super(definition, arguments, services);
        }


        // Methods.
        public List<Supplier<ItemStack>> GetRandomItemList()
        {
            List<Supplier<ItemStack>> Items = new ArrayList<>();

            Items.add(() -> new ItemStack(Material.COBBLESTONE, 16));
            Items.add(() -> new ItemStack(Material.COOKED_BEEF, 4));
            Items.add(() -> new ItemStack(Material.DIAMOND, 1));
            Items.add(() -> new ItemStack(Material.IRON_INGOT, 6));
            Items.add(() -> new ItemStack(Material.GOLDEN_APPLE, 1));
            Items.add(() -> new ItemStack(Material.TNT, 1));
            Items.add(() -> new ItemStack(Material.FLINT_AND_STEEL, 1));
            Items.add(() -> new ItemStack(Material.DEAD_BUSH, 5));
            Items.add(() -> new ItemStack(Material.DIRT, 32));
            Items.add(() -> new ItemStack(Material.ARROW, 6));
            Items.add(() -> new ItemStack(Material.SPECTRAL_ARROW, 3));
            Items.add(() -> new ItemStack(Material.OAK_LOG, 5));
            Items.add(() -> new ItemStack(Material.BIRCH_LOG, 5));
            Items.add(() -> new ItemStack(Material.SPRUCE_LOG, 5));
            Items.add(() -> new ItemStack(Material.POPPY, 1));
            Items.add(() -> new ItemStack(Material.STONE_SWORD, 1));
            Items.add(() -> new ItemStack(Material.IRON_SWORD, 1));
            Items.add(() -> new ItemStack(Material.GOLDEN_CARROT, 2));
            Items.add(() -> new ItemStack(Material.POISONOUS_POTATO, 4));
            Items.add(() -> new ItemStack(Material.BAKED_POTATO, 8));
            Items.add(() -> new ItemStack(Material.COAL, 5));
            Items.add(() ->
            {
                List<Material> AvailableItems = List.of(
                        Material.IRON_HELMET, Material.IRON_CHESTPLATE,
                        Material.IRON_LEGGINGS, Material.IRON_BOOTS);
                ItemStack Item = new ItemStack(AvailableItems.get(RNG.nextInt(AvailableItems.size())), 1);
                RandomEnchantmentApplier EnchantmentApplier = new RandomEnchantmentApplier();
                int EnchantmentCount = 2;
                for (int i = 0; i < EnchantmentCount; i++)
                {
                   EnchantmentApplier.EnchantItem(Item);
                }
                SetDamage(Item, 0.1d);
                return Item;
            });
            Items.add(() ->
            {
                List<Material> AvailableItems = List.of(Material.BOW, Material.CROSSBOW);
                ItemStack Item = new ItemStack(AvailableItems.get(RNG.nextInt(AvailableItems.size())), 1);
                SetDamage(Item, 0.05d);
                return Item;
            });
            Items.add(() ->
            {
                ItemStack Item = new ItemStack(Material.SPLASH_POTION, 1);
                Item.editMeta(meta ->
                {
                    PotionMeta TargetPotionMeta = (PotionMeta)meta;
                    List<PotionType> PotionTypes = List.of(PotionType.HARMING, PotionType.HEALING,
                            PotionType.FIRE_RESISTANCE, PotionType.SWIFTNESS, PotionType.SLOWNESS);
                    TargetPotionMeta.setBasePotionType(PotionTypes.get(RNG.nextInt(PotionTypes.size())));
                });
                return Item;
            });
            return Items;
        }

        public void SetDamage(ItemStack item, double maxDurabilityLeft)
        {
            item.editMeta(meta ->
            {
                int MaxDamage = 100;
                Damageable DamageableMeta = (Damageable)meta;
                DamageableMeta.setMaxDamage(MaxDamage);
                DamageableMeta.setDamage(MaxDamage - (int)(RNG.nextDouble() * maxDurabilityLeft * MaxDamage));
            });
        }


        // Inherited methods.
        @Override
        public void OnAdd()
        {
            List<Supplier<ItemStack>> RandomItemsList = GetRandomItemList();
            PlayerFunctions.AddItem(GetArguments().GetTargetPlayer().GetMCPlayer(),
                    RandomItemsList.get(RNG.nextInt(RandomItemsList.size())).get());
        }

        @Override
        public void Execute() { }

        @Override
        public void OnRemove() { }
    }
}
