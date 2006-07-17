// Prevayler, The Free-Software Prevalence Layer
// Copyright 2001-2006 by Klaus Wuestefeld
//
// This library is distributed in the hope that it will be useful, but WITHOUT
// ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
// FITNESS FOR A PARTICULAR PURPOSE.
//
// Prevayler is a trademark of Klaus Wuestefeld.
// See the LICENSE file for license details.

package org.prevayler.implementation.publishing.censorship;

import org.prevayler.implementation.*;

public class LiberalTransactionCensor<S> implements TransactionCensor<S> {

    public <R, E extends Exception> boolean approve(@SuppressWarnings("unused") TransactionTimestamp<S, R, E> transactionTimestamp) {
        if (transactionTimestamp.capsule().desiresRollback()) {
            throw new RollbackNotSupportedError();
        }
        return true;
    }

}
