//
// SamsoN - utilities for playn clients and servers
// Copyright (c) 2014, Cupric - All rights reserved.
// http://github.com/cupric/samson/blob/master/LICENSE

package samson.text;

import java.util.Collection;
import java.util.Enumeration;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;

import react.Function;

import tripleplay.util.Randoms;

import static samson.Log.log;

/**
 * A message bundle provides an easy mechanism by which to obtain translated message strings from
 * a resource bundle. It substitutes arguments into the translation strings using
 * {@link MessageFormat}.
 */
public class MessageBundle
{
    /** Translates the message from the supplied error. */
    public final Function<Throwable, String> mapFailure = new Function<Throwable, String>() {
        @Override public String apply (Throwable error) {
            // TODO: check whether this is NexusException?
            return xlate(error.getMessage());
        }
    };

    /**
     * Initializes the message bundle which will obtain localized messages from the supplied
     * resource bundle. The path is provided purely for reporting purposes.
     */
    public void init (MessageManager msgmgr, String path, ResourceBundle bundle,
            MessageBundle parent) {
        _msgmgr = msgmgr;
        _path = path;
        _bundle = bundle;
        _parent = parent;
    }

    /**
     * Obtains the translation for the specified message key. No arguments are substituted into
     * the translated string. If a translation message does not exist for the specified key, an
     * error is logged and the key itself is returned so that the caller need not worry about
     * handling a null response.
     */
    public String get (String key) {
        // if this string is tainted, we don't translate it, instead we
        // simply remove the taint character and return it to the caller
        if (Messages.isTainted(key)) {
            return Messages.untaint(key);
        }

        String msg = getResourceString(key);
        return (msg != null) ? msg : key;
    }

    /**
     * Adds all messages whose key starts with the specified prefix to the supplied collection.
     *
     * @param includeParent if true, messages from our parent bundle (and its parent bundle, all
     * the way up the chain will be included).
     */
    public void getAll (String prefix, Collection<String> messages, boolean includeParent) {
        Enumeration<String> iter = _bundle.getKeys();
        while (iter.hasMoreElements()) {
            String key = iter.nextElement();
            if (key.startsWith(prefix)) {
                messages.add(get(key));
            }
        }
        if (includeParent && _parent != null) {
            _parent.getAll(prefix, messages, includeParent);
        }
    }

    /**
     * Adds all keys for messages whose key starts with the specified prefix to the supplied
     * collection.
     *
     * @param includeParent if true, messages from our parent bundle (and its parent bundle, all
     * the way up the chain will be included).
     */
    public void getAllKeys (String prefix, Collection<String> keys, boolean includeParent) {
        Enumeration<String> iter = _bundle.getKeys();
        while (iter.hasMoreElements()) {
            String key = iter.nextElement();
            if (key.startsWith(prefix)) {
                keys.add(key);
            }
        }
        if (includeParent && _parent != null) {
            _parent.getAllKeys(prefix, keys, includeParent);
        }
    }

    /**
     * Returns true if we have a translation mapping for the supplied key, false if not.
     */
    public boolean exists (String key) {
        return getResourceString(key, false) != null;
    }

    /**
     * Get a String from the resource bundle, or null if there was an error.
     */
    public String getResourceString (String key) {
        return getResourceString(key, true);
    }

    /**
     * Get a String from the resource bundle, or null if there was an error.
     *
     * @param key the resource key.
     * @param reportMissing whether or not the method should log an error if the resource didn't
     * exist.
     */
    public String getResourceString (String key, boolean reportMissing) {
        try {
            if (_bundle != null) {
                return _bundle.getString(key);
            }
        } catch (MissingResourceException mre) {
            // fall through and try the parent
        }

        // if we have a parent, try getting the string from them
        if (_parent != null) {
            String value = _parent.getResourceString(key, false);
            if (value != null) {
                return value;
            }
            // if we didn't find it in our parent, we want to fall
            // through and report missing appropriately
        }

        if (reportMissing) {
            log.warning("Missing translation message",
                        "bundle", _path, "key", key, new Exception());
        }

        return null;
    }

    /**
     * Obtains the translation for the specified message key. The specified arguments are
     * substituted into the translated string.
     *
     * <p>The substitution performed is very basic. The Nth argument's {@link Object#toString()}
     * result replaces the occurrence of <code>{N}</code> in the untranslated string.</p>
     *
     * <p>If a translation message does not exist for the specified key, an error is logged and the
     * key itself (plus the arguments) is returned so that the caller need not worry about handling
     * a null response.</p>
     */
    public String get (String key, Object... args) {
        // if this is a qualified key, we need to pass the buck to the
        // appropriate message bundle
        if (key.startsWith(Messages.QUAL_PREFIX)) {
            MessageBundle qbundle = _msgmgr.getBundle(Messages.getBundle(key));
            return qbundle.get(Messages.getUnqualifiedKey(key), args);
        }

        String msg = getResourceString(key, false);

        if (msg == null) {
            log.warning("Missing translation message", "bundle", _path, "key", key,
                new Exception());

            // return something bogus
            return (key + Joiner.on(',').join(args));
        }

        return Messages.format(msg, args);
    }

    /**
     * Translates a string with automatic ordinality. For example, for the key {@code m.foo},
     * {@code m.foo.0} will be used if count is zero. {@code m.foo.1} if count is 1 and
     * {@code m.foo.n} will be used if count is 2 or more.
     *
     * <p>NOTE: the count is passed in as an argument to translate the selected ordinal string,
     * regardless. So, for example, if {@code m.foo} expects an argument, then {@code m.foo.0}
     * would have <code>{1}</code> and normally not contain <code>{0}</code>. {@code m.foo.n}
     * would contain both <code>{0}</code> and <code>{1}</code>.</p>
     */
    public String ordinal (String key, int count, Object... args) {
        Object[] nargs = new Object[args.length + 1];
        System.arraycopy(args, 0, nargs, 1, args.length);
        nargs[0] = count;

        if (count == 0) {
            return get(key + ".0", nargs);
        } else if (count == 1) {
            return get(key + ".1", nargs);
        } else {
            return get(key + ".n", nargs);
        }
    }

    /**
     * Selects a random string to translate using the given prefix. For example, to translate
     * {@code m.foo} for a bundle containing {@code m.foo.0} and {@code m.foo.1}, each string
     * will be returned half the time. The number of strings to use is determined by scanning
     * for keys with each numeric suffix until one is not found.
     * <p>TODO: add optional weights like in puzzle pirates?</p>
     */
    public String random (String prefix, Randoms rng, Object... args) {
        // buffer for analyzing keys
        StringBuilder key = new StringBuilder(prefix).append(".");

        // count number of keys that exist
        int len = key.length();
        int count = -1;
        do {
            key.setLength(len);
            key.append(++count);
        } while (exists(prefix.toString()));

        // warn if none
        if (count == 0) {
            log.warning("No keys found to randomize", "prefix", prefix);
            return prefix;
        }

        // select a random one and translate it
        key.setLength(len);
        key.setLength(rng.getInt(count));
        return get(key.toString(), args);
    }

    /**
     * Obtains the translation for the specified message key. The specified arguments are
     * substituted into the translated string.
     */
    public String get (String key, String... args) {
        return get(key, (Object[]) args);
    }

    /**
     *  Convenience method to compose and translate a compound message formed from translatable
     *  arguments. Note that <code>bundle.xlate("m.foo", arg1, arg2)</code> is equivalent to
     *  <code>bundle.xlate(MessageUtil.compose("m.foo", arg1, arg2))</code>
     *  @param key Untranslated primary message key.
     *  @param args Ready-to-be-translated message subcomponents.
     */
    public String xlate (String key, Object... args) {
        return xlate(Messages.compose(key, args));
    }

    /**
     * Obtains the translation for the specified compound message key. A compound key contains the
     * message key followed by a bar (|) separated list of message arguments which will be
     * substituted into the translation string.
     *
     * <p> See {@link #get(String, Object...)} for more information on how the substitution is
     * performed.</p>
     *
     * If a translation message does not exist for the specified key, an error is logged and the
     * key itself (plus the arguments) is returned so that the caller need not worry about handling
     * a null response.
     */
    public String xlate (String compoundKey)
    {
        // if this is a qualified key, we need to pass the buck to the appropriate message bundle;
        // we have to do it here because we want the compound arguments of this key to be
        // translated in the context of the containing message bundle qualification
        if (compoundKey.startsWith(Messages.QUAL_PREFIX)) {
            MessageBundle qbundle = _msgmgr.getBundle(Messages.getBundle(compoundKey));
            return qbundle.xlate(Messages.getUnqualifiedKey(compoundKey));
        }

        // to be more efficient about creating unnecessary objects, we
        // do some checking before splitting
        int tidx = compoundKey.indexOf('|');
        if (tidx == -1) {
            return get(compoundKey);

        } else {
            String key = compoundKey.substring(0, tidx);
            String argstr = compoundKey.substring(tidx+1);
            String[] args =
                Lists.newArrayList(Splitter.on('|').split(argstr)).toArray(new String[0]);
            // unescape and translate the arguments
            for (int ii = 0; ii < args.length; ii++) {
                // if the argument is tainted, do no further translation
                // (it might contain |s or other fun stuff)
                if (Messages.isTainted(args[ii])) {
                    args[ii] = Messages.unescape(Messages.untaint(args[ii]));
                } else {
                    args[ii] = xlate(Messages.unescape(args[ii]));
                }
            }
            return get(key, (Object[]) args);
        }
    }

    @Override public String toString ()
    {
        return "[bundle=" + _bundle + ", path=" + _path + "]";
    }

    /** The message manager via whom we'll resolve fully qualified translation strings. */
    protected MessageManager _msgmgr;

    /** The path that identifies the resource bundle we are using to obtain our messages. */
    protected String _path;

    /** The resource bundle from which we obtain our messages. */
    protected ResourceBundle _bundle;

    /** Our parent bundle if we're not the global bundle. */
    protected MessageBundle _parent;
}
