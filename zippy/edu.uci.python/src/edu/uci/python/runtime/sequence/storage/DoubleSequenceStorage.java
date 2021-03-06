/*
 * Copyright (c) 2013, Regents of the University of California
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package edu.uci.python.runtime.sequence.storage;

import java.util.*;

import org.python.core.*;

import com.oracle.truffle.api.*;

import edu.uci.python.runtime.sequence.*;

public final class DoubleSequenceStorage extends BasicSequenceStorage {

    private double[] values;

    public DoubleSequenceStorage() {
        values = new double[]{};
    }

    public DoubleSequenceStorage(double[] elements) {
        this.values = elements;
        length = elements.length;
        capacity = elements.length;
    }

    @Override
    protected void increaseCapacityExactWithCopy(int newCapacity) {
        values = Arrays.copyOf(values, newCapacity);
        capacity = values.length;
    }

    @Override
    protected void increaseCapacityExact(int newCapacity) {
        values = new double[newCapacity];
        capacity = values.length;
    }

    @Override
    public SequenceStorage copy() {
        return new DoubleSequenceStorage(Arrays.copyOf(values, length));
    }

    @Override
    public Object[] getInternalArray() {
        /**
         * Have to box and copy.
         */
        Object[] boxed = new Object[length];

        for (int i = 0; i < length; i++) {
            boxed[i] = values[i];
        }

        return boxed;
    }

    public double[] getInternalDoubleArray() {
        return values;
    }

    @Override
    public Object[] getCopyOfInternalArray() {
        return getInternalArray();
    }

    @Override
    public Object getItemNormalized(int idx) {
        return getDoubleItemNormalized(idx);
    }

    public double getDoubleItemNormalized(int idx) {
        try {
            return values[idx];
        } catch (ArrayIndexOutOfBoundsException e) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            throw Py.IndexError("list index out of range");
        }
    }

    @Override
    public void setItemNormalized(int idx, Object value) throws SequenceStoreException {
        if (value instanceof Double) {
            setDoubleItemNormalized(idx, (double) value);
        } else {
            throw SequenceStoreException.INSTANCE;
        }
    }

    public void setDoubleItemNormalized(int idx, double value) {
        try {
            values[idx] = value;
        } catch (ArrayIndexOutOfBoundsException e) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            throw Py.IndexError("list assignment index out of range");
        }
    }

    @Override
    public void insertItem(int idx, Object value) throws SequenceStoreException {
        if (value instanceof Double) {
            insertDoubleItem(idx, (double) value);
        } else {
            throw SequenceStoreException.INSTANCE;
        }
    }

    public void insertDoubleItem(int idx, double value) {
        ensureCapacity(length + 1);

        // shifting tail to the right by one slot
        for (int i = values.length - 1; i > idx; i--) {
            values[i] = values[i - 1];
        }

        values[idx] = value;
        length++;
    }

    @Override
    public SequenceStorage getSliceInBound(int start, int stop, int step, int sliceLength) {
        double[] newArray = new double[sliceLength];

        if (step == 1) {
            System.arraycopy(values, start, newArray, 0, sliceLength);
            return new DoubleSequenceStorage(newArray);
        }

        for (int i = start, j = 0; j < sliceLength; i += step, j++) {
            newArray[j] = values[i];
        }

        return new DoubleSequenceStorage(newArray);
    }

    @Override
    public void setSliceInBound(int start, int stop, int step, SequenceStorage sequence) throws SequenceStoreException {
        if (sequence instanceof DoubleSequenceStorage) {
            setDoubleSliceInBound(start, stop, step, (DoubleSequenceStorage) sequence);
        } else {
            throw new SequenceStoreException();
        }
    }

    public void setDoubleSliceInBound(int start, int stop, int step, DoubleSequenceStorage sequence) {
        int otherLength = sequence.length();

        // range is the whole sequence?
        if (start == 0 && stop == length) {
            values = Arrays.copyOf(sequence.values, otherLength);
            length = otherLength;
            minimizeCapacity();
            return;
        }

        ensureCapacity(stop);

        for (int i = start, j = 0; i < stop; i += step, j++) {
            values[i] = sequence.values[j];
        }

        length = length > stop ? length : stop;
    }

    @Override
    public void delSlice(int start, int stop) {
        if (stop == SequenceUtil.MISSING_INDEX) {
            length = start;
        }
    }

    @Override
    public void delItemInBound(int idx) {
        popInBound(idx);
    }

    @Override
    public Object popInBound(int idx) {
        double pop = values[idx];

        for (int i = idx; i < values.length - 1; i++) {
            values[i] = values[i + 1];
        }

        length--;
        return pop;
    }

    public double popDouble() {
        double pop = values[capacity - 1];
        length--;
        return pop;
    }

    @Override
    public int index(Object value) {
        if (value instanceof Double) {
            return indexOfDouble((double) value);
        } else {
            return super.index(value);
        }

    }

    public int indexOfDouble(double value) {
        for (int i = 0; i < length; i++) {
            if (values[i] == value) {
                return i;
            }
        }

        return -1;
    }

    @Override
    public void append(Object value) throws SequenceStoreException {
        if (value instanceof Double) {
            appendDouble((double) value);
        } else {
            throw SequenceStoreException.INSTANCE;
        }
    }

    public void appendDouble(double value) {
        ensureCapacity(length + 1);
        values[length] = value;
        length++;
    }

    @Override
    public void extend(SequenceStorage other) throws SequenceStoreException {
        if (other instanceof DoubleSequenceStorage) {
            extendWithIntStorage((DoubleSequenceStorage) other);
        } else {
            throw SequenceStoreException.INSTANCE;
        }
    }

    public void extendWithIntStorage(DoubleSequenceStorage other) {
        int extendedLength = length + other.length();
        ensureCapacity(extendedLength);
        double[] otherValues = other.values;

        for (int i = length, j = 0; i < extendedLength; i++, j++) {
            values[i] = otherValues[j];
        }

        length = extendedLength;
    }

    @Override
    public void reverse() {
        int head = 0;
        int tail = length - 1;
        int middle = (length - 1) / 2;

        for (; head <= middle; head++, tail--) {
            double temp = values[head];
            values[head] = values[tail];
            values[tail] = temp;
        }
    }

    @Override
    public void sort() {
        double[] copy = Arrays.copyOf(values, length);
        Arrays.sort(copy);
        values = copy;
        minimizeCapacity();
    }

    @Override
    public SequenceStorage generalizeFor(Object value) {
        return new ObjectSequenceStorage(getInternalArray());
    }

    @Override
    public Object getIndicativeValue() {
        return .0;
    }

    @Override
    public boolean equals(SequenceStorage other) {
        if (other.length() != length()) {
            return false;
        }

        double[] otherArray = ((DoubleSequenceStorage) other).getInternalDoubleArray();
        for (int i = 0; i < length(); i++) {
            if (values[i] != otherArray[i]) {
                return false;
            }
        }

        return true;
    }

}
