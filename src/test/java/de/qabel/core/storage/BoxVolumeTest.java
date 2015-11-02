package de.qabel.core.storage;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import de.qabel.core.crypto.QblECKeyPair;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

public class BoxVolumeTest {

	BoxVolume volume;

	@Test
	public void testS3Init() {
		String bucket = "qabel";
		String prefix = UUID.randomUUID().toString();

		DefaultAWSCredentialsProviderChain chain = new DefaultAWSCredentialsProviderChain();

		volume = new BoxVolume(bucket,prefix, chain.getCredentials(), new QblECKeyPair());

		assertThat(volume.cache.size(), is(0));
	}

	@Test
	public void testLocalInit() throws IOException {
		Path tempFolder = Files.createTempDirectory("");

		volume = new BoxVolume(new LocalReadBackend(tempFolder),
			new LocalWriteBackend(tempFolder),
			new QblECKeyPair());

		assertThat(volume.cache.size(), is(0));
	}

}