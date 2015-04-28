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
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Alexander "d4rkAlf" Johansson Werne
 *
 */
public class TestToken {
	Token token;

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
		token.setFacing(0);
		boolean hasFacing = token.hasFacing();
		assertTrue("hasFacing should return true", hasFacing == true);
	}

	/**
	 * Test method for
	 * {@link net.rptools.maptool.model.Token#setFacing(Integer)} and
	 * {@link net.rptools.maptool.model.Token#getAngleInDegrees()}.
	 */
	@Test
	public void testFacing() {
		int facing;

		token.setFacing(0);
		facing = token.getAngleInDegrees();
		assertEquals("Token does not have facing 0", 0, facing);

		token.setFacing(90);
		facing = token.getAngleInDegrees();
		assertEquals("Token does not have facing 90", 90, facing);

		token.setFacing(180);
		facing = token.getAngleInDegrees();
		assertEquals("Token does not have facing 180", 180, facing);

		token.setFacing(270);
		facing = token.getAngleInDegrees();
		assertEquals("Token does not have facing 270", 270, facing);
	}

	/**
	 * Test method for
	 * {@link net.rptools.maptool.model.Token#setFacing(Integer)} and
	 * {@link net.rptools.maptool.model.Token#getAngleInDegrees()}.
	 */
	@Test
	public void testFacingEdgeCases() {
		int facing;

		token.setFacing(-90);
		facing = token.getAngleInDegrees();
		assertEquals("Token does not have facing 270", 270, facing);

		token.setFacing(360);
		facing = token.getAngleInDegrees();
		assertEquals("Token does not have facing 0", 0, facing);

		token.setFacing(540);
		facing = token.getAngleInDegrees();
		assertEquals("Token does not have facing 180", 180, facing);
	}

}
