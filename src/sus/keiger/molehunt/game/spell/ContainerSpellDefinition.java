package sus.keiger.molehunt.game.spell;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import sus.keiger.molehunt.game.IGameServices;
import sus.keiger.plugincommon.PCMath;
import sus.keiger.plugincommon.TickClock;

import java.util.Random;

public class ContainerSpellDefinition extends GameSpellDefinition
{
    // Constructors.
    public ContainerSpellDefinition()
    {
        super("Container", "Makes the victim open and close random containers.",
                SpellDurationType.Sustained, SpellClass.Regular, 0.43d, SpellDataRequirement.TargetPlayer);
    }


    // Inherited methods.
    @Override
    public GameSpell CreateSpell(GameSpellArguments args, IGameServices services)
    {
        return new ContainerSpell(this, args, services);
    }


    // Types.
    private static class ContainerSpell extends GameSpell
    {
        // Fields.
        public final TickClock OpenClock = new TickClock();
        public final int DURATION = PCMath.SecondsToTicks(22.5d);
        public final int MAX_TIME_BETWEEN_OPENS = PCMath.SecondsToTicks(5d);
        public final Random RNG = new Random();
        public final String[] ItemNames = new String[] {
                "Hello World!",
                "MoleHunt",
                "Who's there?",
                "Insert message here",
                "ItemNames = new String[] { ... }",
                "Message here",
                "Hello!",
                "The quick brown fox jumps over the lazy dog",
                "Insert joke here.",
                "I wasted 30 minutes to create this spell",
                "Are spells OP?",
                "A",
                "B",
                "C",
                "D",
                "E",
                "F",
                "These are quite similar to splash texts",
                "How many messages are there???",
                "qwertyuiop",
                "Who made this?",
                "BSRipoff",
                "Spell definition",
                "Broken code",
                "You are dead",
                "The code is: ",
                "Y u do this?",
                "Long messages",
                "AYO WASSUP MY MAN",
                "Container",
                "These items right here do not stack!",
                "Unstackable items.",
                "Der die das",
                "Guten tag!",
                "I eat glue.",
                "Lorem Ipsum",
                "IntelliJ",
                "Visual Studio",
                "Visual Studio Code",
                "This Cord",
                "Discord",
                "Teach me about WW2 please!",
                "PaperMC",
                "Interesting choice of axe you got there.",
                "420 smoke wee d everyday XDDDD",
                "69 le funny numba",
                "lmao",
                "The shake spell used to be OP...",
                "I don't know how to make plugins.",
                "So usually there is a limit on how long a piece of text can be in the item name, but maybe this" +
                        "bypasses that limit? Who knows.",
                "Shift",
                "AAAAAAAAAAAAAAAAAAAA",
                "h",
                "I am out of ideas for these random messages",
                "Saturation test: Fresh spawn to 3 hunger bars.",
                "(SJ) Sprintjumping: 03:02.43 (135.91 seconds)",
                "(S) Sprinting: 2:15.91 (182.43 seconds)",
                "If SJ = 100%, then S = 134.23%.",
                "Sprinting drains hunger 34.23% faster than sprintjumping.",
                "(a + b)^2 = a + 2ab + b",
                "3.1415",
                "2.71",
                "y = ax + b is over-powered AF",
                "\"",
                "'",
                "\\",
                "I am DONE"
        };


        // Constructors.
        public ContainerSpell(GameSpellDefinition definition,
                              GameSpellArguments arguments,
                              IGameServices services)
        {
            super(definition, arguments, services);
            SetTicksRemaining(DURATION);
            OpenClock.SetIsRunning(true);
            OpenClock.SetHandler(this::OnOpen);
        }


        // Methods.
        public void UpdateClockDuration()
        {
            OpenClock.SetTicksLeft(RNG.nextInt(MAX_TIME_BETWEEN_OPENS + 1));
        }

        public void OnOpen(TickClock clock)
        {
            UpdateClockDuration();
            Player MCPlayer = GetArguments().GetTargetPlayer().GetMCPlayer();

            OpenAction[] Actions = OpenAction.values();
            OpenAction SelectedAction = Actions[RNG.nextInt(Actions.length)];

            switch (SelectedAction)
            {
                case CloseInventory -> MCPlayer.closeInventory();
                case Chest -> OpenChest(MCPlayer);
                case DoubleChest -> OpenDoubleChest(MCPlayer);
                case CraftingTable -> OpenCraftingTable(MCPlayer);
            }
        }

        public ItemStack GetRandomItem()
        {
            ItemStack Item = new ItemStack(Material.PAPER, 1);
            Item.editMeta(meta -> meta.displayName(Component.text(ItemNames[RNG.nextInt(ItemNames.length)])
                    .color(NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, false)));
            return Item;
        }

        public void FillInventoryWithRandomItems(Inventory inventory)
        {
            int ItemsToAdd = RNG.nextInt(inventory.getSize());
            for (int i = 0; i < ItemsToAdd; i++)
            {
                inventory.setItem(RNG.nextInt(inventory.getSize()), GetRandomItem());
            }
        }

        public void OpenChest(Player mcPlayer)
        {
            int InventorySize = 9 * 3;
            Inventory TargetInventory = Bukkit.createInventory(null, InventorySize);
            FillInventoryWithRandomItems(TargetInventory);
            mcPlayer.openInventory(TargetInventory);
        }

        public void OpenDoubleChest(Player mcPlayer)
        {
            int InventorySize = 9 * 6;
            Inventory TargetInventory = Bukkit.createInventory(null, InventorySize);
            FillInventoryWithRandomItems(TargetInventory);
            mcPlayer.openInventory(TargetInventory);
        }

        public void OpenCraftingTable(Player mcPlayer)
        {
            Inventory TargetInventory = Bukkit.createInventory(null, InventoryType.CRAFTING);
            mcPlayer.openInventory(TargetInventory);
        }


        // Inherited methods.
        @Override
        public void OnAdd()
        {
            OnOpen(OpenClock);
        }

        @Override
        public void Execute()
        {
            OpenClock.Tick();
        }

        @Override
        public void OnRemove() { }
    }

    private enum OpenAction
    {
        CloseInventory,
        Chest,
        DoubleChest,
        CraftingTable
    }
}
