package sus.keiger.molehunt.game;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import sus.keiger.molehunt.event.IEventDispatcher;
import sus.keiger.molehunt.event.IMoleHuntEventListener;
import sus.keiger.molehunt.game.player.GamePlayerCollection;
import sus.keiger.molehunt.game.player.IGamePlayer;
import sus.keiger.molehunt.player.IServerPlayer;
import sus.keiger.molehunt.player.IServerPlayerCollection;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public class GameChatInterceptor implements IMoleHuntEventListener, IGameStateContaining
{
    // Private fields.
    private final IGameServices _gameServices;
    private MoleHuntGameState _state = MoleHuntGameState.Initializing;


    // Constructors.
    public GameChatInterceptor(IGameServices gameServices)
    {
        _gameServices = Objects.requireNonNull(gameServices, "gameServices is null");
    }


    // Private methods.
    private void OnAsyncChatEvent(AsyncChatEvent event)
    {
        IServerPlayer PlayerWhoChatted = _gameServices.GetServerPlayerCollection().GetPlayer(event.getPlayer());
        IGamePlayer GamePlayer = _gameServices.GetGamePlayerCollection().GetGamePlayer(PlayerWhoChatted);

        event.setCancelled(true);
        if (GamePlayer != null)
        {
            if (GamePlayer.IsAlive())
            {
                OnAlivePlayerChatEvent(GamePlayer);
            }
            else
            {
                OnDeadPlayerChatEvent(GamePlayer, event.originalMessage());
            }
        }
        else if (_gameServices.GetGamePlayerCollection().ContainsSpectator(PlayerWhoChatted))
        {
            OnSpectatorChatEvent(PlayerWhoChatted, event.originalMessage());

        }
        else
        {
            event.setCancelled(false);
        }
    }

    private void OnAlivePlayerChatEvent(IGamePlayer player)
    {
        player.SendMessage(Component.text("You may not chat.").color(NamedTextColor.RED));
    }

    private void OnDeadPlayerChatEvent(IGamePlayer player, Component message)
    {
        Component Message = Component.text("[Dead]<%s> %s".formatted(player.GetServerPlayer().GetName(),
                PlainTextComponentSerializer.plainText().serialize(message))).color(NamedTextColor.GRAY);

        GetSpectatorTargets().forEach(target -> target.SendMessage(Message));
    }

    private void OnSpectatorChatEvent(IServerPlayer player, Component message)
    {
        Component Message = Component.text("[Spectator]<%s> %s".formatted(player.GetName(),
                PlainTextComponentSerializer.plainText().serialize(message))).color(NamedTextColor.GRAY);

        GetSpectatorTargets().forEach(target -> target.SendMessage(Message));
    }

    private List<IServerPlayer> GetSpectatorTargets()
    {
        return Stream.concat(_gameServices.GetGamePlayerCollection().GetSpectators().stream(),
                _gameServices.GetGamePlayerCollection().GetActivePlayers().stream()
                        .filter(player -> !player.IsAlive()).map(IGamePlayer::GetServerPlayer)).toList();
    }


    // Inherited methods.
    @Override
    public void SubscribeToEvents(IEventDispatcher dispatcher)
    {
        dispatcher.GetAsyncChatEvent().Subscribe(this, this::OnAsyncChatEvent);
    }

    @Override
    public void UnsubscribeFromEvents(IEventDispatcher dispatcher)
    {
        dispatcher.GetAsyncChatEvent().Unsubscribe(this);
    }

    @Override
    public void SetState(MoleHuntGameState state)
    {
        _state = Objects.requireNonNull(state, "state is null");
    }
}