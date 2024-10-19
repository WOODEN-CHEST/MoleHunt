package sus.keiger.molehunt.game.spell;

import sus.keiger.molehunt.game.MoleHuntSettings;
import sus.keiger.molehunt.game.player.GamePlayerCollection;
import sus.keiger.molehunt.player.IServerPlayerCollection;
import sus.keiger.plugincommon.packet.PCGamePacketController;

import java.util.Objects;

public class SpellServiceProvider
{
    // Private fields.
    private final IServerPlayerCollection _serverPlayerCollection;
    private final GamePlayerCollection _gamePlayerCollection;
    private final PCGamePacketController _packetController;
    private final MoleHuntSettings _settings;


    // Constructors.
    public SpellServiceProvider(IServerPlayerCollection serverPlayerCollection,
                                GamePlayerCollection gamePlayerCollection,
                                PCGamePacketController packetController,
                                MoleHuntSettings settings)
    {

        _serverPlayerCollection = Objects.requireNonNull(serverPlayerCollection, "serverPlayerCollection is null");
        _gamePlayerCollection =Objects.requireNonNull(gamePlayerCollection, "gamePlayerCollection is null");
        _packetController = Objects.requireNonNull(packetController, "packetController is null");
        _settings = Objects.requireNonNull(settings, "settings is null");
    }


    // Methods.
    public IServerPlayerCollection GetServerPlayers()
    {
        return _serverPlayerCollection;
    }

    public GamePlayerCollection GetGamePlayers()
    {
        return _gamePlayerCollection;
    }

    public PCGamePacketController GetPacketController()
    {
        return _packetController;
    }

    public MoleHuntSettings GetSettings()
    {
        return _settings;
    }
}