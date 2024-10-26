package sus.keiger.molehunt.game.spell;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Registry;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import sus.keiger.molehunt.MoleHuntPlugin;
import sus.keiger.molehunt.game.IGameServices;
import sus.keiger.molehunt.game.MoleHuntGameState;
import sus.keiger.molehunt.game.player.IGamePlayer;
import sus.keiger.plugincommon.IterationSafeSet;
import sus.keiger.plugincommon.PCMath;
import sus.keiger.plugincommon.TickClock;
import sus.keiger.plugincommon.player.actionbar.ActionbarMessage;

import java.text.NumberFormat;
import java.util.*;

public class DefaultGameSpellExecutor implements IGameSpellExecutor
{
    // Private fields.
    private final IGameServices _gameServices;
    private MoleHuntGameState _state = MoleHuntGameState.PreGame;
    private final Set<GameSpell> _activeSpells = new IterationSafeSet<>();
    private final Map<IGamePlayer, GamePlayerSpellData> _playerSpellData = new HashMap<>();
    private final NumberFormat _manaCostFormat;
    private final NumberFormat _manaAvailableFormat;

    private final double MAX_ABSOLUTE_MANA = 100d;
    private final int NOTIFICATION_DURATION_TICKS = PCMath.SecondsToTicks(5d);
    private final double RELATIVE_MANA_REGEN_PER_TICK = 1d / 9000d;
    public final int AVAILABLE_SPELL_CAST_MESSAGE_DURATION_TICKS = PCMath.SecondsToTicks(2.5);
    public final int MANA_ACTIONBAR_LIFESPAN_TICKS = PCMath.SecondsToTicks(3);
    public final long MANA_ACTIONBAR_ID = -41290812015L;


    // Constructors.
    public DefaultGameSpellExecutor(IGameServices gameServices)
    {
        _gameServices = Objects.requireNonNull(gameServices, "gameServices is null");
        _manaCostFormat = MoleHuntPlugin.GetNumberFormat("0.00");
        _manaAvailableFormat = MoleHuntPlugin.GetNumberFormat("0.0");
    }


    // Private methods.
    private void EnsurePlayerInMap(IGamePlayer player)
    {
        if (!_playerSpellData.containsKey(player))
        {
            GamePlayerSpellData SpellData = new GamePlayerSpellData(player);
            SpellData.CastClock.SetHandler(clock -> ShowSpellAvailableEvent(player));
            SetMana(SpellData, 1);
            _playerSpellData.put(player, new GamePlayerSpellData(player));
        }
    }

    private void CreateSpellCastContent(IGamePlayer castingPlayer,
                                        GameSpell spell,
                                        GameSpellArguments args)
    {
        castingPlayer.SendMessage(Component.text("Casted spell \"%s\" for %s mana".formatted(
                spell.GetDefinition().GetName(), _manaCostFormat.format(
                        spell.GetRelativeManaCost() * MAX_ABSOLUTE_MANA))).color(NamedTextColor.GREEN));
        castingPlayer.PlaySound(Sound.ENTITY_ILLUSIONER_CAST_SPELL, castingPlayer.GetMCPlayer().getLocation(),
                SoundCategory.PLAYERS, 1f, 1f);
        if (_gameServices.GetGameSettings().GetIsNotifiedOnSpellCast() && args.GetTargetPlayer() != null)
        {
            IGamePlayer TargetPlayer = _gameServices.GetGamePlayerCollection().GetGamePlayer(args.GetTargetPlayer());
            if (TargetPlayer != null)
            {
                TargetPlayer.ShowActionbar(new ActionbarMessage(NOTIFICATION_DURATION_TICKS,
                        Component.text("A spell has been cast on you!").color(NamedTextColor.RED)));
                TargetPlayer.PlaySound(Sound.ENTITY_ILLUSIONER_CAST_SPELL, TargetPlayer.GetMCPlayer().getLocation(),
                        SoundCategory.PLAYERS, 1f, 1f);
            }
        }
    }

    private void CastSpell(IGamePlayer castingPlayer,
                           GameSpell spell,
                           GameSpellArguments args,
                           GamePlayerSpellData playerData)
    {
        spell.OnAdd();

        if (spell.GetDefinition().GetType() == SpellType.Instant)
        {
            spell.Tick();
            spell.OnRemove();
        }
        else
        {
            _activeSpells.add(spell);
        }

        int CooldownTicks = _gameServices.GetGameSettings().GetSpellCastCooldownTicks();
        if (CooldownTicks > 0)
        {
            playerData.CastClock.SetTicksLeft(CooldownTicks);
        }
        SetMana(playerData, playerData.RelativeMana - spell.GetRelativeManaCost());

        CreateSpellCastContent(castingPlayer, spell, args);
    }

    private void TryCastSpell(IGamePlayer castingPlayer, GameSpellDefinition definition, GameSpellArguments args)
    {
        if (castingPlayer.IsAlive() && !_gameServices.GetGameSettings().GetCanAliveCastSpells())
        {
            castingPlayer.SendMessage(Component.text("Alive players may not cast spells").color(NamedTextColor.RED));
            return;
        }
        else if (!castingPlayer.IsAlive() && !_gameServices.GetGameSettings().GetCanDeadCastSpells())
        {
            castingPlayer.SendMessage(Component.text("Dead players may not cast spells").color(NamedTextColor.RED));
            return;
        }
        else if (_playerSpellData.get(castingPlayer).CastClock.GetTicksLeft() > 0)
        {
            castingPlayer.SendMessage(Component.text("Spell cast is on cooldown").color(NamedTextColor.RED));
            return;
        }

        GameSpell CreatedSpell = definition.CreateSpell(args, _gameServices);
        GamePlayerSpellData PlayerData = _playerSpellData.get(castingPlayer);
        if (CreatedSpell.GetRelativeManaCost() > PlayerData.RelativeMana)
        {
            castingPlayer.SendMessage(Component.text("Not enough mana to cast spell (%s / %s)".formatted(
                    _manaAvailableFormat.format(PlayerData.RelativeMana * MAX_ABSOLUTE_MANA),
                    _manaAvailableFormat.format(CreatedSpell.GetRelativeManaCost() * MAX_ABSOLUTE_MANA)))
                    .color(NamedTextColor.RED));
            return;
        }

        CastSpell(castingPlayer, definition.CreateSpell(args, _gameServices), args, PlayerData);
    }

    private void ShowSpellAvailableEvent(IGamePlayer gamePlayer)
    {
        gamePlayer.ShowActionbar(new ActionbarMessage(AVAILABLE_SPELL_CAST_MESSAGE_DURATION_TICKS,
                Component.text("Spell cast available").color(NamedTextColor.GREEN)));
        gamePlayer.PlaySound(Sound.BLOCK_NOTE_BLOCK_XYLOPHONE, gamePlayer.GetMCPlayer().getLocation(),
                SoundCategory.PLAYERS, 0.4f, 2f);
    }

    private void PlayerDataTick(GamePlayerSpellData data)
    {
        data.CastClock.Tick();
        SetMana(data, data.RelativeMana + (RELATIVE_MANA_REGEN_PER_TICK *
                _gameServices.GetGameSettings().GetManaRegenerationScale()));

        if ((_gameServices.GetGameSettings().GetCanAliveCastSpells() && data.Player.IsAlive())
                || (_gameServices.GetGameSettings().GetCanDeadCastSpells() && !data.Player.IsAlive()))
        {
            data.Player.ShowActionbar(new ActionbarMessage(MANA_ACTIONBAR_LIFESPAN_TICKS, Component.text(
                            "Mana: %s".formatted(_manaAvailableFormat.format(data.RelativeMana * MAX_ABSOLUTE_MANA)))
                    .color(NamedTextColor.DARK_AQUA), MANA_ACTIONBAR_ID));
        }
    }

    private void SetMana(GamePlayerSpellData data, double amount)
    {
        data.RelativeMana = Math.max(0d, Math.min(amount, 1d));
    }


    // Inherited methods.
    @Override
    public void SetState(MoleHuntGameState state)
    {
        _state = Objects.requireNonNull(state, "state is null");

        if (state == MoleHuntGameState.PreGame)
        {
            _gameServices.GetGamePlayerCollection().GetPlayers().forEach(this::EnsurePlayerInMap);
        }
        else if (state != MoleHuntGameState.InGame)
        {
            _activeSpells.forEach(GameSpell::OnRemove);
            _activeSpells.clear();
        }
    }

    @Override
    public void CastSpell(GameSpellDefinition definition, GameSpellArguments args)
    {
        Objects.requireNonNull(definition, "definition is null");
        Objects.requireNonNull(args, "args is null");

        if (_state != MoleHuntGameState.InGame)
        {
            args.GetCastingPlayer().SendMessage(Component.text("Spells may only be cast during the game.")
                    .color(NamedTextColor.RED));
            return;
        }

        IGamePlayer CastingPlayer = _gameServices.GetGamePlayerCollection().GetGamePlayer(args.GetCastingPlayer());
        if (CastingPlayer == null)
        {
            return;
        }

        EnsurePlayerInMap(CastingPlayer);
        TryCastSpell(CastingPlayer, definition, args);
    }

    @Override
    public double GetMaxMana()
    {
        return MAX_ABSOLUTE_MANA;
    }

    @Override
    public void Tick()
    {
        if (_state != MoleHuntGameState.InGame)
        {
            return;
        }

        for (GameSpell Spell : _activeSpells)
        {
            Spell.Tick();
            if (Spell.GetTicksRemaining() <= 0)
            {
                Spell.OnRemove();
                _activeSpells.remove(Spell);
            }
        }

        for (GamePlayerSpellData PlayerData : _playerSpellData.values())
        {
            PlayerDataTick(PlayerData);
        }
    }


    // Types.
    private static class GamePlayerSpellData
    {
        // Fields.
        public final IGamePlayer Player;
        public final TickClock CastClock = new TickClock();
        public double RelativeMana;


        // Constructors.
        public GamePlayerSpellData(IGamePlayer player)
        {
            Player = Objects.requireNonNull(player, "player is null");
            CastClock.SetIsRunning(true);
        }
    }
}