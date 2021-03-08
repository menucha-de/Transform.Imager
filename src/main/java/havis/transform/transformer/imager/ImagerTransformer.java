package havis.transform.transformer.imager;

import havis.transform.TransformException;
import havis.transform.Transformer;
import havis.transform.ValidationException;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.multi.GenericMultipleBarcodeReader;

public class ImagerTransformer implements Transformer {
	private final static Logger log = Logger.getLogger(ImagerTransformer.class
			.getName());

	final static String NAME = "imager";
	
	@Override
	public void init(Map<String, String> properties) throws ValidationException {
		if (properties != null) {
			for (Entry<String, String> entry : properties.entrySet()) {
				String key = entry.getKey();
				if (key != null) {
					switch (key) {
					default:
						if (key.startsWith(Transformer.PREFIX))
							throw new ValidationException("Unknown property '" + key + "'");
					}
				}
			}
		}
		
		
		/*
		 * Set classloader for imageIO to allow static SPI commands
		 */
		ClassLoader classLoader = Thread.currentThread()
				.getContextClassLoader();
		try {
			Thread.currentThread().setContextClassLoader(
					ClassLoader.getSystemClassLoader());
			ImageIO.read((InputStream) null);
		} catch (Throwable e) {
			log.log(Level.FINE, "ImageIO is up ", e);
		} finally {
			Thread.currentThread().setContextClassLoader(classLoader);
		}

	}

	@SuppressWarnings("unchecked")
	@Override
	public <T, S> T transform(S object) throws TransformException {
		Report current = null;
		byte[] stream = (byte[]) object;
		try {
			if (stream != null) {
				BufferedImage convert = null;
				try (ByteArrayInputStream bis = new ByteArrayInputStream(stream)) {
					convert = ImageIO.read(bis);
				} catch (Exception e) {
					log.log(Level.FINE, "Cannot transform image. ", e);
				}
				if (convert != null) {
					Map<String, Barcode> lastCodes = new HashMap<String, Barcode>();
					LuminanceSource source = new BufferedImageLuminanceSource(
							convert);
					BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(
							source));
					Result[] scan = new GenericMultipleBarcodeReader(
							new MultiFormatReader()).decodeMultiple(bitmap);
					if (scan.length > 0) {
						for (Result result : scan) {
							Barcode barcode = new Barcode();
							barcode.setCode(result.getText());
							barcode.setType(result.getBarcodeFormat().name());
							lastCodes.put(result.getText(), barcode);
						}
						current = new Report();
						current.setTimestamp(new Date().getTime());
						for (Barcode code : lastCodes.values()) {
							current.getBarcodes().add(code);
						}
					} else {
						current = new Report();
						current.setTimestamp(new Date().getTime());
					}
				}
			}
		} catch (NotFoundException e) {
			current = new Report();
			current.setTimestamp(new Date().getTime());
		} catch (Exception e) {
			log.log(Level.FINE, "Error parsing barcode.", e);
		}
		return (T) current;
	}

}
