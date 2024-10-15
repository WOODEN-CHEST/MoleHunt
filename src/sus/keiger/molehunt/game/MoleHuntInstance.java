package sus.keiger.molehunt.game;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import sus.keiger.molehunt.IWorldProvider;
import sus.keiger.molehunt.event.IEventDispatcher;
import sus.keiger.molehunt.game.event.MoleHuntCompleteEvent;
import sus.keiger.molehunt.game.event.MoleHuntEndEvent;
import sus.keiger.molehunt.game.event.MoleHuntPreStartEvent;
import sus.keiger.molehunt.game.event.MoleHuntStartEvent;
import sus.keiger.molehunt.game.player.DefaultGamePlayer;
import sus.keiger.molehunt.game.player.IPlayerStats;
import sus.keiger.molehunt.player.IServerPlayer;
import sus.keiger.molehunt.player.IServerPlayerCollection;
import sus.keiger.plugincommon.PCMath;
import sus.keiger.plugincommon.PCPluginEvent;
import sus.keiger.plugincommon.entity.EntityFunctions;
import sus.keiger.plugincommon.packet.PCGamePacketController;
import sus.keiger.plugincommon.player.PlayerFunctions;

import java.time.Duration;
import java.util.*;
import java.util.stream.Stream;

public class MoleHuntInstance implements IMoleHuntGameInstance
{
    // Private fields.
    private final long _ID;
    private final PCGamePacketController _packetController;
    private final IEventDispatcher _eventDispatcher;
    private final MoleHuntSettings _settings;
    private final IWorldProvider _worldProvider;
    private final IServerPlayerCollection _playerCollection;

    private MoleHuntGameState _state = MoleHuntGameState.Lobby;

    private final Map<GameTeamType, IGameTeam> _teams = new HashMap<>();

    private final PCPluginEvent<MoleHuntPreStartEvent> _preStartEvent = new PCPluginEvent<>();
    private final PCPluginEvent<MoleHuntStartEvent> _startEvent = new PCPluginEvent<>();
    private final PCPluginEvent<MoleHuntEndEvent> _endEvent = new PCPluginEvent<>();
    private final PCPluginEvent<MoleHuntCompleteEvent> _completeEvent = new PCPluginEvent<>();

    private final int PRE_START_TICKS = PCMath.SecondsToTicks(10d);
    private final int POST_GAME_TICKS = PCMath.SecondsToTicks(30d);
    private final int CENTER_X = 0;
    private final int CENTER_Z = 0;
    private final int PLAYER_SPREAD = 10;

    private int _preStartTicksLeft;
    private int _postGameTicksLeft;
    private int _inGameTimerTicks;



    // Constructors.
    public MoleHuntInstance(long id,
                            IWorldProvider worldProvider,
                            PCGamePacketController packetController,
                            IEventDispatcher eventDispatcher,
                            IServerPlayerCollection playerCollection,
                            MoleHuntSettings settings)
    {
        _ID = id;
        _worldProvider = Objects.requireNonNull(worldProvider, "worldProvider is null");
        _packetController = Objects.requireNonNull(packetController, "packetController is null");
        _eventDispatcher = Objects.requireNonNull(eventDispatcher, "eventDispatcher is null");
        _settings = Objects.requireNonNull(settings, "settings is null");;

        _teams.put(GameTeamType.Moles, new MoleHuntTeam(GameTeamType.Moles,
                Color.fromRGB(0xa30000), "Mole"));
        _teams.put(GameTeamType.Innocents, new MoleHuntTeam(GameTeamType.Innocents,
                Color.fromRGB(0x52f22e), "Innocent"));
        _teams.put(GameTeamType.None, new MoleHuntTeam(GameTeamType.None,
                Color.fromRGB(0x5cb8ed), "None"));
    }




    // Private methods.
    private List<IServerPlayer> GetActivePlayersOfTeam(IGameTeam team)
    {
        return team.GetPlayers().stream().filter(_activePlayers::contains).toList();
    }

    private List<IServerPlayer> GetAlivePlayersOfTeam(IGameTeam team)
    {
        return team.GetPlayers().stream().filter(player -> _activePlayers.contains(player)
                && _players.get(player).IsAlive()).toList();
    }

    private List<IServerPlayer> GetAllParticipants()
    {
        return Stream.concat(_activePlayers.stream(), _spectators.stream()).toList();
    }

    private boolean IsStateOneOf(MoleHuntGameState ... states)
    {
        for (MoleHuntGameState State : states)
        {
            if (_state == State)
            {
                return true;
            }
        }
        return false;
    }

    private Location GetCenterLocation()
    {
        return new Location(_worldProvider.GetOverworld(), CENTER_X,
                _worldProvider.GetOverworld().getHighestBlockYAt(CENTER_X, CENTER_Z), CENTER_Z, 0f, 0f);
    }

    private Location GetStartingLocation()
    {
        Location Center = GetCenterLocation();

        Random RNG = new Random();
        Center.add(new Vector((RNG.nextDouble() - 0.5d * 2d) * PLAYER_SPREAD,
                0d,
                (RNG.nextDouble() - 0.5d * 2d) * PLAYER_SPREAD));
        Center.setY(Center.getWorld().getHighestBlockYAt(Center.getBlockX(), Center.getBlockZ()));

        return Center;
    }

    private void ShowStartCountdownContent()
    {
        ShowTitle(Title.title(Component.text(Integer.toString((int)PCMath.TicksToSeconds(_preStartTicksLeft)))
                .color(NamedTextColor.GREEN),
                Component.text("Game starting...").color(NamedTextColor.AQUA),
                Title.Times.times(Duration.ZERO, Duration.ofSeconds(3), Duration.ofMillis(500))));

        for (IServerPlayer TargetPlayer : GetAllParticipants())
        {
            TargetPlayer.PlaySound(Sound.BLOCK_NOTE_BLOCK_HAT, TargetPlayer.GetMCPlayer().getLocation(),
                    SoundCategory.AMBIENT, 1f, 1f);
        }
    }

    private void VerifyGameState()
    {
        if (GetActivePlayersOfTeam(_teams.get(GameTeamType.Moles)).isEmpty() ||
                GetActivePlayersOfTeam(_teams.get(GameTeamType.Innocents)).isEmpty())
        {
            SendMessage(Component.text("Game team missing players, can't continue game!").color(NamedTextColor.RED));
            Cancel();
        }
    }

    private void InitializeSpectator(IServerPlayer player)
    {

    }




    /* Lobby. */
    private void LobbyTick() { }


    /* Pre-start. */
    private void SwitchToPreStartState()
    {
        _preStartTicksLeft = PRE_START_TICKS;
        _state = MoleHuntGameState.PreStart;
        _activePlayers.forEach(this::SwitchToPreStartStatePlayer);
    }

    private void SwitchToPreStartStatePlayer(IServerPlayer player)
    {
       ResetPlayer(player);
    }

    private void PreStartTick()
    {
        if (_preStartTicksLeft <= 0)
        {
            SwitchToInGameState();
            return;
        }

        if (_preStartTicksLeft % PCMath.TICKS_IN_SECOND == 0)
        {
            ShowStartCountdownContent();
        }
        _preStartTicksLeft--;
    }


    /* In-game */


    private void SetupPlayerForGame(IServerPlayer player)
    {
        SendPlayerTabPackets(player);
    }

    private void AssignPlayerRoles()
    {
        Random RNG = new Random();
        int MoleCount = RNG.nextInt(_settings.GetMoleCountMin(), _settings.GetMoleCountMax() + 1);

        List<DefaultGamePlayer> Players = new ArrayList<>(_players.values());

        for (int i = 0; i < MoleCount; i++)
        {
            int Index = RNG.nextInt(Players.size());
            Players.get(Index).SetTeam(_teams.get(GameTeamType.Moles));
            Players.remove(Index);
        }
        Players.forEach(player -> player.SetTeam(_teams.get(GameTeamType.Innocents)));
    }

    private void ShowStartContent(IServerPlayer player)
    {

    }

    private void SwitchToInGameState()
    {
        AssignPlayerRoles();
        _activePlayers.forEach(this::SwitchToInGameStatePlayer);
    }

    private void SwitchToInGameStatePlayer(IServerPlayer player)
    {
        ResetPlayer(player);
        player.GetMCPlayer().teleport(GetStartingLocation());
        ShowStartContent(player);
        SetupPlayerForGame(player);
    }

    private void InGameTick()
    {

    }

    private void SwitchToPostGameState()
    {
        _activePlayers.forEach(this::SwitchToPostGameStatePlayer);

        IGameTeam WinnerTeam, LoserTeam;
        if (GetAlivePlayersOfTeam(_teams.get(GameTeamType.Innocents)).isEmpty())
        {
            WinnerTeam = _teams.get(GameTeamType.Moles);
            LoserTeam = _teams.get(GameTeamType.Innocents);
        }
        else
        {
            WinnerTeam = _teams.get(GameTeamType.Innocents);
            LoserTeam = _teams.get(GameTeamType.Moles);
        }
    }

    private void SwitchToPostGameStatePlayer(IServerPlayer player)
    {

    }

    private void PostGameTick()
    {

    }

    private void SwitchToCompleteState()
    {

    }


    // Inherited methods.
    @Override
    public boolean AddPlayer(IServerPlayer player)
    {
        Objects.requireNonNull(player, "player is null");
        if (_state == MoleHuntGameState.Lobby)
        {
            return false;
        }

        if (!_players.containsKey(player))
        {
            IGameTeam TargetTeam =  _teams.get(GameTeamType.None);
            _players.put(player, new DefaultGamePlayer(player, TargetTeam, _packetController));
            TargetTeam.AddPlayer(player);
        }
        _activePlayers.add(player);

        return true;
    }

    @Override
    public boolean RemovePlayer(IServerPlayer player)
    {
        Objects.requireNonNull(player, "player is null");
        if (_players.containsKey(player))
        {
            return false;
        }

        _activePlayers.remove(player);
        return true;
    }

    @Override
    public void ClearPlayers()
    {
        _activePlayers.clear();
    }

    @Override
    public boolean ContainsPlayer(IServerPlayer player)
    {
        return _players.containsKey(Objects.requireNonNull(player, "player is null"));
    }

    @Override
    public boolean ContainsActivePlayer(IServerPlayer player)
    {
        return _activePlayers.contains(Objects.requireNonNull(player, "player is null"));
    }

    @Override
    public IGameTeam GetTeamOfPlayer(IServerPlayer player)
    {
        DefaultGamePlayer TargetPlayer = _players.get(Objects.requireNonNull(player, "player is null"));
        if (TargetPlayer == null)
        {
            return null;
        }
        return TargetPlayer.GetTeam();
    }

    @Override
    public void Start()
    {
        if ((_state != MoleHuntGameState.Lobby) || (_settings.GetMoleCountMax() >= _activePlayers.size()))
        {
            return;
        }

        SwitchToPreStartState();
    }

    @Override
    public void End()
    {
        if (!IsStateOneOf(MoleHuntGameState.Lobby, MoleHuntGameState.PreStart))
        {
            return;
        }


        SwitchToPostGameState();
    }

    @Override
    public void Cancel()
    {
        SendMessage(Component.text("Game cancelled.").color(NamedTextColor.RED));
        SwitchToCompleteState();
    }

    @Override
    public MoleHuntGameState GetState()
    {
        return MoleHuntGameState.Lobby;
    }

    @Override
    public boolean AddSpectator(IServerPlayer player)
    {
        Objects.requireNonNull(player, "player is null");
        if (_spectators.contains(player) || (_state == MoleHuntGameState.Lobby))
        {
            return false;
        }

        _spectators.add(player);

        return true;
    }

    @Override
    public boolean RemoveSpectator(IServerPlayer player)
    {
        return false;
    }

    @Override
    public boolean ContainsSpectator(IServerPlayer player)
    {
        return false;
    }

    @Override
    public List<IServerPlayer> GetSpectators(IServerPlayer player)
    {
        return List.of();
    }

    @Override
    public IPlayerStats GetStatsOfPlayer(IServerPlayer player)
    {
        return null;
    }

    @Override
    public long GetGameID()
    {
        return _ID;
    }

    @Override
    public PCPluginEvent<MoleHuntPreStartEvent> GetPreStartEvent()
    {
        return _preStartEvent;
    }

    @Override
    public PCPluginEvent<MoleHuntStartEvent> GetStartEvent()
    {
        return _startEvent;
    }

    @Override
    public PCPluginEvent<MoleHuntEndEvent> GetEndEvent()
    {
        return _endEvent;
    }

    @Override
    public PCPluginEvent<MoleHuntCompleteEvent> GetCompleteEvent()
    {
        return _completeEvent;
    }

    @Override
    public void SubscribeToEvents(IEventDispatcher dispatcher)
    {

    }

    @Override
    public void UnsubscribeFromEvents(IEventDispatcher dispatcher)
    {

    }


    @Override
    public void Tick()
    {
        switch (_state)
        {
            case Lobby -> LobbyTick();
            case PreStart -> PreStartTick();
            case InGame -> InGameTick();
            case PostEnd -> PostGameTick();
            default -> throw new IllegalStateException("Illegal game state: %s".formatted(_settings.toString()));
        }
    }
}