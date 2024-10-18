package sus.keiger.molehunt.command;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import sus.keiger.molehunt.MoleHuntPlugin;
import sus.keiger.molehunt.game.MoleHuntSettings;
import sus.keiger.molehunt.player.IPlayerStateController;
import sus.keiger.plugincommon.command.*;

import java.text.NumberFormat;
import java.util.Objects;
import java.util.function.Consumer;

public class MoleHuntCommand
{
    // Static fields.
    public static final String LABEL = "molehunt";


    // Private static fields.
    private static final String KEYWORD_START = "start";
    private static final String KEYWORD_END = "end";
    private static final String KEYWORD_CANCEL = "cancel";
    private static final String KEYWORD_SETTING = "setting";
    private static final String KEYWORD_MOLE_COUNT = "mole_count";
    private static final String KEYWORD_GAME_TIME = "game_time";
    private static final String KEYWORD_GRACE_PERIOD_TICKS = "grace_period_ticks";
    private static final String KEYWORD_DOES_BORDER_SHRINK = "does_border_shrink";
    private static final String KEYWORD_BORDER_SIZE_START = "border_size_block_start";
    private static final String KEYWORD_BORDER_SIZE_END = "border_size_block_end";
    private static final String KEYWORD_BORDER_SHRINK_TIME = "border_shrink_time_ticks";
    private static final String KEYWORD_SPELL_COOLDOWN = "spell_cooldown_ticks";
    private static final String KEYWORD_CAN_CAST_SPELLS = "can_cast_spells";
    private static final String KEYWORD_IS_NOTIFIED_ON_SPELL_CAST = "is_notified_on_spell_cast";
    private static final String KEYWORD_PLAYER_HEALTH_HALF_HEARTS = "player_health_half_hearts";

    private static final String KEY_VALUE = "value";


    // Private fields.
    private final IPlayerStateController _playerStateController;
    private final MoleHuntSettings _gameSettings;


    // Constructors.
    public MoleHuntCommand(IPlayerStateController playerStateController, MoleHuntSettings gameSettings)
    {
        _playerStateController = Objects.requireNonNull(playerStateController, "playerStateController is null");
        _gameSettings = Objects.requireNonNull(gameSettings, "gameSettings is null");
    }


    // Static methods.
    public static ServerCommand GetCommand(IPlayerStateController playerStateController,
                                           MoleHuntSettings gameSettings)
    {
        ServerCommand Command = new ServerCommand(LABEL, null);

        MoleHuntCommand Data = new MoleHuntCommand(playerStateController, gameSettings);

        Command.AddSubNode(new KeywordNode(KEYWORD_START, Data::Start, null));
        Command.AddSubNode(new KeywordNode(KEYWORD_END, Data::End, null));
        Command.AddSubNode(new KeywordNode(KEYWORD_CANCEL, Data::Cancel, null));
        Command.AddSubNode(GetSettingSubNode(Data));

        return Command;
    }

    private static CommandNode GetSettingSubNode(MoleHuntCommand data)
    {
        CommandNode SettingNode = new KeywordNode(KEYWORD_SETTING, data::Preview, null);

        CommandNode MoleCountNode = new KeywordNode(KEYWORD_MOLE_COUNT, null, KEY_VALUE);
        MoleCountNode.AddSubNode(new NumberNode(data::SetMoleCount, null, KEY_VALUE, NumberNodeType.Integer));
        SettingNode.AddSubNode(MoleCountNode);

        CommandNode GracePeriodNode = new KeywordNode(KEYWORD_GRACE_PERIOD_TICKS, null, KEY_VALUE);
        GracePeriodNode.AddSubNode(new NumberNode(data::SetGracePeriodTicks, null, KEY_VALUE, NumberNodeType.Integer));
        SettingNode.AddSubNode(GracePeriodNode);

        CommandNode DoesBorderShrinkNode = new KeywordNode(KEYWORD_DOES_BORDER_SHRINK, null, KEY_VALUE);
        DoesBorderShrinkNode.AddSubNode(new BooleanNode(data::SetDoesBorderShrink, KEY_VALUE));
        SettingNode.AddSubNode(DoesBorderShrinkNode);

        CommandNode BorderSizeStartNode = new KeywordNode(KEYWORD_BORDER_SIZE_START, null, KEY_VALUE);
        BorderSizeStartNode.AddSubNode(new NumberNode(data::SetBorderSizeStart, null,
                KEY_VALUE, NumberNodeType.Integer));
        SettingNode.AddSubNode(BorderSizeStartNode);

        CommandNode BorderSizeEndNode = new KeywordNode(KEYWORD_BORDER_SIZE_END, null, KEY_VALUE);
        BorderSizeEndNode.AddSubNode(new NumberNode(data::SetBorderSizeEnd, null, KEY_VALUE, NumberNodeType.Integer));
        SettingNode.AddSubNode(BorderSizeEndNode);

        CommandNode BorderShrinkTimeNode = new KeywordNode(KEYWORD_BORDER_SHRINK_TIME, null, KEY_VALUE);
        BorderShrinkTimeNode.AddSubNode(new NumberNode(data::SetBorderShrinkTime, null,
                KEY_VALUE, NumberNodeType.Integer));
        SettingNode.AddSubNode(BorderShrinkTimeNode);

        CommandNode SpellCooldownNode = new KeywordNode(KEYWORD_SPELL_COOLDOWN, null, KEY_VALUE);
        SpellCooldownNode.AddSubNode(new NumberNode(data::SetSpellCooldown, null, KEY_VALUE, NumberNodeType.Integer));
        SettingNode.AddSubNode(SpellCooldownNode);

        CommandNode CanCastSpellsNode = new KeywordNode(KEYWORD_CAN_CAST_SPELLS, null, KEY_VALUE);
        CanCastSpellsNode.AddSubNode(new BooleanNode(data::SetCanCastSpells, KEY_VALUE));
        SettingNode.AddSubNode(CanCastSpellsNode);

        CommandNode IsNotifiedNode = new KeywordNode(KEYWORD_IS_NOTIFIED_ON_SPELL_CAST, null, KEY_VALUE);
        IsNotifiedNode.AddSubNode(new BooleanNode(data::SetIsNotifiedOnSpellCast, KEY_VALUE));
        SettingNode.AddSubNode(IsNotifiedNode);

        CommandNode HealthNode = new KeywordNode(KEYWORD_PLAYER_HEALTH_HALF_HEARTS, null, KEY_VALUE);
        HealthNode.AddSubNode(new NumberNode(data::SetHealth, null, KEY_VALUE, NumberNodeType.Double));
        SettingNode.AddSubNode(HealthNode);

        return SettingNode;
    }


    // Private methods.
    private void Start(CommandData data)
    {
        if (_playerStateController.StartGame())
        {
            data.SetFeedback(Component.text("Started game.").color(NamedTextColor.GREEN));
        }
        else
        {
            data.SetFeedback(Component.text("Failed to start game.").color(NamedTextColor.RED));
        }
    }

    private void End(CommandData data)
    {
        if (_playerStateController.GetActiveGameInstance().End())
        {
            data.SetFeedback(Component.text("Ended game.").color(NamedTextColor.GREEN));
        }
        else
        {
            data.SetFeedback(Component.text("Failed to end game.").color(NamedTextColor.RED));
        }
    }

    private void Cancel(CommandData data)
    {
        _playerStateController.GetActiveGameInstance().Cancel();
    }

    private <T> void SetSimpleValue(CommandData data, Consumer<T> setter)
    {
        T Value = data.GetParsedData(KEY_VALUE);
        setter.accept(Value);
        TellSetDataFeedback(data);
    }

    private void TellSetDataFeedback(CommandData data)
    {
        data.SetFeedback(Component.text("Updated game property!").color(NamedTextColor.GREEN));
    }

    private void Preview(CommandData data)
    {
        TextComponent.Builder PreviewMessage = Component.text();

        NumberFormat Formatter = MoleHuntPlugin.GetNumberFormat("0.00");

        PreviewMessage.append(Component.text("Game settings: ").color(NamedTextColor.GREEN));
        PreviewMessage.append(Component.text("\nMole count: %d to %d ".formatted(_gameSettings.GetMoleCountMin(),
                _gameSettings.GetMoleCountMax())).color(NamedTextColor.AQUA));

        PreviewMessage.append(Component.text("\nGame Time Ticks: %d ".formatted(_gameSettings.GetGameTimeTicks()))
                .color(NamedTextColor.AQUA));

        PreviewMessage.append(Component.text("\nGrace Period Ticks: %d ".formatted(
                _gameSettings.GetGracePeriodTimeTicks())).color(NamedTextColor.AQUA));

        PreviewMessage.append(Component.text("\nDoes Border Shrink: %b ".formatted(
                _gameSettings.GetDoesBorderShrink())).color(NamedTextColor.AQUA));

        PreviewMessage.append(Component.text("\nBorder Shrink Start Time Ticks: %d ".formatted(
                _gameSettings.GetBorderShrinkStartTimeTicks())).color(NamedTextColor.AQUA));

        PreviewMessage.append(Component.text("\nBorder Start Size Blocks: %d ".formatted(
                _gameSettings.GetBorderSizeStartBlocks())).color(NamedTextColor.AQUA));

        PreviewMessage.append(Component.text("\nBorder End Size Blocks: %d ".formatted(
                _gameSettings.GetBorderSizeEndBlocks())).color(NamedTextColor.AQUA));

        PreviewMessage.append(Component.text("\nSpell Cast Cooldown Ticks: %d ".formatted(
                _gameSettings.GetSpellCastCooldownTicks())).color(NamedTextColor.AQUA));

        PreviewMessage.append(Component.text("\nCan Cast Spells: %b ".formatted(
                _gameSettings.GetCanCastSpells())).color(NamedTextColor.AQUA));

        PreviewMessage.append(Component.text("\nDo Spells Notify: %b ".formatted(
                _gameSettings.GetIsNotifiedOnSpellCast())).color(NamedTextColor.AQUA));

        PreviewMessage.append(Component.text("\nPlayer Health Half Hearts: %s ".formatted(
                Formatter.format(_gameSettings.GetPlayerHealthHalfHearts()))).color(NamedTextColor.AQUA));

        data.SetFeedback(PreviewMessage.build());
    }

    private void SetMoleCount(CommandData data)
    {
        RangeNodeIntResult Range = data.GetParsedData(KEY_VALUE);

        _gameSettings.SetMoleCountMin((int)Range.GetMin());
        _gameSettings.SetMoleCountMin((int)Range.GetMax());
        TellSetDataFeedback(data);
    }

    private void SetGameTime(CommandData data)
    {
        SetSimpleValue(data, _gameSettings::SetGameTimeTicks);
    }

    private void SetGracePeriodTicks(CommandData data)
    {
        SetSimpleValue(data, _gameSettings::SetGracePeriodTimeTicks);
    }

    private void SetDoesBorderShrink(CommandData data)
    {
        SetSimpleValue(data, _gameSettings::SetDoesBorderShrink);
    }

    private void SetBorderSizeStart(CommandData data)
    {
        SetSimpleValue(data, _gameSettings::SetBorderSizeStartBlocks);
    }

    private void SetBorderSizeEnd(CommandData data)
    {
        SetSimpleValue(data, _gameSettings::SetBorderSizeEndBlocks);
    }

    private void SetBorderShrinkTime(CommandData data)
    {
        SetSimpleValue(data, _gameSettings::SetBorderShrinkStartTimeTicks);
    }

    private void SetSpellCooldown(CommandData data)
    {
        SetSimpleValue(data, _gameSettings::SetSpellCastCooldownTicks);
    }

    private void SetCanCastSpells(CommandData data)
    {
        SetSimpleValue(data, _gameSettings::SetCanCastSpells);
    }

    private void SetIsNotifiedOnSpellCast(CommandData data)
    {
        SetSimpleValue(data, _gameSettings::SetIsNotifiedOnSpellCast);
    }

    private void SetHealth(CommandData data)
    {
        SetSimpleValue(data, _gameSettings::SetPlayerHealthHalfHearts);
    }
}