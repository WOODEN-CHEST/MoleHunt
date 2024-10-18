package sus.keiger.molehunt.game;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface IntGameProperty
{
    String Name();
    int MinValue();
    int MaxValue();
}