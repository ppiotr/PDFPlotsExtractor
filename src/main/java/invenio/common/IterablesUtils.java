/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package invenio.common;

import com.google.common.collect.Iterables;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 *
 * @author piotr
 */
public class IterablesUtils {

    /**
     * Produces all possible iterables which iterate over subsequences (order
     * preserved) of the original iterable, skipping n entries
     *
     * @param <T>
     * @param iter the original iterable to take elements from
     * @param n number of entreis to skip
     * @return
     */
    public static <T> Iterable<Iterable<T>> skipN(Iterable<T> iter, int n) {
        return new IterableOfIterables<T>(iter, n);
    }

    public static class IterableOfIterables<T> implements Iterable<Iterable<T>> {

        public Iterable<T> origIterable;
        public int n;

        public IterableOfIterables(Iterable<T> origIterable, int n) {
            this.origIterable = origIterable;
            this.n = n;
        }

        @Override
        public Iterator<Iterable<T>> iterator() {
            return new IteratorOfIterables<T>(this.origIterable, n);
        }
    }

    public static class IteratorOfIterables<T> implements Iterator<Iterable<T>> {

        private Iterable<T> origIterable;
        private int n;
        private int[] stateVector;
        private int iterLen;
        private int currentMove; // which indicator should be moved towards the right
        private boolean finished;

        public IteratorOfIterables(Iterable<T> origIterable, int n) {
            this.origIterable = origIterable;
            this.n = n;
            this.stateVector = new int[n];
            this.iterLen = Iterables.size(this.origIterable);

            for (int i = 0; i < n; i++) {
                this.stateVector[i] = i;
            }
            this.currentMove = n - 1;

            this.finished = this.n >= this.iterLen;
        }

        @Override
        public boolean hasNext() {
            return !this.finished;
        }

        private boolean isLast() {
            // the end has been reached if all the indicators point to the rightmost
            for (int i = 0; i < this.n; i++) {
                if (this.stateVector[i] != this.iterLen - n + i) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public Iterable<T> next() {
            if (!this.hasNext()) {
                throw new NoSuchElementException();
            }

            SkipIterable<T> res = new SkipIterable<T>(this.origIterable, this.stateVector.clone());
            this.finished = this.isLast();

            boolean resetOthers = false; // should re override values of all indicors at the right ?
            int i = this.n - 1;
            while ((this.currentMove >= 0)
                    && ((this.currentMove == this.n - 1 && this.stateVector[this.currentMove] == this.iterLen - 1)
                    || (this.currentMove < this.n - 1 && this.stateVector[this.currentMove] == this.stateVector[this.currentMove + 1] - 1))) {
                resetOthers = true;
                this.currentMove--;
            }

            if (this.currentMove >= 0) {
                this.stateVector[this.currentMove] += 1;
                if (resetOthers) {
                    for (int j = this.currentMove + 1; j < this.n; j++) {
                        this.stateVector[j] = this.stateVector[j - 1] + 1;
                    }
                    this.currentMove = this.n - 1;
                }
            }
            return res;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("This operation does not make sense");
        }
    }

    public static class SkipIterable<T> implements Iterable<T> {

        private Iterable<T> originalIterable;
        private int[] toSkip;

        public SkipIterable(Iterable<T> original, int[] toSkip) {
            this.originalIterable = original;
            this.toSkip = toSkip;
        }

        @Override
        public Iterator<T> iterator() {
            return new SkipIterator<T>(this.originalIterable.iterator(), this.toSkip);
        }
    }

    public static class SkipIterator<T> implements Iterator<T> {

        private final int[] toSkip;
        private final Iterator<T> origIter;
        private int curInd;
        private int curSkip;

        public SkipIterator(Iterator<T> origIter, int[] toSkip) {
            this.toSkip = toSkip;
            this.origIter = origIter;
            this.curInd = 0;
            this.curSkip = 0;
        }

        @Override
        public boolean hasNext() {
            try {
                rewind();
            } catch (NoSuchElementException e) {
                return false;
            }
            return this.origIter.hasNext();
        }

        @Override
        public T next() {
            this.rewind();
            this.curInd++;
            return this.origIter.next();
        }

        private void rewind() throws NoSuchElementException {
            while (this.curSkip != this.toSkip.length && this.toSkip[this.curSkip] == this.curInd) {
                this.curInd++;
                this.curSkip++;
                this.origIter.next(); // rewind
            }
        }

        @Override
        public void remove() {
            this.rewind();
            this.origIter.remove();
        }
    }
}
