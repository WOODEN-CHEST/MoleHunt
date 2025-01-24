package sus.keiger.molehunt.game.spell;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.*;
import sus.keiger.molehunt.game.IGameServices;
import sus.keiger.plugincommon.player.ItemSearchResult;
import sus.keiger.plugincommon.player.PlayerFunctions;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class InstantSmeltSpellDefinition extends GameSpellDefinition
{
    // Constructors.
    public InstantSmeltSpellDefinition()
    {
        super("InstantSmelt", "Instantly smelts a smeltable item in the player's inventory.",
                SpellDurationType.Instant, SpellClass.Regular, 0.5d, SpellDataRequirement.TargetPlayer);
    }


    // Inherited methods.
    @Override
    public GameSpell CreateSpell(GameSpellArguments args, IGameServices services)
    {
        return new InstantSmeltSpell(this, args, services);
    }


    // Types.
    private static class InstantSmeltSpell extends GameSpell
    {
        // Fields.
        public final Map<Material, Material> _smeltResults = new HashMap<>();



        // Constructors.
        public InstantSmeltSpell(GameSpellDefinition definition, GameSpellArguments arguments, IGameServices services)
        {
            super(definition, arguments, services);
            InitializeSmeltingMapHardCoded();
        }


        // Methods.
        public void InitializeSmeltingMapHardCoded()
        {
            _smeltResults.put(Material.RAW_IRON, Material.IRON_INGOT);
            _smeltResults.put(Material.RAW_COPPER, Material.COPPER_INGOT);
            _smeltResults.put(Material.RAW_GOLD, Material.GOLD_INGOT);

            _smeltResults.put(Material.COAL_ORE, Material.COAL);
            _smeltResults.put(Material.DEEPSLATE_COAL_ORE, Material.COAL);
            _smeltResults.put(Material.IRON_ORE, Material.IRON_INGOT);
            _smeltResults.put(Material.DEEPSLATE_IRON_ORE, Material.IRON_INGOT);
            _smeltResults.put(Material.GOLD_ORE, Material.GOLD_INGOT);
            _smeltResults.put(Material.DEEPSLATE_GOLD_ORE, Material.GOLD_INGOT);
            _smeltResults.put(Material.REDSTONE_ORE, Material.REDSTONE);
            _smeltResults.put(Material.DEEPSLATE_REDSTONE_ORE, Material.REDSTONE);
            _smeltResults.put(Material.EMERALD_ORE, Material.EMERALD);
            _smeltResults.put(Material.DEEPSLATE_EMERALD_ORE, Material.EMERALD);
            _smeltResults.put(Material.DIAMOND_ORE, Material.DIAMOND);
            _smeltResults.put(Material.DEEPSLATE_DIAMOND_ORE, Material.DIAMOND);
            _smeltResults.put(Material.LAPIS_ORE, Material.LAPIS_LAZULI);
            _smeltResults.put(Material.DEEPSLATE_LAPIS_ORE, Material.LAPIS_LAZULI);
            _smeltResults.put(Material.COPPER_ORE, Material.COPPER_INGOT);
            _smeltResults.put(Material.DEEPSLATE_COPPER_ORE, Material.COPPER_INGOT);
            _smeltResults.put(Material.NETHER_GOLD_ORE, Material.GOLD_INGOT);
            _smeltResults.put(Material.NETHER_QUARTZ_ORE, Material.QUARTZ);
            _smeltResults.put(Material.ANCIENT_DEBRIS, Material.NETHERITE_SCRAP);

            _smeltResults.put(Material.OAK_LOG, Material.CHARCOAL);
            _smeltResults.put(Material.STRIPPED_OAK_LOG, Material.CHARCOAL);
            _smeltResults.put(Material.SPRUCE_LOG, Material.CHARCOAL);
            _smeltResults.put(Material.STRIPPED_SPRUCE_LOG, Material.CHARCOAL);
            _smeltResults.put(Material.BIRCH_LOG, Material.CHARCOAL);
            _smeltResults.put(Material.STRIPPED_BIRCH_LOG, Material.CHARCOAL);
            _smeltResults.put(Material.JUNGLE_LOG, Material.CHARCOAL);
            _smeltResults.put(Material.STRIPPED_JUNGLE_LOG, Material.CHARCOAL);
            _smeltResults.put(Material.ACACIA_LOG, Material.CHARCOAL);
            _smeltResults.put(Material.STRIPPED_ACACIA_LOG, Material.CHARCOAL);
            _smeltResults.put(Material.DARK_OAK_LOG, Material.CHARCOAL);
            _smeltResults.put(Material.STRIPPED_DARK_OAK_LOG, Material.CHARCOAL);
            _smeltResults.put(Material.MANGROVE_LOG, Material.CHARCOAL);
            _smeltResults.put(Material.STRIPPED_MANGROVE_LOG, Material.CHARCOAL);
            _smeltResults.put(Material.CHERRY_LOG, Material.CHARCOAL);
            _smeltResults.put(Material.STRIPPED_CHERRY_LOG, Material.CHARCOAL);

            _smeltResults.put(Material.WET_SPONGE, Material.SPONGE);

            _smeltResults.put(Material.KELP, Material.DRIED_KELP);
            _smeltResults.put(Material.CHICKEN, Material.COOKED_CHICKEN);
            _smeltResults.put(Material.BEEF, Material.COOKED_BEEF);
            _smeltResults.put(Material.PORKCHOP, Material.COOKED_PORKCHOP);
            _smeltResults.put(Material.SALMON, Material.COOKED_SALMON);
            _smeltResults.put(Material.COD, Material.COOKED_COD);
            _smeltResults.put(Material.RABBIT, Material.COOKED_RABBIT);
            _smeltResults.put(Material.MUTTON, Material.COOKED_MUTTON);
            _smeltResults.put(Material.POTATO, Material.BAKED_POTATO);
            _smeltResults.put(Material.CHORUS_FRUIT, Material.POPPED_CHORUS_FRUIT);

            _smeltResults.put(Material.IRON_SWORD, Material.IRON_NUGGET);
            _smeltResults.put(Material.IRON_AXE, Material.IRON_NUGGET);
            _smeltResults.put(Material.IRON_PICKAXE, Material.IRON_NUGGET);
            _smeltResults.put(Material.IRON_SHOVEL, Material.IRON_NUGGET);
            _smeltResults.put(Material.IRON_HOE, Material.IRON_NUGGET);
            _smeltResults.put(Material.IRON_HELMET, Material.IRON_NUGGET);
            _smeltResults.put(Material.IRON_CHESTPLATE, Material.IRON_NUGGET);
            _smeltResults.put(Material.IRON_LEGGINGS, Material.IRON_NUGGET);
            _smeltResults.put(Material.IRON_BOOTS, Material.IRON_NUGGET);
            _smeltResults.put(Material.CHAINMAIL_HELMET, Material.IRON_NUGGET);
            _smeltResults.put(Material.CHAINMAIL_CHESTPLATE, Material.IRON_NUGGET);
            _smeltResults.put(Material.CHAINMAIL_LEGGINGS, Material.IRON_NUGGET);
            _smeltResults.put(Material.CHAINMAIL_BOOTS, Material.IRON_NUGGET);

            _smeltResults.put(Material.GOLDEN_SWORD, Material.GOLD_NUGGET);
            _smeltResults.put(Material.GOLDEN_AXE, Material.GOLD_NUGGET);
            _smeltResults.put(Material.GOLDEN_PICKAXE, Material.GOLD_NUGGET);
            _smeltResults.put(Material.GOLDEN_SHOVEL, Material.GOLD_NUGGET);
            _smeltResults.put(Material.GOLDEN_HOE, Material.GOLD_NUGGET);
            _smeltResults.put(Material.GOLDEN_HELMET, Material.GOLD_NUGGET);
            _smeltResults.put(Material.GOLDEN_CHESTPLATE, Material.GOLD_NUGGET);
            _smeltResults.put(Material.GOLDEN_LEGGINGS, Material.GOLD_NUGGET);
            _smeltResults.put(Material.GOLDEN_BOOTS, Material.GOLD_NUGGET);

            _smeltResults.put(Material.SAND, Material.GLASS);
            _smeltResults.put(Material.RED_SAND, Material.GLASS);

            _smeltResults.put(Material.COBBLESTONE, Material.STONE);
            _smeltResults.put(Material.STONE, Material.SMOOTH_STONE);
            _smeltResults.put(Material.STONE_BRICKS, Material.CRACKED_STONE_BRICKS);
            _smeltResults.put(Material.COBBLED_DEEPSLATE, Material.DEEPSLATE);
            _smeltResults.put(Material.DEEPSLATE_BRICKS, Material.CRACKED_DEEPSLATE_BRICKS);
            _smeltResults.put(Material.DEEPSLATE_TILES, Material.CRACKED_DEEPSLATE_TILES);
            _smeltResults.put(Material.SANDSTONE, Material.SMOOTH_SANDSTONE);
            _smeltResults.put(Material.RED_SANDSTONE, Material.SMOOTH_RED_SANDSTONE);
            _smeltResults.put(Material.NETHER_BRICKS, Material.CRACKED_NETHER_BRICKS);
            _smeltResults.put(Material.BASALT, Material.SMOOTH_BASALT);
            _smeltResults.put(Material.POLISHED_BLACKSTONE_BRICKS, Material.CRACKED_POLISHED_BLACKSTONE_BRICKS);
            _smeltResults.put(Material.CLAY, Material.TERRACOTTA);
            _smeltResults.put(Material.SEA_PICKLE, Material.LIME_DYE);
            _smeltResults.put(Material.CACTUS, Material.GREEN_DYE);
            _smeltResults.put(Material.CLAY_BALL, Material.BRICK);
            _smeltResults.put(Material.NETHERRACK, Material.NETHER_BRICK);

            _smeltResults.put(Material.WHITE_TERRACOTTA, Material.WHITE_GLAZED_TERRACOTTA);
            _smeltResults.put(Material.LIGHT_GRAY_TERRACOTTA, Material.LIGHT_GRAY_GLAZED_TERRACOTTA);
            _smeltResults.put(Material.GRAY_TERRACOTTA, Material.GRAY_GLAZED_TERRACOTTA);
            _smeltResults.put(Material.BLACK_TERRACOTTA, Material.BLACK_GLAZED_TERRACOTTA);
            _smeltResults.put(Material.BROWN_TERRACOTTA, Material.BROWN_GLAZED_TERRACOTTA);
            _smeltResults.put(Material.RED_TERRACOTTA, Material.RED_GLAZED_TERRACOTTA);
            _smeltResults.put(Material.ORANGE_TERRACOTTA, Material.ORANGE_GLAZED_TERRACOTTA);
            _smeltResults.put(Material.YELLOW_TERRACOTTA, Material.YELLOW_GLAZED_TERRACOTTA);
            _smeltResults.put(Material.LIME_TERRACOTTA, Material.LIME_GLAZED_TERRACOTTA);
            _smeltResults.put(Material.GREEN_TERRACOTTA, Material.GREEN_GLAZED_TERRACOTTA);
            _smeltResults.put(Material.CYAN_TERRACOTTA, Material.BLUE_GLAZED_TERRACOTTA);
            _smeltResults.put(Material.LIGHT_BLUE_TERRACOTTA, Material.LIGHT_BLUE_GLAZED_TERRACOTTA);
            _smeltResults.put(Material.BLUE_TERRACOTTA, Material.BLUE_GLAZED_TERRACOTTA);
            _smeltResults.put(Material.PURPLE_TERRACOTTA, Material.PURPLE_GLAZED_TERRACOTTA);
            _smeltResults.put(Material.MAGENTA_TERRACOTTA, Material.MAGENTA_GLAZED_TERRACOTTA);
            _smeltResults.put(Material.PINK_TERRACOTTA, Material.PINK_GLAZED_TERRACOTTA);
        }

        public void SmeltItem(int slot, ItemStack item, Player player)
        {
            ItemStack SmeltedItem = new ItemStack(_smeltResults.get(item.getType()), item.getAmount());
            SmeltedItem.setItemMeta(item.getItemMeta());

            if ((slot == PlayerFunctions.SLOT_ARMOR_HEAD) || (slot == PlayerFunctions.SLOT_ARMOR_CHEST)
                    || (slot == PlayerFunctions.SLOT_ARMOR_LEGS) || (slot == PlayerFunctions.SLOT_ARMOR_FEET))
            {
                PlayerFunctions.SetItem(player, slot, null);
                PlayerFunctions.AddItem(player, SmeltedItem);
            }
            else
            {
                PlayerFunctions.SetItem(player, slot, SmeltedItem);
            }
        }


        // Inherited methods.

        @Override
        public void OnAdd()
        {
            ItemSearchResult FoundItems = PlayerFunctions.FindItems(GetArguments().GetTargetPlayer().GetMCPlayer(),
                    item -> _smeltResults.containsKey(item.getType()));

            if (FoundItems.IsEmpty())
            {
                return;
            }

            Random RNG = new Random();
            int Slot = FoundItems.GetSlots().get(RNG.nextInt(FoundItems.GetItemCount()));
            SmeltItem(Slot, FoundItems.GetItem(Slot), GetArguments().GetTargetPlayer().GetMCPlayer());
        }

        @Override
        public void Execute() { }

        @Override
        public void OnRemove() { }
    }
}