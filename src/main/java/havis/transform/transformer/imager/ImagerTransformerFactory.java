package havis.transform.transformer.imager;

import havis.transform.Transformer;
import havis.transform.TransformerFactory;
import havis.transform.TransformerProperties;

@TransformerProperties(value = "imager", src = byte[].class, dst = Report.class)
public class ImagerTransformerFactory implements TransformerFactory{

	@Override
	public Transformer newInstance() {
		return new ImagerTransformer();
	}

}
