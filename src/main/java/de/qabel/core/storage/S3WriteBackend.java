package de.qabel.core.storage;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectResult;
import de.qabel.core.exceptions.QblStorageException;

import java.io.InputStream;

class S3WriteBackend extends StorageWriteBackend {

	final AmazonS3Client s3Client;
	final String bucket;
	final String prefix;

	S3WriteBackend(AWSCredentials credentials, String bucket, String prefix) {
		s3Client = new AmazonS3Client(credentials);
		this.bucket = bucket;
		this.prefix = prefix;

	}

	@Override
	long upload(String name, InputStream inputStream) throws QblStorageException {
		try {
			String path = prefix + '/' + name;
			s3Client.putObject(bucket, path, inputStream, new ObjectMetadata());
			ObjectMetadata objectMetadata = s3Client.getObjectMetadata(bucket, path);
			return objectMetadata.getLastModified().getTime();
		} catch (RuntimeException e) {
			throw new QblStorageException(e);
		}
	}

	@Override
	void delete(String name) throws QblStorageException {
		try {
			s3Client.deleteObject(bucket, prefix + '/' + name);
		} catch (RuntimeException e) {
			throw new QblStorageException(e);
		}
	}

}