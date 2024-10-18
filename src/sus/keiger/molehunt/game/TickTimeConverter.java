package sus.keiger.molehunt.game;

import sus.keiger.plugincommon.PCMath;

public class TickTimeConverter
{
    // Methods.
    public String GetTimeString(int ticks)
    {
        int Seconds = ticks / PCMath.TICKS_IN_SECOND % 60;
        int Minutes = ticks / PCMath.TICKS_IN_SECOND / 60 % 60;
        int Hours = ticks / PCMath.TICKS_IN_SECOND / 60 / 60;

        final char MiddleSymbol = ':';
        StringBuilder Builder = new StringBuilder();
        Builder.append(Hours);
        Builder.append(MiddleSymbol);

        if (Minutes < 10)
        {
            Builder.append('0');
        }
        Builder.append(Minutes);

        Builder.append(MiddleSymbol);
        if (Seconds < 10)
        {
            Builder.append('0');
        }
        Builder.append(Seconds);

        return Builder.toString();
    }
}