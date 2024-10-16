package sus.keiger.molehunt.game;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.Vector;
import sus.keiger.molehunt.IWorldProvider;
import sus.keiger.molehunt.event.IEventDispatcher;
import sus.keiger.molehunt.game.event.*;
import sus.keiger.molehunt.game.player.*;
import sus.keiger.molehunt.game.spell.SpellContainer;
import sus.keiger.molehunt.game.spell.SpellSettings;
import sus.keiger.molehunt.player.*;
import sus.keiger.plugincommon.*;
import sus.keiger.plugincommon.packet.PCGamePacketController;
import sus.keiger.plugincommon.player.PlayerFunctions;
import sus.keiger.plugincommon.player.actionbar.ActionbarMessage;

import java.time.Duration;
import java.util.*;

public class MoleHuntInstance implements IMoleHuntGameInstance
{
    // Private fields.
    private final long _ID;
    private final PCGamePacketController _packetController;
    private final IEventDispatcher _eventDispatcher;
    private final MoleHuntSettings _settings;
    private final IWorldProvider _worldProvider;
    private final IServerPlayerCollection _serverPlayerCollection;
    private final Map<GameTeamType, IGameTeam> _teams = new HashMap<>();
    private final GamePlayerCollection _gamePlayerCollection;
    private final GameTabListUpdater _tabUpdater;
    private final SpellContainer _spells = new SpellContainer();
    private final TickClock _clock = new TickClock();
    private final TickClock _graceClock = new TickClock();
    private final TickClock _borderShrinkClock = new TickClock();
    private final Scoreboard _scoreboard;
    private final Team _minecraftTeam;

    private final PCPluginEvent<MoleHuntPreStartEvent> _preStartEvent = new PCPluginEvent<>();
    private final PCPluginEvent<MoleHuntStartEvent> _startEvent = new PCPluginEvent<>();
    private final PCPluginEvent<MoleHuntEndEvent> _endEvent = new PCPluginEvent<>();
    private final PCPluginEvent<MoleHuntCompleteEvent> _completeEvent = new PCPluginEvent<>();

    private MoleHuntGameState _state = MoleHuntGameState.Initializing;

    private final int PRE_START_TICKS = PCMath.SecondsToTicks(10d);
    private final int POST_GAME_TICKS = PCMath.SecondsToTicks(10d);
    private final int CENTER_X = 0;
    private final int CENTER_Z = 0;
    private final int PLAYER_SPREAD = 10;


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
        _settings = new MoleHuntSettings(Objects.requireNonNull(settings, "settings is null"));
        _serverPlayerCollection = Objects.requireNonNull(playerCollection, "playerCollection is null");
        _gamePlayerCollection = new GamePlayerCollection();
        _tabUpdater = new GameTabListUpdater(packetController);

        _teams.put(GameTeamType.Moles, new MoleHuntTeam(GameTeamType.Moles,
                Color.fromRGB(0xa30000), "Mole"));
        _teams.put(GameTeamType.Innocents, new MoleHuntTeam(GameTeamType.Innocents,
                Color.fromRGB(0x52f22e), "Innocent"));
        _teams.put(GameTeamType.None, new MoleHuntTeam(GameTeamType.None,
                Color.fromRGB(0x5cb8ed), "None"));
        _clock.SetIsRunning(false);

        _scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();

        _minecraftTeam = _scoreboard.registerNewTeam("mole_hunt_team");
        _minecraftTeam.color(NamedTextColor.WHITE);
        _minecraftTeam.prefix(null);
        _minecraftTeam.suffix(null);
        _minecraftTeam.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.ALWAYS);
        _minecraftTeam.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.FOR_OWN_TEAM);
        _minecraftTeam.setAllowFriendlyFire(true);
    }




    // Private methods.
    private String GetTimeString(int ticks)
    {
        int Seconds = ticks / PCMath.TICKS_IN_SECOND % 60;
        int Minutes = ticks / PCMath.TICKS_IN_SECOND / 60 % 60;
        int Hours = ticks / PCMath.TICKS_IN_SECOND / 60 / 60;

        final char MiddleSymbol = ':';
        StringBuilder Builder = new StringBuilder();
        Builder.append(Hours);
        Builder.append(MiddleSymbol);

        if (Minutes < 10)
        {
            Builder.append('0');
        }
        Builder.append(Minutes);

        Builder.append(MiddleSymbol);
        if (Seconds < 10)
        {
            Builder.append('0');
        }
        Builder.append(Seconds);

        return Builder.toString();
    }

    private void ShowGameTime()
    {
        if (_clock.GetTicksLeft() % PCMath.TICKS_IN_SECOND != 0)
        {
            return;
        }

        Component Text = Component.text("Time left: %s".formatted(GetTimeString(_clock.GetTicksLeft())))
                .color(NamedTextColor.GREEN);
        _gamePlayerCollection.GetActivePlayers().forEach(player -> player.ShowActionbar(new ActionbarMessage(
                PCMath.TICKS_IN_SECOND, Text)));
    }

    private void ShowGraceTime()
    {
        if ((_graceClock.GetTicksLeft() <= 0) || (_graceClock.GetTicksLeft() % PCMath.TICKS_IN_SECOND != 0))
        {
            return;
        }

        Component Text = Component.text("Grace Period: %s".formatted(GetTimeString(_graceClock.GetTicksLeft())))
                .color(NamedTextColor.GREEN);
        _gamePlayerCollection.GetActivePlayers().forEach(player -> player.ShowActionbar(new ActionbarMessage(
                PCMath.TICKS_IN_SECOND, Text)));
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

    private void InitializeSpectator(IServerPlayer player)
    {
        Player MCPlayer = player.GetMCPlayer();
        MCPlayer.clearActivePotionEffects();
        PlayerFunctions.ResetAttributes(MCPlayer);
        MCPlayer.setGameMode(GameMode.SPECTATOR);
        MCPlayer.teleport(GetCenterLocation());
        _tabUpdater.UpdateSpectatorTabList(player, _gamePlayerCollection);
    }

    private void TestGameCancelConditions()
    {
        if (_gamePlayerCollection.GetActivePlayers().isEmpty())
        {
            Cancel();
        }
    }

    private void TestGameEndConditions()
    {
        int AlivePlayerCount = (int)_teams.get(GameTeamType.Innocents).GetPlayers()
                .stream().filter(IGamePlayer::IsAlive).count();
        int DeadPlayerCount = (int)_teams.get(GameTeamType.Innocents).GetPlayers()
                .stream().filter(IGamePlayer::IsAlive).count();

        if ((AlivePlayerCount == 0) || (DeadPlayerCount == 0))
        {
            End();
        }
    }


    /* Events. */
    private void OnPlayerQuitEvent(PlayerQuitEvent event)
    {
        IServerPlayer ServerPlayer = _serverPlayerCollection.GetPlayer(event.getPlayer());
        if (ContainsSpectator(ServerPlayer))
        {
            RemoveSpectator(ServerPlayer);
            return;
        }

        IGamePlayer GamePlayer = _gamePlayerCollection.GetGamePlayer(ServerPlayer);
        if (GamePlayer == null)
        {
            return;
        }

        _minecraftTeam.removeEntity(GamePlayer.GetMCPlayer());
        GamePlayer.SetIsAlive(false);
        GamePlayer.UnsubscribeFromEvents(_eventDispatcher);

        if (_state == MoleHuntGameState.InGame)
        {
            TestGameEndConditions();
            TestGameCancelConditions();
        }
        else if (_state != MoleHuntGameState.Complete)
        {
            TestGameCancelConditions();
        }
    }


    /* Initializing. */
    private void InitializingTick() { }


    /* Pre-start. */
    private void ResetWorldBorder(World world)
    {
        double DefaultBorderSize = 15_000_000d;
        WorldBorder Border = world.getWorldBorder();
        Border.setSize(DefaultBorderSize);
    }

    private void SwitchToPreStartState()
    {
        _clock.SetTicksLeft(PRE_START_TICKS);
        _state = MoleHuntGameState.PreStart;
        _gamePlayerCollection.GetActivePlayers().forEach(this::SwitchToPreStartStatePlayer);
        _clock.SetHandler(clock -> SwitchToInGameState());
        _clock.SetTickFunction(clock -> PreStartTick());
        _clock.SetIsRunning(true);
        _graceClock.SetIsRunning(false);
        _worldProvider.GetWorlds().forEach(this::ResetWorldBorder);
        _borderShrinkClock.SetIsRunning(false);
        _preStartEvent.FireEvent(new MoleHuntPreStartEvent(this));
    }

    private void SwitchToPreStartStatePlayer(IGamePlayer player)
    {
       player.SetTargetState(GamePlayerState.PreGame);
       _tabUpdater.UpdateNonInGameTabList(player.GetServerPlayer(), _gamePlayerCollection);
       player.SubscribeToEvents(_eventDispatcher);
    }

    private void ShowStartCountdownContent()
    {
        ShowTitle(Title.title(Component.text(Integer.toString((int)PCMath.TicksToSeconds(_clock.GetTicksLeft())))
                        .color(NamedTextColor.GREEN),
                Component.text("Game starting...").color(NamedTextColor.AQUA),
                Title.Times.times(Duration.ZERO, Duration.ofSeconds(3), Duration.ofMillis(500))));

        for (IServerPlayer TargetPlayer : _gamePlayerCollection.GetParticipants())
        {
            TargetPlayer.PlaySound(Sound.BLOCK_NOTE_BLOCK_HAT, TargetPlayer.GetMCPlayer().getLocation(),
                    SoundCategory.AMBIENT, 1f, 1f);
        }
    }

    private void PreStartTick()
    {
        _gamePlayerCollection.GetActivePlayers().forEach(ITickable::Tick);
        if (_clock.GetTicksLeft() % PCMath.TICKS_IN_SECOND == 0)
        {
            ShowStartCountdownContent();
        }

        TestGameCancelConditions();
    }


    /* In-game */
    private void InitializeWorldBorder(World world)
    {
        WorldBorder Border = world.getWorldBorder();
        Border.setSize(_settings.GetBorderSizeStartBlocks());
    }

    private void BeginWorldBorderShrinkForWorld(World world)
    {
        WorldBorder Border = world.getWorldBorder();
        Border.setSize(_settings.GetBorderSizeEndBlocks(),
                _settings.GetBorderShrinkStartTimeTicks() / PCMath.TICKS_IN_SECOND);
    }

    private void AssignPlayerRoles()
    {
        Random RNG = new Random();
        int MoleCount = RNG.nextInt(_settings.GetMoleCountMin(), _settings.GetMoleCountMax() + 1);

        List<IGamePlayer> Players = new ArrayList<>(_gamePlayerCollection.GetActivePlayers());

        for (int i = 0; i < MoleCount; i++)
        {
            int Index = RNG.nextInt(Players.size());
            Players.get(Index).SetTeam(_teams.get(GameTeamType.Moles));
            Players.remove(Index);
        }
        Players.forEach(player -> player.SetTeam(_teams.get(GameTeamType.Innocents)));
    }

    private void ShowStartContent(IGamePlayer player)
    {
        final int TITLE_FADE_IN_MILLISECONDS = 500;
        final int TITLE_FADE_OUT_MILLISECONDS = 500;
        final int TITLE_TIME_SECONDS = 5;

        TextComponent.Builder MessageBuilder = Component.text();
        IGameTeam PlayerTeam = player.GetTeam();
        TextColor PlayerTextColor = TextColor.color(PlayerTeam.GetColor().asRGB());
        MessageBuilder.append(Component.text("Game started!").color(NamedTextColor.GREEN));
        MessageBuilder.append(Component.text(" Your role: %s".formatted(PlayerTeam.GetName())).color(PlayerTextColor));
        player.SendMessage(MessageBuilder.build());

        player.ShowTitle(Title.title(Component.text(PlayerTeam.GetName()).color(PlayerTextColor),
                Component.text(""), Title.Times.times(
                        Duration.ofMillis(TITLE_FADE_IN_MILLISECONDS),
                        Duration.ofSeconds(TITLE_TIME_SECONDS),
                        Duration.ofMillis(TITLE_FADE_OUT_MILLISECONDS))));

        player.PlaySound(PlayerTeam.GetType() == GameTeamType.Innocents ? Sound.ENTITY_ALLAY_AMBIENT_WITH_ITEM :
                Sound.ENTITY_WITHER_SPAWN, player.GetMCPlayer().getLocation(), SoundCategory.PLAYERS, 1f, 1f);
    }

    private void SwitchToInGameState()
    {
        AssignPlayerRoles();
        _gamePlayerCollection.GetActivePlayers().forEach(this::SwitchToInGameStatePlayer);

        _worldProvider.GetWorlds().forEach(this::InitializeWorldBorder);
        _borderShrinkClock.SetIsRunning(true);
        _borderShrinkClock.SetTicksLeft(_settings.GetBorderShrinkStartTimeTicks());
        _borderShrinkClock.SetHandler(clock -> StartBorderShrink());

        _clock.SetTicksLeft(_settings.GetGameTimeTicks());
        _clock.SetHandler(clock -> SwitchToPostGameState());
        _clock.SetTickFunction(clock -> InGameTick());

        _graceClock.SetIsRunning(true);
        _graceClock.SetTicksLeft(_settings.GetGracePeriodTimeTicks());
        _graceClock.SetHandler(clock -> SendMessage(Component.text("Grace period over").color(NamedTextColor.RED)));
    }

    private void SwitchToInGameStatePlayer(IGamePlayer player)
    {
        player.SetIsAlive(true);
        player.SetTargetState(GamePlayerState.InGame);
        player.GetMCPlayer().teleport(GetStartingLocation());
        ShowStartContent(player);

        _tabUpdater.UpdateTabListForGamePlayer(player, _gamePlayerCollection);
        _minecraftTeam.addPlayer(player.GetMCPlayer());
        _startEvent.FireEvent(new MoleHuntStartEvent(this));
    }

    private void StartBorderShrink()
    {
        SendMessage(Component.text("The border is now shrinking.").color(NamedTextColor.RED));
        _worldProvider.GetWorlds().forEach(this::BeginWorldBorderShrinkForWorld);
        _borderShrinkClock.SetIsRunning(false);
    }

    private void InGameTick()
    {
        ShowGameTime();
        ShowGraceTime();

        _gamePlayerCollection.GetActivePlayers().forEach(ITickable::Tick);

        if (_clock.GetTicksLeft() == _settings.GetBorderShrinkStartTimeTicks())
        {
            StartBorderShrink();
        }

        TestGameCancelConditions();
        TestGameEndConditions();
    }


    /* Post-game. */
    private IGameTeam GetWinningTeam()
    {
        boolean AreInnocentsAlive = _teams.get(GameTeamType.Innocents).GetPlayers().stream()
                .anyMatch(IGamePlayer::IsAlive);

        return AreInnocentsAlive ? _teams.get(GameTeamType.Innocents) : _teams.get(GameTeamType.Moles);
    }

    private void ShowTeamEndContent(IGameTeam winnerTeam, IGameTeam loserTeam)
    {
        int TITLE_FADE_IN_MILLISECONDS = 500;
        int TITLE_FADE_OUT_MILLISECONDS = 500;
        int TITLE_TIME_SECONDS = 5;

        loserTeam.ShowTitle(Title.title(Component.text("Defeat").color(NamedTextColor.RED),
                Component.text(""), Title.Times.times(
                        Duration.ofMillis(TITLE_FADE_IN_MILLISECONDS),
                        Duration.ofSeconds(TITLE_TIME_SECONDS),
                        Duration.ofSeconds(TITLE_FADE_OUT_MILLISECONDS))));

        winnerTeam.ShowTitle(Title.title(Component.text("Victory").color(NamedTextColor.RED),
                Component.text(""), Title.Times.times(
                        Duration.ofMillis(TITLE_FADE_IN_MILLISECONDS),
                        Duration.ofSeconds(TITLE_TIME_SECONDS),
                        Duration.ofSeconds(TITLE_FADE_OUT_MILLISECONDS))));

        loserTeam.GetPlayers().forEach(player -> player.PlaySound(Sound.ENTITY_ENDER_DRAGON_GROWL,
                player.GetMCPlayer().getLocation(), SoundCategory.PLAYERS, 1f, 1f));

        winnerTeam.GetPlayers().forEach(player -> player.PlaySound(Sound.BLOCK_END_PORTAL_SPAWN,
                player.GetMCPlayer().getLocation(), SoundCategory.PLAYERS, 1f, 1f));

        Component TeamMessage = Component.text("Team %s has won the game!".formatted(PCString.Pluralize(
                winnerTeam.GetName(), true))).color(TextColor.color(winnerTeam.GetColor().asRGB()));

        SendMessage(TeamMessage);
    }

    private void HandleTeamEnd()
    {
        IGameTeam WinnerTeam = GetWinningTeam();
        IGameTeam LoserTeam = WinnerTeam.GetType() == GameTeamType.Innocents ? _teams.get(GameTeamType.Moles)
                : _teams.get(GameTeamType.Innocents);
        ShowTeamEndContent(WinnerTeam, LoserTeam);
    }

    private void SwitchToPostGameState()
    {
        _clock.SetTicksLeft(POST_GAME_TICKS);
        _clock.SetHandler(clock -> SwitchToCompleteState());
        _clock.SetTickFunction(clock -> PostGameTick());

        _graceClock.SetIsRunning(false);

        _worldProvider.GetWorlds().forEach(this::ResetWorldBorder);
        _borderShrinkClock.SetIsRunning(false);

        _gamePlayerCollection.GetActivePlayers().forEach(this::SwitchToPostGameStatePlayer);
        HandleTeamEnd();
        _endEvent.FireEvent(new MoleHuntEndEvent(this));
    }

    private void SwitchToPostGameStatePlayer(IGamePlayer player)
    {
        player.SetTargetState(GamePlayerState.PostGame);
        _tabUpdater.UpdateNonInGameTabList(player.GetServerPlayer(), _gamePlayerCollection);
    }

    private void PostGameTick()
    {
        _gamePlayerCollection.GetActivePlayers().forEach(ITickable::Tick);
    }


    /* Complete. */
    private void SwitchToCompleteState()
    {
        _gamePlayerCollection.GetActivePlayers().forEach(player -> _minecraftTeam.removeEntity(player.GetMCPlayer()));
        _state = MoleHuntGameState.Complete;
        _clock.SetIsRunning(false);
        _graceClock.SetIsRunning(false);
        _gamePlayerCollection.GetActivePlayers().forEach(player -> player.UnsubscribeFromEvents(_eventDispatcher));
        _completeEvent.FireEvent(new MoleHuntCompleteEvent(this));
    }


    // Inherited methods.
    @Override
    public boolean AddPlayer(IServerPlayer player)
    {
        Objects.requireNonNull(player, "player is null");
        if ((_state != MoleHuntGameState.Initializing) || _gamePlayerCollection.ContainsPlayer(player))
        {
            return false;
        }

        IGamePlayer GamePlayer = new DefaultGamePlayer(player, this, _teams.get(GameTeamType.None), _packetController,
                _serverPlayerCollection, new SpellSettings(_settings.GetSpellCastCooldownTicks(),
                _settings.GetIsNotifiedOnSpellCast()), _spells);
        return _gamePlayerCollection.AddPlayer(GamePlayer);
    }

    @Override
    public boolean ContainsPlayer(IServerPlayer player)
    {
        return _gamePlayerCollection.ContainsPlayer(Objects.requireNonNull(player, "player is null"));
    }

    @Override
    public boolean ContainsActivePlayer(IServerPlayer player)
    {
        return _gamePlayerCollection.ContainsActivePlayer(Objects.requireNonNull(player, "player is null"));
    }

    @Override
    public IGamePlayer GetGamePlayer(IServerPlayer player)
    {
        return _gamePlayerCollection.GetGamePlayer(Objects.requireNonNull(player, "player is null"));
    }

    @Override
    public boolean Start()
    {
        if ((_state != MoleHuntGameState.Initializing) ||
                (_settings.GetMoleCountMax() >= _gamePlayerCollection.GetActivePlayers().size()))
        {
            return false;
        }

        SwitchToPreStartState();
        return true;
    }

    @Override
    public boolean End()
    {
        if (!IsStateOneOf(MoleHuntGameState.Initializing, MoleHuntGameState.PreStart))
        {
            return false;
        }

        SwitchToPostGameState();
        return true;
    }

    @Override
    public void Cancel()
    {
        if (_state == MoleHuntGameState.Complete)
        {
            return;
        }
        SendMessage(Component.text("Game cancelled").color(NamedTextColor.RED));
        SwitchToCompleteState();
    }

    @Override
    public MoleHuntGameState GetState()
    {
        return MoleHuntGameState.Initializing;
    }

    @Override
    public boolean AddSpectator(IServerPlayer player)
    {
        Objects.requireNonNull(player, "player is null");
        if (_gamePlayerCollection.ContainsSpectator(player) ||
                !IsStateOneOf(MoleHuntGameState.PreStart, MoleHuntGameState.InGame, MoleHuntGameState.PostGame))
        {
            return false;
        }

        _gamePlayerCollection.AddSpectator(player);
        InitializeSpectator(player);
        return true;
    }

    @Override
    public boolean RemoveSpectator(IServerPlayer player)
    {
        return _gamePlayerCollection.RemoveSpectator(player);
    }

    @Override
    public boolean ContainsSpectator(IServerPlayer player)
    {
        return _gamePlayerCollection.ContainsSpectator(player);
    }

    @Override
    public List<IServerPlayer> GetSpectators(IServerPlayer player)
    {
        return _gamePlayerCollection.GetSpectators();
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
        dispatcher.GetPlayerQuitEvent().Subscribe(this, this::OnPlayerQuitEvent);
    }

    @Override
    public void UnsubscribeFromEvents(IEventDispatcher dispatcher)
    {
        dispatcher.GetPlayerQuitEvent().Unsubscribe(this);
    }


    @Override
    public void Tick()
    {
        _clock.Tick();
        _graceClock.Tick();
        _borderShrinkClock.Tick();
    }

    @Override
    public List<? extends IAudienceMember> GetAudienceMembers()
    {
        return _gamePlayerCollection.GetAudienceMembers();
    }

    @Override
    public void ShowTitle(Title title)
    {
        _gamePlayerCollection.ShowTitle(title);
    }

    @Override
    public void ClearTitle()
    {
        _gamePlayerCollection.ClearTitle();
    }

    @Override
    public void ShowActionbar(ActionbarMessage message)
    {
        _gamePlayerCollection.ShowActionbar(message);
    }

    @Override
    public void RemoveActionbar(long id)
    {
        _gamePlayerCollection.RemoveActionbar(id);
    }

    @Override
    public void ClearActionbar()
    {
        _gamePlayerCollection.ClearActionbar();
    }

    @Override
    public void PlaySound(Sound sound, Location location, SoundCategory category, float volume, float pitch)
    {
        _gamePlayerCollection.PlaySound(sound, location, category, volume, pitch);
    }

    @Override
    public void SendMessage(Component message)
    {
        _gamePlayerCollection.SendMessage(message);
    }

    @Override
    public <T> void SpawnParticle(Particle particle,
                                  Location location,
                                  double deltaX,
                                  double deltaY,
                                  double deltaZ,
                                  int count,
                                  double extra,
                                  T data)
    {
        _gamePlayerCollection.SpawnParticle(particle, location, deltaX, deltaY, deltaZ, count, extra, data);
    }
}