package jackson;

import com.fasterxml.jackson.databind.exc.InvalidTypeIdException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.ObjectMapper;

import specman.SchrittID;
import specman.model.ModelEnvelope;
import specman.model.v001.EinfacherSchrittModel_V001;
import specman.model.v001.SchrittSequenzModel_V001;
import specman.model.v001.StruktogrammModel_V001;
import specman.view.RoundedBorderDecorationStyle;

import java.awt.*;

public class JacksonTest {

	@Test
	/** Jackson kann Typinformation im JSON unterbringen, so dass die Deserialisierung
	 * auch mit Objekten abgeleiteter Klassen klar kommt */
	void testDeserializationWithDerivedTypes() throws Exception {
		SchrittID id = new SchrittID(0);
		SchrittSequenzModel_V001 writemodel = new SchrittSequenzModel_V001(id, false, 10);
		EinfacherSchrittModel_V001 step1 = new EinfacherSchrittModel_V001
				(id.naechsteID(), null, Color.white.getRGB(), RoundedBorderDecorationStyle.None);
		writemodel.schritte.add(step1);
	    ObjectMapper objectMapper = new ObjectMapper();
	    objectMapper.enableDefaultTyping();
	    String json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(writemodel);
	    System.out.println(json);
	    SchrittSequenzModel_V001 readmodel = objectMapper.readValue(json, SchrittSequenzModel_V001.class);
	    assertNotNull(readmodel.schritte);
	    assertEquals(EinfacherSchrittModel_V001.class, readmodel.schritte.get(0).getClass());
	}

	@Test
	void testEnvelope() throws Exception {
		SchrittID id = new SchrittID(0);
		StruktogrammModel_V001 model = new StruktogrammModel_V001(null, 10, 100, null, null, null);
		ModelEnvelope writeenvelope = new ModelEnvelope();
		writeenvelope.model = model;
		writeenvelope.modelType = model.getClass().getName();
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.enableDefaultTyping();
		String json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(writeenvelope);
		System.out.println(json);

		ModelEnvelope readenvelope = objectMapper.readValue(json, ModelEnvelope.class);
		assertEquals(StruktogrammModel_V001.class, readenvelope.model.getClass());
	}

	@Test
	void testExceptionOnReadingUnsupportedModeltype() throws Exception {
		String UNKNOWN_CLASS = "unknown.model.class";
		String json = "{" +
				"  \"modelType\" : \"" + UNKNOWN_CLASS + "\"," +
				"  \"model\" : { }" +
				"}";
		ObjectMapper objectMapper = new ObjectMapper();
		try {
			objectMapper.readValue(json, ModelEnvelope.class);
			fail();
		}
		catch(InvalidTypeIdException x) {
			assertEquals(UNKNOWN_CLASS, x.getTypeId());
		}
	}

}
