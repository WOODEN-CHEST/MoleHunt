package sus.keiger.molehunt.command;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import sus.keiger.molehunt.MoleHuntPlugin;
import sus.keiger.molehunt.game.MoleHuntSettings;
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
    private static final String KEYWORD_PREVIEW = "preview";
    private static final String KEYWORD_SETTING = "setting";
    private static final String KEYWORD_MOLE_COUNT_MIN= "mole_count_min";


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
        Command.AddSubNode(new KeywordNode(KEYWORD_PREVIEW, Data::Preview, null));

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

    private void Preview(CommandData data)
    {
        TextComponent.Builder PreviewMessage = Component.text();

        PreviewMessage.append(Component.text("Game settings: ").color(NamedTextColor.GREEN));
        PreviewMessage.append(Component.text("\nMole count: %d..%d ".formatted(_gameSettings.GetMoleCountMin(),
                _gameSettings.GetMoleCountMax())).color(NamedTextColor.AQUA));
        PreviewMessage.append(Component.text("\nGame Time Ticks: %d ".formatted(_gameSettings.GetGameTimeTicks()))
                .color(NamedTextColor.AQUA));
        PreviewMessage.append(Component.text("\nGrace Period Ticks: %d ".formatted(
                _gameSettings.GetGracePeriodTimeTicks())).color(NamedTextColor.AQUA));
        PreviewMessage.append(Component.text("\nDoes Border Shrink: %b ".formatted(
                _gameSettings.GetDoesBorderShrink())).color(NamedTextColor.AQUA));
        PreviewMessage.append(Component.text("\nBorder Shrink Start Time Ticks: %d ".formatted(
                _gameSettings.GetBorderShrinkStartTimeTicks())).color(NamedTextColor.AQUA));
        PreviewMessage.append(Component.text("\nSpell Cast Cooldown Ticks: %d ".formatted(
                _gameSettings.GetSpellCastCooldownTicks())).color(NamedTextColor.AQUA));
        PreviewMessage.append(Component.text("\nDo Spells Notify: %b ".formatted(
                _gameSettings.GetIsNotifiedOnSpellCast())).color(NamedTextColor.AQUA));
        PreviewMessage.append(Component.text("\nDo Spells Notify: %b ".formatted(
                _gameSettings.GetIsNotifiedOnSpellCast())).color(NamedTextColor.AQUA));


        data.SetFeedback(PreviewMessage.build());
    }
}