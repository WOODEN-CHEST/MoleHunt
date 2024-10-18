package sus.keiger.molehunt.game;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.*;
import sus.keiger.molehunt.IWorldProvider;
import sus.keiger.molehunt.event.IEventDispatcher;
import sus.keiger.molehunt.game.event.*;
import sus.keiger.molehunt.game.player.*;
import sus.keiger.plugincommon.ITickable;
import sus.keiger.plugincommon.PCMath;
import sus.keiger.plugincommon.TickClock;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class InGameStateExecutor extends GenericGameStateExecutor
{
    // Private fields.
    private final IMoleHuntGameInstance _moleHunt;
    private final GamePlayerCollection _gamePlayers;
    private final GameTabListUpdater _tabUpdater;
    private final IWorldProvider _worldProvider;
    private final MoleHuntSettings _settings;
    private final IGameLocationProvider _locationProvider;
    private final IEventDispatcher _eventDispatcher;
    private final TickClock _clock = new TickClock();
    private final TickClock _graceClock = new TickClock();
    private final TickClock _borderBlock = new TickClock();
    private final GameScoreboard _scoreboard;
    private final TickTimeConverter _timeConverter = new TickTimeConverter();

    private final int TITLE_FADE_IN_MILLISECONDS = 500;
    private final int TITLE_FADE_OUT_MILLISECONDS = 500;
    private final int TITLE_TIME_SECONDS = 5;


    // Constructors.
    public InGameStateExecutor(GamePlayerCollection gamePlayerCollection,
                               GameTabListUpdater tabUpdater,
                               IWorldProvider worldProvider,
                               MoleHuntSettings settings,
                               IEventDispatcher eventDispatcher,
                               IGameLocationProvider locationProvider,
                               IMoleHuntGameInstance moleHunt,
                               GameScoreboard scoreBoard)
    {
        super(MoleHuntGameState.InGame);
        _gamePlayers = Objects.requireNonNull(gamePlayerCollection, "gamePlayerCollection is null");
        _tabUpdater = Objects.requireNonNull(tabUpdater, "tabUpdater is null");
        _worldProvider = Objects.requireNonNull(worldProvider, "worldProvider is null");
        _settings = Objects.requireNonNull(settings, "settings is null");
        _locationProvider = Objects.requireNonNull(locationProvider, "locationProvider is null");
        _eventDispatcher = Objects.requireNonNull(eventDispatcher, "eventDispatcher is null");
        _moleHunt = Objects.requireNonNull(moleHunt, "moleHunt is null");
        _scoreboard = Objects.requireNonNull(scoreBoard, "scoreBoard is null");
    }


    // Private methods.
    private void OnParticipantRemoveEvent(ParticipantRemoveEvent event)
    {
        IGamePlayer Player = _gamePlayers.GetGamePlayer(event.GetPlayer());
        if (Player != null)
        {
            DeinitializePlayer(Player);
        }
        _gamePlayers.GetPlayers().forEach(this::UpdateTabForPlayer);
    }

    private void OnParticipantAddEvent(ParticipantAddEvent event)
    {
        _gamePlayers.GetPlayers().forEach(this::UpdateTabForPlayer);
    }

    private void UpdateTabForPlayer(IGamePlayer player)
    {
        _tabUpdater.UpdateGamePlayerTabList(player, _gamePlayers);
    }

    private void DeinitializePlayer(IGamePlayer player)
    {
        player.UnsubscribeFromEvents(_eventDispatcher);
        PlayerEndState(player);
    }

    private void PlayerEndState(IGamePlayer player)
    {
        _scoreboard.RemoveFromTeam(player.GetServerPlayer());
        _scoreboard.SetIsBoardEnabledForPlayer(player.GetServerPlayer(), false);
        _tabUpdater.UpdateNonInGameTabList(player.GetServerPlayer(), _gamePlayers);
    }

    private void TestGameEndConditions()
    {
        int AliveInnocentCount = (int)_gamePlayers.GetTeamByType(GameTeamType.Innocents).GetPlayers()
                .stream().filter(IGamePlayer::IsAlive).count();
        int AliveMoleCount = (int)_gamePlayers.GetTeamByType(GameTeamType.Moles).GetPlayers()
                .stream().filter(IGamePlayer::IsAlive).count();

        if ((AliveInnocentCount == 0) || (AliveMoleCount == 0) || _gamePlayers.GetActivePlayers().isEmpty())
        {
            EndState(true);
        }
    }

    private void ShowGameTime()
    {
        if (_clock.GetTicksLeft() % PCMath.TICKS_IN_SECOND != 0)
        {
            return;
        }

        List<Component> Lines = new ArrayList<>();

        Lines.add(Component.text("Time Left: %s".formatted(_timeConverter.GetTimeString(_clock.GetTicksLeft())))
                .color(NamedTextColor.GOLD));

        if (_borderBlock.GetTicksLeft() > 0)
        {
            Lines.add(Component.text(""));
            Lines.add(Component.text("Border Shrink: %s".formatted(
                    _timeConverter.GetTimeString(_borderBlock.GetTicksLeft()))).color(NamedTextColor.AQUA));
        }

        if (_graceClock.GetTicksLeft() > 0)
        {
            Lines.add(Component.text(""));
            Lines.add(Component.text("Grace Period: %s".formatted(_timeConverter.GetTimeString(
                    _graceClock.GetTicksLeft()))).color(NamedTextColor.GREEN));
        }

        _scoreboard.SetText(Lines);
    }



    private void BeginWorldBorderShrinkForWorld(World world)
    {
        WorldBorder Border = world.getWorldBorder();
        Border.setSize(_settings.GetBorderSizeEndBlocks(),
                _settings.GetBorderShrinkStartTimeTicks() / PCMath.TICKS_IN_SECOND);
    }

    private void ShowStartContent(IGamePlayer player)
    {
        TextComponent.Builder MessageBuilder = Component.text();
        IGameTeam PlayerTeam = _gamePlayers.GetTeamOfPlayer(player);
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

    private void OnGraceTimerRunOut()
    {
        _gamePlayers.SendMessage(Component.text("Grace period over").color(NamedTextColor.RED));
        _gamePlayers.GetActivePlayers().forEach(player -> player.SetMayDealDamage(true));
    }

    private void StartStatePlayer(IGamePlayer player)
    {
        _scoreboard.SetIsBoardEnabledForPlayer(player.GetServerPlayer(), true);
        player.GetMaxHealth().SetBaseValue(_settings.GetPlayerHealthHalfHearts());
        player.SetTargetState(GamePlayerState.InGame);
        player.GetMCPlayer().teleport(_locationProvider.GetRandomCenterLocation());
        ShowStartContent(player);
        _scoreboard.AddToTeam(player.GetServerPlayer());
        player.SetMayDealDamage(_graceClock.GetTicksLeft() <= 0);
        UpdateTabForPlayer(player);
    }

    private void StartBorderShrink()
    {
        _gamePlayers.SendMessage(Component.text("The border is now shrinking.").color(NamedTextColor.RED));
        _worldProvider.GetWorlds().forEach(this::BeginWorldBorderShrinkForWorld);
        _borderBlock.SetIsRunning(false);
    }

    private void EndState(boolean endedNaturally)
    {
        _gamePlayers.GetPlayers().forEach(this::PlayerEndState);
        _graceClock.SetIsRunning(false);
        _clock.SetIsRunning(false);
        _borderBlock.SetIsRunning(false);
        GetEndEvent().FireEvent(new GameStateExecutorEndEvent(this, endedNaturally));
    }

    private void OnClockTick()
    {
        ShowGameTime();

        _gamePlayers.GetActivePlayers().forEach(ITickable::Tick);

        if (_borderBlock.GetTicksLeft() == _settings.GetBorderShrinkStartTimeTicks())
        {
            StartBorderShrink();
        }

        TestGameEndConditions();
    }


    // Inherited methods.
    @Override
    public void StartState()
    {
        _scoreboard.SetTitle(Component.text("Info").color(NamedTextColor.WHITE));

        if (_settings.GetGracePeriodTimeTicks() > 0)
        {
            _graceClock.SetTicksLeft(_settings.GetGracePeriodTimeTicks());
            _graceClock.SetHandler(clock -> OnGraceTimerRunOut());
            _graceClock.SetIsRunning(true);
        }

        if (_settings.GetDoesBorderShrink() && _settings.GetGracePeriodTimeTicks() > 0)
        {
            _borderBlock.SetTicksLeft(Math.min(_settings.GetBorderShrinkStartTimeTicks(),
                    _settings.GetGameTimeTicks()));
            _borderBlock.SetHandler(clock -> StartBorderShrink());
            _borderBlock.SetIsRunning(true);
        }

        _clock.SetTicksLeft(_settings.GetGameTimeTicks());
        _clock.SetHandler(clock -> EndState(true));
        _clock.SetTickFunction(clock -> OnClockTick());
        _clock.SetIsRunning(true);

        GameWorldInitializer WorldInitializer = new GameWorldInitializer();
        _worldProvider.GetWorlds().forEach(world -> WorldInitializer.InitializeWorldInGame(world,
                _settings.GetBorderSizeStartBlocks()));

        IRoleAssigner RoleAssigned = new RandomRoleAssigner();
        RoleAssigned.AssignRoles(_gamePlayers, _settings.GetMoleCountMin(), _settings.GetMoleCountMax());
        _gamePlayers.GetPlayers().forEach(this::StartStatePlayer);

        _moleHunt.GetParticipantRemoveEvent().Unsubscribe(this);
        _moleHunt.GetParticipantRemoveEvent().Subscribe(this, this::OnParticipantRemoveEvent);
        _moleHunt.GetParticipantAddEvent().Unsubscribe(this);
        _moleHunt.GetParticipantAddEvent().Subscribe(this, this::OnParticipantAddEvent);
    }

    @Override
    public void EndState()
    {
        EndState(false);
    }

    @Override
    public void Tick()
    {
        _borderBlock.Tick();
        _graceClock.Tick();
        _clock.Tick();
    }
}