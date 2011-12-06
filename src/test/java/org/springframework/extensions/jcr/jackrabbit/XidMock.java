package org.springframework.extensions.jcr.jackrabbit;

import javax.transaction.xa.Xid;

/**
 * Simple mock which overrides equals.
 * @author Costin Leau
 * @author Sergio Bossa
 * @author Salvatore Incandela
 */
class XidMock implements Xid {
    /**
     * @see javax.transaction.xa.Xid#getBranchQualifier()
     */
    @Override
    public byte[] getBranchQualifier() {
        return null;
    }

    /**
     * @see javax.transaction.xa.Xid#getFormatId()
     */
    @Override
    public int getFormatId() {
        return 0;
    }

    /**
     * @see javax.transaction.xa.Xid#getGlobalTransactionId()
     */
    @Override
    public byte[] getGlobalTransactionId() {
        return null;
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        return true;
    }

}