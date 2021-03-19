package havis.transform.transformer.imager;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import mockit.Mocked;
import mockit.NonStrictExpectations;
import mockit.Verifications;

import org.junit.Assert;
import org.junit.Test;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.multi.GenericMultipleBarcodeReader;

public class ImagerTransformerTest {

	@Test
	public void init(final @Mocked ImageIO image) throws Exception {
		ImagerTransformer transformer = new ImagerTransformer();
		transformer.init(null);
		new Verifications() {
			{
				ImageIO.read(this.<InputStream> withNull());
				times = 1;
			}
		};
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

	@Test
	public void setValue(final @Mocked Logger log) throws Exception {
		ImagerTransformer transformer = new ImagerTransformer();
		transformer.init(null);
		Report current = transformer.transform(getImageData());
		Assert.assertNotNull(current);
		Assert.assertNotNull(current.getTimestamp());
		Assert.assertNotEquals(0, current.getTimestamp());
		List<Barcode> barcodes = current.getBarcodes();
		Assert.assertEquals(1, barcodes.size());
		Assert.assertEquals("http://www.menucha.de", barcodes.get(0).getCode());
		new Verifications() {
			{
				log.log(Level.FINE, "Cannot transform image. ",
						this.<Exception> withNotNull());
				times = 0;
			}
		};
	}

	@Test
	public void setValueWithNotTransformableBarcode(final @Mocked ImageIO io,
			final @Mocked Logger log,
			final @Mocked BufferedImageLuminanceSource bils) throws Exception {
		new NonStrictExpectations() {
			{
				ImageIO.read(this.<InputStream> withNull());
				result = new IOException();
			}
		};
		ImagerTransformer transformer = new ImagerTransformer();
		transformer.init(null);

		Report current = null;
		try {
			current = transformer.transform(new byte[] { 0x01 });
		} catch (Exception e) {

			Assert.assertNull(current);
			new Verifications() {
				{
					log.log(Level.FINE, "Cannot transform image. ",
							this.<Exception> withNotNull());
					times = 1;

					new BufferedImageLuminanceSource(new BufferedImage(anyInt,
							anyInt, anyInt));
					times = 0;
				}
			};
		}
	}

	@Test
	public void setValueWithUnparsableBarcode(final @Mocked Logger log,
			final @Mocked GenericMultipleBarcodeReader gmbr) throws Exception {
		new NonStrictExpectations() {
			{
				new GenericMultipleBarcodeReader(
						this.<MultiFormatReader> withNotNull())
						.decodeMultiple(this.<BinaryBitmap> withNotNull());
				result = new Exception();
			}
		};

		ImagerTransformer transformer = new ImagerTransformer();
		transformer.init(null);
		Report current = transformer.transform(getImageData());
		Assert.assertNull(current);
		new Verifications() {
			{
				new GenericMultipleBarcodeReader(
						this.<MultiFormatReader> withNotNull())
						.decodeMultiple(this.<BinaryBitmap> withNotNull());
				times = 1;

				log.log(Level.FINE, "Error parsing barcode.",
						this.<Exception> withNotNull());
				times = 1;
			}
		};
	}



}
