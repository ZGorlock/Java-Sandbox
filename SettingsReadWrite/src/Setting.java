/*
 * File:    Setting.java
 * Package: dla.resource.setting
 * Author:  Zachary Gill
 */


import java.io.*;
import java.lang.instrument.IllegalClassFormatException;

/**
 * Stores a setting object.
 */
public class Setting
{
    
    //Logger
    
    //Enums
    
    /**
     * An enumeration of Setting value types.
     */
    public enum Type
    {
        BOOLEAN,
        BYTE,
        SHORT,
        INTEGER,
        LONG,
        FLOAT,
        DOUBLE,
        CHARACTER,
        STRING,
        OBJECT
    }
    
    
    //Fields
    
    /**
     * The name of the setting.
     */
    private String name = "";
    
    /**
     * The type of the setting value.
     */
    private Type type = null;
    
    /**
     * The class type of the setting value.
     */
    private Class classType = null;
    
    /**
     * The value of the setting.
     */
    private Object value = null;
    
    /**
     * The value string of the setting.
     */
    private String stringValue = "";
    
    
    //Constructor
    
    /**
     * Constructor for a Setting.
     *
     * @param name  The name of the setting.
     * @param value The value of the setting.
     */
    public Setting(String name, Object value)
    {
        this.name = name;
        
        type = determineType(value);
        classType = determineClassType(type);
        
        this.value = value;
        stringValue = determineStringValue(this.value, type);
    }
    
    /**
     * Constructor for a Setting.
     *
     * @param name  The name of the setting.
     * @param type  The type of the setting as a Setting.Type name.
     * @param value The value of the setting as a string.
     */
    public Setting(String name, String type, Object value)
    {
        this.name = name;
        
        this.type = determineType(type);
        classType = determineClassType(this.type);
        
        try {
            this.value = determineValue(value.toString(), this.type);
        } catch (IllegalClassFormatException ignored) {
            this.value = null;
        }
        stringValue = determineStringValue(this.value, Type.valueOf(type));
    }
    
    
    //Getters
    
    /**
     * Returns the name of the setting.
     *
     * @return The name of the setting.
     */
    public String getName()
    {
        return name;
    }
    
    /**
     * Returns the type of the setting value.
     *
     * @return The type of the setting value.
     */
    public Type getType()
    {
        return type;
    }
    
    /**
     * Returns the type class of the setting value.
     *
     * @return The type class of the setting value.
     */
    public Class getClassType()
    {
        return classType;
    }
    
    /**
     * Returns the value of the setting.
     *
     * @return The value of the setting.
     */
    public Object getValue()
    {
        return value;
    }
    
    /**
     * Returns the boolean value of the setting.
     *
     * @return The boolean value of the setting.
     *
     * @throws IllegalClassException When the setting is not of type BOOLEAN.
     */
    public boolean getBooleanValue() throws IllegalClassException
    {
        if (type == Type.BOOLEAN) {
            return (Boolean) value;
        }
        
        throw new IllegalClassException("Setting value not of type BOOLEAN.");
    }
    
    /**
     * Returns the byte value of the setting.
     *
     * @return The byte value of the setting.
     *
     * @throws IllegalClassException When the setting is not of type BYTE.
     */
    public byte getByteValue() throws IllegalClassException
    {
        if (type == Type.BYTE) {
            return (Byte) value;
        }
        
        throw new IllegalClassException("Setting value not of type BYTE.");
    }
    
    /**
     * Returns the short value of the setting.
     *
     * @return The short value of the setting.
     *
     * @throws IllegalClassException When the setting is not of type SHORT.
     */
    public short getShortValue() throws IllegalClassException
    {
        if (type == Type.SHORT) {
            return (Short) value;
        }
        
        throw new IllegalClassException("Setting value not of type SHORT.");
    }
    
    /**
     * Returns the integer value of the setting.
     *
     * @return The integer value of the setting.
     *
     * @throws IllegalClassException When the setting is not of type INTEGER.
     */
    public int getIntegerValue() throws IllegalClassException
    {
        if (type == Type.INTEGER) {
            return (Integer) value;
        }
        
        throw new IllegalClassException("Setting value not of type INTEGER.");
    }
    
    /**
     * Returns the long value of the setting.
     *
     * @return The long value of the setting.
     *
     * @throws IllegalClassException When the setting is not of type LONG.
     */
    public long getLongValue() throws IllegalClassException
    {
        if (type == Type.LONG) {
            return (Long) value;
        }
        
        throw new IllegalClassException("Setting value not of type LONG.");
    }
    
    /**
     * Returns the float value of the setting.
     *
     * @return The float value of the setting.
     *
     * @throws IllegalClassException When the setting is not of type FLOAT.
     */
    public float getFloatValue() throws IllegalClassException
    {
        if (type == Type.FLOAT) {
            return (Float) value;
        }
        
        throw new IllegalClassException("Setting value not of type FLOAT.");
    }
    
    /**
     * Returns the double value of the setting.
     *
     * @return The double value of the setting.
     *
     * @throws IllegalClassException When the setting is not of type DOUBLE.
     */
    public double getDoubleValue() throws IllegalClassException
    {
        if (type == Type.DOUBLE) {
            return (Double) value;
        }
        
        throw new IllegalClassException("Setting value not of type DOUBLE.");
    }
    
    /**
     * Returns the character value of the setting.
     *
     * @return The character value of the setting.
     *
     * @throws IllegalClassException When the setting is not of type CHARACTER.
     */
    public char getCharacterValue() throws IllegalClassException
    {
        if (type == Type.CHARACTER) {
            return (Character) value;
        }
        
        throw new IllegalClassException("Setting value not of type CHARACTER.");
    }
    
    /**
     * Returns the string value of the setting.
     *
     * @return The string value of the setting.
     */
    public String getStringValue() throws IllegalClassException
    {
        return stringValue;
    }
    
    
    //Setters
    
    /**
     * Attempts to set the value of a setting.
     *
     * @param value The new value of the setting.
     * @return Whether the value was successfully set.
     */
    public boolean setValue(Object value)
    {
        if (type != determineType(value)) {
            return false;
        }
        
        this.value = value;
        stringValue = determineStringValue(this.value, type);
        return true;
    }
    
    /**
     * Attempts to set the value of a setting with a string.
     *
     * @param value The new string value of the setting.
     * @return Whether the value was successfully set.
     */
    public boolean setValueByString(String value)
    {
        try {
            this.value = determineValue(value, type);
            if (type != determineType(this.value)) {
                return false;
            }
        } catch (IllegalClassFormatException e) {
            return false;
        }
        
        stringValue = value;
        return true;
    }
    
    
    //Functions
    
    /**
     * Determines the setting type from the type string.
     *
     * @param type The type string of the setting.
     * @return The type of the setting.
     */
    private static Type determineType(String type)
    {
        try {
            return Type.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException ignored) {
            
            switch (type) {
                case "class java.lang.Boolean":
                    return Type.BOOLEAN;
                case "class java.lang.Byte":
                    return Type.BYTE;
                case "class java.lang.Short":
                    return Type.SHORT;
                case "class java.lang.Integer":
                    return Type.INTEGER;
                case "class java.lang.Long":
                    return Type.LONG;
                case "class java.lang.Float":
                    return Type.FLOAT;
                case "class java.lang.Double":
                    return Type.DOUBLE;
                case "class java.lang.Character":
                    return Type.CHARACTER;
                case "class java.lang.String":
                    return Type.STRING;
                default:
                    return Type.OBJECT;
            }
        }
    }
    
    /**
     * Determines the setting type from the value.
     *
     * @param value The value of the setting.
     * @return The type of the setting.
     */
    private static Type determineType(Object value)
    {
        return determineType(value.getClass().toString());
    }
    
    /**
     * Determines the setting type from the type class.
     *
     * @param classType The type class of the setting.
     * @return The type of the setting.
     */
    private static Type determineType(Class classType)
    {
        return determineType(classType.toString());
    }
    
    /**
     * Determines the setting type class from the type.
     *
     * @param type The type of the setting.
     * @return The type class of the setting.
     */
    private static Class determineClassType(Type type)
    {
        switch (type) {
            case BOOLEAN:
                return Boolean.class;
            case BYTE:
                return Byte.class;
            case SHORT:
                return Short.class;
            case INTEGER:
                return Integer.class;
            case LONG:
                return Long.class;
            case FLOAT:
                return Float.class;
            case DOUBLE:
                return Double.class;
            case CHARACTER:
                return Character.class;
            case STRING:
                return String.class;
            case OBJECT:
                return Object.class;
            default:
                return Object.class;
        }
    }
    
    /**
     * Determines the setting type class from the type string.
     *
     * @param type The type string of the setting.
     * @return The type class of the setting.
     */
    private static Class determineClassType(String type)
    {
        return determineClassType(determineType(type));
    }
    
    /**
     * Determines the setting type class from the value.
     *
     * @param value The value of the setting.
     * @return The type class of the setting.
     */
    private static Class determineClassType(Object value)
    {
        return determineClassType(determineType(value));
    }
    
    /**
     * Determines the setting value from the value string.
     *
     * @param value The value string of the setting.
     * @param type  The type of the setting.
     * @return The value of the setting.
     *
     * @throws IllegalClassFormatException When the value is not of the specified type.
     */
    private static Object determineValue(String value, Type type) throws IllegalClassFormatException
    {
        switch (type) {
            case BOOLEAN:
                if ("false".equals(value.toLowerCase()) || "0".equals(value)) {
                    return Boolean.FALSE;
                }
                if ("true".equals(value.toLowerCase()) || "1".equals(value)) {
                    return Boolean.TRUE;
                }
                throw new IllegalClassFormatException("Setting value not of type BOOLEAN.");
            case BYTE:
                try {
                    return Byte.valueOf(value);
                } catch (NumberFormatException ignored) {
                    throw new IllegalClassFormatException("Setting value not of type BYTE.");
                }
            case SHORT:
                try {
                    return Short.valueOf(value);
                } catch (NumberFormatException ignored) {
                    throw new IllegalClassFormatException("Setting value not of type SHORT.");
                }
            case INTEGER:
                try {
                    return Integer.valueOf(value);
                } catch (NumberFormatException ignored) {
                    throw new IllegalClassFormatException("Setting value not of type INTEGER.");
                }
            case LONG:
                try {
                    return Long.valueOf(value);
                } catch (NumberFormatException ignored) {
                    throw new IllegalClassFormatException("Setting value not of type LONG.");
                }
            case FLOAT:
                try {
                    return Float.valueOf(value);
                } catch (NumberFormatException ignored) {
                    throw new IllegalClassFormatException("Setting value not of type FLOAT.");
                }
            case DOUBLE:
                try {
                    return Double.valueOf(value);
                } catch (NumberFormatException ignored) {
                    throw new IllegalClassFormatException("Setting value not of type DOUBLE.");
                }
            case CHARACTER:
                if (value.length() == 1) {
                    return value.charAt(0);
                } else {
                    throw new IllegalClassFormatException("Setting value not of type CHARACTER.");
                }
            case STRING:
                return value;
            case OBJECT:
                try (ByteArrayInputStream in = new ByteArrayInputStream(value.getBytes());
                     ObjectInputStream is = new ObjectInputStream(in)
                ) {
                    return is.readObject();
                } catch (IOException | ClassNotFoundException ignored) {
                    throw new IllegalClassFormatException("Setting value cannot be converted to an OBJECT.");
                }
            default:
                throw new IllegalClassFormatException("Setting value type not valid.");
        }
    }
    
    /**
     * Determines the setting value string from the value.
     *
     * @param value The value of the setting.
     * @param type  The type of the setting.
     * @return The value string of the setting.
     */
    private static String determineStringValue(Object value, Type type)
    {
        switch (type) {
            case BOOLEAN:
                return (Boolean) value ? "TRUE" : "FALSE";
            case BYTE:
            case SHORT:
            case INTEGER:
            case LONG:
            case FLOAT:
            case DOUBLE:
            case CHARACTER:
            case STRING:
                return value.toString();
            case OBJECT:
                try (ByteArrayOutputStream out = new ByteArrayOutputStream();
                     ObjectOutput os = new ObjectOutputStream(out)
                ) {
                    os.writeObject(value);
                    return new String(out.toByteArray());
                } catch (IOException ignored) {
                    return "null";
                }
            default:
                return "null";
        }
    }
    
}
