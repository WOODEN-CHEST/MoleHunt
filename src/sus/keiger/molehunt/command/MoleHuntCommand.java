package sus.keiger.molehunt.command;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import sus.keiger.molehunt.MoleHuntPlugin;
import sus.keiger.molehunt.player.IPlayerStateController;
import sus.keiger.plugincommon.command.CommandData;
import sus.keiger.plugincommon.command.KeywordNode;
import sus.keiger.plugincommon.command.ServerCommand;

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


    // Private fields.
    private final IPlayerStateController _playerStateController;


    // Constructors.
    public MoleHuntCommand(IPlayerStateController playerStateController)
    {
        _playerStateController = Objects.requireNonNull(playerStateController, "playerStateController is null");
    }


    // Static methods.
    public static ServerCommand GetCommand(IPlayerStateController playerStateController)
    {
        ServerCommand Command = new ServerCommand(LABEL, null);

        MoleHuntCommand Data = new MoleHuntCommand(playerStateController);

        Command.AddSubNode(new KeywordNode(KEYWORD_START, Data::Start, null));
        Command.AddSubNode(new KeywordNode(KEYWORD_END, Data::End, null));
        Command.AddSubNode(new KeywordNode(KEYWORD_CANCEL, Data::Cancel, null));

        return Command;
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
        data.SetFeedback(Component.text("Cancelled game.").color(NamedTextColor.GREEN));
    }
}
