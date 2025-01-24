package sus.keiger.molehunt;

import com.comphenix.protocol.ProtocolLibrary;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.BoundingBox;
import sus.keiger.molehunt.command.MoleHuntCommand;
import sus.keiger.molehunt.command.SpellCommand;
import sus.keiger.molehunt.event.*;
import sus.keiger.molehunt.game.MoleHuntSettings;
import sus.keiger.molehunt.game.spell.*;
import sus.keiger.molehunt.lobby.*;
import sus.keiger.molehunt.player.DefaultServerPlayerCollection;
import sus.keiger.molehunt.player.*;
import sus.keiger.molehunt.service.DefaultServerServices;
import sus.keiger.molehunt.service.IServerServices;
import sus.keiger.molehunt.voicechat.IVoiceChatController;
import sus.keiger.plugincommon.CachedMojangAPIClient;
import sus.keiger.plugincommon.ITickable;
import sus.keiger.plugincommon.command.ServerCommand;
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

    public static String PluginID()
    {
        return "MoleHuntWC";
    }

    public static String GetKey()
    {
        return "molehuntwc";
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
        final double SPAWN_SIZE_RADIUS = 30d;
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

    private void RegisterCommands(IPlayerStateController playerStateController,
                                  MoleHuntSettings settings,
                                  IServerPlayerCollection serverPlayers,
                                  GameSpellCollection spells)
    {
        ServerCommand MCommand = MoleHuntCommand.CreateCommand(playerStateController, settings);
        Bukkit.getPluginCommand(MoleHuntCommand.LABEL).setTabCompleter(MCommand);
        Bukkit.getPluginCommand(MoleHuntCommand.LABEL).setExecutor(MCommand);

        ServerCommand SCommand = SpellCommand.CreateCommand(playerStateController, spells, serverPlayers);
        Bukkit.getPluginCommand(SpellCommand.LABEL).setTabCompleter(SCommand);
        Bukkit.getPluginCommand(SpellCommand.LABEL).setExecutor(SCommand);
    }

    private GameSpellCollection GetSpellCollection()
    {
        GameSpellCollection Spells = new GameSpellCollection();

        Spells.AddSpell(new HeartBeatSpellDefinition());
        Spells.AddSpell(new HealthScrambleSpellDefinition());
        Spells.AddSpell(new RotateSpellDefinition());
        Spells.AddSpell(new ShakeSpellDefinition());
        Spells.AddSpell(new DarknessSpellDefinition());
        Spells.AddSpell(new BreakBlockSpell());
        Spells.AddSpell(new RandomEnchantSpellDefinition());
        Spells.AddSpell(new VoiceMuteSpellDefinition());
        Spells.AddSpell(new BlockSlownessSpellDefinition());
        Spells.AddSpell(new InstantSmeltSpellDefinition());
        Spells.AddSpell(new RandomLootSpellDefinition());
        Spells.AddSpell(new SmallCreeperSpellDefinition());
        Spells.AddSpell(new DemoScreenSpellDefinition());
        Spells.AddSpell(new ContainerSpellDefinition());
        Spells.AddSpell(new RandomWeatherSpellDefinition());
        Spells.AddSpell(new HotbarGrooveSpellDefinition());
        Spells.AddSpell(new InvisSpellDefinition());

        return Spells;
    }


    // Inherited methods.
    @Override
    public void onEnable()
    {
        // Create core objects.
        IWorldProvider WorldProvider = new DefaultWorldProvider();
        IServerPlayerCollection Players = new DefaultServerPlayerCollection();
        PCGamePacketController PacketController = new PCGamePacketController(
                this, ProtocolLibrary.getProtocolManager());
        IEventDispatcher EventDispatcher = new DefaultEventDispatcher();
        IVoiceChatController VoiceChatController = new DefaultVoiceChatController();



        // Create server components.
        IServerServices Services = new DefaultServerServices(EventDispatcher,
                VoiceChatController, WorldProvider, Players, PacketController,
                new CachedMojangAPIClient(getLogger()));

        GameSpellCollection Spells = GetSpellCollection();

        IPlayerExistenceController ExistenceController = new PlayerExistenceController(Players);

        IServerLobby Lobby = CreateLobby(WorldProvider, Players, EventDispatcher);

        MoleHuntSettings GameSettings = new MoleHuntSettings();
        IPlayerStateController PlayerStateController =
                new DefaultPlayerStateController(Services, GameSettings, Lobby);
        SkinChanger PlayerSkinChanger = new SkinChanger(Services, GameSettings);


        // Initialize created components and objects.
        InitializeWorlds(WorldProvider);
        ExistenceController.SubscribeToEvents(EventDispatcher);
        PlayerStateController.SubscribeToEvents(EventDispatcher);
        Lobby.SubscribeToEvents(EventDispatcher);
        Bukkit.getPluginManager().registerEvents(EventDispatcher, this);
        PacketController.StartListeningForPackets();
        ExistenceController.ReloadPlayers();
        PlayerSkinChanger.SubscribeToEvents(EventDispatcher);

        List<ITickable> Tickables = List.of(ExistenceController, Lobby, PlayerStateController);
        EventDispatcher.GetTickStartEvent().Subscribe(this, event -> Tickables.forEach(ITickable::Tick));
        VoiceChatController.SubscribeToEvents(EventDispatcher);

        // Commands.
        RegisterCommands(PlayerStateController, GameSettings, Players, Spells);
    }

    @Override
    public void onDisable() { }
}
