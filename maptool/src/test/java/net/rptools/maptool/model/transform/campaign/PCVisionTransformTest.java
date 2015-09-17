package net.rptools.maptool.model.transform.campaign;

import junit.framework.TestCase;

public class PCVisionTransformTest extends TestCase {

	public void testIt() throws Exception {

		String str = "one two three <tokenType>PC</tokenType>blah blah blah<hasSight>false</hasSight>something something";
		assertEquals("one two three <tokenType>PC</tokenType>blah blah blah<hasSight>true</hasSight>something something", new PCVisionTransform().transform(str));
	}
}
