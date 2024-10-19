package sus.keiger.molehunt.game.spell;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import sus.keiger.molehunt.game.MoleHuntGameState;
import sus.keiger.molehunt.game.player.IGamePlayer;
import sus.keiger.plugincommon.ITickable;
import sus.keiger.plugincommon.IterationSafeSet;
import sus.keiger.plugincommon.PCMath;
import sus.keiger.plugincommon.TickClock;
import sus.keiger.plugincommon.player.actionbar.ActionbarMessage;

import java.util.*;

public class DefaultGameSpellExecutor implements IGameSpellExecutor
{
    // Private fields.
    private final SpellServiceProvider _spellServiceProvider;
    private MoleHuntGameState _state = MoleHuntGameState.PreGame;
    private final Set<GameSpell> _activeSpells = new IterationSafeSet<>();
    private final Map<IGamePlayer, GamePlayerSpellData> _playerSpellData = new HashMap<>();

    private final int NOTIFICATION_DURATION_TICKS = PCMath.SecondsToTicks(5d);


    // Constructors.
    public DefaultGameSpellExecutor(SpellServiceProvider serviceProvider)
    {
        _spellServiceProvider = Objects.requireNonNull(serviceProvider, "serviceProvider is null");
    }


    // Private methods.
    private void EnsurePlayerInMap(IGamePlayer player)
    {
        if (!_playerSpellData.containsKey(player))
        {
            _playerSpellData.put(player, new GamePlayerSpellData(player));
        }
    }

    private void CreateSpellCastContent(IGamePlayer castingPlayer,
                                        GameSpellDefinition definition,
                                        GameSpellArguments args)
    {
        castingPlayer.SendMessage(Component.text("Casted spell \"%s\"".formatted(definition.GetName()))
                .color(NamedTextColor.GREEN));
        castingPlayer.PlaySound(Sound.ENTITY_ILLUSIONER_CAST_SPELL, castingPlayer.GetMCPlayer().getLocation(),
                SoundCategory.PLAYERS, 1f, 1f);
        if (_spellServiceProvider.GetSettings().GetIsNotifiedOnSpellCast() && args.GetTargetPlayer() != null)
        {
            IGamePlayer TargetPlayer = _spellServiceProvider.GetGamePlayers().GetGamePlayer(args.GetTargetPlayer());
            if (TargetPlayer != null)
            {
                TargetPlayer.ShowActionbar(new ActionbarMessage(NOTIFICATION_DURATION_TICKS,
                        Component.text("A spell has been cast on you!").color(NamedTextColor.RED)));
                TargetPlayer.PlaySound(Sound.ENTITY_ILLUSIONER_CAST_SPELL, TargetPlayer.GetMCPlayer().getLocation(),
                        SoundCategory.PLAYERS, 1f, 1f);
            }
        }
    }

    private void CastSpell(IGamePlayer castingPlayer, GameSpellDefinition definition, GameSpellArguments args)
    {
        GameSpell CreatedSpell = definition.CreateSpell(args, _spellServiceProvider);
        CreatedSpell.OnAdd();

        if (definition.GetType() == SpellType.Instant)
        {
            CreatedSpell.Tick();
            CreatedSpell.OnRemove();
        }
        else
        {
            _activeSpells.add(CreatedSpell);
        }

        int CooldownTicks = _spellServiceProvider.GetSettings().GetSpellCastCooldownTicks();
        if (CooldownTicks > 0)
        {
            _playerSpellData.get(castingPlayer).CastClock.SetTicksLeft(CooldownTicks);
        }

        CreateSpellCastContent(castingPlayer, definition, args);
    }

    private void TryCastSpell(IGamePlayer castingPlayer, GameSpellDefinition definition, GameSpellArguments args)
    {
        if (castingPlayer.IsAlive() && !_spellServiceProvider.GetSettings().GetCanAliveCastSpells())
        {
            castingPlayer.SendMessage(Component.text("Alive players may not cast spells").color(NamedTextColor.RED));
        }
        else if (!castingPlayer.IsAlive() && !_spellServiceProvider.GetSettings().GetCanDeadCastSpells())
        {
            castingPlayer.SendMessage(Component.text("Dead players may not cast spells").color(NamedTextColor.RED));
        }
        else if (!_playerSpellData.get(castingPlayer).IsSpellCastAvailable())
        {
            castingPlayer.SendMessage(Component.text("Spell cast is on cooldown").color(NamedTextColor.RED));
        }
        else
        {
            CastSpell(castingPlayer, definition, args);
        }
    }


    // Inherited methods.
    @Override
    public void SetState(MoleHuntGameState state)
    {
        _state = Objects.requireNonNull(state, "state is null");

        if (state != MoleHuntGameState.InGame)
        {
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

        IGamePlayer CastingPlayer = _spellServiceProvider.GetGamePlayers().GetGamePlayer(args.GetCastingPlayer());
        if (CastingPlayer == null)
        {
            return;
        }
        EnsurePlayerInMap(CastingPlayer);

        TryCastSpell(CastingPlayer, definition, args);
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
            if (Spell.GetClock().GetTicksLeft() <= 0)
            {
                Spell.OnRemove();
                _activeSpells.remove(Spell);
            }
        }

        _playerSpellData.values().forEach(ITickable::Tick);
    }


    // Types.
    private static class GamePlayerSpellData implements ITickable
    {
        // Fields.
        public final IGamePlayer Player;
        public final TickClock CastClock = new TickClock();
        public int LootSpellIndex = 0;
        public final int AVAILABLE_SPELL_CAST_MESSAGE_DURATION_TICKS = PCMath.SecondsToTicks(5);


        // Constructors.
        public GamePlayerSpellData(IGamePlayer player)
        {
            Player = Objects.requireNonNull(player, "player is null");
            CastClock.SetIsRunning(true);
            CastClock.SetHandler(this::OnSpellAvailableEvent);
        }


        // Methods.
        public boolean IsSpellCastAvailable()
        {
            return CastClock.GetTicksLeft() <= 0;
        }

        public void OnSpellAvailableEvent(TickClock clock)
        {
            Player.ShowActionbar(new ActionbarMessage(AVAILABLE_SPELL_CAST_MESSAGE_DURATION_TICKS,
                    Component.text("Spell cast available").color(NamedTextColor.GREEN)));
            Player.PlaySound(Sound.BLOCK_NOTE_BLOCK_XYLOPHONE, Player.GetMCPlayer().getLocation(),
                    SoundCategory.PLAYERS, 0.4f, 2f);
        }


        // Inherited methods.
        @Override
        public void Tick()
        {
            CastClock.Tick();
        }
    }
}