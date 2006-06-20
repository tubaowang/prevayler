// Prevayler, The Free-Software Prevalence Layer
// Copyright 2001-2006 by Klaus Wuestefeld
//
// This library is distributed in the hope that it will be useful, but WITHOUT
// ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
// FITNESS FOR A PARTICULAR PURPOSE.
//
// Prevayler is a trademark of Klaus Wuestefeld.
// See the LICENSE file for license details.

package org.prevayler.demos.scalability.jdbc;

import org.prevayler.demos.scalability.Record;
import org.prevayler.demos.scalability.TransactionConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

class JDBCTransactionConnection extends JDBCScalabilityConnection implements TransactionConnection {

    private final PreparedStatement updateStatement;

    private final PreparedStatement deleteStatement;

    JDBCTransactionConnection(Connection connection) {
        super(connection);

        updateStatement = prepare("update " + table() + " set NAME=?,STRING1=?,BIGDECIMAL1=?,BIGDECIMAL2=?,DATE1=?,DATE2=? where ID=?");
        deleteStatement = prepare("delete from " + table() + " where ID=?");
    }

    @Override protected String table() {
        return "TRANSACTION_TEST";
    }

    public void performTransaction(Record recordToInsert, Record recordToUpdate, long idToDelete) {
        insert(recordToInsert);
        update(recordToUpdate);
        delete(idToDelete);

        try {
            connection.commit();
        } catch (SQLException sqlx) {
            dealWithSQLException(sqlx, "commiting transaction");
        }
    }

    private void update(Record recordToUpdate) {
        try {
            updateStatement.setString(1, recordToUpdate.getName());
            updateStatement.setString(2, recordToUpdate.getString1());
            updateStatement.setBigDecimal(3, recordToUpdate.getBigDecimal1());
            updateStatement.setBigDecimal(4, recordToUpdate.getBigDecimal2());
            updateStatement.setDate(5, new java.sql.Date(recordToUpdate.getDate1().getTime()));
            updateStatement.setDate(6, new java.sql.Date(recordToUpdate.getDate2().getTime()));
            updateStatement.setLong(7, recordToUpdate.getId()); // "...where
                                                                // ID=?"
            updateStatement.execute();
        } catch (SQLException sqlx) {
            dealWithSQLException(sqlx, "updating record");
        }
    }

    private void delete(long idToDelete) {
        try {
            deleteStatement.setLong(1, idToDelete);
            deleteStatement.execute();
        } catch (SQLException sqlx) {
            dealWithSQLException(sqlx, "deleting record");
        }
    }
}
