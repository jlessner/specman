package jackson;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.ObjectMapper;

import specman.model.IfElseSchrittModel;
import specman.model.SchrittSequenzModel;

public class JacksonTest {

	@Test
	void test() throws Exception {
		SchrittSequenzModel model = new SchrittSequenzModel();
		IfElseSchrittModel ifelse = new IfElseSchrittModel();
		model.schritte.add(ifelse);
	    ObjectMapper objectMapper = new ObjectMapper();
	    objectMapper.enableDefaultTyping();
	    String json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(model);
	    System.out.println(json);
	    SchrittSequenzModel model2 = objectMapper.readValue(json, SchrittSequenzModel.class);
	    assertNotNull(model2.schritte);
	    assertEquals(IfElseSchrittModel.class, model2.schritte.get(0).getClass());
	}
}
