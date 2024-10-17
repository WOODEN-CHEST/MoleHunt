package sus.keiger.molehunt.game;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.scoreboard.*;
import sus.keiger.molehunt.player.IServerPlayer;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class GameScoreboard
{
    // Private fields.
    private final Scoreboard _scoreboard;
    private final Team _minecraftTeam;
    private final Objective _targetObjective;

    private final String OBJECTIVE_NAME = "panel";
    private final String MINECRAFT_TEAM_NAME = "mole_hunt_team";
    private final int MAX_LINE_COUNT = 16;

    private final Set<String> _currentTextEntries = new HashSet<>();




    // Constructors.
    public GameScoreboard()
    {
        _scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();

        _minecraftTeam = _scoreboard.registerNewTeam(MINECRAFT_TEAM_NAME);
        _minecraftTeam.color(NamedTextColor.WHITE);
        _minecraftTeam.prefix(null);
        _minecraftTeam.suffix(null);
        _minecraftTeam.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.ALWAYS);
        _minecraftTeam.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.FOR_OWN_TEAM);
        _minecraftTeam.setOption(Team.Option.DEATH_MESSAGE_VISIBILITY, Team.OptionStatus.NEVER);
        _minecraftTeam.setAllowFriendlyFire(true);

        _targetObjective = _scoreboard.registerNewObjective(OBJECTIVE_NAME, Criteria.DUMMY, Component.text(""));
        _targetObjective.setDisplaySlot(DisplaySlot.SIDEBAR);
    }


    // Methods.
    public void SetIsBoardEnabledForPlayer(IServerPlayer player, boolean isEnabled)
    {
        Objects.requireNonNull(player, "player is null");

        if (isEnabled)
        {
            player.GetMCPlayer().setScoreboard(_scoreboard);
        }
        else
        {
            player.GetMCPlayer().setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
        }
    }

    public void AddToTeam(IServerPlayer player)
    {
        Objects.requireNonNull(player, "player is null");
        _minecraftTeam.addEntity(player.GetMCPlayer());
    }

    public void RemoveFromTeam(IServerPlayer player)
    {
        Objects.requireNonNull(player, "player is null");
        _minecraftTeam.removeEntity(player.GetMCPlayer());
    }

    public void SetTitle(Component text)
    {
        _targetObjective.displayName(Objects.requireNonNull(text, "text is null"));
    }

    public void SetText(List<Component> lines)
    {
        for (int i = 0; i < MAX_LINE_COUNT; i++)
        {
            _targetObjective.getScore(Integer.toString(i)).resetScore();
        }

        _currentTextEntries.forEach(entry -> _targetObjective.getScore(entry).resetScore());

        for (int i = 0 ; (i < lines.size()) && (i < MAX_LINE_COUNT); i++)
        {
            Score TargetScore = _targetObjective.getScore(Integer.toString(i));
            TargetScore.setScore(lines.size() - i - 1);
            TargetScore.customName(lines.get(i));
        }
    }
}