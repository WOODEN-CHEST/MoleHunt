package sus.keiger.molehunt.game;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.*;
import sus.keiger.molehunt.IWorldProvider;
import sus.keiger.molehunt.event.IEventDispatcher;
import sus.keiger.molehunt.game.event.ParticipantAddEvent;
import sus.keiger.molehunt.game.event.ParticipantRemoveEvent;
import sus.keiger.molehunt.game.player.GamePlayerCollection;
import sus.keiger.molehunt.game.player.GamePlayerState;
import sus.keiger.molehunt.game.player.IGamePlayer;
import sus.keiger.molehunt.player.IServerPlayer;
import sus.keiger.plugincommon.ITickable;
import sus.keiger.plugincommon.PCMath;
import sus.keiger.plugincommon.TickClock;

import java.time.Duration;
import java.util.Objects;

public class GamePreStartStateExecutor extends GenericGameStateExecutor
{
    // Private fields.
    private final IMoleHuntGameInstance _moleHunt;
    private final GamePlayerCollection _gamePlayers;
    private final GameTabListUpdater _tabUpdater;
    private final IEventDispatcher _eventDispatcher;
    private final IWorldProvider _worldProvider;
    private final TickClock _clock = new TickClock();

    private final int STATE_DURATION_TICKS = PCMath.SecondsToTicks(10d);
    private final int START_TITLE_FADE_OUT_MILLISECONDS = 500;
    private final int START_TITLE_STAY_MILLISECONDS = 5000;


    // Constructors.
    public GamePreStartStateExecutor(GamePlayerCollection gamePlayerCollection,
                                     GameTabListUpdater tabUpdater,
                                     IEventDispatcher eventDispatcher,
                                     IWorldProvider worldProvider,
                                     IMoleHuntGameInstance moleHunt)
    {
        super(MoleHuntGameState.PreGame);;
        _moleHunt = Objects.requireNonNull(moleHunt, "moleHunt is null");
        _eventDispatcher = Objects.requireNonNull(eventDispatcher, "eventDispatcher is null");
        _gamePlayers = Objects.requireNonNull(gamePlayerCollection, "gamePlayerCollection is null");
        _tabUpdater = Objects.requireNonNull(tabUpdater, "tabUpdater is null");
        _worldProvider = Objects.requireNonNull(worldProvider, "worldProvider is null");
    }


    // Private methods.
    private void OnParticipantRemoveEvent(ParticipantRemoveEvent event)
    {
        IGamePlayer Player = _gamePlayers.GetGamePlayer(event.GetPlayer());
        if (Player != null)
        {
            DeinitializePlayer(Player);
        }
    }

    private void OnParticipantAddEvent(ParticipantAddEvent event)
    {
        _gamePlayers.GetPlayers().forEach(this::UpdateTabForPlayer);
    }

    private void DeinitializePlayer(IGamePlayer player)
    {
        player.UnsubscribeFromEvents(_eventDispatcher);
    }



    private void StartStatePlayer(IGamePlayer player)
    {
        player.SetTargetState(GamePlayerState.PreGame);
        player.SubscribeToEvents(_eventDispatcher);
       UpdateTabForPlayer(player);
    }

    private void UpdateTabForPlayer(IGamePlayer player)
    {
        _tabUpdater.UpdateNonInGameTabList(player.GetServerPlayer(), _gamePlayers);
    }

    private void ShowStartCountdownContent()
    {
        _gamePlayers.ShowTitle(Title.title(Component.text(Integer.toString(
                (int)PCMath.TicksToSeconds(_clock.GetTicksLeft()))).color(NamedTextColor.GREEN),
                Component.text("Game starting...").color(NamedTextColor.AQUA),
                Title.Times.times(Duration.ZERO, Duration.ofMillis(START_TITLE_STAY_MILLISECONDS),
                        Duration.ofMillis(START_TITLE_FADE_OUT_MILLISECONDS))));

        for (IServerPlayer TargetPlayer : _gamePlayers.GetParticipants())
        {
            TargetPlayer.PlaySound(Sound.BLOCK_NOTE_BLOCK_HAT, TargetPlayer.GetMCPlayer().getLocation(),
                    SoundCategory.AMBIENT, 1f, 1f);
        }
    }

    private void EndState(boolean endedNaturally)
    {
        GetEndEvent().FireEvent(new GameStateExecutorEndEvent(this, endedNaturally));
    }

    private void OnClockTick()
    {
        _gamePlayers.GetActivePlayers().forEach(ITickable::Tick);
        if (_clock.GetTicksLeft() % PCMath.TICKS_IN_SECOND == 0)
        {
            ShowStartCountdownContent();
        }
    }



    // Inherited methods.
    @Override
    public void StartState()
    {
        _clock.SetTicksLeft(STATE_DURATION_TICKS);
        _clock.SetHandler(clock -> EndState(true));
        _clock.SetTickFunction(clock -> OnClockTick());
        _clock.SetIsRunning(true);

        _gamePlayers.GetPlayers().forEach(this::StartStatePlayer);
        GameWorldInitializer WorldInitializer = new GameWorldInitializer();
        _worldProvider.GetWorlds().forEach(WorldInitializer::InitializeWorldNotInGame);

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
        _clock.Tick();
    }
}