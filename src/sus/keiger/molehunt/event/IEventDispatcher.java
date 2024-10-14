package sus.keiger.molehunt.event;

import com.destroystokyo.paper.event.server.ServerTickStartEvent;
import io.papermc.paper.event.player.AsyncChatEvent;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import sus.keiger.plugincommon.PCPluginEvent;

public interface IEventDispatcher extends Listener
{
    PCPluginEvent<ServerTickStartEvent> GetTickStartEvent();
    PCPluginEvent<PlayerJoinEvent> GetPlayerJoinEvent();
    PCPluginEvent<PlayerQuitEvent> GetPlayerQuitEvent();
    PCPluginEvent<PlayerDeathEvent> GetPlayerDeathEvent();
    PCPluginEvent<AsyncChatEvent> GetAsyncChatEvent();
    PCPluginEvent<EntityDamageEvent> GetEntityDamageEvent();
    PCPluginEvent<BlockBreakEvent> GetBlockBreakEvent();
    PCPluginEvent<BlockPlaceEvent> GetBlockPlaceEvent();
    PCPluginEvent<PlayerInteractEvent> GetPlayerInteractEvent();
    PCPluginEvent<PlayerCommandSendEvent> GetPlayerCommandSendEvent();
    PCPluginEvent<PlayerCommandPreprocessEvent> GetPlayerCommandPreprocessEvent();
    PCPluginEvent<EntityDeathEvent> GetEntityDeathEvent();
    PCPluginEvent<PlayerDropItemEvent> GetPlayerDropItemEvent();
}