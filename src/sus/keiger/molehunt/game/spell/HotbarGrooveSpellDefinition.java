package sus.keiger.molehunt.game.spell;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import sus.keiger.molehunt.game.IGameServices;
import sus.keiger.plugincommon.PCMath;
import sus.keiger.plugincommon.TickClock;
import sus.keiger.plugincommon.player.PlayerFunctions;

public class HotbarGrooveSpellDefinition extends GameSpellDefinition
{
    // Constructors.
    public HotbarGrooveSpellDefinition()
    {
        super("Hotbar", "Makes the hotbar do a groovy dance for the victim.",
                SpellDurationType.Sustained, SpellClass.Regular, 0.7d, SpellDataRequirement.TargetPlayer);
    }


    // Inherited methods.
    @Override
    public GameSpell CreateSpell(GameSpellArguments args, IGameServices services)
    {
        return new HotbarGrooveSpell(this, args, services);
    }


    // Types.
    private static class HotbarGrooveSpell extends GameSpell
    {
        // Fields.
        public final TickClock MoveClock = new TickClock();
        public final int TICKS_BEFORE_MOVE = PCMath.SecondsToTicks(1d);
        public final int DURATION = PCMath.SecondsToTicks(20d);
        public final int SLOT_COUNT = PlayerFunctions.SLOT_HOTBAR9 - PlayerFunctions.SLOT_HOTBAR1 + 1;


        // Constructors.
        public HotbarGrooveSpell(GameSpellDefinition definition,
                              GameSpellArguments arguments,
                              IGameServices services)
        {
            super(definition, arguments, services);
            SetTicksRemaining(DURATION);
            MoveClock.SetTicksLeft(TICKS_BEFORE_MOVE);
            MoveClock.SetIsRunning(true);
            MoveClock.SetHandler(this::OnMove);
        }


        // Methods.
        public void OnMove(TickClock clock)
        {
            clock.SetTicksLeft(TICKS_BEFORE_MOVE);

            ItemStack[] Hotbar = GetCurrentHotbar();
            SetHotbar(ShiftHotbar(Hotbar, 1));
        }

        public ItemStack[] GetCurrentHotbar()
        {
            Player MCPlayer = GetArguments().GetTargetPlayer().GetMCPlayer();

            ItemStack[] Hotbar = new ItemStack[SLOT_COUNT];
            for (int i = 0; i < SLOT_COUNT; i++)
            {
                Hotbar[i] = PlayerFunctions.GetItem(MCPlayer, i + PlayerFunctions.SLOT_HOTBAR1);
            }

            return Hotbar;
        }

        public ItemStack[] ShiftHotbar(ItemStack[] items, int moveCount)
        {
            ItemStack[] NewHotBar = new ItemStack[items.length];

            for (int i = 0; i < SLOT_COUNT; i++)
            {
                int NewSlot = (i + moveCount) % SLOT_COUNT;

                NewHotBar[NewSlot] = items[i];
            }

            return NewHotBar;
        }

        public void SetHotbar(ItemStack[] hotbar)
        {
            Player MCPlayer = GetArguments().GetTargetPlayer().GetMCPlayer();

            for (int i = 0; (i < SLOT_COUNT) && (i < hotbar.length); i++)
            {
                PlayerFunctions.SetItem(MCPlayer, i + PlayerFunctions.SLOT_HOTBAR1, hotbar[i]);
            }
        }


        // Inherited methods.
        @Override
        public void OnAdd() { }

        @Override
        public void Execute()
        {
            MoveClock.Tick();
        }

        @Override
        public void OnRemove() { }
    }
}
