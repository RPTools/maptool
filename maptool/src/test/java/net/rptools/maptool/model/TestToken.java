/*
 *  This software copyright by various authors including the RPTools.net
 *  development team, and licensed under the LGPL Version 3 or, at your
 *  option, any later version.
 *
 *  Portions of this software were originally covered under the Apache
 *  Software License, Version 1.1 or Version 2.0.
 *
 *  See the file LICENSE elsewhere in this distribution for license details.
 */

package net.rptools.maptool.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * @author Alexander "d4rkAlf" Johansson Werne
 *
 */
public class TestToken {
	Token token;

	@Rule
	public ExpectedException exception = ExpectedException.none();

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		token = new Token();
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
		token = null;
	}

	/**
	 * Test method for {@link net.rptools.maptool.model.Token#hasFacing()}.
	 */
	@Test
	public void testDoesNotHaveFacing() {
		boolean hasFacing = token.hasFacing();
		assertTrue("hasFacing should return false", hasFacing == false);
	}

	/**
	 * Test method for {@link net.rptools.maptool.model.Token#hasFacing()}.
	 */
	@Test
	public void testHasFacing() {
		token.setFacingInDegrees(0);
		boolean hasFacing = token.hasFacing();
		assertTrue("hasFacing should return true", hasFacing == true);
	}

	/**
	 * Test method for
	 * {@link net.rptools.maptool.model.Token#setFacingInDegrees(float)} and
	 * {@link net.rptools.maptool.model.Token#getFacingInDegrees()}.
	 */
	@Test
	public void testFacingInDegrees() {
		float facing;

		token.setFacingInDegrees(0);
		facing = token.getFacingInDegrees();
		assertEquals("Token does not have facing 0", 0, facing, 0);

		token.setFacingInDegrees(90);
		facing = token.getFacingInDegrees();
		assertEquals("Token does not have facing 90", 90, facing, 0);

		token.setFacingInDegrees(180);
		facing = token.getFacingInDegrees();
		assertEquals("Token does not have facing 180", 180, facing, 0);

		token.setFacingInDegrees(270);
		facing = token.getFacingInDegrees();
		assertEquals("Token does not have facing 270", 270, facing, 0);
	}

	/**
	 * Test method for
	 * {@link net.rptools.maptool.model.Token#setFacingInDegrees(float)} and
	 * {@link net.rptools.maptool.model.Token#getFacingInDegrees()}.
	 */
	@Test
	public void testFacingEdgeCases() {
		float facing;

		token.setFacingInDegrees(-90);
		facing = token.getFacingInDegrees();
		assertEquals("Token does not have facing 270", 270, facing, 0);

		token.setFacingInDegrees(360);
		facing = token.getFacingInDegrees();
		assertEquals("Token does not have facing 0", 0, facing, 0);

		token.setFacingInDegrees(540);
		facing = token.getFacingInDegrees();
		assertEquals("Token does not have facing 180", 180, facing, 0);
	}

	/**
	 * Test method for
	 * {@link net.rptools.maptool.model.Token#setFacingInRadians(float)} and
	 * {@link net.rptools.maptool.model.Token#getFacingInRadians()}.
	 */
	@Test
	public void testFacingInRadians() {
		float facing;

		token.setFacingInRadians(0);
		facing = token.getFacingInRadians();
		assertEquals("Token does not have facing 0 radians", 0, facing, 0.01);

		token.setFacingInRadians((float) Math.PI / 2);
		facing = token.getFacingInRadians();
		assertEquals("Token does not have facing PI/2 radians",  Math.PI / 2, facing, 0.01);

		token.setFacingInRadians((float) Math.PI);
		facing = token.getFacingInRadians();
		assertEquals("Token does not have facing PI radians", Math.PI, facing, 0.01);

		token.setFacingInRadians((float) (3 * Math.PI / 2));
		facing = token.getFacingInRadians();
		assertEquals("Token does not have facing 3PI/2 radians", 3 * Math.PI / 2, facing, 0.01);
	}

	/**
	 * Test method for
	 * {@link net.rptools.maptool.model.Token#removeFacing()}.
	 */
	@Test
	public void testRemoveFacing() {
		boolean hasFacing;

		hasFacing = token.hasFacing();
		assertFalse("The token has a facing when it should not.", hasFacing);

		token.setFacingInDegrees(-90);
		hasFacing = token.hasFacing();
		assertTrue("The token does not have a facing when it should", hasFacing);

		token.removeFacing();
		hasFacing = token.hasFacing();
		assertFalse("The token has a facing even after removal", hasFacing);
	}

	@Test
	public void testException() {
		exception.expect(IllegalStateException.class);
		token.getFacingInDegrees();
	}
}
