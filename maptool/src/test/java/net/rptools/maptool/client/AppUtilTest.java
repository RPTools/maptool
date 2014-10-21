package net.rptools.maptool.client;

import java.io.File;

import junit.framework.TestCase;

/**
 * Unit tests for AppUtil class
 */
public class AppUtilTest extends TestCase {
	
	private static final String REL_DIR_ALT = ".net.rptools.AppUtilTest";
	private static final String ABS_DIR_UNIX = "/.net.rptools/AppUtilTest";
	private static final String ABS_DIR_WINDOWS = "C:\\.net.rptools\\AppUtilTest";
	
	/**
	 * Test getDataDir()
	 * @throws Exception
	 */
	public void testGetDataDir() throws Exception {
		
		/*
		 * Verify that the default path is:
		 * user.home + "/" + ".maptool"
		 */
		AppUtil.reset();
		File dataDir = new File(System.getProperty("user.home") 
				+ "/" + AppUtil.DEFAULT_DATADIR_NAME);
		assertEquals(dataDir, AppUtil.getDataDir());
		assertTrue(dataDir.exists());
		
		/*
		 * Verify that a relative path in DATADIR_PROPERTY_NAME
		 * is prefixed with user.home.
		 */
		AppUtil.reset();
		System.setProperty(AppUtil.DATADIR_PROPERTY_NAME, REL_DIR_ALT);
		dataDir = new File(System.getProperty("user.home") 
				+ "/" + REL_DIR_ALT);
		assertEquals(dataDir, AppUtil.getDataDir());		
		
		/* 
		 * The next two tests verify that a absolute paths in 
		 * DATADIR_PROPERTY_NAME is not prefixed with user.home.
		 */		
		AppUtil.reset();
		/* Absolute UNIX path. */
		System.setProperty(AppUtil.DATADIR_PROPERTY_NAME, ABS_DIR_UNIX);
		assertEquals(new File(ABS_DIR_UNIX), AppUtil.getDataDir());
		
		AppUtil.reset();
		/* Absolute Windows path. */
		System.setProperty(AppUtil.DATADIR_PROPERTY_NAME, ABS_DIR_WINDOWS);
		assertEquals(new File(ABS_DIR_WINDOWS), AppUtil.getDataDir());
		
		// Cleanup:
		// Reset the system property.
		System.clearProperty(AppUtil.DATADIR_PROPERTY_NAME);
		
	}
	
	/**
	 * Test getAppHome() and getAppHome(String subdir)
	 * @throws Exception
	 */
	public void testGetAppHome() throws Exception {
		
		/*
		 * Verify that getAppHome() creates the default
		 * maptool data directory.
		 */
		AppUtil.reset();
		File dataDir = new File(System.getProperty("user.home") 
				+ "/" + AppUtil.DEFAULT_DATADIR_NAME);
		assertEquals(dataDir, AppUtil.getAppHome());
		assertTrue(dataDir.exists());
		
		/*
		 * Verify that getAppHome(String subdir) creates
		 * the subdirectory subdir.
		 */
		AppUtil.reset();
		File subDir = new File(System.getProperty("user.home") 
				+ "/" + AppUtil.DEFAULT_DATADIR_NAME 
				+ "/" + REL_DIR_ALT);
		assertEquals(subDir, AppUtil.getAppHome(REL_DIR_ALT));
		assertTrue(subDir.exists());
		
		/*
		 * Verify that getAppHome() creates a non-default
		 * maptool data directory. We delete the non-default
		 * maptool data directory first, check to see if 
		 * it is created and writable, and then delete it 
		 * on cleanup.
		 */
		AppUtil.reset();
		File dataDirAlt = new File(System.getProperty("user.home")
				+ "/" + REL_DIR_ALT);
		System.setProperty(AppUtil.DATADIR_PROPERTY_NAME, dataDirAlt.getAbsolutePath());
		if (dataDirAlt.exists())
		{
			assertTrue(dataDirAlt.delete());
		}
		assertEquals(dataDirAlt, AppUtil.getAppHome());
		assertTrue(dataDirAlt.exists());
		assertTrue(dataDirAlt.canWrite());
		
		/*
		 * Before deleting REL_DIR_ALT, we make it unwritable
		 * and test again to see if getAppHome returns null
		 * as expected.
		 */
		AppUtil.reset();
		assertTrue(dataDirAlt.setReadOnly());
		try {
			File result = AppUtil.getAppHome();
			fail("Read only datadir should have thrown an exception");
		} catch (Throwable t) {
			// Expected
		}
		
		// Cleanup:
		// Remove REL_DIR_ALT.
		subDir.delete();
		// Remove the default maptool directory. This will
		// fail if the user has data there, otherwise if
		// this was a clean test run it will be deleted.
		dataDir.delete();
		// Remove the alternate data directory.
		dataDirAlt.delete();
		// Reset the system property.
		System.clearProperty(AppUtil.DATADIR_PROPERTY_NAME);
	}
}
