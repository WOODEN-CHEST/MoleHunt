package sus.keiger.molehunt.game;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import sus.keiger.molehunt.command.SpellCommand;
import sus.keiger.molehunt.event.IEventDispatcher;
import sus.keiger.molehunt.event.IMoleHuntEventListener;
import sus.keiger.molehunt.game.player.GamePlayerCollection;
import sus.keiger.molehunt.game.player.IGamePlayer;
import sus.keiger.molehunt.player.IServerPlayerCollection;

import java.util.Objects;

public class GameCommandInterceptor implements IMoleHuntEventListener
{
    // Private fields.
    private final IGameServices _gameServices;



    // Constructors.
    public GameCommandInterceptor(IGameServices gameServices)
    {
        _gameServices = Objects.requireNonNull(gameServices, "gameServices is null");
    }


    // Private methods.
    private boolean IsCommandAllowed(String command)
    {
        int FirstWhiteSpaceIndex = command.indexOf(' ');
        if (FirstWhiteSpaceIndex == -1)
        {
            return command.equals("/" + SpellCommand.LABEL);
        }
        return command.startsWith("/" + SpellCommand.LABEL + " ");
    }

    private void OnCommandPreProcessEvent(PlayerCommandPreprocessEvent event)
    {
        IGamePlayer Sender = _gameServices.GetGamePlayerCollection().GetGamePlayer(
                _gameServices.GetServerPlayerCollection().GetPlayer(event.getPlayer()));
        if (Sender == null)
        {
            return;
        }

        if (!Sender.GetServerPlayer().IsAdmin() && !IsCommandAllowed(event.getMessage()))
        {
            event.setCancelled(true);
            Sender.SendMessage(Component.text("Cannot send commands during MoleHunt.")
                    .color(NamedTextColor.RED));
        }
    }


    // Inherited methods.
    @Override
    public void SubscribeToEvents(IEventDispatcher dispatcher)
    {
        dispatcher.GetPlayerCommandPreprocessEvent().Subscribe(this, this::OnCommandPreProcessEvent);
    }

    @Override
    public void UnsubscribeFromEvents(IEventDispatcher dispatcher)
    {
        dispatcher.GetPlayerCommandPreprocessEvent().Unsubscribe(this);
    }

}