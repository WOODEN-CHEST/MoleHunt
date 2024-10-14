package sus.keiger.molehunt.event;

import com.destroystokyo.paper.event.server.ServerTickStartEvent;
import io.papermc.paper.event.player.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.*;
import sus.keiger.plugincommon.PCPluginEvent;

public class DefaultEventDispatcher implements IEventDispatcher
{
    // Private fields.
    private final PCPluginEvent<ServerTickStartEvent> _serverTickStartEvent = new PCPluginEvent<>();
    private final PCPluginEvent<PlayerJoinEvent> _playerJoinEvent = new PCPluginEvent<>();
    private final PCPluginEvent<PlayerQuitEvent> _playerQuitEvent = new PCPluginEvent<>();
    private final PCPluginEvent<PlayerDeathEvent> _playerDeathEvent = new PCPluginEvent<>();
    private final PCPluginEvent<AsyncChatEvent> _asyncChatEvent = new PCPluginEvent<>();
    private final PCPluginEvent<EntityDamageEvent> _entityDamageEvent = new PCPluginEvent<>();
    private final PCPluginEvent<BlockBreakEvent> _blockBreakEvent = new PCPluginEvent<>();
    private final PCPluginEvent<BlockPlaceEvent> _blockPlaceEvent = new PCPluginEvent<>();
    private final PCPluginEvent<PlayerInteractEvent> _playerInteractEvent = new PCPluginEvent<>();
    private final PCPluginEvent<PlayerCommandSendEvent> _playerCommandSendEvent = new PCPluginEvent<>();
    private final PCPluginEvent<PlayerCommandPreprocessEvent> _playerCommandPreprocessEvent = new PCPluginEvent<>();
    private final PCPluginEvent<EntityDeathEvent> _entityDeathEvent = new PCPluginEvent<>();
    private final PCPluginEvent<PlayerDropItemEvent> _playerDropItemEvent = new PCPluginEvent<>();


    // Private methods.
    @EventHandler
    private void OnPlayerJoinEvent(PlayerJoinEvent event)
    {
        _playerJoinEvent.FireEvent(event);
    }

    @EventHandler
    private void OnPlayerQuitEvent(PlayerQuitEvent event)
    {
        _playerQuitEvent.FireEvent(event);
    }

    @EventHandler
    private void OnPlayerDeathEvent(PlayerDeathEvent event)
    {
        _playerDeathEvent.FireEvent(event);
    }

    @EventHandler
    private void OnAsyncChatEvent(AsyncChatEvent event)
    {
        _asyncChatEvent.FireEvent(event);
    }

    @EventHandler
    private void OnEntityDamageEvent(EntityDamageEvent event)
    {
        _entityDamageEvent.FireEvent(event);
    }

    @EventHandler
    private void OnBlockBreakEventEvent(BlockBreakEvent event)
    {
        _blockBreakEvent.FireEvent(event);
    }

    @EventHandler
    private void OnBlockPlaceEventEvent(BlockPlaceEvent event)
    {
        _blockPlaceEvent.FireEvent(event);
    }

    @EventHandler
    private void OnPlayerInteractEvent(PlayerInteractEvent event)
    {
        _playerInteractEvent.FireEvent(event);
    }

    @EventHandler
    private void OnPlayerCommandSendEvent(PlayerCommandSendEvent event)
    {
        _playerCommandSendEvent.FireEvent(event);
    }

    @EventHandler
    private void OnPlayerCommandPreprocessEvent(PlayerCommandPreprocessEvent event)
    {
        _playerCommandPreprocessEvent.FireEvent(event);
    }

    @EventHandler
    private void OnServerTickStartEvent(ServerTickStartEvent event)
    {
        _serverTickStartEvent.FireEvent(event);
    }

    @EventHandler
    private void OnEntityDeathEvent(EntityDeathEvent event)
    {
        _entityDeathEvent.FireEvent(event);
    }

    @EventHandler
    private void OnPlayerDropItemEvent(PlayerDropItemEvent event)
    {
        _playerDropItemEvent.FireEvent(event);
    }


    // Inherited methods.
    @Override
    public PCPluginEvent<PlayerJoinEvent> GetPlayerJoinEvent()
    {
        return _playerJoinEvent;
    }

    @Override
    public PCPluginEvent<PlayerQuitEvent> GetPlayerQuitEvent()
    {
        return _playerQuitEvent;
    }

    @Override
    public PCPluginEvent<PlayerDeathEvent> GetPlayerDeathEvent()
    {
        return _playerDeathEvent;
    }

    @Override
    public PCPluginEvent<AsyncChatEvent> GetAsyncChatEvent()
    {
        return _asyncChatEvent;
    }

    @Override
    public PCPluginEvent<EntityDamageEvent> GetEntityDamageEvent()
    {
        return _entityDamageEvent;
    }

    @Override
    public PCPluginEvent<BlockBreakEvent> GetBlockBreakEvent()
    {
        return _blockBreakEvent;
    }

    @Override
    public PCPluginEvent<BlockPlaceEvent> GetBlockPlaceEvent()
    {
        return _blockPlaceEvent;
    }

    @Override
    public PCPluginEvent<PlayerInteractEvent> GetPlayerInteractEvent()
    {
        return _playerInteractEvent;
    }

    @Override
    public PCPluginEvent<PlayerCommandSendEvent> GetPlayerCommandSendEvent()
    {
        return _playerCommandSendEvent;
    }

    @Override
    public PCPluginEvent<PlayerCommandPreprocessEvent> GetPlayerCommandPreprocessEvent()
    {
        return _playerCommandPreprocessEvent;
    }

    @Override
    public PCPluginEvent<EntityDeathEvent> GetEntityDeathEvent()
    {
        return _entityDeathEvent;
    }

    @Override
    public PCPluginEvent<PlayerDropItemEvent> GetPlayerDropItemEvent()
    {
        return _playerDropItemEvent;
    }

    @Override
    public PCPluginEvent<ServerTickStartEvent> GetTickStartEvent()
    {
        return _serverTickStartEvent;
    }

}
