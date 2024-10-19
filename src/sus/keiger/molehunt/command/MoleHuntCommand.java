package sus.keiger.molehunt.command;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import sus.keiger.molehunt.game.MoleHuntSettings;
import sus.keiger.molehunt.player.IPlayerStateController;
import sus.keiger.plugincommon.command.*;

import java.lang.reflect.Field;
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
    private MoleHuntCommand(IPlayerStateController playerStateController, MoleHuntSettings gameSettings)
    {
        _playerStateController = Objects.requireNonNull(playerStateController, "playerStateController is null");
        _gameSettings = Objects.requireNonNull(gameSettings, "gameSettings is null");
    }


    // Static methods.
    public static ServerCommand CreateCommand(IPlayerStateController playerStateController,
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

    private void Preview(CommandData data)
    {
        TextComponent.Builder PreviewMessage = Component.text();
        PreviewMessage.append(Component.text("Game settings: ").color(NamedTextColor.GREEN));

        try
        {
            for (Field Property : _gameSettings.GetProperties())
            {
                Property.setAccessible(true);
                PreviewMessage.append(Component.text("\n%s: %s".formatted(_gameSettings.GetPropertyName(Property),
                        Property.get(_gameSettings).toString())).color(NamedTextColor.AQUA));
            }
        }
        catch (IllegalAccessException e)
        {
            data.SetFeedback(Component.text("Failed to preview game settings due to an internal error.")
                    .color(NamedTextColor.RED));
            return;
        }

        data.SetFeedback(PreviewMessage.build());
    }
}