package de.qabel.core.storage;

import de.qabel.core.exceptions.QblStorageException;
import de.qabel.core.exceptions.QblStorageNotFound;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

class S3ReadBackend extends StorageReadBackend {

	String root;
	private CloseableHttpClient httpclient;

	S3ReadBackend(String bucket, String prefix) {
		 this("https://"+bucket+".s3.amazonaws.com/"+prefix);
	}

	S3ReadBackend(String root) {
		this.root = root;
		httpclient = HttpClients.createMinimal();
	}

	InputStream download(String name) throws QblStorageException {
		URI uri;
		try {
			uri = new URI(this.root + '/' + name);
		} catch (URISyntaxException e) {
			throw new QblStorageException(e);
		}
		HttpGet httpGet = new HttpGet(uri);
		HttpResponse response;
		try {
			response = httpclient.execute(httpGet);
		} catch (IOException e) {
			throw new QblStorageException(e);
		}
		int status = response.getStatusLine().getStatusCode();
		if (status == 404 || status == 403) {
			throw new QblStorageNotFound("File not found");
		} else if (status != 200) {
			throw new QblStorageException("Download error");
		}
		HttpEntity entity = response.getEntity();
		if (entity == null) {
			throw new QblStorageException("No content");
		}
		try {
			InputStream content = entity.getContent();
			return content;
		} catch (IOException e) {
			throw new QblStorageException(e);
		}
	}
}
