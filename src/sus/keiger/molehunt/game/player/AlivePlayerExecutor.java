package sus.keiger.molehunt.game.player;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.util.Vector;
import sus.keiger.molehunt.player.IServerPlayerCollection;
import sus.keiger.plugincommon.entity.EntityFunctions;
import sus.keiger.plugincommon.player.PlayerFunctions;

public class AlivePlayerExecutor extends PlayerExecutorBase
{
    // Constructors.
    public AlivePlayerExecutor(IGamePlayer gamePlayer, IServerPlayerCollection playerCollection)
    {
        super(gamePlayer, playerCollection);
    }


    // Private methods.
    private void ResetPlayer()
    {
        Player MCPlayer = GetPlayer().GetMCPlayer();
        PlayerFunctions.ClearInventory(MCPlayer);

        MCPlayer.setGameMode(GameMode.SURVIVAL);
        MCPlayer.setInvulnerable(true);
        MCPlayer.setInvisible(false);
        MCPlayer.clearActivePotionEffects();
        PlayerFunctions.ResetAttributes(MCPlayer);
        MCPlayer.setFreezeTicks(0);
        MCPlayer.setFireTicks(0);
        MCPlayer.setVisualFire(false);
        MCPlayer.setLevel(0);
        MCPlayer.setExp(0);
        MCPlayer.setVelocity(new Vector());
        MCPlayer.setGlowing(false);
        MCPlayer.setFoodLevel(PlayerFunctions.MAX_FOOD);
        EntityFunctions.SetHealthPortion(MCPlayer, 1d);
        MCPlayer.setAllowFlight(false);
        MCPlayer.setFallDistance(0f);
        MCPlayer.setRemainingAir(MCPlayer.getMaximumAir());
    }

    private void SwitchToPreGameState()
    {
        ResetPlayer();
    }

    private void SwitchToInGameState()
    {

    }

    private void SwitchToPostGameState()
    {

    }



    // Inherited methods.
    @Override
    public void SwitchState(GamePlayerState state)
    {
        switch (state)
        {
            case PreGame -> SwitchToPreGameState();
            case InGame -> SwitchToInGameState();
            case PostGame -> SwitchToPostGameState();
        }
    }

    @Override
    public void OnPlayerDeathEvent(PlayerDeathEvent event)
    {

    }

    @Override
    public void OnEntityDamageEvent(EntityDamageEvent event)
    {

    }

    @Override
    public void Tick()
    {

    }
}