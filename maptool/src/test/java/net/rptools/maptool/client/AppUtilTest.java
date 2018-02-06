package net.rptools.maptool.client;

import java.io.File;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import junit.framework.TestCase;

/**
 * Unit tests for AppUtil class
 */
public class AppUtilTest extends TestCase {
	private static final String REL_DIR_ALT = ".net.rptools.AppUtilTest";
	private static final String ABS_DIR_UNIX = "/.net.rptools/AppUtilTest";
	private static final String ABS_DIR_WINDOWS = "C:\\.net.rptools\\AppUtilTest";

	private static final String user_home = System.getProperty("user.home") + "/";

	@Override
	@Before
	public void setUp() throws Exception {
		AppUtil.reset();
	}

	@Override
	@After
	public void tearDown() throws Exception {
		// Cleanup:
		// Reset the system property.
		System.clearProperty(AppUtil.DATADIR_PROPERTY_NAME);
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		// Cleanup:
		// Remove REL_DIR_ALT.
		File dataDirAlt = new File(user_home + REL_DIR_ALT);
		dataDirAlt.setWritable(true);
		dataDirAlt.delete();

		// Remove the default maptool directory. This will
		// fail if the user has data there, otherwise if
		// this was a clean test run it will be deleted.
		File dataDir = new File(user_home + AppUtil.DEFAULT_DATADIR_NAME);
		dataDir.delete();
	}

	@Test
	public void testDataDir() {
		/*
		 * Verify that the default path is: user.home + "/" + ".maptool"
		 */
		File dataDir = new File(user_home + AppUtil.DEFAULT_DATADIR_NAME);
		assertEquals(dataDir, AppUtil.getDataDir());
		assertTrue(dataDir.exists());
		// We attempt to delete this directory in @AfterClass, but that won't work unless it was empty,
		// in which case it's okay to delete it. :)
	}

	@Test
	public void testDataDirSystemProperty() {
		/*
		 * Verify that a relative path in DATADIR_PROPERTY_NAME is prefixed with
		 * user.home.
		 */
		System.setProperty(AppUtil.DATADIR_PROPERTY_NAME, REL_DIR_ALT);
		File dataDir = new File(user_home + REL_DIR_ALT);
		File result = AppUtil.getDataDir();
		assertEquals(dataDir, result);
	}

	@Test
	public void testUnixDataDirIsAbsolute() {
		/*
		 * The next two tests verify that absolute paths in
		 * DATADIR_PROPERTY_NAME are not prefixed with user.home.
		 */
		AppUtil.reset();
		/* Absolute UNIX path. */
		System.setProperty(AppUtil.DATADIR_PROPERTY_NAME, ABS_DIR_UNIX);
		assertEquals(new File(ABS_DIR_UNIX), AppUtil.getDataDir());
	}

	@Test
	public void testWindowsDataDirIsAbsolute() {
		if (AppUtil.WINDOWS) {
			AppUtil.reset();
			/* Absolute Windows path. */
			System.setProperty(AppUtil.DATADIR_PROPERTY_NAME, ABS_DIR_WINDOWS);
			assertEquals(new File(ABS_DIR_WINDOWS), AppUtil.getDataDir());
		}
	}

	/**
	 * Test getAppHome() and getAppHome(String subdir)
	 *
	 * @throws Exception
	 */
	@Test
	public void testAppHome() {
		/*
		 * Verify that getAppHome() creates the default maptool data directory.
		 */
		AppUtil.reset();
		File dataDir = new File(user_home + AppUtil.DEFAULT_DATADIR_NAME);
		assertEquals(dataDir, AppUtil.getAppHome());
		assertTrue(dataDir.exists());
	}

	@Test
	public void testAppHomeSystemProperty() {
		/*
		 * Verify that getAppHome(String subdir) creates the subdirectory
		 * subdir.
		 */
		AppUtil.reset();
		File subDir = new File(user_home + AppUtil.DEFAULT_DATADIR_NAME + "/" + REL_DIR_ALT);
		assertEquals(subDir, AppUtil.getAppHome(REL_DIR_ALT));
		assertTrue(subDir.exists());
	}

	@Test
	public void testAppHomeWithRelativeSubdir() {
		/*
		 * Verify that getAppHome() creates a non-default maptool data
		 * directory. We delete the non-default maptool data directory first,
		 * check to see if it is created and writable, and then delete it on
		 * cleanup.
		 */
		AppUtil.reset();
		File dataDirAlt = new File(user_home + REL_DIR_ALT);
		System.setProperty(AppUtil.DATADIR_PROPERTY_NAME, dataDirAlt.getAbsolutePath());
		if (dataDirAlt.exists()) {
			assertTrue("Couldn't delete directory", dataDirAlt.delete());
		}
		assertEquals(dataDirAlt, AppUtil.getAppHome());
		assertTrue("Directory wasn't created", dataDirAlt.exists());
		assertTrue("Directory is not writable", dataDirAlt.canWrite());
		assertTrue("Couldn't delete directory", dataDirAlt.delete());
	}

	@Test(expected = java.lang.RuntimeException.class)
	public void testAppHomeWithRelativeSubdirPermissionCheck() {
		/*
		 * Before deleting REL_DIR_ALT, we make it unwritable and test again to
		 * see if getAppHome() returns null as expected.
		 */
		File dataDirAlt = new File(user_home + REL_DIR_ALT);
		System.setProperty(AppUtil.DATADIR_PROPERTY_NAME, dataDirAlt.getAbsolutePath());
		boolean result = dataDirAlt.mkdir();
		assertTrue("Directory not created?", result);

		result = dataDirAlt.setReadOnly();
		assertTrue("Directory " + dataDirAlt + " is still writable?", result);

		AppUtil.getAppHome(); // Should throw RuntimeException when dataDir is read-only
	}
}
