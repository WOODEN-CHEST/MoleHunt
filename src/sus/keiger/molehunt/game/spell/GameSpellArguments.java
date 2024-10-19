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


    // Constructors.
    public GameSpellArguments(IServerPlayer castingPlayer, IServerPlayer targetPlayer)
    {
        _castingPlayer = Objects.requireNonNull(castingPlayer, "castingPlayer is null");
        _targetPlayer = targetPlayer;
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
}