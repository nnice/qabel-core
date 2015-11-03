package de.qabel.core.storage;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import de.qabel.core.crypto.QblECKeyPair;
import de.qabel.core.exceptions.QblStorageException;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

public class BoxVolumeTest {
	private final static Logger logger = LoggerFactory.getLogger(BoxVolumeTest.class.getName());

	BoxVolume volume;


	private void s3Init() {
		String bucket = "qabel";
		String prefix = UUID.randomUUID().toString();

		DefaultAWSCredentialsProviderChain chain = new DefaultAWSCredentialsProviderChain();

		volume = new BoxVolume(bucket,prefix, chain.getCredentials(), new QblECKeyPair());
	}

	@Test
	public void testS3Init() {
		s3Init();
	}

	private void localInit() throws IOException {
		Path tempFolder = Files.createTempDirectory("");

		volume = new BoxVolume(new LocalReadBackend(tempFolder),
			new LocalWriteBackend(tempFolder),
			new QblECKeyPair());
	}

	@Test
	public void testLocalInit() throws IOException {
		localInit();
	}

	@Test
	public void testShareManagement() {
		fail("Not implemented");
	}

	@Test
	public void testFindRootRef() throws QblStorageException {
		s3Init();
		String root = volume.getRootRef();
		assertThat(UUID.fromString(root), isA(UUID.class));
	}

}