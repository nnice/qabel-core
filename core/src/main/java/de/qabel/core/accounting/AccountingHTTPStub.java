package de.qabel.core.accounting;


import de.qabel.core.exceptions.QblInvalidCredentials;

import java.io.IOException;

public class AccountingHTTPStub implements AccountingHTTP {

    long quota = 24;

    public AccountingHTTPStub() {
    }

    public AccountingHTTPStub(long quota) {
        this.quota = quota;
    }

    @Override
    public long getQuota() throws IOException, QblInvalidCredentials {
        return quota;
    }
}
