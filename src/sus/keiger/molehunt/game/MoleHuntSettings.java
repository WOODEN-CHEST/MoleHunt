package sus.keiger.molehunt.game;

import sus.keiger.plugincommon.PCMath;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public class MoleHuntSettings
{
    // Private fields.
    /* All settings initialized to a default value. */
    @IntGameProperty(Name = "GameTimeTicks", MinValue =1, MaxValue = Integer.MAX_VALUE)
    private int _gameTimeTicks = PCMath.SecondsToTicks(60d * 45d);

    @IntGameProperty(Name = "GracePeriodTicks", MinValue =1, MaxValue = Integer.MAX_VALUE)
    private int _gracePeriodTimeTicks = PCMath.SecondsToTicks(60d * 5d);

    @IntGameProperty(Name = "MoleCountMin", MinValue = 1, MaxValue = Integer.MAX_VALUE)
    private int _moleCountMin = 1;

    @IntGameProperty(Name = "MoleCountMax", MinValue = 1, MaxValue = Integer.MAX_VALUE)
    private int _moleCountMax = 1;

    @BooleanGameProperty(Name = "DoesBorderShrink")
    private boolean _doesBorderShrink = true;

    @IntGameProperty(Name = "BorderSizeStart", MinValue = 1, MaxValue = Integer.MAX_VALUE)
    private int _worldBorderSizeStartBlocks = 400;

    @IntGameProperty(Name = "BorderSizeEnd", MinValue = 1, MaxValue = Integer.MAX_VALUE)
    private int _worldBorderSizeEndBlocks = 50;

    @IntGameProperty(Name = "ShrinkStartTimeTicks", MinValue = 1, MaxValue = Integer.MAX_VALUE)
    private int _borderShrinkStartTimeTicks = PCMath.SecondsToTicks(60d * 10d);

    @IntGameProperty(Name = "SpellCooldownTicks", MinValue = 1, MaxValue = Integer.MAX_VALUE)
    private int _spellCastCooldownTicks = PCMath.SecondsToTicks(15d);

    @BooleanGameProperty(Name = "CanDeadCastSpells")
    private boolean _canDeadCastSpells = true;

    @BooleanGameProperty(Name = "CanAliveCastSpells")
    private boolean _canAliveCastSpells = false;

    @BooleanGameProperty(Name = "IsNotifiedOnSpellCast")
    private boolean _isNotifiedOnSpellCast = true;

    @FloatGameProperty(Name = "ManaRegenerationScale", MinValue = 0d, MaxValue = 1000d)
    private double _manaRegenerationScale = 1d;

    @BooleanGameProperty(Name = "ArePortalsAllowed")
    private boolean _arePortalsAllowed = false;

    @FloatGameProperty(Name = "PlayerHealth", MinValue = 0.0001d, MaxValue = 1000d)
    private double _playerHealthHalfHearts = 20d;

    @StringGameProperty(Name = "SkinPlayerName")
    private String _skinPlayerName = "Default";


    // Constructors.
    public MoleHuntSettings() { }


    // Private methods.
    private void SetField(Field field, Object value) throws IllegalAccessException
    {
        if (field.getType().equals(int.class))
        {
            int MinValue = field.getAnnotationsByType(IntGameProperty.class)[0].MinValue();
            int MaxValue = field.getAnnotationsByType(IntGameProperty.class)[0].MaxValue();
            field.set(this, Math.max(MinValue, Math.min((Integer)value, MaxValue)));
        }
        else if (field.getType().equals(double.class))
        {
            double MinValue = field.getAnnotationsByType(FloatGameProperty.class)[0].MinValue();
            double MaxValue = field.getAnnotationsByType(FloatGameProperty.class)[0].MaxValue();
            field.set(this, Math.max(MinValue, Math.min((Double)value, MaxValue)));
        }
        else
        {
            field.set(this, value);
        }
    }


    // Methods.
    public int GetGameTimeTicks()
    {
        return _gameTimeTicks;
    }
    public int GetGracePeriodTimeTicks()
    {
        return _gracePeriodTimeTicks;
    }

    public int GetMoleCountMin()
    {
        return _moleCountMin;
    }

    public int GetMoleCountMax()
    {
        return _moleCountMax;
    }
    public int GetBorderSizeStartBlocks()
    {
        return _worldBorderSizeStartBlocks;
    }

    public int GetBorderSizeEndBlocks()
    {
        return _worldBorderSizeEndBlocks;
    }

    public int GetBorderShrinkStartTimeTicks()
    {
        return _borderShrinkStartTimeTicks;
    }

    public boolean GetDoesBorderShrink()
    {
        return _doesBorderShrink;
    }

    public int GetSpellCastCooldownTicks()
    {
        return _spellCastCooldownTicks;
    }

    public boolean GetCanAliveCastSpells()
    {
        return _canAliveCastSpells;
    }

    public boolean GetCanDeadCastSpells()
    {
        return _canDeadCastSpells;
    }

    public boolean GetIsNotifiedOnSpellCast()
    {
        return _isNotifiedOnSpellCast;
    }

    public double GetPlayerHealthHalfHearts()
    {
        return _playerHealthHalfHearts;
    }

    public boolean GetArePortalsAllowed()
    {
        return _arePortalsAllowed;
    }

    public double GetManaRegenerationScale()
    {
        return _manaRegenerationScale;
    }

    public String GetSkinPlayerName()
    {
        return _skinPlayerName;
    }

    public boolean SetValue(String fieldName, Object value)
    {
        Objects.requireNonNull(fieldName, "fieldName is null");

        Field TargetField = GetProperties().stream().filter(
                field -> field.getName().equals(fieldName)).findFirst()
                .orElse(null);

        if (TargetField == null)
        {
            return false;
        }
        return SetValue(TargetField, value);
    }

    public boolean SetValue(Field field, Object value)
    {
        Objects.requireNonNull(field, "field is null");
        if (!field.getDeclaringClass().equals(this.getClass()))
        {
            return false;
        }

        try
        {
            SetField(field, value);
            return true;
        }
        catch (IllegalAccessException e)
        {
            return false;
        }
    }

    public List<Field> GetProperties()
    {
        return Arrays.stream(MoleHuntSettings.class.getDeclaredFields()).filter(
                property -> Stream.of(IntGameProperty.class, FloatGameProperty.class,
                                BooleanGameProperty.class, StringGameProperty.class)
                        .anyMatch(type -> property.getAnnotation(type) != null)).toList();
    }

    public String GetPropertyName(Field field)
    {
        IntGameProperty IntProperty = field.getAnnotation(IntGameProperty.class);
        if (IntProperty != null)
        {
            return IntProperty.Name();
        }

        FloatGameProperty FloatProperty = field.getAnnotation(FloatGameProperty.class);
        if (FloatProperty != null)
        {
            return FloatProperty.Name();
        }

        BooleanGameProperty BooleanProperty = field.getAnnotation(BooleanGameProperty.class);
        if (BooleanProperty != null)
        {
            return BooleanProperty.Name();
        }

        StringGameProperty StringProperty = field.getAnnotation(StringGameProperty.class);
        if (StringProperty != null)
        {
            return StringProperty.Name();
        }
        return null;
    }
}