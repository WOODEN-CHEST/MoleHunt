package sus.keiger.molehunt.game.player;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import sus.keiger.molehunt.command.SpellCommand;
import sus.keiger.molehunt.event.*;
import sus.keiger.molehunt.game.GameTeamType;
import sus.keiger.molehunt.game.IGameServices;
import sus.keiger.molehunt.game.IGameTeam;
import sus.keiger.molehunt.game.spell.*;
import sus.keiger.molehunt.player.*;
import sus.keiger.plugincommon.PCPluginEvent;
import sus.keiger.plugincommon.player.actionbar.ActionbarMessage;
import sus.keiger.plugincommon.value.GameModifiableValue;

import java.util.Objects;

public class DefaultGamePlayer implements IGamePlayer
{
    // Private fields.
    private final IModifiablePlayerStats _stats = new PlayerStatsContainer();
    private final IGameServices _gameServices;
    private final IServerPlayer _serverPlayer;

    private final PCPluginEvent<GamePlayerLifeChangeEvent> _lifeChangeEvent = new PCPluginEvent<>();

    private boolean _isAlive;
    private boolean _mayDealDamage = true;
    private GamePlayerState _state = GamePlayerState.PreGame;
    private IGamePlayerExecutor _executor;

    private final double DEFAULT_HEALTH = 20d;
    private final double DEFAULT_MINING_SPEED = 1d;
    private final double DEFAULT_ATTACK_SPEED = 4d;
    private final double DEFAULT_ENTITY_REACH = 3d;
    private final double DEFAULT_BLOCK_REACH = 4.5d;
    private final double MOLE_PARTICLE_OFFSET_DELTA = 0.5d;
    private final float MOLE_PARTICLE_SIZE = 0.75f;
    private final GameModifiableValue _maxHealth = new GameModifiableValue(DEFAULT_HEALTH);
    private final GameModifiableValue _miningSpeed = new GameModifiableValue(DEFAULT_MINING_SPEED);
    private final GameModifiableValue _attackSpeed = new GameModifiableValue(DEFAULT_ATTACK_SPEED);
    private final GameModifiableValue _entityReach = new GameModifiableValue(DEFAULT_ENTITY_REACH);
    private final GameModifiableValue _blockReach = new GameModifiableValue(DEFAULT_BLOCK_REACH);



    // Constructors.
    public DefaultGamePlayer(IServerPlayer player,
                             IGameServices services)
    {
        _serverPlayer = Objects.requireNonNull(player, "player is null");
        _gameServices = Objects.requireNonNull(services, "services is null");

        _isAlive = true;
        _executor = new AlivePlayerExecutor(this, services);
    }


    // Private methods.
    private void OnPlayerDeathEvent(PlayerDeathEvent event)
    {
        _executor.OnPlayerDeathEvent(event);
    }

    private void OnEntityDamageEvent(EntityDamageEvent event)
    {
        _executor.OnEntityDamageEvent(event);

        if ((_state != GamePlayerState.InGame) && (event.getEntity() == GetMCPlayer()))
        {
            event.setCancelled(true);
            return;
        }

        if (!(event.getDamageSource().getCausingEntity() instanceof Player PlayerCause))
        {
            return;
        }

        IGamePlayer DamagerPlayer = _gameServices.GetGamePlayerCollection().GetGamePlayer(
                _gameServices.GetServerPlayerCollection().GetPlayer(PlayerCause));

        if ((GetMCPlayer() == event.getEntity()) && (DamagerPlayer != null)
                && !DamagerPlayer.GetMayDealDamage())
        {
            event.setCancelled(true);
        }
    }

    private void ShowMoleParticle()
    {
        IGameTeam MoleTeam = _gameServices.GetGamePlayerCollection().GetTeamByType(GameTeamType.Moles);
        if (MoleTeam.ContainsPlayer(this) || !IsAlive())
        {
            for (IGamePlayer Teammate : MoleTeam.GetPlayers())
            {
                if ((Teammate == this) || !Teammate.IsAlive())
                {
                    continue;
                }
                SpawnParticle(Particle.DUST, Teammate.GetMCPlayer().getEyeLocation(),
                        MOLE_PARTICLE_OFFSET_DELTA, MOLE_PARTICLE_OFFSET_DELTA, MOLE_PARTICLE_OFFSET_DELTA,
                        1, 1d, new Particle.DustOptions(MoleTeam.GetColor(), MOLE_PARTICLE_SIZE));
            }
        }
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

        if (_state != GamePlayerState.InGame)
        {
            return;
        }

        _executor = _isAlive ?
                new AlivePlayerExecutor(this, _gameServices):
                new DeadPlayerExecutor(this, _gameServices);
        _executor.SwitchState(_state);

        _lifeChangeEvent.FireEvent(new GamePlayerLifeChangeEvent(this, _isAlive));
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

    @Override
    public boolean GetMayDealDamage()
    {
        return _mayDealDamage;
    }

    @Override
    public void SetMayDealDamage(boolean value)
    {
        _mayDealDamage = value;
    }

    @Override
    public GameModifiableValue GetMaxHealth()
    {
        return _maxHealth;
    }

    @Override
    public GameModifiableValue GetMiningSpeed()
    {
        return _miningSpeed;
    }

    @Override
    public GameModifiableValue GetEntityReach()
    {
        return _entityReach;
    }

    @Override
    public GameModifiableValue GetAttackSpeed()
    {
        return _attackSpeed;
    }

    @Override
    public GameModifiableValue GetBlockReach()
    {
        return _blockReach;
    }

    @Override
    public PCPluginEvent<GamePlayerLifeChangeEvent> GetLifeChangeEvent()
    {
        return _lifeChangeEvent;
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
        _blockReach.Tick();
        _entityReach.Tick();
        _attackSpeed.Tick();
        _miningSpeed.Tick();
        _maxHealth.Tick();

        if (_state == GamePlayerState.InGame)
        {
            _executor.Tick();
        }
        ShowMoleParticle();
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