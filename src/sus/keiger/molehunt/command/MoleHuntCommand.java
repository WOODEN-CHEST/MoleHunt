package sus.keiger.molehunt.command;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import sus.keiger.molehunt.MoleHuntPlugin;
import sus.keiger.molehunt.game.MoleHuntSettings;
import sus.keiger.molehunt.player.IPlayerStateController;
import sus.keiger.plugincommon.command.*;

import java.lang.reflect.Field;
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

    private static CommandNode GetValueNodeFromProperty(MoleHuntCommand data, Field property)
    {
        if (property.getType().equals(int.class))
        {
            return new NumberNode(commandData -> data.SetProperty(property, commandData),
                    null, KEY_VALUE, NumberNodeType.Integer);
        }
        else if (property.getType().equals(double.class))
        {
            return new NumberNode(commandData -> data.SetProperty(property, commandData),
                    null, KEY_VALUE, NumberNodeType.Double);
        }
        else if (property.getType().equals(boolean.class))
        {
            return new BooleanNode(commandData -> data.SetProperty(property, commandData), KEY_VALUE);
        }
        throw new IllegalStateException("Unsupported property type: %s".formatted(property.getType().getName()));
    }

    private static CommandNode GetSettingSubNode(MoleHuntCommand data)
    {
        CommandNode SettingNode = new KeywordNode(KEYWORD_SETTING, data::Preview, null);

        for (Field Property : data._gameSettings.GetProperties())
        {
            KeywordNode NameNode = new KeywordNode(data._gameSettings.GetPropertyName(Property), null, KEY_VALUE);
            NameNode.AddSubNode(GetValueNodeFromProperty(data, Property));
            SettingNode.AddSubNode(NameNode);
        }

        return SettingNode;
    }


    // Private methods.
    private void SetProperty(Field property, CommandData data)
    {
        Object Value = data.GetParsedData(KEY_VALUE);
        _gameSettings.SetValue(property, Value);
        data.SetFeedback(Component.text("Set property \"%s\" to %s".formatted(
                _gameSettings.GetPropertyName(property), Value.toString()))
                .color(NamedTextColor.GREEN));
    }


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
}