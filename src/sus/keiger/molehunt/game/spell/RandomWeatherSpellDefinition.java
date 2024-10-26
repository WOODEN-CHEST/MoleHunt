package sus.keiger.molehunt.game.spell;

import de.maxhenkel.voicechat.api.events.MicrophonePacketEvent;
import org.bukkit.WeatherType;
import sus.keiger.molehunt.game.IGameServices;
import sus.keiger.molehunt.player.IServerPlayer;
import sus.keiger.plugincommon.PCMath;

import java.util.Random;

public class RandomWeatherSpellDefinition extends GameSpellDefinition
{
    // Constructors.
    public RandomWeatherSpellDefinition()
    {
        super("RandomWeather", "Randomizes the world's weather.",
                SpellType.Instant, 0.3d);
    }


    // Inherited methods.
    @Override
    public GameSpell CreateSpell(GameSpellArguments args, IGameServices services)
    {
        return new RandomWeatherSpell(this, args, services);
    }


    // Types.
    private static class RandomWeatherSpell extends GameSpell
    {
        // Constructors.
        public RandomWeatherSpell(GameSpellDefinition definition,
                              GameSpellArguments arguments,
                              IGameServices services)
        {
            super(definition, arguments, services);
        }


        // Inherited methods.
        @Override
        public void OnAdd()
        {
            GameWeatherType[] Types = GameWeatherType.values();
            GameWeatherType SelectedType = Types[new Random().nextInt(Types.length)];

            switch (SelectedType)
            {
                case Clear ->
                {
                    GetServices().GetLocationProvider().GetOverworld().setStorm(false);
                    GetServices().GetLocationProvider().GetOverworld().setThundering(false);
                }
                case Rain ->
                {
                    GetServices().GetLocationProvider().GetOverworld().setStorm(true);
                    GetServices().GetLocationProvider().GetOverworld().setThundering(false);
                }
                case Thunder ->
                {
                    GetServices().GetLocationProvider().GetOverworld().setStorm(true);
                    GetServices().GetLocationProvider().GetOverworld().setThundering(true);
                }
            }
        }

        @Override
        public void Execute() { }

        @Override
        public void OnRemove() { }
    }

    private enum GameWeatherType
    {
        Clear,
        Rain,
        Thunder
    }
}
