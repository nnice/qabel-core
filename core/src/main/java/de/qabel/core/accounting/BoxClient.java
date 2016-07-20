package de.qabel.core.accounting;

import de.qabel.core.exceptions.QblInvalidCredentials;

import java.io.IOException;

public interface BoxClient {
    long getQuota() throws IOException, QblInvalidCredentials;
}
