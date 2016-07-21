package de.qabel.core.accounting;

import de.qabel.core.exceptions.QblCreateAccountFailException;
import de.qabel.core.exceptions.QblInvalidCredentials;
import org.apache.http.HttpRequest;

import java.io.IOException;
import java.util.ArrayList;

public interface BoxClient {

    void login() throws IOException, QblInvalidCredentials;

    QuotaState getQuotaState() throws IOException, QblInvalidCredentials;

    void authorize(HttpRequest request) throws IOException, QblInvalidCredentials;

    void updatePrefixes() throws IOException, QblInvalidCredentials;

    void createPrefix() throws IOException, QblInvalidCredentials;

    ArrayList<String> getPrefixes() throws IOException, QblInvalidCredentials;

    void resetPassword(String email) throws IOException;

    void createBoxAccount(String email) throws IOException, QblCreateAccountFailException;
}
