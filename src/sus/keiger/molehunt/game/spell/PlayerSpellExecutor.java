package sus.keiger.molehunt.game.spell;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import sus.keiger.molehunt.game.player.IGamePlayer;
import sus.keiger.plugincommon.ITickable;
import sus.keiger.plugincommon.PCMath;
import sus.keiger.plugincommon.packet.PCGamePacketController;
import sus.keiger.plugincommon.player.actionbar.ActionbarMessage;

import java.util.Objects;

public class PlayerSpellExecutor implements ITickable
{
    // Private fields.
    private final IGamePlayer _gamePlayer;
    private final SpellSettings _spellSettings;
    private final PCGamePacketController _packetController;
    private final SpellContainer _spells;
    private final int SPELL_MESSAGE_DURATION_TICKS = PCMath.SecondsToTicks(3d);

    private int _spellCooldownTicks;
    private int _lootSpellIndex = 0;


    // Constructors,
    public PlayerSpellExecutor(IGamePlayer gamePlayer,
                               SpellSettings spellSettings,
                               PCGamePacketController packetController,
                               SpellContainer spells)
    {
        _spells = Objects.requireNonNull(spells, "spells is null");
        _gamePlayer = Objects.requireNonNull(gamePlayer, "gamePlayer is null");
        _spellSettings = Objects.requireNonNull(spellSettings, "spellSettings is null");
        _packetController = Objects.requireNonNull(packetController, "packetController is null");
    }


    // Private methods.
    private void OnSpellEndEvent(SpellEndEvent event)
    {
        event.GetSpell().GetSpellEndEvent().Unsubscribe(this);
    }

    private void ShowSpellAvailableContent()
    {
        _gamePlayer.ShowActionbar(new ActionbarMessage(SPELL_MESSAGE_DURATION_TICKS,
                Component.text("Spell cast available").color(NamedTextColor.GREEN)));
        _gamePlayer.PlaySound(Sound.BLOCK_NOTE_BLOCK_XYLOPHONE, _gamePlayer.GetMCPlayer().getLocation(),
                SoundCategory.PLAYERS, 0.6f, 2f);
    }

    private void ShowSpellCastContentSelf(String spellName)
    {
        _gamePlayer.ShowActionbar(new ActionbarMessage(SPELL_MESSAGE_DURATION_TICKS,
                Component.text("Casted spell \"%s\"".formatted(spellName)).color(NamedTextColor.GREEN)));
        _gamePlayer.PlaySound(Sound.ENTITY_ILLUSIONER_CAST_SPELL, _gamePlayer.GetMCPlayer().getLocation(),
                SoundCategory.PLAYERS, 1f, 1f);
    }

    private void ShowSpellCastContentTarget(IGamePlayer targetPlayer)
    {
        if (!_spellSettings.GetIsNotified())
        {
            return;
        }

        targetPlayer.ShowActionbar(new ActionbarMessage(SPELL_MESSAGE_DURATION_TICKS,
                Component.text("A spell has been cast on you").color(NamedTextColor.RED)));
        targetPlayer.PlaySound(Sound.ENTITY_ILLUSIONER_CAST_SPELL, _gamePlayer.GetMCPlayer().getLocation(),
                SoundCategory.PLAYERS, 1f, 1f);
    }

    private void ShowSpellCooldownContent()
    {
        _gamePlayer.ShowActionbar(new ActionbarMessage(SPELL_MESSAGE_DURATION_TICKS,
                Component.text("You are on spell cooldown").color(NamedTextColor.RED)));
        _gamePlayer.PlaySound(Sound.BLOCK_NOTE_BLOCK_DIDGERIDOO, _gamePlayer.GetMCPlayer().getLocation(),
                SoundCategory.PLAYERS, 0.8f, 0.8f);
    }

    private void ShowCantCastSpellContent()
    {
        _gamePlayer.ShowActionbar(new ActionbarMessage(SPELL_MESSAGE_DURATION_TICKS,
                Component.text("Can't cast spells while alive").color(NamedTextColor.RED)));
        _gamePlayer.PlaySound(Sound.BLOCK_NOTE_BLOCK_DIDGERIDOO, _gamePlayer.GetMCPlayer().getLocation(),
                SoundCategory.PLAYERS, 0.8f, 0.8f);
    }


    // Methods.
    public void CastSpell(GameSpellDefinition spellDefinition, GameSpellArguments args)
    {
        if (_spellCooldownTicks > 0)
        {
            ShowSpellCooldownContent();
            return;
        }
        else if (_gamePlayer.IsAlive())
        {
            ShowCantCastSpellContent();
            return;
        }

        GameSpell Spell = spellDefinition.CreateSpell(args);
        _spells.AddSpell(Spell);
        ShowSpellCastContentSelf(spellDefinition.GetName());
        if (args.GetTargetPlayer() != null)
        {
            ShowSpellCastContentTarget(args.GetTargetPlayer());
        }
        _spellCooldownTicks = _spellSettings.GetSpellCooldownTicks();
    }


    // Inherited methods.
    @Override
    public void Tick()
    {
        if(_spellCooldownTicks >= 0)
        {
            _spellCooldownTicks--;
        }
        if (_spellCooldownTicks == 0)
        {
            ShowSpellAvailableContent();
        }
    }
}