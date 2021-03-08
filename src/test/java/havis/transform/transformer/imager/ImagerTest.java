package havis.transform.transformer.imager;

import havis.transform.TransformException;
import havis.transform.ValidationException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class ImagerTest {

	public static void main(String[] args) throws TransformException,
			IOException, ValidationException {
		ImagerTest test = new ImagerTest();
		ImagerTransformer transformer = new ImagerTransformer();
		transformer.init(null);

		Report current = transformer.transform(test.getImageData());
		System.out.println(current);

	}

	private byte[] getImageData() throws IOException {
		byte[] data = null;
		try (InputStream is = this.getClass().getResourceAsStream("qr.jpg")) {
			try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
				byte[] buffer = new byte[2048];
				int len;
				while ((len = is.read(buffer)) > -1) {
					os.write(buffer, 0, len);
				}
				data = os.toByteArray();
			}
		}
		return data;
	}

}
