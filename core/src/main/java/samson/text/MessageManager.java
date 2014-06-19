//
// SamsoN - utilities for playn clients and servers
// Copyright (c) 2014, Cupric - All rights reserved.
// http://github.com/cupric/samson/blob/master/LICENSE

package samson.text;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;

import playn.core.PlayN;

import static samson.Log.log;

/**
 * The message manager provides a thin wrapper around Java's built-in localization support,
 * supporting a policy of dividing up localization resources into logical units, all of the
 * translations for which are contained in a single messages file.
 *
 * <p>The message manager assumes that the locale remains constant for the duration of its
 * operation, unless {@link #setLocale} is called. This clears all cached bundles. Thus, callers
 * that wish to change locales should not maintain references to bundles returned by
 * {@link #getBundle(String)}.</p>
 */
public class MessageManager
{
    /**
     * Defines how underlying resource bundles are loaded.
     */
    public interface BundleLoader {
        /**
         * Returns the bundle referred to by the given path, for the given locale, or null if the
         * bundle could not be loaded.
         */
        ResourceBundle loadBundle (String path, Locale locale);
    }

    /**
     * The default loader, using {@link PropertiesResourceBundle}. Makes various assumptions
     * about the location of bundles:
     * <ul><li>Dots in the names of bundles are directories on the file system</li>
     * <li>The file for a bundle is found by appending .properties to its name.</li>
     * <li>The file for a language-specific bundle is found by appending an underscore and the
     * language name at the end of the bundle name. If not found, falls back to the unlocalized
     * version of the file.</li></ul>
     * A warning is logged if the file could not be loaded.
     * <p>For example, for the bundle {@code game.chess}, running in a French locale, the file
     * {@code "game/chess_fr.properties"} will first be attempted. If not found, then {@code
     * game/chess.properties} will be loaded.</p>
     */
    public static class PropertiesBundleLoader
            implements BundleLoader {
        @Override
        public ResourceBundle loadBundle (String path, Locale locale) {
            path = path.replace('.', '/');

            String bundleText = getBundleText(path + "_" + locale.getLanguage() + ".properties");

            if (bundleText == null) {
                // Fall back to no language.
                bundleText = getBundleText(path + ".properties");
            }

            if (bundleText == null) {
                return null;
            }

            try {
                return new PropertyResourceBundle(new StringReader(bundleText));
            } catch (IOException ioe) {
                log.warning("Trouble loading resource bundle", ioe);
                return null;
            }
        }

        /**
         * Returns the text in the bundle at the given path or null if we don't have it.
         */
        protected String getBundleText (String path) {
            // FIXME - This will break in HTML5 due to deferred asset loading.
            try {
                return PlayN.assets().getTextSync(path);
            } catch (Throwable t) {
                // That's fine, we just don't have this asset.
                return null;
            }
        }
    }

    /**
     * Constructs a message manager with the supplied resource prefix and the default locale. The
     * prefix will be prepended to the path of all resource bundles prior to their resolution. For
     * example, if a prefix of <code>rsrc.messages</code> was provided and a message bundle with
     * the name <code>game.chess</code> was later requested, the message manager would attempt to
     * load a resource bundle with the path <code>rsrc.messages.game.chess</code> and would
     * eventually search for a file in the classpath with the path
     * <code>rsrc/messages/game/chess.properties</code>.
     * @param loader - if not null, overrides the default bundle loading behavior defined in {@link
     * PropertiesBundleLoader}.
     * @param globalBundle - if not null, provides a default parent for all loaded bundles. this
     * allows translations to be moved from a specific bundle into a shared location
     */
    public MessageManager (String resourcePrefix, String globalBundle, BundleLoader loader) {
        // keep the prefix
        _prefix = resourcePrefix;

        // how we load our bundles
        _loader = loader == null ? new PropertiesBundleLoader() : loader;

        // use the default locale
        _locale = Locale.getDefault();
        //DEBUGlog.info("Using locale: " + _locale + ".");

        // make sure the prefix ends with a dot
        if (!Strings.isNullOrEmpty(_prefix) && !_prefix.endsWith(".")) {
            _prefix += ".";
        }

        // load up the global bundle
        _globalName = globalBundle;
        _global = _globalName == null ? null : getBundle(_globalName);
    }

    /**
     * Get the locale that is being used to translate messages. This may be useful if using
     * standard translations, for example new SimpleDateFormat("EEEE", getLocale()) to get the
     * name of a weekday that matches the language being used for all other client translations.
     */
    public Locale getLocale () {
        return _locale;
    }

    /**
     * Sets the locale to the specified locale. Subsequent message bundles fetched via the message
     * manager will use the new locale. The message bundle cache will also be cleared.
     */
    public void setLocale (Locale locale) {
        _locale = locale;
        _cache.clear();
        _global = getBundle(_globalName);
    }

    /**
     * Fetches the message bundle for the specified path. If no bundle can be located with the
     * specified path, a special bundle is returned that returns the untranslated message
     * identifiers instead of an associated translation. This is done so that error code to handle
     * a failed bundle load need not be replicated wherever bundles are used. Instead an error
     * will be logged and the requesting service can continue to function in an impaired state.
     */
    public MessageBundle getBundle (String path) {
        // first look in the cache
        MessageBundle bundle = _cache.get(path);
        if (bundle != null) {
            return bundle;
        }

        // if it's not cached, we'll need to resolve it
        ResourceBundle rbundle = loadBundle(_prefix + path);

        // if the resource bundle contains a special resource, we'll interpret that as a derivation
        // of MessageBundle to instantiate for handling that class
        MessageBundle customBundle = null;
        if (rbundle != null) {
            String mbclass = null;
            try {
                mbclass = rbundle.getString(MBUNDLE_CLASS_KEY).trim();
                if (!Strings.isNullOrEmpty(mbclass)) {
                    customBundle = (MessageBundle)Class.forName(mbclass).newInstance();
                }

            } catch (MissingResourceException mre) {
                // nothing to worry about

            } catch (Throwable t) {
                log.warning("Failure instantiating custom message bundle", "mbclass", mbclass,
                            "error", t);
            }
        }

        // initialize our message bundle, cache it and return it (if we couldn't resolve the
        // bundle, the message bundle will cope with its null resource bundle)
        bundle = createBundle(path, rbundle, customBundle);
        _cache.put(path, bundle);
        return bundle;
    }

    /**
     * Returns the bundle to use for the given path and resource bundle. If customBundle is
     * non-null, it's an instance of the bundle class specified by the bundle itself and should be
     * used as part of the created bundle.
     */
    protected MessageBundle createBundle (String path, ResourceBundle rbundle,
            MessageBundle customBundle) {
        // if there was no custom class, or we failed to instantiate the custom class, use a
        // standard message bundle
        if (customBundle == null) {
            customBundle = new MessageBundle();
        }
        initBundle(customBundle, path, rbundle);
        return customBundle;
    }

    /**
     * Initializes the given bundle with this manager and the given path and resource bundle.
     */
    protected void initBundle (MessageBundle bundle, String path, ResourceBundle rbundle) {
        MessageBundle parentBundle = null;
        try {
            parentBundle = getBundle(rbundle.getString("__parent"));
            // Note: if getBundle() fails to find our parent it will log a warning and return null
        } catch (MissingResourceException mre ) {
            // no resource named __parent: it's ok.
        }
        if (parentBundle == null) {
            parentBundle = _global;
        }
        bundle.init(this, path, rbundle, parentBundle);
    }

    /**
     * Loads a bundle from the given path, or returns null if it can't be found.
     */
    protected ResourceBundle loadBundle (String path) {
        try {
            return _loader.loadBundle(path, _locale);
        } catch (MissingResourceException mre) {
            log.warning("Unable to resolve resource bundle", "path", path, "locale", _locale,
                mre);
            return null;
        }
    }

    /** A key that can contain the classname of a custom message bundle
     * class to be used to handle messages for a particular bundle. */
    protected static final String MBUNDLE_CLASS_KEY = "msgbundle_class";

    /** The prefix we prepend to resource paths prior to loading. */
    protected String _prefix;

    /** The locale for which we're obtaining message bundles. */
    protected Locale _locale;

    /** A custom loader that we use to load resource bundles. */
    protected final BundleLoader _loader;

    /** A cache of instantiated message bundles. */
    protected final HashMap<String, MessageBundle> _cache = Maps.newHashMap();

    /** The name of our top-level message bundle. */
    protected final String _globalName;

    /** Our top-level message bundle, from which others obtain messages if
     * they can't find them within themselves. */
    protected MessageBundle _global;
}
