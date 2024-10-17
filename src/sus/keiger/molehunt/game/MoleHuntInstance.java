package sus.keiger.molehunt.game;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.*;
import net.kyori.adventure.title.Title;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.util.Vector;
import sus.keiger.molehunt.IWorldProvider;
import sus.keiger.molehunt.event.IEventDispatcher;
import sus.keiger.molehunt.game.event.*;
import sus.keiger.molehunt.game.player.*;
import sus.keiger.molehunt.game.spell.*;
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
    private final GameScoreboard _scoreboard = new GameScoreboard();

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
    }




    // Private methods.
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


    /* Game logic. */
    private void TestGameCancelConditions()
    {
        if (_gamePlayerCollection.GetActivePlayers().isEmpty())
        {
            Cancel();
        }
    }

    private void TestGameEndConditions()
    {
        int AliveInnocentCount = (int)_teams.get(GameTeamType.Innocents).GetPlayers()
                .stream().filter(IGamePlayer::IsAlive).count();
        int AliveMoleCount = (int)_teams.get(GameTeamType.Moles).GetPlayers()
                .stream().filter(IGamePlayer::IsAlive).count();

        if ((AliveInnocentCount == 0) || (AliveMoleCount == 0))
        {
            End();
        }
    }


    /* Players and spectators. */
    private void InitializeSpectator(IServerPlayer player)
    {
        Player MCPlayer = player.GetMCPlayer();
        MCPlayer.clearActivePotionEffects();
        PlayerFunctions.ResetAttributes(MCPlayer);
        MCPlayer.setGameMode(GameMode.SPECTATOR);
        MCPlayer.teleport(GetCenterLocation());


        switch (_state)
        {
            case PreStart -> SwitchToPreStartStateParticipant(player);
            case InGame -> SwitchToInGameStateParticipant(player);
            case PostGame -> SwitchToPostGameStateParticipant(player);
        }

        _gamePlayerCollection.GetParticipants().forEach(this::UpdatePlayerTabList);
    }

    private void DeinitializeSpectator(IServerPlayer player)
    {
        _scoreboard.SetIsBoardEnabledForPlayer(player, false);
    }

    private void DeinitializeGamePlayer(IGamePlayer player)
    {
        _scoreboard.SetIsBoardEnabledForPlayer(player.GetServerPlayer(), false);
        _scoreboard.RemoveFromTeam(player.GetServerPlayer());
        player.UnsubscribeFromEvents(_eventDispatcher);
    }

    private void OnSpectatorQuit(IServerPlayer player)
    {
        RemoveSpectator(player);
    }

    private void OnGamePlayerQuit(IGamePlayer player)
    {
        player.SetIsAlive(false);
        DeinitializeGamePlayer(player);

        _gamePlayerCollection.UpdateCollection();
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

    private void UpdatePlayerTabList(IServerPlayer player)
    {
        if (_state != MoleHuntGameState.InGame)
        {
            _tabUpdater.UpdateNonInGameTabList(player, _gamePlayerCollection);
            return;
        }


        IGamePlayer GamePlayer = _gamePlayerCollection.GetGamePlayer(player);
        if (GamePlayer != null)
        {
            _tabUpdater.UpdateGamePlayerTabList(GamePlayer, _gamePlayerCollection);
        }
        else if (_gamePlayerCollection.ContainsSpectator(player))
        {
            _tabUpdater.UpdateSpectatorTabList(player, _gamePlayerCollection);
        }
    }


    /* Events. */
    private void OnPlayerQuitEvent(PlayerQuitEvent event)
    {
        IServerPlayer ServerPlayer = _serverPlayerCollection.GetPlayer(event.getPlayer());
        if (ContainsSpectator(ServerPlayer))
        {
            OnSpectatorQuit(ServerPlayer);
            return;
        }

        IGamePlayer GamePlayer = _gamePlayerCollection.GetGamePlayer(ServerPlayer);
        if (GamePlayer == null)
        {
            return;
        }
        OnGamePlayerQuit(GamePlayer);
    }


    /* Initializing. */
    private void InitializingTick() { }


    /* Pre-start. */
    private void SwitchToPreStartState()
    {
        _state = MoleHuntGameState.PreStart;

        _clock.SetTicksLeft(PRE_START_TICKS);
        _clock.SetHandler(clock -> SwitchToInGameState());
        _clock.SetTickFunction(clock -> PreStartTick());
        _clock.SetIsRunning(true);

        _gamePlayerCollection.GetParticipants().forEach(this::SwitchToPreStartStateParticipant);
        _gamePlayerCollection.GetParticipants().forEach(this::UpdatePlayerTabList);

        _graceClock.SetIsRunning(false);

        _worldProvider.GetWorlds().forEach(this::InitializeWorldNotInGame);
        _borderShrinkClock.SetIsRunning(false);

        _preStartEvent.FireEvent(new MoleHuntPreStartEvent(this));
    }

    private void SwitchToPreStartStateParticipant(IServerPlayer participant)
    {
        _scoreboard.SetIsBoardEnabledForPlayer(participant, false);
        _scoreboard.RemoveFromTeam(participant);

        if (_gamePlayerCollection.ContainsPlayer(participant))
        {
            SwitchToPreStartStatePlayer(_gamePlayerCollection.GetGamePlayer(participant));
        }
        if (_gamePlayerCollection.ContainsSpectator(participant))
        {
            SwitchToPreStartStateSpectator(participant);
        }
    }

    private void SwitchToPreStartStatePlayer(IGamePlayer player)
    {
       player.SetTargetState(GamePlayerState.PreGame);

       player.SubscribeToEvents(_eventDispatcher);
    }

    private void SwitchToPreStartStateSpectator(IServerPlayer player) { }

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

        List<Component> Lines = new ArrayList<>();

        Lines.add(Component.text("Time Left: %s".formatted(GetTimeString(_clock.GetTicksLeft())))
                .color(NamedTextColor.GOLD));

        if (_borderShrinkClock.GetTicksLeft() > 0)
        {
            Lines.add(Component.text(""));
            Lines.add(Component.text("Border Shrink: %s".formatted(GetTimeString(_borderShrinkClock.GetTicksLeft())))
                    .color(NamedTextColor.AQUA));
        }

        if (_graceClock.GetTicksLeft() > 0)
        {
            Lines.add(Component.text(""));
            Lines.add(Component.text("Grace Period: %s".formatted(GetTimeString(_graceClock.GetTicksLeft())))
                    .color(NamedTextColor.GREEN));
        }

        _scoreboard.SetText(Lines);
    }

    private void InitializeWorldInGame(World world)
    {
        WorldBorder Border = world.getWorldBorder();
        Border.setSize(_settings.GetBorderSizeStartBlocks());

        world.setTime(0L);
        world.setStorm(false);
        world.setThundering(false);

        world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, true);
        world.setGameRule(GameRule.DO_WEATHER_CYCLE, true);
        world.setGameRule(GameRule.DO_IMMEDIATE_RESPAWN, true);
    }

    private void InitializeWorldNotInGame(World world)
    {
        double DefaultBorderSize = 15_000_000d;
        WorldBorder Border = world.getWorldBorder();
        Border.setSize(DefaultBorderSize);

        world.setTime(6000L);
        world.setStorm(false);
        world.setThundering(false);

        world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
        world.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
        world.setGameRule(GameRule.DO_IMMEDIATE_RESPAWN, false);
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

        IGameTeam MoleTeam = _teams.get(GameTeamType.Moles);
        IGameTeam InnocentTeam = _teams.get(GameTeamType.Innocents);
        for (int i = 0; i < MoleCount; i++)
        {
            int Index = RNG.nextInt(Players.size());
            Players.get(Index).SetTeam(MoleTeam);
            MoleTeam.AddPlayer(Players.get(Index));
            Players.remove(Index);
        }
        Players.forEach(player ->
        {
            player.SetTeam(_teams.get(GameTeamType.Innocents));
            InnocentTeam.AddPlayer(player);
        });
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
                Sound.ENTITY_WITHER_SPAWN, player.GetMCPlayer().getLocation(), SoundCategory.PLAYERS, 100000f, 1f);
    }

    private void OnGraceTimerRunOut()
    {
        SendMessage(Component.text("Grace period over").color(NamedTextColor.RED));
        _gamePlayerCollection.GetActivePlayers().forEach(player -> player.SetMayDealDamage(true));
    }

    private void SwitchToInGameState()
    {
        _state = MoleHuntGameState.InGame;
        _scoreboard.SetTitle(Component.text("Info").color(NamedTextColor.WHITE));

        _graceClock.SetTicksLeft(_settings.GetGracePeriodTimeTicks());
        _graceClock.SetHandler(clock -> OnGraceTimerRunOut());
        _graceClock.SetIsRunning(true);

        _borderShrinkClock.SetTicksLeft(_settings.GetBorderShrinkStartTimeTicks());
        _borderShrinkClock.SetHandler(clock -> StartBorderShrink());
        _borderShrinkClock.SetIsRunning(true);

        _clock.SetTicksLeft(_settings.GetGameTimeTicks());
        _clock.SetHandler(clock -> SwitchToPostGameState());
        _clock.SetTickFunction(clock -> InGameTick());

        AssignPlayerRoles();
        _gamePlayerCollection.GetParticipants().forEach(this::SwitchToInGameStateParticipant);
        _gamePlayerCollection.GetParticipants().forEach(this::UpdatePlayerTabList);

        _worldProvider.GetWorlds().forEach(this::InitializeWorldInGame);

        _startEvent.FireEvent(new MoleHuntStartEvent(this));
    }

    private void SwitchToInGameStateParticipant(IServerPlayer participant)
    {
        _scoreboard.SetIsBoardEnabledForPlayer(participant, true);

        if (_gamePlayerCollection.ContainsPlayer(participant))
        {
           SwitchToInGameStatePlayer(_gamePlayerCollection.GetGamePlayer(participant));
        }
        if (_gamePlayerCollection.ContainsSpectator(participant))
        {
            SwitchToInGameStateSpectator(participant);
        }
    }

    private void SwitchToInGameStatePlayer(IGamePlayer player)
    {
        player.SetTargetState(GamePlayerState.InGame);
        player.GetMCPlayer().teleport(GetStartingLocation());
        ShowStartContent(player);
        _scoreboard.AddToTeam(player.GetServerPlayer());
        player.SetMayDealDamage(_graceClock.GetTicksLeft() <= 0);
    }

    private void SwitchToInGameStateSpectator(IServerPlayer player) { }

    private void StartBorderShrink()
    {
        SendMessage(Component.text("The border is now shrinking.").color(NamedTextColor.RED));
        _worldProvider.GetWorlds().forEach(this::BeginWorldBorderShrinkForWorld);
        _borderShrinkClock.SetIsRunning(false);
    }

    private void InGameTick()
    {
        ShowGameTime();

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
                        Duration.ofMillis(TITLE_FADE_OUT_MILLISECONDS))));

        winnerTeam.ShowTitle(Title.title(Component.text("Victory").color(NamedTextColor.GREEN),
                Component.text(""), Title.Times.times(
                        Duration.ofMillis(TITLE_FADE_IN_MILLISECONDS),
                        Duration.ofSeconds(TITLE_TIME_SECONDS),
                        Duration.ofMillis(TITLE_FADE_OUT_MILLISECONDS))));

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
        _state = MoleHuntGameState.PostGame;
        _clock.SetTicksLeft(POST_GAME_TICKS);
        _clock.SetHandler(clock -> SwitchToCompleteState());
        _clock.SetTickFunction(clock -> PostGameTick());

        _graceClock.SetIsRunning(false);

        _worldProvider.GetWorlds().forEach(this::InitializeWorldNotInGame);
        _borderShrinkClock.SetIsRunning(false);

        _gamePlayerCollection.GetParticipants().forEach(this::SwitchToPostGameStateParticipant);
        _gamePlayerCollection.GetParticipants().forEach(this::UpdatePlayerTabList);
        HandleTeamEnd();


        _endEvent.FireEvent(new MoleHuntEndEvent(this));
    }

    private void SwitchToPostGameStateParticipant(IServerPlayer participant)
    {
        _scoreboard.RemoveFromTeam(participant);
        _scoreboard.SetIsBoardEnabledForPlayer(participant, false);

        if (_gamePlayerCollection.ContainsPlayer(participant))
        {
            SwitchToPostGameStatePlayer(_gamePlayerCollection.GetGamePlayer(participant));
        }
        if (_gamePlayerCollection.ContainsSpectator(participant))
        {
            SwitchToPostGameStateSpectator(participant);
        }
    }

    private void SwitchToPostGameStatePlayer(IGamePlayer player)
    {
        player.SetTargetState(GamePlayerState.PostGame);
    }

    private void SwitchToPostGameStateSpectator(IServerPlayer player) { }

    private void PostGameTick()
    {
        _gamePlayerCollection.GetActivePlayers().forEach(ITickable::Tick);
    }


    /* Complete. */
    private void SwitchToCompleteState()
    {
        _state = MoleHuntGameState.Complete;
        _gamePlayerCollection.GetParticipants().forEach(this::SwitchToCompleteStateParticipant);
        _gamePlayerCollection.GetParticipants().forEach(this::UpdatePlayerTabList);
        _clock.SetIsRunning(false);
        _graceClock.SetIsRunning(false);
        _borderShrinkClock.SetIsRunning(false);
        _worldProvider.GetWorlds().forEach(this::InitializeWorldNotInGame);
        _completeEvent.FireEvent(new MoleHuntCompleteEvent(this));
    }

    private void SwitchToCompleteStateParticipant(IServerPlayer participant)
    {
        _scoreboard.RemoveFromTeam(participant);
        _scoreboard.SetIsBoardEnabledForPlayer(participant, false);

        if (_gamePlayerCollection.ContainsPlayer(participant))
        {
            SwitchToCompleteStatePlayer(_gamePlayerCollection.GetGamePlayer(participant));
        }
        if (_gamePlayerCollection.ContainsSpectator(participant))
        {
            SwitchToCompleteStateSpectator(participant);
        }
    }

    private void SwitchToCompleteStatePlayer(IGamePlayer player)
    {
        DeinitializeGamePlayer(player);
    }

    private void SwitchToCompleteStateSpectator(IServerPlayer player)
    {
        DeinitializeSpectator(player);
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

        IGamePlayer GamePlayer = new DefaultGamePlayer(player, this, _teams.get(GameTeamType.None),
                _packetController, _serverPlayerCollection, new SpellSettings(_settings.GetSpellCastCooldownTicks(),
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
    public List<IGamePlayer> GetActivePlayers()
    {
        return _gamePlayerCollection.GetActivePlayers();
    }

    @Override
    public List<IGamePlayer> GetPlayers()
    {
        return _gamePlayerCollection.GetPlayers();
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
        if (!IsStateOneOf(MoleHuntGameState.PreStart, MoleHuntGameState.InGame))
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
        return _state;
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
        if (_gamePlayerCollection.RemoveSpectator(player))
        {
            DeinitializeSpectator(player);
            return true;
        }
        return false;
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