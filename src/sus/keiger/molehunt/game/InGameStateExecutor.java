package sus.keiger.molehunt.game;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.*;
import org.bukkit.advancement.Advancement;
import org.bukkit.entity.Player;
import sus.keiger.molehunt.game.event.*;
import sus.keiger.molehunt.game.player.*;
import sus.keiger.plugincommon.ITickable;
import sus.keiger.plugincommon.PCMath;
import sus.keiger.plugincommon.TickClock;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

public class InGameStateExecutor extends GenericGameStateExecutor
{
    // Private fields.
    private final IMoleHuntGameInstance _moleHunt;
    private final IGameServices _gameServices;
    private final TickClock _clock = new TickClock();
    private final TickClock _graceClock = new TickClock();
    private final TickClock _borderClock = new TickClock();
    private final TickTimeConverter _timeConverter = new TickTimeConverter();

    private final int TITLE_FADE_IN_MILLISECONDS = 500;
    private final int TITLE_FADE_OUT_MILLISECONDS = 500;
    private final int TITLE_TIME_SECONDS = 5;


    // Constructors.
    public InGameStateExecutor(IGameServices services, IMoleHuntGameInstance game)
    {
        super(MoleHuntGameState.InGame);
        _moleHunt = Objects.requireNonNull(game, "game is null");
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
        PlayerEndState(player);
    }

    private void PlayerEndState(IGamePlayer player)
    {
        _gameServices.GetScoreBoard().RemoveFromTeam(player.GetServerPlayer());
        _gameServices.GetScoreBoard().SetIsBoardEnabledForPlayer(player.GetServerPlayer(), false);
    }

    private void TestGameEndConditions()
    {
        int AliveInnocentCount = (int)_gameServices.GetGamePlayerCollection()
                .GetTeamByType(GameTeamType.Innocents).GetPlayers().stream().filter(IGamePlayer::IsAlive).count();
        int AliveMoleCount = (int)_gameServices.GetGamePlayerCollection().GetTeamByType(GameTeamType.Moles).GetPlayers()
                .stream().filter(IGamePlayer::IsAlive).count();

        if ((AliveInnocentCount == 0) || (AliveMoleCount == 0) ||
                _gameServices.GetGamePlayerCollection().GetActivePlayers().isEmpty())
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

        if (_borderClock.GetTicksLeft() > 0)
        {
            Lines.add(Component.text(""));
            Lines.add(Component.text("Border Shrink: %s".formatted(
                    _timeConverter.GetTimeString(_borderClock.GetTicksLeft()))).color(NamedTextColor.AQUA));
        }

        if (_graceClock.GetTicksLeft() > 0)
        {
            Lines.add(Component.text(""));
            Lines.add(Component.text("Grace Period: %s".formatted(_timeConverter.GetTimeString(
                    _graceClock.GetTicksLeft()))).color(NamedTextColor.GREEN));
        }

        _gameServices.GetScoreBoard().SetText(Lines);
    }



    private void BeginWorldBorderShrinkForWorld(World world)
    {
        WorldBorder Border = world.getWorldBorder();
        Border.setSize(_gameServices.GetGameSettings().GetBorderSizeEndBlocks(),
                _clock.GetTicksLeft() / PCMath.TICKS_IN_SECOND);
    }

    private void ShowStartContent(IGamePlayer player)
    {
        TextComponent.Builder MessageBuilder = Component.text();
        IGameTeam PlayerTeam = _gameServices.GetGamePlayerCollection().GetTeamOfPlayer(player);
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
        _gameServices.GetGamePlayerCollection().SendMessage(Component.text("Grace period over")
                .color(NamedTextColor.RED));
        _gameServices.GetGamePlayerCollection().GetActivePlayers().forEach(player -> player.SetMayDealDamage(true));
    }

    private void RevokeAdvancements(IGamePlayer player)
    {
        Iterator<Advancement> AdvancementIterator = Bukkit.advancementIterator();
        Player MCPlayer = player.GetMCPlayer();

        while (AdvancementIterator.hasNext())
        {
            Advancement TargetAdvancement = AdvancementIterator.next();
            for (String Criteria : TargetAdvancement.getCriteria())
            {
                MCPlayer.getAdvancementProgress(TargetAdvancement).revokeCriteria(Criteria);
            }
        }
    }

    private void StartStatePlayer(IGamePlayer player)
    {
        _gameServices.GetScoreBoard().SetIsBoardEnabledForPlayer(player.GetServerPlayer(), true);
        player.GetMaxHealth().SetBaseValue(_gameServices.GetGameSettings().GetPlayerHealthHalfHearts());
        player.SetTargetState(GamePlayerState.InGame);
        player.GetMCPlayer().teleport(_gameServices.GetLocationProvider().GetRandomCenterLocation());
        _gameServices.GetScoreBoard().AddToTeam(player.GetServerPlayer());
        player.SetMayDealDamage(_graceClock.GetTicksLeft() <= 0);
        RevokeAdvancements(player);
        ShowStartContent(player);
    }

    private void StartBorderShrink()
    {
        _gameServices.GetGamePlayerCollection().SendMessage(Component.text("The border is now shrinking.")
                .color(NamedTextColor.RED));
        _gameServices.GetLocationProvider().GetWorlds().forEach(this::BeginWorldBorderShrinkForWorld);
        _borderClock.SetIsRunning(false);
    }

    private void EndState(boolean endedNaturally)
    {
        _gameServices.GetGamePlayerCollection().GetPlayers().forEach(player ->
        {
            PlayerEndState(player);
        });
        _graceClock.SetIsRunning(false);
        _clock.SetIsRunning(false);
        _borderClock.SetIsRunning(false);
        GetEndEvent().FireEvent(new GameStateExecutorEndEvent(this, endedNaturally));
    }

    private void OnClockTick()
    {
        ShowGameTime();

        _gameServices.GetGamePlayerCollection().GetActivePlayers().forEach(ITickable::Tick);

        if (_borderClock.GetTicksLeft() == _gameServices.GetGameSettings().GetBorderShrinkStartTimeTicks())
        {
            StartBorderShrink();
        }

        TestGameEndConditions();
    }

    private void AssignRoles()
    {
        IRoleAssigner RoleAssigned = new RandomRoleAssigner();
        int MinMoles = _gameServices.GetGameSettings().GetMoleCountMin();
        int MaxMoles = Math.max(MinMoles, _gameServices.GetGameSettings().GetMoleCountMax());
        RoleAssigned.AssignRoles(_gameServices.GetGamePlayerCollection(), MinMoles, MaxMoles);
    }


    // Inherited methods.
    @Override
    public void StartState()
    {
        _gameServices.GetScoreBoard().SetTitle(Component.text("Info").color(NamedTextColor.WHITE));

        if (_gameServices.GetGameSettings().GetGracePeriodTimeTicks() > 0)
        {
            _graceClock.SetTicksLeft(_gameServices.GetGameSettings().GetGracePeriodTimeTicks());
            _graceClock.SetHandler(clock -> OnGraceTimerRunOut());
            _graceClock.SetIsRunning(true);
        }

        if (_gameServices.GetGameSettings().GetDoesBorderShrink() &&
                (_gameServices.GetGameSettings().GetGracePeriodTimeTicks() > 0))
        {
            _borderClock.SetTicksLeft(Math.min(_gameServices.GetGameSettings().GetBorderShrinkStartTimeTicks(),
                    _gameServices.GetGameSettings().GetGameTimeTicks()));
            _borderClock.SetHandler(clock -> StartBorderShrink());
            _borderClock.SetIsRunning(true);
        }

        _clock.SetTicksLeft(_gameServices.GetGameSettings().GetGameTimeTicks());
        _clock.SetHandler(clock -> EndState(true));
        _clock.SetTickFunction(clock -> OnClockTick());
        _clock.SetIsRunning(true);

        GameWorldInitializer WorldInitializer = new GameWorldInitializer();
        _gameServices.GetLocationProvider().GetWorlds().forEach(world -> WorldInitializer.InitializeWorldInGame(
                world, _gameServices.GetGameSettings().GetBorderSizeStartBlocks()));

        AssignRoles();
        _gameServices.GetGamePlayerCollection().GetPlayers().forEach(this::StartStatePlayer);

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
        _borderClock.Tick();
        _graceClock.Tick();
        _clock.Tick();
    }
}