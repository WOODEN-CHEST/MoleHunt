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
    private final IGameServices _gameServices;
    private final TickClock _clock = new TickClock();

    private final int STATE_DURATION_TICKS = PCMath.SecondsToTicks(10d);
    private final int START_TITLE_FADE_OUT_MILLISECONDS = 500;
    private final int START_TITLE_STAY_MILLISECONDS = 5000;


    // Constructors.
    public GamePreStartStateExecutor(IGameServices services,
                                     IMoleHuntGameInstance moleHunt)
    {
        super(MoleHuntGameState.PreGame);;
        _moleHunt = Objects.requireNonNull(moleHunt, "moleHunt is null");
        _gameServices = Objects.requireNonNull(services, "services is null");
    }


    // Private methods.
    private void OnParticipantRemoveEvent(ParticipantRemoveEvent event)
    {
        IGamePlayer Player = _gameServices.GetGamePlayerCollection().GetGamePlayer(event.GetPlayer());
        if (Player != null)
        {
            DeinitializePlayer(Player);
        }
    }

    private void DeinitializePlayer(IGamePlayer player)
    {
        player.UnsubscribeFromEvents(_gameServices.GetEventDispatcher());
    }



    private void StartStatePlayer(IGamePlayer player)
    {
        player.SetTargetState(GamePlayerState.PreGame);
        player.SubscribeToEvents(_gameServices.GetEventDispatcher());
    }

    private void ShowStartCountdownContent()
    {
        _gameServices.GetGamePlayerCollection().ShowTitle(Title.title(Component.text(Integer.toString(
                (int)PCMath.TicksToSeconds(_clock.GetTicksLeft()))).color(NamedTextColor.GREEN),
                Component.text("Game starting...").color(NamedTextColor.AQUA),
                Title.Times.times(Duration.ZERO, Duration.ofMillis(START_TITLE_STAY_MILLISECONDS),
                        Duration.ofMillis(START_TITLE_FADE_OUT_MILLISECONDS))));

        for (IServerPlayer TargetPlayer : _gameServices.GetGamePlayerCollection().GetParticipants())
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
        _gameServices.GetGamePlayerCollection().GetActivePlayers().forEach(ITickable::Tick);
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

        _gameServices.GetGamePlayerCollection().GetPlayers().forEach(this::StartStatePlayer);
        GameWorldInitializer WorldInitializer = new GameWorldInitializer();
        _gameServices.GetLocationProvider().GetWorlds().forEach(WorldInitializer::InitializeWorldNotInGame);

        _moleHunt.GetParticipantRemoveEvent().Subscribe(this, this::OnParticipantRemoveEvent);
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