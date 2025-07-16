package com.github.argon4w.acceleratedrendering.core.utils;

import lombok.Getter;

import java.util.Arrays;

public abstract class SimpleResetPool<T, C> {

    @Getter
    protected final C context;
    protected int size;
    protected Object[] pool;
    private int cursor;

    public SimpleResetPool(int size, C context) {
        this.size = size;
        this.pool = new Object[size];
        this.context = context;

        this.cursor = 0;

        for (var i = 0; i < this.size; i++) {
            this.pool[i] = create(this.context, i);
        }
    }

    protected abstract T create(C context, int i);

    protected abstract void reset(T t);

    protected abstract void delete(T t);

    @SuppressWarnings("unchecked")
    public T get() {
        if (cursor < size) {
            var t = (T) pool[cursor++];

            if (test(t)) {
                return t;
            }
        }

        return fail();
    }

    @SuppressWarnings("unchecked")
    public void reset() {
        for (var i = 0; i < cursor; i++) {
            reset((T) pool[i]);
        }

        cursor = 0;
    }

    @SuppressWarnings("unchecked")
    public void delete() {
        for (var i = 0; i < size; i++) {
            delete((T) pool[i]);
        }
    }

    protected void expand() {
        var old = size;

        size = old * 2;
        pool = Arrays.copyOf(pool, size);

        for (var i = old; i < size; i++) {
            pool[i] = create(context, i);
        }
    }

    public T fail() {
        return null;
    }

    public boolean test(T t) {
        return true;
    }
}