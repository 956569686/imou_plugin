package xl.honggv.cameraimouplugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ConstraintMap {

    private final Map<String, Object> mMap;

    public ConstraintMap() {
        mMap = new HashMap<String, Object>();
    }

    public ConstraintMap(Map<String, Object> map) {
        this.mMap = map;
    }

    public Map<String, Object> toMap() {
        return mMap;
    }

    public boolean hasKey(String name) {
        return this.mMap.containsKey(name);
    }

    public boolean isNull(String name) {
        return mMap.get(name) == null;
    }

    public boolean getBoolean(String name) {
        return (boolean) mMap.get(name);
    }

    public double getDouble(String name) {
        return (double) mMap.get(name);
    }

    public int getInt(String name) {
        return (int) mMap.get(name);
    }

    public String getString(String name) {
        return (String) mMap.get(name);
    }

    public ConstraintMap getMap(String name) {
        return new ConstraintMap((Map<String, Object>) mMap.get(name));
    }

    public ObjectType getType(String name) {
        Object value = mMap.get(name);
        if (value == null) {
            return ObjectType.Null;
        } else if (value instanceof Number) {
            return ObjectType.Number;
        } else if (value instanceof String) {
            return ObjectType.String;
        } else if (value instanceof Boolean) {
            return ObjectType.Boolean;
        } else if (value instanceof Map) {
            return ObjectType.Map;
        } else if (value instanceof ArrayList) {
            return ObjectType.Array;
        } else {
            throw new IllegalArgumentException("Invalid value" + value.toString() + " for key " + name + "contained in ConstraintMap");
        }
    }

    public void putBoolean(String key, boolean value) {
        mMap.put(key, value);
    }

    public void putDouble(String key, double value) {
        mMap.put(key, value);
    }

    public void putInt(String key, int value) {
        mMap.put(key, value);
    }

    public void putString(String key, String value) {
        mMap.put(key, value);
    }

    public void putNull(String key) {
        mMap.put(key, null);
    }

}
