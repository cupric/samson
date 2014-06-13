//
// SamsoN - utilities for playn clients and servers
// Copyright (c) 2014, Cupric - All rights reserved.
// http://github.com/cupric/samson/blob/master/LICENSE

package samson;

import java.util.Map;

import com.google.common.collect.Maps;

import react.RList;

public abstract class Notifications
{
    public static interface Handle {
        void cancel ();
    }

    public static final class Incoming {
        public final Map<String, String> data;

        public final boolean wasActive;

        public String id () {
            return data.get(ID);
        }

        private Incoming (Map<String, String> data, boolean wasActive) {
            this.data = data;
            this.wasActive = wasActive;
        }
    }

    public class Builder {
        public Builder id (String id) {
            return data(ID, id);
        }

        public Builder soundPath (String path) {
            _soundPath = path;
            return this;
        }

        public Builder message (String message) {
            _message = message;
            return this;
        }

        public Builder data (String key, String value) {
            _data.put(key, value);
            return this;
        }

        public Builder silent () {
            _silent = true;
            return this;
        }

        public Handle schedule (long time) {
            return Notifications.this.schedule(time, this);
        }

        protected String _message;
        protected String _soundPath;
        protected boolean _silent;
        protected Map<String, String> _data = Maps.newHashMap();
    }

    public Builder builder () {
        return new Builder();
    }

    public RList<Incoming> arrived () {
        return _incoming;
    }

    protected void dispatch (Map<String, String> data, boolean active) {
        _incoming.add(new Incoming(data, active));
    }

    protected abstract Handle schedule (long when, Builder builder);

    protected final RList<Incoming> _incoming = RList.create();
    protected static final String ID = "notifier_id";
}
