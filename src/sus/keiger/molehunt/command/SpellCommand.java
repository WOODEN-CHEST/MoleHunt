package sus.keiger.molehunt.command;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import sus.keiger.molehunt.MoleHuntPlugin;
import sus.keiger.molehunt.game.MoleHuntGameState;
import sus.keiger.molehunt.game.MoleHuntSettings;
import sus.keiger.molehunt.game.player.IGamePlayer;
import sus.keiger.molehunt.game.spell.*;
import sus.keiger.molehunt.player.IPlayerStateController;
import sus.keiger.molehunt.player.IServerPlayer;
import sus.keiger.molehunt.player.IServerPlayerCollection;
import sus.keiger.plugincommon.PCString;
import sus.keiger.plugincommon.command.*;

import java.util.List;
import java.util.Objects;

public class SpellCommand
{
    // Static fields.
    public static final String LABEL = "spell";


    // Private static fields.
    private static final String KEYWORD_CAST = "cast";
    private static final String KEYWORD_HELP = "help";

    private static final String KEY_SPELL_NAME = "name";
    private static final String KEY_SPELL_REQUIREMENT_PLAYER = "player";


    // Private fields.
    private final IPlayerStateController _playerStateController;
    private final GameSpellCollection _spells;
    private final IServerPlayerCollection _serverPlayers;


    // Constructors.
    public SpellCommand(IPlayerStateController playerStateController,
                        GameSpellCollection spells,
                        IServerPlayerCollection serverPlayers)
    {
        _playerStateController = Objects.requireNonNull(playerStateController, "playerStateController is null");
        _spells = Objects.requireNonNull(spells, "spells is null");
        _serverPlayers = Objects.requireNonNull(serverPlayers, "serverPlayers is null");
    }


    // Static methods.
    public static ServerCommand CreateCommand(IPlayerStateController playerStateController,
                                              GameSpellCollection spells,
                                              IServerPlayerCollection serverPlayers)
    {
        ServerCommand Command = new ServerCommand(LABEL, null);
        SpellCommand Data = new SpellCommand(playerStateController, spells, serverPlayers);

        Command.AddSubNode(GetHelpNode(Data));
        Command.AddSubNode(GetCastNode(Data));

        return Command;
    }


    // Private static methods.
    private static CommandNode GetHelpNode(SpellCommand data)
    {
        CommandNode HelpNode = new KeywordNode(KEYWORD_HELP, data::GetHelp, null);

        CommandNode SpellsNode = new ListNode(data::GetHelpForSpell, KEY_SPELL_NAME,
                commandData -> data._spells.GetSpells().stream().map(GameSpellDefinition::GetName).toList());
        HelpNode.AddSubNode(SpellsNode);
        return HelpNode;
    }

    private static CommandNode GetNodeForSpell(SpellCommand data, GameSpellDefinition definition)
    {
        if (definition.GetRequirements().contains(SpellDataRequirement.TargetPlayer))
        {
            CommandNode SpellNameNode = new KeywordNode(definition.GetName(), null, KEY_SPELL_NAME);
            SpellNameNode.AddSubNode(new PlayerSelectorNode(data::CastSpell, false, commandData ->
                    data._playerStateController.GetActiveGameInstance().GetActivePlayers().stream().filter(
                            IGamePlayer::IsAlive).map(IGamePlayer::GetMCPlayer).toList(),
                    1, KEY_SPELL_REQUIREMENT_PLAYER));
            return SpellNameNode;
        }
        else
        {
            return new KeywordNode(definition.GetName(), data::CastSpell, KEY_SPELL_NAME);
        }
    }

    private static CommandNode GetCastNode(SpellCommand data)
    {
        CommandNode CastNode = new KeywordNode(KEYWORD_CAST, null, null);

        for (GameSpellDefinition Definition : data._spells.GetSpells())
        {
            CastNode.AddSubNode(GetNodeForSpell(data, Definition));
        }

        return CastNode;
    }


    // Private methods.
    private boolean IsGameStateValid(CommandData data)
    {
        if (_playerStateController.GetActiveGameInstance().GetState() != MoleHuntGameState.InGame)
        {
            data.SetFeedback(Component.text("Cannot use /spell while not in-game."));
            return false;
        }
        return true;
    }

    private GameSpellArguments GetArguments(CommandData data)
    {
        List<Player> SelectedPlayers = data.GetParsedData(KEY_SPELL_REQUIREMENT_PLAYER);

        IServerPlayer TargetPlayer;
        if ((SelectedPlayers != null) && !SelectedPlayers.isEmpty())
        {
            TargetPlayer = _serverPlayers.GetPlayer(SelectedPlayers.getFirst());
        }
        else
        {
            TargetPlayer = null;
        }

        return new GameSpellArguments(_serverPlayers.GetPlayer(data.GetPlayerSender()), TargetPlayer);
    }

    private void CastSpell(CommandData data)
    {
        if (!IsGameStateValid(data))
        {
            return;
        }

        if (data.GetPlayerSender() == null)
        {
            data.SetStatus(CommandStatus.Unsuccessful);
            data.SetFeedback("Caster of spell must be a player");
            return;
        }

        String SpellName = data.GetParsedData(KEY_SPELL_NAME);
        GameSpellDefinition Definition = _spells.GetSpell(SpellName);

        if (Definition == null)
        {
            data.SetFeedback(Component.text("That spell doesn't exist").color(NamedTextColor.RED));
            return;
        }

        GameSpellArguments Args = GetArguments(data);
        _playerStateController.GetActiveGameInstance().CastSpell(Definition, Args);
    }

    private void GetHelp(CommandData data)
    {
        if (!IsGameStateValid(data))
        {
            return;
        }

        data.SetFeedback(Component.text("Use /spell cast to cast a spell." +
                "\nUse /spell help <spell_name> to get a description of a spell.\nSpells have a cast cooldown.")
                .color(NamedTextColor.GREEN));
    }

    private void GetHelpForSpell(CommandData data)
    {
        if (!IsGameStateValid(data))
        {
            return;
        }

        String SpellName = data.GetParsedData(KEY_SPELL_NAME);
        GameSpellDefinition Definition = _spells.GetSpell(SpellName);

        if (Definition == null)
        {
            data.SetFeedback(Component.text("That spell doesn't exist").color(NamedTextColor.RED));
            return;
        }

        Double RelativeCost = Definition.GetRelativeManaCost();
        String ManaCostText = RelativeCost == null ? "variable" : "%s".formatted(
                MoleHuntPlugin.GetNumberFormat("0.00").format(RelativeCost
                        * _playerStateController.GetActiveGameInstance().GetMaxMana()));


        data.SetFeedback(Component.text("Spell description: %s\nMana cost: %s".formatted(
                Definition.GetDescription(), ManaCostText, ManaCostText))
                .color(NamedTextColor.WHITE));
    }
}