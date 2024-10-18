package sus.keiger.molehunt.game;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface FloatGameProperty
{
    String Name();
    double MinValue();
    double MaxValue();
}