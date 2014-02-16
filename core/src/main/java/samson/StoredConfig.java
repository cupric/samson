//
// SamsoN - utilities for playn clients and servers
// Copyright (c) 2014, Cupric - All rights reserved.
// http://github.com/cupric/samson/blob/master/LICENSE

package samson;

import playn.core.PlayN;
import react.Connection;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

/**
 * Extends the base config using {@link PlayN#storage()} to allow the values to be changed and
 * persisted in local client storage.
 *
 * <p>Typical usage is to create a static instance of {@code StorageConfig} and a static
 * {@code Value} for each entry to be exposed. For example:
 *
 * <pre>
 * public class FooPrefs
 * {
 *     public static StoredConfig config = new StoredConfig("foo");
 *     public static StoredConfig.Value<String> bar = config.create("bar", Config.STRING, "");
 * }
 * </pre>
 * Client code may then use the {@code FooPrefs.bar} value just as it would any other value. For
 * example, an interface panel may set a field using the config value and later update it if a
 * successful condition, such as an ok button press, is encountered:
 * <pre>
 * public class BazGroup extends Group
 * {
 *     Field bar;
 *     Connection conn;
 *     public BazGroup (...) {
 *         //...
 *         bar = new Field();
 *         bar.text.update(FooPrefs.bar.get());
 *     }
 *     void onSuccess () {
 *         FooPrefs.bar.update(bar.text.get());
 *     }
 * }
 * </pre>
 * <p>NOTE: Beware of memory leaks when connecting to global Value instances. Either use
 * {@link Connection#holdWeakly()}, or bind the lifetime of the connection to an {@code Element}
 * instance.
 */
public class StoredConfig extends Config
{
    public class Value<T> extends react.Value<T>
    {
        public final String name;
        public final ValueType<T> vtype;
        public final T defaultVal;

        /**
         * Removes this value from the storage. In turn, causes the react value to be updated
         * to the original default value.
         */
        public void delete () {
            PlayN.storage().removeItem(_prefix + name);
            reread();
        }

        @Override
        protected T updateLocal (T value) {
            T oval = super.updateLocal(value);
            setValue(vtype, name, value, false);
            return oval;
        }

        private Value (String name, ValueType<T> vtype, T defaultVal) {
            super(getValue(vtype, name, defaultVal));
            this.defaultVal = defaultVal;
            this.vtype = vtype;
            this.name = name;
        }

        private void reread () {
            update(getValue(vtype, name, defaultVal));
        }
    }

    /**
     * Creates a new preferences config.
     */
    public StoredConfig ()
    {
        _prefix = "";
    }

    /**
     * Creates a new preferences config with the given prefix. All values read or written will
     * begin with the prefix and a dot, effectively scoping all values. Typically a prefix
     * corresponds to a login name, or some subset of the app.
     */
    public StoredConfig (String prefix)
    {
        _prefix = prefix + ".";
    }

    public <T> Value<T> create (String name, ValueType<T> vtype, T defaultVal)
    {
        Value<T> value = new Value<T>(name, vtype, defaultVal);
        _values.put(name, value);
        return value;
    }

    @Override
    public String getEntry (String key)
    {
        return PlayN.storage().getItem(_prefix + key);
    }

    public <T> void setValue (ValueType<T> vtype, String name, T val)
    {
        setValue(vtype, name, val, true);
    }

    private void setEntry (String key, String val)
    {
        PlayN.storage().setItem(_prefix + key, val);
    }

    private <T> void setValue (ValueType<T> vtype, String name, T val, boolean fire)
    {
        setEntry(name, vtype.toString(val));
        if (fire) {
            for (Value<?> value : _values.get(name)) {
                value.reread();
            }
        }
    }

    private final String _prefix;
    private Multimap<String, Value<?>> _values = ArrayListMultimap.create();
}
