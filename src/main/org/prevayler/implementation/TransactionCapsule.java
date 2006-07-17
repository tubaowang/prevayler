// Prevayler, The Free-Software Prevalence Layer
// Copyright 2001-2006 by Klaus Wuestefeld
//
// This library is distributed in the hope that it will be useful, but WITHOUT
// ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
// FITNESS FOR A PARTICULAR PURPOSE.
//
// Prevayler is a trademark of Klaus Wuestefeld.
// See the LICENSE file for license details.

package org.prevayler.implementation;

import org.prevayler.*;
import org.prevayler.Safety.*;
import org.prevayler.foundation.*;
import org.prevayler.foundation.serialization.*;

import java.io.*;

public class TransactionCapsule<S, R, E extends Exception> implements Serializable {

    private static final long serialVersionUID = 1L;

    private final byte[] _serialized;

    private final boolean _desiresRollback;

    private transient R _result;

    private transient Exception _exception;

    public TransactionCapsule(GenericTransaction<? super S, R, E> transaction, Serializer<GenericTransaction> journalSerializer) {
        try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            journalSerializer.writeObject(bytes, transaction);
            _serialized = bytes.toByteArray();
            _desiresRollback = SafetyCache.getJournaling(transaction).compareTo(Journaling.ROLLBACK_ON_RUNTIME_EXCEPTION) >= 0;
        } catch (Exception exception) {
            throw new TransactionNotSerializableError("Unable to serialize transaction", exception);
        }
    }

    public TransactionCapsule(byte[] serialized, boolean desiresRollback) {
        _serialized = serialized;
        _desiresRollback = desiresRollback;
    }

    /**
     * Get the serialized representation of the transaction. Callers must not
     * modify the returned array.
     */
    public byte[] serialized() {
        return _serialized;
    }

    /**
     * Deserialize the contained Transaction or TransactionWithQuery.
     */
    @SuppressWarnings("unchecked") public final GenericTransaction<? super S, R, E> deserialize(Serializer<GenericTransaction> journalSerializer) {
        try {
            return journalSerializer.readObject(new ByteArrayInputStream(_serialized));
        } catch (Exception exception) {
            throw new TransactionNotDeserializableError("Unable to deserialize transaction", exception);
        }
    }

    Chunk toChunk() {
        return new Chunk(_serialized);
    }

    @SuppressWarnings("unchecked") static <S> TransactionCapsule<S, ?, ?> fromChunk(Chunk chunk) {
        return new TransactionCapsule(chunk.getBytes(), false);
    }

    protected void execute(GenericTransaction<? super S, R, E> transaction, PrevalenceContext<S> prevalenceContext) {
        try {
            _result = transaction.executeOn(prevalenceContext);
        } catch (Exception e) {
            _exception = e;
        }
    }

    @SuppressWarnings("unchecked") public R result() throws E {
        if (_exception != null) {
            // No cast is actually performed, so this works even for
            // RuntimeExceptions.
            throw (E) _exception;
        } else {
            return _result;
        }
    }

    public boolean desiresRollback() {
        return _desiresRollback;
    }

    public boolean requiresRollback() {
        return _desiresRollback && _exception instanceof RuntimeException;
    }

    public void cleanUp() {
        _result = null;
        _exception = null;
    }

    /**
     * Make a clean copy of this capsule that will have its own query result
     * fields.
     */
    public TransactionCapsule<S, R, E> cleanCopy() {
        return new TransactionCapsule<S, R, E>(_serialized, _desiresRollback);
    }

}
