package de.qabel.core.accounting;


import de.qabel.core.exceptions.QblInvalidCredentials;

import java.io.IOException;

public class BoxClientStub implements BoxClient {

    long quota = 24;

    public BoxClientStub() {
    }

    public BoxClientStub(long quota) {
        this.quota = quota;
    }

    @Override
    public long getQuota() throws IOException, QblInvalidCredentials {
        return quota;
    }
}
