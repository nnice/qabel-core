package de.qabel.core.accounting;


import de.qabel.core.exceptions.QblCreateAccountFailException;
import de.qabel.core.exceptions.QblInvalidCredentials;
import org.apache.http.HttpRequest;
import org.apache.http.client.utils.URIBuilder;

import java.io.IOException;
import java.util.ArrayList;

public class BoxClientStub implements BoxClient {

    QuotaDto quota = new QuotaDto();

    public BoxClientStub() {
        quota.quota = 24;
        quota.size = 200;
    }

    @Override
    public void login() throws IOException, QblInvalidCredentials {

    }

    @Override
    public QuotaDto getQuota() throws IOException, QblInvalidCredentials {
        return quota;
    }

    @Override
    public void authorize(HttpRequest request) throws IOException, QblInvalidCredentials {

    }

    @Override
    public void updatePrefixes() throws IOException, QblInvalidCredentials {

    }

    @Override
    public void createPrefix() throws IOException, QblInvalidCredentials {

    }

    @Override
    public URIBuilder buildUri(String resource) {
        return null;
    }

    @Override
    public URIBuilder buildBlockUri(String resource) {
        return null;
    }

    @Override
    public ArrayList<String> getPrefixes() throws IOException, QblInvalidCredentials {
        return null;
    }

    @Override
    public AccountingProfile getProfile() {
        return null;
    }

    @Override
    public void resetPassword(String email) throws IOException {

    }

    @Override
    public void createBoxAccount(String email) throws IOException, QblCreateAccountFailException {

    }
}
