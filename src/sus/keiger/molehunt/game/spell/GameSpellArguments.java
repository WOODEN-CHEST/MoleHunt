package sus.keiger.molehunt.game.spell;

import org.bukkit.Location;
import sus.keiger.molehunt.game.player.IGamePlayer;
import sus.keiger.molehunt.player.IServerPlayer;

import java.util.Objects;

public class GameSpellArguments
{
    // Private fields.
    private IServerPlayer _castingPlayer;
    private IServerPlayer _targetPlayer;
    private Location _lookingAtBlock;


    // Constructors.
    public GameSpellArguments(IServerPlayer castingPlayer, IServerPlayer targetPlayer, Location lookingAtBlock)
    {
        _castingPlayer = Objects.requireNonNull(castingPlayer, "castingPlayer is null");
        _targetPlayer = targetPlayer;
        _lookingAtBlock = lookingAtBlock;
    }


    // Methods.
    public IServerPlayer GetCastingPlayer()
    {
        return _castingPlayer;
    }

    public IServerPlayer GetTargetPlayer()
    {
        return _targetPlayer;
    }

    public Location GetLookingAtBlock()
    {
        return _lookingAtBlock;
    }
}