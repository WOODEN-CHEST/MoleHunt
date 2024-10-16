package sus.keiger.molehunt.game.spell;

import org.bukkit.Location;
import sus.keiger.molehunt.game.player.IGamePlayer;

import java.util.Objects;

public class GameSpellArguments
{
    // Private fields.
    private IGamePlayer _castingPlayer;
    private IGamePlayer _targetPlayer;
    private Location _lookingAtBlock;


    // Constructors.
    public GameSpellArguments(IGamePlayer castingPlayer, IGamePlayer targetPlayer, Location lookingAtBlock)
    {
        _castingPlayer = Objects.requireNonNull(castingPlayer, "castingPlayer is null");
        _targetPlayer = targetPlayer;
        _lookingAtBlock = lookingAtBlock;
    }


    // Methods.
    public IGamePlayer GetCastingPlayer()
    {
        return _castingPlayer;
    }

    public IGamePlayer GetTargetPlayer()
    {
        return _targetPlayer;
    }

    public Location GetLookingAtBlock()
    {
        return _lookingAtBlock;
    }
}