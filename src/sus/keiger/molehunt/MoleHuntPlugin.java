package sus.keiger.molehunt;

import com.comphenix.protocol.ProtocolLibrary;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.BoundingBox;
import sus.keiger.molehunt.event.*;
import sus.keiger.molehunt.game.MoleHuntSettings;
import sus.keiger.molehunt.lobby.*;
import sus.keiger.molehunt.player.DefaultServerPlayerCollection;
import sus.keiger.molehunt.player.*;
import sus.keiger.plugincommon.IIDProvider;
import sus.keiger.plugincommon.ITickable;
import sus.keiger.plugincommon.SequentialIDProvider;
import sus.keiger.plugincommon.packet.PCGamePacketController;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.*;

public class MoleHuntPlugin extends JavaPlugin
{
    // Static fields.
    public static Locale GetLocale()
    {
        return Locale.ROOT;
    }

    public static NumberFormat GetNumberFormat(String pattern)
    {
        return new DecimalFormat(pattern, DecimalFormatSymbols.getInstance(GetLocale()));
    }


    // Private methods.
    private void InitializeWorlds(IWorldProvider worldProvider)
    {
        IWorldInitializer WorldInitializer = new DefaultWorldInitializer();
        WorldInitializer.Initialize(worldProvider.GetOverworld());
        WorldInitializer.Initialize(worldProvider.GetNether());
        WorldInitializer.Initialize(worldProvider.GetTheEnd());
    }


    // Private methods.
    private IServerLobby CreateLobby(IWorldProvider worldProvider,
                                     IServerPlayerCollection players,
                                     IEventDispatcher eventDispatcher)
    {
        final double SPAWN_SIZE_RADIUS = 100d;
        final int SPAWN_LOCATION_X = 0;
        final int SPAWN_LOCATION_Z = 0;

        World TargetWorld = worldProvider.GetOverworld();
        BoundingBox Bounds = new BoundingBox(-SPAWN_SIZE_RADIUS, Integer.MIN_VALUE, -SPAWN_SIZE_RADIUS,
                SPAWN_SIZE_RADIUS, Integer.MAX_VALUE, SPAWN_SIZE_RADIUS);
        Location SpawnLocation = new Location(TargetWorld, SPAWN_LOCATION_X,
                TargetWorld.getHighestBlockYAt(SPAWN_LOCATION_X, SPAWN_LOCATION_Z) ,
                SPAWN_LOCATION_Z, 0f, 0f);

        return new DefaultServerLobby(players, SpawnLocation, Bounds, eventDispatcher);
    }


    // Inherited methods.
    @Override
    public void onEnable()
    {
        // Create core objects.
        IWorldProvider WorldProvider = new DefaultWorldProvider();
        IServerPlayerCollection Players = new DefaultServerPlayerCollection();
        IIDProvider GameIDProvider = new SequentialIDProvider();
        PCGamePacketController PacketController = new PCGamePacketController(
                this, ProtocolLibrary.getProtocolManager());


        // Create server components.
        IEventDispatcher EventDispatcher = new DefaultEventDispatcher(Players);
        IPlayerExistenceController ExistenceController = new PlayerExistenceController(Players);

        IServerLobby Lobby = CreateLobby(WorldProvider, Players, EventDispatcher);

        IPlayerStateController PlayerStateController = new DefaultPlayerStateController(
                Players, new MoleHuntSettings(), Lobby);


        // Initialize created components and objects.
        InitializeWorlds(WorldProvider);
        ExistenceController.SubscribeToEvents(EventDispatcher);
        PlayerStateController.SubscribeToEvents(EventDispatcher);
        Lobby.SubscribeToEvents(EventDispatcher);
        Bukkit.getPluginManager().registerEvents(EventDispatcher, this);
        PacketController.StartListeningForPackets();
        ExistenceController.ReloadPlayers();

        List<ITickable> Tickables = List.of(ExistenceController, Lobby, PlayerStateController);
        EventDispatcher.GetTickStartEvent().Subscribe(this, event -> Tickables.forEach(ITickable::Tick));
    }

    @Override
    public void onDisable()
    {

    }
}
