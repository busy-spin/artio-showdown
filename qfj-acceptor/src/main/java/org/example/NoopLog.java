package org.example;

import quickfix.Log;

public class NoopLog implements Log {
    @Override
    public void clear() {

    }

    @Override
    public void onIncoming(String s) {

    }

    @Override
    public void onOutgoing(String s) {

    }

    @Override
    public void onEvent(String s) {

    }

    @Override
    public void onErrorEvent(String s) {

    }
}
