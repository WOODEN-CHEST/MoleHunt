package sus.keiger.molehunt.game;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.*;
import sus.keiger.molehunt.IWorldProvider;
import sus.keiger.molehunt.event.IEventDispatcher;
import sus.keiger.molehunt.game.event.ParticipantAddEvent;
import sus.keiger.molehunt.game.event.ParticipantRemoveEvent;
import sus.keiger.molehunt.game.player.GamePlayerCollection;
import sus.keiger.molehunt.game.player.GamePlayerState;
import sus.keiger.molehunt.game.player.IGamePlayer;
import sus.keiger.plugincommon.ITickable;
import sus.keiger.plugincommon.PCMath;
import sus.keiger.plugincommon.PCString;
import sus.keiger.plugincommon.TickClock;

import java.time.Duration;
import java.util.Objects;

public class GamePostEndStateExecutor extends GenericGameStateExecutor
{
    // Private fields.
    private final IMoleHuntGameInstance _moleHunt;
    private final GamePlayerCollection _gamePlayers;
    private final IEventDispatcher _eventDispatcher;
    private final IWorldProvider _worldProvider;
    private final TickClock _clock = new TickClock();

    private final int STATE_DURATION_TICKS = PCMath.SecondsToTicks(10d);
    private int END_TITLE_FADE_IN_MILLISECONDS = 500;
    private int END_TITLE_FADE_OUT_MILLISECONDS = 500;
    private int END_TITLE_TIME_SECONDS = 5;


    // Constructors.
    public GamePostEndStateExecutor(GamePlayerCollection gamePlayerCollection,
                                    IEventDispatcher eventDispatcher,
                                    IWorldProvider worldProvider,
                                    IMoleHuntGameInstance moleHunt)
    {
        super(MoleHuntGameState.PostGame);
        _moleHunt = Objects.requireNonNull(moleHunt, "moleHunt is null");
        _eventDispatcher = Objects.requireNonNull(eventDispatcher, "eventDispatcher is null");
        _gamePlayers = Objects.requireNonNull(gamePlayerCollection, "gamePlayerCollection is null");
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

    private void DeinitializePlayer(IGamePlayer player)
    {
        player.UnsubscribeFromEvents(_eventDispatcher);
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

    private void StartStatePlayer(IGamePlayer player)
    {
        player.SetTargetState(GamePlayerState.PostGame);
    }

    private IGameTeam GetWinningTeam()
    {
        boolean AreInnocentsAlive = _gamePlayers.GetTeamByType(GameTeamType.Innocents)
                .GetPlayers().stream().anyMatch(IGamePlayer::IsAlive);

        return AreInnocentsAlive ? _gamePlayers.GetTeamByType(GameTeamType.Innocents) :
                _gamePlayers.GetTeamByType(GameTeamType.Moles);
    }

    private void ShowTeamEndContent(IGameTeam winnerTeam, IGameTeam loserTeam)
    {
        loserTeam.ShowTitle(Title.title(Component.text("Defeat").color(NamedTextColor.RED),
                Component.text(""), Title.Times.times(
                        Duration.ofMillis(END_TITLE_FADE_IN_MILLISECONDS),
                        Duration.ofSeconds(END_TITLE_TIME_SECONDS),
                        Duration.ofMillis(END_TITLE_FADE_OUT_MILLISECONDS))));

        winnerTeam.ShowTitle(Title.title(Component.text("Victory").color(NamedTextColor.GREEN),
                Component.text(""), Title.Times.times(
                        Duration.ofMillis(END_TITLE_FADE_IN_MILLISECONDS),
                        Duration.ofSeconds(END_TITLE_TIME_SECONDS),
                        Duration.ofMillis(END_TITLE_FADE_OUT_MILLISECONDS))));

        loserTeam.GetPlayers().forEach(player -> player.PlaySound(Sound.ENTITY_ENDER_DRAGON_GROWL,
                player.GetMCPlayer().getLocation(), SoundCategory.PLAYERS, 1f, 1f));

        winnerTeam.GetPlayers().forEach(player -> player.PlaySound(Sound.BLOCK_END_PORTAL_SPAWN,
                player.GetMCPlayer().getLocation(), SoundCategory.PLAYERS, 1f, 1f));

        Component TeamMessage = Component.text("Team %s has won the game!".formatted(PCString.Pluralize(
                winnerTeam.GetName(), true))).color(TextColor.color(winnerTeam.GetColor().asRGB()));

        _gamePlayers.SendMessage(TeamMessage);
    }

    private void HandleTeamEnd()
    {
        IGameTeam WinnerTeam = GetWinningTeam();
        IGameTeam LoserTeam = WinnerTeam.GetType() == GameTeamType.Innocents ?
                _gamePlayers.GetTeamByType(GameTeamType.Moles) :
                _gamePlayers.GetTeamByType(GameTeamType.Innocents);
        ShowTeamEndContent(WinnerTeam, LoserTeam);
    }

    private void EndState(boolean endedNaturally)
    {
        GetEndEvent().FireEvent(new GameStateExecutorEndEvent(this, endedNaturally));
    }

    private void OnClockTick()
    {
        _gamePlayers.GetActivePlayers().forEach(ITickable::Tick);
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
        _worldProvider.GetWorlds().forEach(this::InitializeWorldNotInGame);

        _moleHunt.GetParticipantRemoveEvent().Subscribe(this, this::OnParticipantRemoveEvent);

        HandleTeamEnd();
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