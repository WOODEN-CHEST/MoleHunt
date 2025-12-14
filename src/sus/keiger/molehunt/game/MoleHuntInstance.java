package sus.keiger.molehunt.game;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.*;
import net.kyori.adventure.title.Title;
import org.bukkit.*;
import org.bukkit.event.player.PlayerQuitEvent;
import sus.keiger.molehunt.event.IEventDispatcher;
import sus.keiger.molehunt.game.event.*;
import sus.keiger.molehunt.game.player.*;
import sus.keiger.molehunt.player.*;
import sus.keiger.molehunt.service.DefaultServerServices;
import sus.keiger.molehunt.service.IServerServices;
import sus.keiger.plugincommon.*;
import sus.keiger.plugincommon.player.actionbar.ActionbarMessage;

import java.util.*;

public class MoleHuntInstance implements IMoleHuntGameInstance
{
    // Private fields.
    private final long _ID;
    private final IGameServices _gameServices;
    private final Map<MoleHuntGameState, IGameStateExecutor> _stateExecutors = new HashMap<>();
    private final IGameSpectatorController _spectatorController;
    private final GameChatInterceptor _chatInterceptor;
    private final GameCommandInterceptor _commandInterceptor;
    private final GameAdvancementInterceptor _advancementInterceptor;
    private final GameTabListUpdater _tabUpdater;
    private final GamePortalController _portalController;
    private final GameItemModifier _itemModifier;

    private final PCPluginEvent<MoleHuntPreStartEvent> _preStartEvent = new PCPluginEvent<>();
    private final PCPluginEvent<MoleHuntStartEvent> _startEvent = new PCPluginEvent<>();
    private final PCPluginEvent<MoleHuntEndEvent> _endEvent = new PCPluginEvent<>();
    private final PCPluginEvent<MoleHuntCompleteEvent> _completeEvent = new PCPluginEvent<>();
    private final PCPluginEvent<ParticipantAddEvent> _participantAddEvent = new PCPluginEvent<>();
    private final PCPluginEvent<ParticipantRemoveEvent> _participantRemoveEvent = new PCPluginEvent<>();

    private MoleHuntGameState _state = MoleHuntGameState.Initializing;



    // Constructors.
    public MoleHuntInstance(long id,
                            IServerServices serverServices,
                            MoleHuntSettings settings)
    {
        _ID = id;
        Objects.requireNonNull(serverServices, "worldProvider is null");
        Objects.requireNonNull(settings, "settings is null");

        // Init services.
        _gameServices = new DefaultGameServices(
                serverServices.GetEventDispatcher(),
                serverServices.GetServerPlayerCollection(),
                serverServices.GetPacketController(),
                new GamePlayerCollection(),
                new GameScoreboard(),
                new GameLocationProvider(serverServices.GetWorldProvider()),
                settings);


        // Init components.
        _spectatorController = new DefaultGameSpectatorController(_gameServices);
        _chatInterceptor = new GameChatInterceptor(_gameServices);

        _commandInterceptor = new GameCommandInterceptor(_gameServices);
        _advancementInterceptor = new GameAdvancementInterceptor(_gameServices);
        _tabUpdater = new GameTabListUpdater(_gameServices, this);
        _portalController = new GamePortalController(_gameServices);
        _itemModifier = new GameItemModifier();


        // Init state executors.
        InitializeStatExecutors(_gameServices);
    }




    // Private methods.
    private void InitializeStatExecutors(IGameServices _gameServices)
    {
        IGameStateExecutor InitializingExecutor = new GameInitializingStateExecutor();
        IGameStateExecutor PreStartExecutor = new GamePreStartStateExecutor(_gameServices, this);
        IGameStateExecutor InGameExecutor = new InGameStateExecutor(_gameServices, this);
        IGameStateExecutor PostEndExecutor = new GamePostEndStateExecutor(_gameServices, this);
        IGameStateExecutor CompleteStateExecutor = new GameCompleteStateExecutor(_gameServices);

        _stateExecutors.put(InitializingExecutor.GetTargetState(), InitializingExecutor);
        _stateExecutors.put(PreStartExecutor.GetTargetState(), PreStartExecutor);
        _stateExecutors.put(InGameExecutor.GetTargetState(), InGameExecutor);
        _stateExecutors.put(PostEndExecutor.GetTargetState(), PostEndExecutor);
        _stateExecutors.put(CompleteStateExecutor.GetTargetState(), CompleteStateExecutor);
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

    private IGameStateExecutor GetCurrentExecutor()
    {
        return _stateExecutors.get(_state);
    }

    private void OnPreGameStateEnd(GameStateExecutorEndEvent event)
    {
        if (!event.GetEndedNaturally())
        {
            return;
        }
        SetState(MoleHuntGameState.InGame);
    }

    private void OnInGameStateEnd(GameStateExecutorEndEvent event)
    {
        if (!event.GetEndedNaturally())
        {
            return;
        }
        SetState(MoleHuntGameState.PostGame);
    }

    private void OnPostGameStateEnd(GameStateExecutorEndEvent event)
    {
        if (!event.GetEndedNaturally())
        {
            return;
        }
        SetState(MoleHuntGameState.Complete);
    }


    /* Events. */
//    private void OnPlayerQuitEvent(PlayerQuitEvent event)
//    {
//        IServerPlayer ServerPlayer = _gameServices.GetServerPlayerCollection().GetPlayer(event.getPlayer());
//        if (ContainsSpectator(ServerPlayer))
//        {
//            RemoveSpectator(ServerPlayer);
//            return;
//        }
//
//        IGamePlayer GamePlayer = _gameServices.GetGamePlayerCollection().GetGamePlayer(ServerPlayer);
//        if (GamePlayer == null)
//        {
//            return;
//        }
//        OnGamePlayerQuit(GamePlayer);
//    }
//
//    private void OnGamePlayerQuit(IGamePlayer player)
//    {
//        _gameServices.GetGamePlayerCollection().UpdateCollection();
//        player.SetIsAlive(false);
//        _participantRemoveEvent.FireEvent(new ParticipantRemoveEvent(this, player.GetServerPlayer()));
//    }

    private void SubscribeToEvents()
    {
        IEventDispatcher Dispatcher = _gameServices.GetEventDispatcher();

        _chatInterceptor.SubscribeToEvents(Dispatcher);
        _commandInterceptor.SubscribeToEvents(Dispatcher);
        _advancementInterceptor.SubscribeToEvents(Dispatcher);
        _tabUpdater.SubscribeToEvents(Dispatcher);
        _itemModifier.SubscribeToEvents(Dispatcher);
        _portalController.SubscribeToEvents(Dispatcher);

        _stateExecutors.get(MoleHuntGameState.PreGame).GetEndEvent().Subscribe(this, this::OnPreGameStateEnd);
        _stateExecutors.get(MoleHuntGameState.InGame).GetEndEvent().Subscribe(this, this::OnInGameStateEnd);
        _stateExecutors.get(MoleHuntGameState.PostGame).GetEndEvent().Subscribe(this, this::OnPostGameStateEnd);
    }

    private void UnsubscribeFromEvents()
    {
        IEventDispatcher Dispatcher = _gameServices.GetEventDispatcher();

        Dispatcher.GetPlayerQuitEvent().Unsubscribe(this);
        _chatInterceptor.UnsubscribeFromEvents(Dispatcher);
        _commandInterceptor.UnsubscribeFromEvents(Dispatcher);
        _advancementInterceptor.UnsubscribeFromEvents(Dispatcher);
        _tabUpdater.UnsubscribeFromEvents(Dispatcher);
        _itemModifier.UnsubscribeFromEvents(Dispatcher);
        _portalController.UnsubscribeFromEvents(Dispatcher);

        _stateExecutors.values().forEach(executor -> executor.GetEndEvent().Unsubscribe(this));
    }

    private void CompleteGame()
    {
        UnsubscribeFromEvents();
    }

    private void SetState(MoleHuntGameState state)
    {
        GetCurrentExecutor().EndState();
        _state = state;

        switch (state)
        {
            case PreGame -> _preStartEvent.FireEvent(new MoleHuntPreStartEvent(this));
            case InGame -> _startEvent.FireEvent(new MoleHuntStartEvent(this));
            case PostGame -> _endEvent.FireEvent(new MoleHuntEndEvent(this));
            case Complete -> _completeEvent.FireEvent(new MoleHuntCompleteEvent(this));
        }

        GetCurrentExecutor().StartState();
        _spectatorController.SetState(state);
        _chatInterceptor.SetState(state);
        _advancementInterceptor.SetState(state);
        _tabUpdater.SetState(state);
        _itemModifier.SetState(state);
        _portalController.SetState(state);

        if (state == MoleHuntGameState.Complete)
        {
            CompleteGame();
        }
    }



    // Inherited methods.
    @Override
    public boolean AddPlayer(IServerPlayer player)
    {
        Objects.requireNonNull(player, "player is null");
        if ((_state != MoleHuntGameState.Initializing)
                || _gameServices.GetGamePlayerCollection().ContainsPlayer(player))
        {
            return false;
        }

        IGamePlayer GamePlayer = new DefaultGamePlayer(player, _gameServices);
        return _gameServices.GetGamePlayerCollection().AddPlayer(GamePlayer);

    }

    @Override
    public boolean ContainsPlayer(IServerPlayer player)
    {
        return _gameServices.GetGamePlayerCollection().ContainsPlayer(
                Objects.requireNonNull(player, "player is null"));
    }

    @Override
    public boolean ContainsActivePlayer(IServerPlayer player)
    {
        return _gameServices.GetGamePlayerCollection().ContainsActivePlayer(
                Objects.requireNonNull(player, "player is null"));
    }

    @Override
    public List<IGamePlayer> GetActivePlayers()
    {
        return _gameServices.GetGamePlayerCollection().GetActivePlayers();
    }

    @Override
    public List<IGamePlayer> GetPlayers()
    {
        return _gameServices.GetGamePlayerCollection().GetPlayers();
    }

    @Override
    public boolean Start()
    {
        if ((_state != MoleHuntGameState.Initializing) || (_gameServices.GetGameSettings().GetMoleCountMax() >=
                        _gameServices.GetGamePlayerCollection().GetActivePlayers().size()))
        {
            return false;
        }

        GetPlayers().forEach(player -> player.GetServerPlayer().AddReference(this));
        SetState(MoleHuntGameState.PreGame);
        SubscribeToEvents();

        return true;
    }

    @Override
    public boolean End()
    {
        if (!IsStateOneOf(MoleHuntGameState.PreGame, MoleHuntGameState.InGame))
        {
            return false;
        }

        GetPlayers().forEach(player -> player.GetServerPlayer().AddReference(this));
        SetState(MoleHuntGameState.PostGame);
        return true;
    }

    @Override
    public void Cancel()
    {
        if (_state == MoleHuntGameState.Complete)
        {
            return;
        }

        GetPlayers().forEach(player -> player.GetServerPlayer().AddReference(this));
        SendMessage(Component.text("Game cancelled").color(NamedTextColor.RED));
        SetState(MoleHuntGameState.Complete);
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
        return _gameServices.GetGamePlayerCollection().GetAudienceMembers();
    }

    @Override
    public void ShowTitle(Title title)
    {
        _gameServices.GetGamePlayerCollection().ShowTitle(title);
    }

    @Override
    public void ClearTitle()
    {
        _gameServices.GetGamePlayerCollection().ClearTitle();
    }

    @Override
    public void ShowActionbar(ActionbarMessage message)
    {
        _gameServices.GetGamePlayerCollection().ShowActionbar(message);
    }

    @Override
    public void RemoveActionbar(long id)
    {
        _gameServices.GetGamePlayerCollection().RemoveActionbar(id);
    }

    @Override
    public void ClearActionbar()
    {
        _gameServices.GetGamePlayerCollection().ClearActionbar();
    }

    @Override
    public void PlaySound(Sound sound, Location location, SoundCategory category, float volume, float pitch)
    {
        _gameServices.GetGamePlayerCollection().PlaySound(sound, location, category, volume, pitch);
    }

    @Override
    public void SendMessage(Component message)
    {
        _gameServices.GetGamePlayerCollection().SendMessage(message);
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
        _gameServices.GetGamePlayerCollection()
                .SpawnParticle(particle, location, deltaX, deltaY, deltaZ, count, extra, data);
    }
}