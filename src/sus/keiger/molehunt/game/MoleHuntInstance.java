package sus.keiger.molehunt.game;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.*;
import net.kyori.adventure.title.Title;
import org.bukkit.*;
import org.bukkit.event.player.PlayerQuitEvent;
import sus.keiger.molehunt.IWorldProvider;
import sus.keiger.molehunt.event.IEventDispatcher;
import sus.keiger.molehunt.game.event.*;
import sus.keiger.molehunt.game.player.*;
import sus.keiger.molehunt.player.*;
import sus.keiger.plugincommon.*;
import sus.keiger.plugincommon.packet.PCGamePacketController;
import sus.keiger.plugincommon.player.actionbar.ActionbarMessage;

import java.util.*;

public class MoleHuntInstance implements IMoleHuntGameInstance
{
    // Private fields.
    private final long _ID;
    private final GamePlayerCollection _gamePlayerCollection;
    private final IServerPlayerCollection _serverPlayerCollection;
    private final IGameSpectatorController _spectatorController;
    private final MoleHuntSettings _settings;
    private final IEventDispatcher _eventDispatcher;
    private final Map<MoleHuntGameState, IGameStateExecutor> _stateExecutors = new HashMap<>();
    private final GameChatInterceptor _chatInterceptor;

    private final PCPluginEvent<MoleHuntPreStartEvent> _preStartEvent = new PCPluginEvent<>();
    private final PCPluginEvent<MoleHuntStartEvent> _startEvent = new PCPluginEvent<>();
    private final PCPluginEvent<MoleHuntEndEvent> _endEvent = new PCPluginEvent<>();
    private final PCPluginEvent<MoleHuntCompleteEvent> _completeEvent = new PCPluginEvent<>();
    private final PCPluginEvent<ParticipantAddEvent> _participantAddEvent = new PCPluginEvent<>();
    private final PCPluginEvent<ParticipantRemoveEvent> _participantRemoveEvent = new PCPluginEvent<>();

    private MoleHuntGameState _state = MoleHuntGameState.Initializing;



    // Constructors.
    public MoleHuntInstance(long id,
                            IWorldProvider worldProvider,
                            PCGamePacketController packetController,
                            IEventDispatcher eventDispatcher,
                            IServerPlayerCollection playerCollection,
                            MoleHuntSettings settings)
    {
        _ID = id;
        Objects.requireNonNull(worldProvider, "worldProvider is null");
        Objects.requireNonNull(packetController, "packetController is null");
        Objects.requireNonNull(eventDispatcher, "eventDispatcher is null");
        Objects.requireNonNull(playerCollection, "playerCollection is null");
        Objects.requireNonNull(settings, "settings is null");

        GameScoreboard ScoreBoard = new GameScoreboard();
        GameTabListUpdater TabUpdater = new GameTabListUpdater(packetController);
        IGameLocationProvider LocationProvider = new GameLocationProvider(worldProvider);

        _gamePlayerCollection = new GamePlayerCollection();
        _spectatorController = new DefaultGameSpectatorController(_gamePlayerCollection,
                ScoreBoard, TabUpdater, LocationProvider);
        _settings = settings;
        _serverPlayerCollection = playerCollection;
        _eventDispatcher = eventDispatcher;
        _chatInterceptor = new GameChatInterceptor(_gamePlayerCollection, _serverPlayerCollection);

        IGameStateExecutor InitializingExecutor = new GameInitializingStateExecutor();
        IGameStateExecutor PreStartExecutor = new GamePreStartStateExecutor(_gamePlayerCollection,
                TabUpdater, _eventDispatcher, LocationProvider, this);
        IGameStateExecutor InGameExecutor = new InGameStateExecutor(_gamePlayerCollection, TabUpdater,
                worldProvider, settings, _eventDispatcher, LocationProvider, this, ScoreBoard);
        IGameStateExecutor PostEndExecutor = new GamePostEndStateExecutor(_gamePlayerCollection,
                TabUpdater, _eventDispatcher, LocationProvider, this);
        IGameStateExecutor CompleteStateExecutor = new GameCompleteStateExecutor(
                _gamePlayerCollection, _eventDispatcher, LocationProvider);

        _stateExecutors.put(InitializingExecutor.GetTargetState(), InitializingExecutor);
        _stateExecutors.put(PreStartExecutor.GetTargetState(), PreStartExecutor);
        _stateExecutors.put(InGameExecutor.GetTargetState(), InGameExecutor);
        _stateExecutors.put(PostEndExecutor.GetTargetState(), PostEndExecutor);
        _stateExecutors.put(CompleteStateExecutor.GetTargetState(), CompleteStateExecutor);

        PreStartExecutor.GetEndEvent().Subscribe(this, this::OnPreGameStateEnd);
        InGameExecutor.GetEndEvent().Subscribe(this, this::OnInGameStateEnd);
        PostEndExecutor.GetEndEvent().Subscribe(this, this::OnPostGameStateEnd);
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

    private IGameStateExecutor GetCurrentExecutor()
    {
        return _stateExecutors.get(_state);
    }

    private void OnPreGameStateEnd(GameStateExecutorEndEvent event)
    {
        _startEvent.FireEvent(new MoleHuntStartEvent(this));

        if (!event.GetEndedNaturally())
        {
            return;
        }
        _state = MoleHuntGameState.InGame;
        GetCurrentExecutor().StartState();
    }

    private void OnInGameStateEnd(GameStateExecutorEndEvent event)
    {
        _endEvent.FireEvent(new MoleHuntEndEvent(this));

        if (!event.GetEndedNaturally())
        {
            return;
        }
        _state = MoleHuntGameState.PostGame;
        GetCurrentExecutor().StartState();
    }

    private void OnPostGameStateEnd(GameStateExecutorEndEvent event)
    {
        if (!event.GetEndedNaturally())
        {
            return;
        }

        CompleteGame();
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
        OnGamePlayerQuit(GamePlayer);
    }

    private void OnGamePlayerQuit(IGamePlayer player)
    {
        _gamePlayerCollection.UpdateCollection();
        player.SetIsAlive(false);
        _participantRemoveEvent.FireEvent(new ParticipantRemoveEvent(this, player.GetServerPlayer()));
    }

    private void SubscribeToEvents()
    {
        _eventDispatcher.GetPlayerQuitEvent().Subscribe(this, this::OnPlayerQuitEvent);
        _chatInterceptor.SubscribeToEvents(_eventDispatcher);
    }

    private void UnsubscribeFromEvents()
    {
        _eventDispatcher.GetPlayerQuitEvent().Unsubscribe(this);
        _chatInterceptor.UnsubscribeFromEvents(_eventDispatcher);
    }

    private void CompleteGame()
    {
        UnsubscribeFromEvents();
        _state = MoleHuntGameState.Complete;
        _stateExecutors.values().forEach(executor -> executor.GetEndEvent().Unsubscribe(this));
        GetCurrentExecutor().StartState();
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

        IGamePlayer GamePlayer = new DefaultGamePlayer(player,
                _serverPlayerCollection, _gamePlayerCollection);
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

        _state = MoleHuntGameState.PreGame;
        SubscribeToEvents();
        _stateExecutors.get(MoleHuntGameState.PreGame).StartState();
        return true;
    }

    @Override
    public boolean End()
    {
        if (!IsStateOneOf(MoleHuntGameState.PreGame, MoleHuntGameState.InGame))
        {
            return false;
        }

        GetCurrentExecutor().EndState();
        _state = MoleHuntGameState.PostGame;
        GetCurrentExecutor().StartState();
        return true;
    }

    @Override
    public void Cancel()
    {
        if (_state == MoleHuntGameState.Complete)
        {
            return;
        }


        GetCurrentExecutor().EndState();
        SendMessage(Component.text("Game cancelled").color(NamedTextColor.RED));
        CompleteGame();
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
        if (_spectatorController.ContainsSpectator(player) ||
                !IsStateOneOf(MoleHuntGameState.PreGame, MoleHuntGameState.InGame, MoleHuntGameState.PostGame))
        {
            return false;
        }

        _spectatorController.AddSpectator(player);
        _participantAddEvent.FireEvent(new ParticipantAddEvent(this, player));
        return true;
    }

    @Override
    public boolean RemoveSpectator(IServerPlayer player)
    {
        if (_spectatorController.RemoveSpectator(player))
        {
            _participantRemoveEvent.FireEvent(new ParticipantRemoveEvent(this, player));
            return true;
        }
        return false;
    }

    @Override
    public boolean ContainsSpectator(IServerPlayer player)
    {
        return _spectatorController.ContainsSpectator(player);
    }

    @Override
    public List<IServerPlayer> GetSpectators(IServerPlayer player)
    {
        return _spectatorController.GetSpectators();
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
    public PCPluginEvent<ParticipantAddEvent> GetParticipantAddEvent()
    {
        return _participantAddEvent;
    }

    @Override
    public PCPluginEvent<ParticipantRemoveEvent> GetParticipantRemoveEvent()
    {
        return _participantRemoveEvent;
    }

    @Override
    public void Tick()
    {
        GetCurrentExecutor().Tick();
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