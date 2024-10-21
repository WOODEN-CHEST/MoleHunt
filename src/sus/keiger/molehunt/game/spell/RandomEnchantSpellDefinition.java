package sus.keiger.molehunt.game.spell;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
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
        super("Enchant", "Randomly enchants a random item in the victim's inventory.", SpellType.Instant, 0.75d,
                SpellDataRequirement.TargetPlayer);
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
        // Fields.
        public final Set<Material> Swords = Set.of(
                Material.WOODEN_SWORD, Material.GOLDEN_SWORD,
                Material.STONE_SWORD, Material.IRON_SWORD,
                Material.DIAMOND_SWORD, Material.NETHERITE_SWORD);

        public final Set<Material> Axes = Set.of(
                Material.WOODEN_AXE, Material.GOLDEN_AXE,
                Material.STONE_AXE, Material.IRON_AXE,
                Material.DIAMOND_AXE, Material.NETHERITE_AXE);

        public final Set<Material> Weapons = new HashSet<>();

        public final Set<Material> Tools = Set.of(
                Material.WOODEN_PICKAXE, Material.GOLDEN_PICKAXE,
                Material.STONE_PICKAXE, Material.IRON_PICKAXE,
                Material.DIAMOND_PICKAXE, Material.NETHERITE_PICKAXE,
                Material.WOODEN_AXE, Material.GOLDEN_AXE,
                Material.STONE_AXE, Material.IRON_AXE,
                Material.DIAMOND_AXE, Material.NETHERITE_AXE,
                Material.WOODEN_SHOVEL, Material.GOLDEN_SHOVEL,
                Material.STONE_SHOVEL, Material.IRON_SHOVEL,
                Material.DIAMOND_SHOVEL, Material.NETHERITE_SHOVEL,
                Material.WOODEN_HOE, Material.GOLDEN_HOE,
                Material.STONE_HOE, Material.IRON_HOE,
                Material.DIAMOND_HOE, Material.NETHERITE_HOE);

        public final Set<Material> Misc = Set.of(
                Material.SHEARS, Material.FLINT_AND_STEEL,
                Material.SHIELD, Material.ELYTRA,
                Material.CARROT_ON_A_STICK, Material.WARPED_FUNGUS_ON_A_STICK,
                Material.BRUSH);

        public final Set<Material> Bows = Set.of(Material.BOW);

        public final Set<Material> Crossbows = Set.of(Material.CROSSBOW);

        public final Set<Material> FishingRods = Set.of(Material.FISHING_ROD);

        public final Set<Material> Tridents = Set.of(Material.TRIDENT);

        public final Set<Material> Maces = Set.of(Material.MACE);

        public final Set<Material> Boots = Set.of(
                Material.LEATHER_BOOTS, Material.GOLDEN_BOOTS,
                Material.IRON_BOOTS, Material.DIAMOND_BOOTS,
                Material.NETHERITE_BOOTS);

        public final Set<Material> Leggings = Set.of(
                Material.LEATHER_LEGGINGS, Material.GOLDEN_LEGGINGS,
                Material.IRON_LEGGINGS, Material.DIAMOND_LEGGINGS,
                Material.NETHERITE_LEGGINGS);

        public final Set<Material> ChestPlates = Set.of(
                Material.LEATHER_CHESTPLATE, Material.GOLDEN_CHESTPLATE,
                Material.IRON_CHESTPLATE, Material.DIAMOND_CHESTPLATE,
                Material.NETHERITE_CHESTPLATE);

        public final Set<Material> Helmets = Set.of(
                Material.LEATHER_HELMET, Material.GOLDEN_HELMET,
                Material.IRON_HELMET, Material.DIAMOND_HELMET,
                Material.NETHERITE_HELMET, Material.TURTLE_HELMET);

        public final Set<Material> Armor = Set.of(
                Material.LEATHER_HELMET, Material.LEATHER_CHESTPLATE,
                Material.LEATHER_LEGGINGS, Material.LEATHER_BOOTS,
                Material.GOLDEN_HELMET, Material.GOLDEN_CHESTPLATE,
                Material.GOLDEN_LEGGINGS, Material.GOLDEN_BOOTS,
                Material.IRON_HELMET, Material.IRON_CHESTPLATE,
                Material.IRON_LEGGINGS, Material.IRON_BOOTS,
                Material.DIAMOND_HELMET, Material.DIAMOND_CHESTPLATE,
                Material.DIAMOND_LEGGINGS, Material.DIAMOND_BOOTS,
                Material.NETHERITE_HELMET, Material.NETHERITE_CHESTPLATE,
                Material.NETHERITE_LEGGINGS, Material.NETHERITE_BOOTS);

        public final Set<Material> AllItems = new HashSet<>();

        public final Set<Enchantment> GenericEnchantments = Set.of(
                Enchantment.UNBREAKING, Enchantment.MENDING,
                Enchantment.VANISHING_CURSE);

        public final Set<Enchantment> WeaponEnchantments = Set.of(
                Enchantment.SHARPNESS, Enchantment.SMITE,
                Enchantment.BANE_OF_ARTHROPODS, Enchantment.FIRE_ASPECT);

        public final Set<Enchantment> SwordEnchantments = Set.of(
                Enchantment.SWEEPING_EDGE, Enchantment.LOOTING);

        public final Set<Enchantment> ToolEnchantments = Set.of(
                Enchantment.EFFICIENCY, Enchantment.FORTUNE,
                Enchantment.SILK_TOUCH);

        public final Set<Enchantment> BowEnchantments = Set.of(
                Enchantment.PUNCH, Enchantment.POWER,
                Enchantment.FLAME, Enchantment.INFINITY);

        public final Set<Enchantment> CrossbowEnchantments = Set.of(
                Enchantment.PIERCING, Enchantment.MULTISHOT,
                Enchantment.QUICK_CHARGE);

        public final Set<Enchantment> FishingRodEnchantments = Set.of(
                Enchantment.LURE, Enchantment.LUCK_OF_THE_SEA);

        public final Set<Enchantment> TridentEnchantments = Set.of(
                Enchantment.RIPTIDE, Enchantment.CHANNELING,
                Enchantment.LOYALTY, Enchantment.IMPALING);

        public final Set<Enchantment> MaceEnchantments = Set.of(
                Enchantment.DENSITY, Enchantment.BREACH,
                Enchantment.WIND_BURST);

        public final Set<Enchantment> BootEnchantments = Set.of(
                Enchantment.SOUL_SPEED, Enchantment.DEPTH_STRIDER,
                Enchantment.FEATHER_FALLING, Enchantment.FROST_WALKER);

        public final Set<Enchantment> LeggingEnchantments = Set.of(
                Enchantment.SWIFT_SNEAK);

        public final Set<Enchantment> ChestplateEnchantments = Set.of();

        public final Set<Enchantment> HelmetEnchantments = Set.of(
                Enchantment.AQUA_AFFINITY, Enchantment.RESPIRATION);

        public final Set<Enchantment> ArmorEnchantments = Set.of(
                Enchantment.PROTECTION, Enchantment.BLAST_PROTECTION,
                Enchantment.PROJECTILE_PROTECTION, Enchantment.THORNS,
                Enchantment.FIRE_PROTECTION, Enchantment.BINDING_CURSE);

        public final Map<Set<Material>, Set<Enchantment>> TypeEnchantments = new HashMap<>();




        // Constructors.
        public RandomEnchantSpell(GameSpellDefinition definition,
                           GameSpellArguments arguments,
                                  IGameServices services)
        {
            super(definition, arguments, services);

            Weapons.addAll(Swords);
            Weapons.addAll(Axes);

            TypeEnchantments.put(Swords, SwordEnchantments);
            TypeEnchantments.put(Axes, Collections.emptySet());
            TypeEnchantments.put(Weapons, WeaponEnchantments);
            TypeEnchantments.put(Tools, ToolEnchantments);
            TypeEnchantments.put(Misc,  Collections.emptySet());
            TypeEnchantments.put(Bows, BowEnchantments);
            TypeEnchantments.put(Crossbows, CrossbowEnchantments);
            TypeEnchantments.put(FishingRods, FishingRodEnchantments);
            TypeEnchantments.put(Tridents, TridentEnchantments);
            TypeEnchantments.put(Maces, MaceEnchantments);
            TypeEnchantments.put(Boots, BootEnchantments);
            TypeEnchantments.put(Leggings, LeggingEnchantments);
            TypeEnchantments.put(ChestPlates, ChestplateEnchantments);
            TypeEnchantments.put(Helmets, HelmetEnchantments);
            TypeEnchantments.put(Armor, ArmorEnchantments);

            TypeEnchantments.keySet().forEach(AllItems::addAll);
        }


        // Methods.
        public List<Enchantment> GetEnchantmentList(Material itemMaterial)
        {
            List<Enchantment> AvailableEnchantments = new ArrayList<>(GenericEnchantments);

            for (Set<Material> MaterialSet : TypeEnchantments.keySet())
            {
                if (MaterialSet.contains(itemMaterial))
                {
                    AvailableEnchantments.addAll(TypeEnchantments.get(MaterialSet));
                }
            }

            return AvailableEnchantments;
        }

        public void EnchantItem(Player player, int slot, ItemStack item)
        {
            List<Enchantment> AvailableEnchants = GetEnchantmentList(item.getType());
            Random RNG = new Random();
            boolean Enchanted = false;
            ItemStack EnchantedItem = item.clone();

            while (!Enchanted && !AvailableEnchants.isEmpty())
            {
                Enchantment PickedEnchantment = AvailableEnchants.get(RNG.nextInt(AvailableEnchants.size()));
                Enchanted = EnchantedItem.addEnchant(PickedEnchantment,
                        item.getEnchantLevel(PickedEnchantment) + 1, false);
                AvailableEnchants.remove(PickedEnchantment);
            }

            PlayerFunctions.SetItem(player, slot, EnchantedItem);
        }


        // Inherited methods.
        @Override
        public void OnAdd()
        {
            Player MCPlayer = GetArguments().GetTargetPlayer().GetMCPlayer();
            ItemSearchResult SearchResult = PlayerFunctions.FindItems(MCPlayer,
                    item -> AllItems.contains(item.getType()));

            if (SearchResult.GetItemCount() == 0)
            {
                return;
            }

            List<Integer> Slots = SearchResult.GetSlots();
            int Slot = Slots.get(new Random().nextInt(Slots.size()));
            EnchantItem(MCPlayer, Slot, SearchResult.GetItem(Slot));
        }

        @Override
        public void Execute() { }

        @Override
        public void OnRemove() { }
    }
}