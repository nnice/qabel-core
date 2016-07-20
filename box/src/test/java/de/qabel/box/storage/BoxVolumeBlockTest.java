package de.qabel.box.storage;

import de.qabel.box.http.BlockReadBackend;
import de.qabel.box.http.BlockWriteBackend;
import de.qabel.core.accounting.AccountingHTTPClient;
import de.qabel.core.accounting.AccountingProfile;
import de.qabel.core.config.AccountingServer;
import de.qabel.core.crypto.QblECKeyPair;
import org.junit.BeforeClass;

import java.io.IOException;
import java.net.URI;
import java.util.List;

public class BoxVolumeBlockTest extends BoxVolumeTest {
    private BlockReadBackend readBackend;
    private static AccountingHTTPClient accountingHTTP;

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @Override
    protected StorageReadBackend getReadBackend() {
        return readBackend;
    }

    @Override
    protected void setUpVolume() {
        try {
            AccountingServer server = new AccountingServer(new URI("http://localhost:9696"), new URI("http://localhost:9697"), "testuser", "testuser");
            accountingHTTP = new AccountingHTTPClient(server, new AccountingProfile());
            QblECKeyPair keyPair = new QblECKeyPair();

            List<String> prefixes = accountingHTTP.getPrefixes();
            if (prefixes.isEmpty()) {
                accountingHTTP.createPrefix();
                prefixes = accountingHTTP.getPrefixes();
            }
            prefix = prefixes.get(0);


            String root = accountingHTTP.buildBlockUri("api/v0/files/" + prefix).build().toString();
            readBackend = new BlockReadBackend(root, accountingHTTP);


            volume = new BoxVolume(
                    readBackend,
                    new BlockWriteBackend(root, accountingHTTP),
                    keyPair,
                    deviceID,
                    volumeTmpDir,
                    prefix
            );
            volume2 = new BoxVolume(
                    new BlockReadBackend(root, accountingHTTP),
                    new BlockWriteBackend(root, accountingHTTP),
                    keyPair,
                    deviceID,
                    volumeTmpDir,
                    prefix
            );
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    @Override
    protected void cleanVolume() throws IOException {
    }
}
