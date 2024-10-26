package sus.keiger.molehunt.game;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import sus.keiger.molehunt.event.IEventDispatcher;
import sus.keiger.molehunt.event.IMoleHuntEventListener;

import java.util.*;

public class GameItemModifier implements IGameStateContaining, IMoleHuntEventListener
{
    // Private fields.
    private MoleHuntGameState _state = MoleHuntGameState.Initializing;
    private final Map<NamespacedKey, Recipe> _recipesToRemove;
    private final Map<NamespacedKey, Recipe> _recipesToAdd;

    private final double APPLE_SPAWN_CHANCE = 0.1d;


    // Constructors.
    public GameItemModifier()
    {
        _recipesToRemove = GetRecipesToFilter();
        _recipesToAdd = GetRecipesToAdd();
    }


    // Private methods.
    private void OnBlockBreakEvent(BlockBreakEvent event)
    {
        if (_state != MoleHuntGameState.InGame)
        {
            return;
        }

        if ((event.getBlock().getType() == Material.OAK_LEAVES) && (new Random().nextDouble() <= APPLE_SPAWN_CHANCE))
        {
            Item ItemEntity = (Item)event.getBlock().getWorld().spawnEntity(
                    event.getBlock().getLocation(), EntityType.ITEM);
            ItemEntity.setItemStack(new ItemStack(Material.APPLE, 1));
        }
    }

    private Map<NamespacedKey, Recipe> GetRecipesToFilter()
    {
        Map<NamespacedKey, Recipe> RecipesToFilter = new HashMap<>();

        NamespacedKey Key = new NamespacedKey(NamespacedKey.MINECRAFT, "golden_apple");
        RecipesToFilter.put(Key, Bukkit.getRecipe(Key));

        return RecipesToFilter;
    }

    private Map<NamespacedKey, Recipe> GetRecipesToAdd()
    {
        Map<NamespacedKey, Recipe> RecipesToAdd = new HashMap<>();

        NamespacedKey Key = new NamespacedKey(NamespacedKey.MINECRAFT, "golden_apple");
        ShapedRecipe GoldenAppleRecipe = new ShapedRecipe(Key, new ItemStack(Material.GOLDEN_APPLE, 1));
        GoldenAppleRecipe.shape(" g ", "gag", " g ");
        GoldenAppleRecipe.setIngredient('g', Material.GOLD_INGOT);
        GoldenAppleRecipe.setIngredient('a', Material.APPLE);
        RecipesToAdd.put(Key, GoldenAppleRecipe);

        return RecipesToAdd;
    }

    private void InitializeRecipes()
    {
        _recipesToRemove.keySet().forEach(Bukkit::removeRecipe);
        _recipesToAdd.values().forEach(Bukkit::addRecipe);
        Bukkit.updateRecipes();
    }

    private void DeinitializeRecipes()
    {
        _recipesToAdd.keySet().forEach(Bukkit::removeRecipe);
        _recipesToRemove.values().forEach(Bukkit::addRecipe);
        Bukkit.updateRecipes();
    }



    // Inherited methods.
    @Override
    public void SubscribeToEvents(IEventDispatcher dispatcher)
    {
        dispatcher.GetBlockBreakEvent().Subscribe(this, this::OnBlockBreakEvent);
    }

    @Override
    public void UnsubscribeFromEvents(IEventDispatcher dispatcher)
    {
        dispatcher.GetBlockBreakEvent().Unsubscribe(this, this::OnBlockBreakEvent);
    }

    @Override
    public void SetState(MoleHuntGameState state)
    {
        _state = Objects.requireNonNull(state, "state is null");

        if (_state == MoleHuntGameState.InGame)
        {
            InitializeRecipes();
        }
        else
        {
            DeinitializeRecipes();
        }
    }
}
