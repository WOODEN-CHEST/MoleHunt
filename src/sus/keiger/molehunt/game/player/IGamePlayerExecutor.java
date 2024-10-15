package sus.keiger.molehunt.game.player;

import io.papermc.paper.event.player.AsyncChatEvent;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import sus.keiger.plugincommon.ITickable;

public interface IGamePlayerExecutor extends ITickable
{
    void SwitchState(GamePlayerState state);

    void OnPlayerDeathEvent(PlayerDeathEvent event);
    void OnEntityDamageEvent(EntityDamageEvent event);
    void OnAsyncChatEvent(AsyncChatEvent event);
    void OnBlockBreakEvent(BlockBreakEvent event);
    void OnBlockPlaceEvent(BlockPlaceEvent event);
    void OnPlayerInteractEvent(PlayerInteractEvent event);
    void OnPlayerDropItemEvent(PlayerDropItemEvent event);
}