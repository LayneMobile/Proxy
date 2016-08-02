/*
 * Copyright 2016 Layne Mobile, LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.laynemobile.proxy.model;

import com.google.common.collect.ImmutableList;

import java.util.Iterator;

public abstract class AbstractRound<R extends AbstractRound<R>> implements Round<R> {
    private final int round;
    private final R previous;

    protected AbstractRound() {
        this.round = 0;
        this.previous = null;
    }

    protected AbstractRound(R previous) {
        if (previous == null) {
            throw new NullPointerException("previous round cannot be null");
        }
        this.round = previous.round() + 1;
        this.previous = previous;
    }

    protected abstract R current();

    @Override public final int round() {
        return round;
    }

    @Override public final R previous() {
        return previous;
    }

    @Override public final boolean isFirstRound() {
        return previous == null;
    }

    @Override public final Iterator<R> iterator() {
        R current = current();
        if (current == null) {
            throw new NullPointerException("current cannot be null");
        } else if (current != this) {
            throw new IllegalArgumentException("current '" + current + "' must equal this");
        }
        return new RoundIterator<>(current);
    }

    @Override public final ImmutableList<R> allRounds() {
        return ImmutableList.copyOf(iterator());
    }

    private static final class RoundIterator<R extends AbstractRound<R>> implements Iterator<R> {
        private R round;

        private RoundIterator(R round) {
            this.round = round;
        }

        @Override public boolean hasNext() {
            return round != null;
        }

        @Override public R next() {
            R r = round;
            round = r.previous();
            return r;
        }

        @Override public void remove() {
            throw new UnsupportedOperationException("immutable");
        }
    }
}
