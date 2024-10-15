package sus.keiger.molehunt.game.player;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import sus.keiger.molehunt.event.*;
import sus.keiger.molehunt.game.IGameTeam;
import sus.keiger.molehunt.player.*;
import sus.keiger.plugincommon.packet.PCGamePacketController;
import sus.keiger.plugincommon.player.actionbar.ActionbarMessage;

import java.util.Objects;

public class DefaultGamePlayer implements IGamePlayer
{
    // Private fields.
    private final IServerPlayer _serverPlayer;
    private final PCGamePacketController _packetController;
    private final IModifiablePlayerStats _stats = new PlayerStatsContainer();
    private final IServerPlayerCollection _playerCollection;
    private boolean _isAlive;
    private IGameTeam _team;
    private GamePlayerState _state = GamePlayerState.PreGame;
    private IGamePlayerExecutor _executor;


    // Constructors.
    public DefaultGamePlayer(IServerPlayer player,
                             IGameTeam team,
                             PCGamePacketController packetController,
                             IServerPlayerCollection playerCollection)
    {
        _team = Objects.requireNonNull(team, "team is null");
        _serverPlayer = Objects.requireNonNull(player, "player is null");
        _packetController = Objects.requireNonNull(packetController, "packetContainer is null");
        _playerCollection = Objects.requireNonNull(playerCollection, "playerCollection is null");
    }


    // Private methods.
    private void OnPlayerDeathEvent(PlayerDeathEvent event)
    {
        _executor.OnPlayerDeathEvent(event);
    }

    private void OnEntityDamageEvent(EntityDamageEvent event)
    {
        _executor.OnEntityDamageEvent(event);
    }

    private void OnAsyncChatEvent(AsyncChatEvent event)
    {
        _executor.OnAsyncChatEvent(event);
    }

    private void OnBlockBreakEvent(BlockBreakEvent event)
    {
        _executor.OnBlockBreakEvent(event);
    }

    private void OnBlockPlaceEvent(BlockPlaceEvent event)
    {
        _executor.OnBlockPlaceEvent(event);
    }

    private void OnPlayerInteractEvent(PlayerInteractEvent event)
    {
        _executor.OnPlayerInteractEvent(event);
    }

    private void OnPlayerDropItemEvent(PlayerDropItemEvent event)
    {
        _executor.OnPlayerDropItemEvent(event);
    }


    // Inherited methods.
    @Override
    public Player GetMCPlayer()
    {
        return _serverPlayer.GetMCPlayer();
    }

    @Override
    public IServerPlayer GetServerPlayer()
    {
        return _serverPlayer;
    }

    @Override
    public boolean IsAlive()
    {
        return _isAlive;
    }

    @Override
    public void SetIsAlive(boolean value)
    {
        if (value == _isAlive)
        {
            return;
        }

        _isAlive = value;
        _executor = _isAlive ?
                new AlivePlayerExecutor(this, _playerCollection) :
                new DeadPlayerExecutor(this, _playerCollection);
    }

    public IGameTeam GetTeam()
    {
        return _team;
    }

    @Override
    public void SetTeam(IGameTeam team)
    {
        _team = Objects.requireNonNull(team, "team is null");
    }

    @Override
    public GamePlayerState GetTargetState()
    {
        return _state;
    }

    @Override
    public void SetTargetState(GamePlayerState state)
    {
        _state = Objects.requireNonNull(state, "state is null");
        _executor.SwitchState(state);
    }


    // Inherited methods.
    @Override
    public void SubscribeToEvents(IEventDispatcher dispatcher)
    {
        dispatcher.GetPlayerDeathEvent().Subscribe(this, this::OnPlayerDeathEvent);
        dispatcher.GetEntityDamageEvent().Subscribe(this, this::OnEntityDamageEvent);
        dispatcher.GetAsyncChatEvent().Subscribe(this, this::OnAsyncChatEvent);
        dispatcher.GetBlockBreakEvent().Subscribe(this, this::OnBlockBreakEvent);
        dispatcher.GetBlockPlaceEvent().Subscribe(this, this::OnBlockPlaceEvent);
        dispatcher.GetPlayerInteractEvent().Subscribe(this, this::OnPlayerInteractEvent);
        dispatcher.GetPlayerDropItemEvent().Subscribe(this, this::OnPlayerDropItemEvent);
    }

    @Override
    public void UnsubscribeFromEvents(IEventDispatcher dispatcher)
    {
        dispatcher.GetPlayerDeathEvent().Unsubscribe(this);
        dispatcher.GetEntityDamageEvent().Unsubscribe(this);
        dispatcher.GetAsyncChatEvent().Unsubscribe(this);
        dispatcher.GetBlockBreakEvent().Unsubscribe(this);
        dispatcher.GetBlockPlaceEvent().Unsubscribe(this);
        dispatcher.GetPlayerInteractEvent().Unsubscribe(this);
        dispatcher.GetPlayerDropItemEvent().Unsubscribe(this);
    }

    @Override
    public void SetDamageDealtHalfHearts(double value)
    {
        _stats.SetDamageDealtHalfHearts(value);
    }

    @Override
    public void SetDamageTakenHalfHearts(double value)
    {
        _stats.SetDamageTakenHalfHearts(value);
    }

    @Override
    public void SetKills(int value)
    {
        _stats.SetKills(value);
    }

    @Override
    public void SetTicksSurvived(int value)
    {
        _stats.SetTicksSurvived(value);
    }

    @Override
    public void SetScore(double value)
    {
        _stats.SetScore(value);
    }

    @Override
    public void SetWasGameFinished(boolean value)
    {
        _stats.SetWasGameFinished(value);
    }

    @Override
    public void ShowTitle(Title title)
    {
        _serverPlayer.ShowTitle(title);
    }

    @Override
    public void ClearTitle()
    {
        _serverPlayer.ClearTitle();
    }

    @Override
    public void ShowActionbar(ActionbarMessage message)
    {
        _serverPlayer.ShowActionbar(message);
    }

    @Override
    public void RemoveActionbar(long id)
    {
        _serverPlayer.RemoveActionbar(id);
    }

    @Override
    public void ClearActionbar()
    {
        _serverPlayer.ClearActionbar();
    }

    @Override
    public void PlaySound(Sound sound, Location location, SoundCategory category, float volume, float pitch)
    {
        _serverPlayer.PlaySound(sound, location, category, volume, pitch);
    }

    @Override
    public void SendMessage(Component message)
    {
        _serverPlayer.SendMessage(message);
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
        _serverPlayer.SpawnParticle(particle, location, deltaX, deltaY, deltaZ, count, extra, data);
    }

    @Override
    public void Tick()
    {
        _executor.Tick();
    }

    @Override
    public double GetDamageDealtHalfHearts()
    {
        return _stats.GetDamageDealtHalfHearts();
    }

    @Override
    public double GetDamageTakenHalfHearts()
    {
        return _stats.GetDamageTakenHalfHearts();
    }

    @Override
    public int GetKills()
    {
        return _stats.GetKills();
    }

    @Override
    public int GetTicksSurvived()
    {
        return _stats.GetTicksSurvived();
    }

    @Override
    public double GetScore()
    {
        return _stats.GetScore();
    }

    @Override
    public boolean GetWasGameFinished()
    {
        return _stats.GetWasGameFinished();
    }
}