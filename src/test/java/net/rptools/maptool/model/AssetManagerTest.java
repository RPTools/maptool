/*
 * This software Copyright by the RPTools.net development team, and
 * licensed under the Affero GPL Version 3 or, at your option, any later
 * version.
 *
 * MapTool Source Code is distributed in the hope that it will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * You should have received a copy of the GNU Affero General Public
 * License * along with this source Code.  If not, please visit
 * <https://www.gnu.org/licenses/> and specifically the Affero license
 * text at <https://www.gnu.org/licenses/agpl.html>.
 */
package net.rptools.maptool.model;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import java.util.function.BooleanSupplier;
import java.util.stream.Stream;
import net.rptools.lib.MD5Key;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class AssetManagerTest {

  @TempDir Path tempDir;

  private File originalCacheDir;

  // Minimal 1x1 PNG image data
  private static final byte[] SAMPLE_PNG_BYTES =
      new byte[] {
        (byte) 0x89,
        0x50,
        0x4E,
        0x47,
        0x0D,
        0x0A,
        0x1A,
        0x0A,
        0x00,
        0x00,
        0x00,
        0x0D,
        0x49,
        0x48,
        0x44,
        0x52,
        0x00,
        0x00,
        0x00,
        0x01,
        0x00,
        0x00,
        0x00,
        0x01,
        0x08,
        0x06,
        0x00,
        0x00,
        0x00,
        0x1F,
        0x15,
        (byte) 0xC4,
        (byte) 0x89,
        0x00,
        0x00,
        0x00,
        0x0A,
        0x49,
        0x44,
        0x41,
        0x54,
        0x78,
        (byte) 0x9C,
        0x63,
        0x00,
        0x01,
        0x00,
        0x00,
        0x05,
        0x00,
        0x01,
        0x0D,
        0x0A,
        0x2D,
        (byte) 0xB4,
        0x00,
        0x00,
        0x00,
        0x00,
        0x49,
        0x45,
        0x4E,
        0x44,
        (byte) 0xAE,
        0x42,
        0x60,
        (byte) 0x82
      };

  @BeforeEach
  void setUp() {
    originalCacheDir = AssetManager.getCacheDir();
    AssetManager.setCacheDir(tempDir.toFile());
    AssetManager.setUsePersistentCache(true);
    // All tests using the sample image share the same key, so make sure it is not in memory.
    AssetManager.removeAsset(new MD5Key(SAMPLE_PNG_BYTES));
  }

  @AfterEach
  void tearDown() {
    AssetManager.removeAsset(new MD5Key(SAMPLE_PNG_BYTES));
    AssetManager.setCacheDir(originalCacheDir);
  }

  @Test
  void testSubdirectoryPartitioningAndInfoForNewAssets() throws Exception {
    Asset asset = Asset.createImageAsset("test-token", SAMPLE_PNG_BYTES);
    MD5Key key = asset.getMD5Key();
    String keyStr = key.toString();
    String expectedPrefix = keyStr.substring(0, 2).toLowerCase();

    AssetManager.putAsset(asset);

    File expectedSubdir = new File(tempDir.toFile(), expectedPrefix);
    File expectedAssetFile = new File(expectedSubdir, keyStr);
    File expectedInfoFile = new File(expectedSubdir, keyStr + ".info");

    // File writing happens asynchronously in worker thread
    waitFor(() -> expectedAssetFile.exists() && expectedInfoFile.exists(), 5000);

    assertEquals(expectedAssetFile, AssetManager.getAssetCacheFile(key));
    assertEquals(expectedInfoFile, AssetManager.getAssetInfoFile(key));

    // Verify .info content
    Properties props = AssetManager.getAssetInfo(key);
    assertEquals("1", props.getProperty(AssetManager.VERSION));
    assertEquals("test-token", props.getProperty(AssetManager.NAME));
    assertEquals(Asset.Type.IMAGE.name(), props.getProperty(AssetManager.TYPE));
    assertEquals("image/png", props.getProperty(AssetManager.MEDIA_TYPE));
    assertEquals("MD5", props.getProperty(AssetManager.HASH_ALGORITHM));
  }

  static Stream<Arguments> typesNotDerivableFromMediaType() {
    return Stream.of(
        Arguments.of(Asset.Type.DATA, "blob", new byte[] {1, 2, 3, 4, 5, 9, 0, 7}),
        Arguments.of(Asset.Type.HANDLEBARS, "template", "<p>{{name}}</p>".getBytes()),
        Arguments.of(Asset.Type.MTLIB, "net.rptools.test", new byte[] {'P', 'K', 3, 4, 0, 0}));
  }

  @ParameterizedTest
  @MethodSource("typesNotDerivableFromMediaType")
  void testAssetTypeSurvivesPersistentCacheRoundTrip(Asset.Type type, String name, byte[] data)
      throws Exception {
    Asset asset = Asset.createAsset(name, data, type);
    MD5Key key = asset.getMD5Key();

    AssetManager.putAsset(asset);
    File assetFile = AssetManager.getSubdirAssetFile(key);
    waitFor(assetFile::exists, 5000);
    AssetManager.removeAsset(key);

    Asset loaded = AssetManager.getAsset(key);
    assertNotNull(loaded);
    assertEquals(type, loaded.getType());
    AssetManager.removeAsset(key);
  }

  @Test
  void testFallbackToLegacyAssetCreatesHardLinkAndSubdirInfo() throws Exception {
    MD5Key key = new MD5Key(SAMPLE_PNG_BYTES);
    String keyStr = key.toString();
    String prefix = keyStr.substring(0, 2).toLowerCase();

    // Write directly to root legacy location
    File legacyAssetFile = new File(tempDir.toFile(), keyStr);
    File legacyInfoFile = new File(tempDir.toFile(), keyStr + ".info");
    FileUtils.writeByteArrayToFile(legacyAssetFile, SAMPLE_PNG_BYTES);
    writeLegacyInfo(legacyInfoFile, "legacy-token", Asset.Type.IMAGE);

    // Retrieve asset - should locate legacy asset, create hard link in subdir, and write new .info
    Asset loaded = AssetManager.getAsset(key);
    assertNotNull(loaded);
    assertEquals("legacy-token", loaded.getName());
    assertEquals(key, loaded.getMD5Key());

    File subdirAssetFile = new File(new File(tempDir.toFile(), prefix), keyStr);
    File subdirInfoFile = new File(new File(tempDir.toFile(), prefix), keyStr + ".info");

    // Both legacy and subdir files must exist (hard linked, not moved)
    assertTrue(legacyAssetFile.exists(), "Legacy file must still exist (not moved)");
    assertTrue(legacyInfoFile.exists(), "Legacy info file must still exist");
    assertHardLinked(legacyAssetFile, subdirAssetFile);

    // Verify new .info file was created in the subdirectory
    assertTrue(subdirInfoFile.exists(), "New .info file must be created in subdirectory");
    Properties newProps = AssetManager.getAssetInfo(key);
    assertEquals("1", newProps.getProperty(AssetManager.VERSION));
    assertEquals("legacy-token", newProps.getProperty(AssetManager.NAME));
    assertEquals(Asset.Type.IMAGE.name(), newProps.getProperty(AssetManager.TYPE));
    assertEquals("image/png", newProps.getProperty(AssetManager.MEDIA_TYPE));
    assertEquals("MD5", newProps.getProperty(AssetManager.HASH_ALGORITHM));

    // getAssetCacheFile now resolves to the subdirectory file
    assertEquals(subdirAssetFile, AssetManager.getAssetCacheFile(key));
  }

  @Test
  void testFallbackToLegacyAssetKeepsLegacyType() throws Exception {
    byte[] data = new byte[] {1, 2, 3, 4, 5, 9, 0, 7};
    MD5Key key = new MD5Key(data);
    FileUtils.writeByteArrayToFile(new File(tempDir.toFile(), key.toString()), data);
    writeLegacyInfo(new File(tempDir.toFile(), key + ".info"), "blob", Asset.Type.DATA);

    Asset loaded = AssetManager.getAsset(key);
    assertNotNull(loaded);
    assertEquals(Asset.Type.DATA, loaded.getType());
    assertEquals(
        Asset.Type.DATA.name(),
        loadProperties(AssetManager.getSubdirAssetInfoFile(key)).getProperty(AssetManager.TYPE));
    AssetManager.removeAsset(key);
  }

  @Test
  void testHardLinkFailureDoesNotCopyMoveOrCreateSubdirInfo() throws Exception {
    Asset asset = Asset.createImageAsset("unlinkable-legacy", SAMPLE_PNG_BYTES);
    MD5Key key = asset.getMD5Key();
    String keyStr = key.toString();
    String prefix = keyStr.substring(0, 2).toLowerCase();

    File legacyAssetFile = new File(tempDir.toFile(), keyStr);
    FileUtils.writeByteArrayToFile(legacyAssetFile, SAMPLE_PNG_BYTES);

    // Create a regular file where the subdirectory should be to force link creation failure
    File subdirAsFile = new File(tempDir.toFile(), prefix);
    FileUtils.writeByteArrayToFile(subdirAsFile, new byte[] {0x00});

    File targetSubdirInfo = new File(subdirAsFile, keyStr + ".info");

    // Attempting link should fail because parent is a regular file, not a directory
    assertFalse(
        AssetManager.tryLinkLegacyAssetToSubdir(key, asset),
        "Linking must fail when directory cannot be created");

    // Must NOT copy or move legacy asset
    assertTrue(legacyAssetFile.exists(), "Legacy file must still exist untouched");
    assertEquals(SAMPLE_PNG_BYTES.length, legacyAssetFile.length());

    // Must NOT create subdir .info file
    assertFalse(targetSubdirInfo.exists(), "Must not create .info file when hard link fails");
  }

  @Test
  void testPersistingExistingLegacyAssetHardLinksInsteadOfDuplicating() throws Exception {
    Asset asset = Asset.createImageAsset("existing-legacy", SAMPLE_PNG_BYTES);
    MD5Key key = asset.getMD5Key();
    String keyStr = key.toString();
    String prefix = keyStr.substring(0, 2).toLowerCase();

    // Setup legacy file in root
    File legacyAssetFile = new File(tempDir.toFile(), keyStr);
    FileUtils.writeByteArrayToFile(legacyAssetFile, SAMPLE_PNG_BYTES);
    writeLegacyInfo(new File(tempDir.toFile(), keyStr + ".info"), "existing-legacy", null);

    // Linking happens synchronously, only new asset data is written by the worker thread
    AssetManager.putAsset(asset);

    File subdirAssetFile = new File(new File(tempDir.toFile(), prefix), keyStr);
    assertHardLinked(legacyAssetFile, subdirAssetFile);
    assertEquals(
        Asset.Type.IMAGE.name(),
        loadProperties(AssetManager.getSubdirAssetInfoFile(key)).getProperty(AssetManager.TYPE));
  }

  @Test
  void testMissingInfoWrittenToLegacyLocationWhenHardLinkFails() throws Exception {
    Asset asset = Asset.createImageAsset("missing-info-legacy", SAMPLE_PNG_BYTES);
    MD5Key key = asset.getMD5Key();
    String keyStr = key.toString();

    // Legacy asset exists, but info file does not, and the subdirectory can not be created
    FileUtils.writeByteArrayToFile(new File(tempDir.toFile(), keyStr), SAMPLE_PNG_BYTES);
    FileUtils.writeByteArrayToFile(
        new File(tempDir.toFile(), keyStr.substring(0, 2).toLowerCase()), new byte[] {0x00});

    File expectedInfoFile = new File(tempDir.toFile(), keyStr + ".info");
    assertEquals(expectedInfoFile, AssetManager.getAssetInfoFile(key));
    assertEquals(new File(tempDir.toFile(), keyStr + ".lnk"), AssetManager.getAssetLinkFile(key));

    AssetManager.putAsset(asset);

    // Older versions only read the legacy location, so the type must be present for them
    assertTrue(expectedInfoFile.exists());
    assertEquals(
        Asset.Type.IMAGE.name(), loadProperties(expectedInfoFile).getProperty(AssetManager.TYPE));
  }

  @Test
  void testCorruptedAssetDetectionAndPurge() throws Exception {
    Asset asset = Asset.createImageAsset("corrupt-test", SAMPLE_PNG_BYTES);
    MD5Key key = asset.getMD5Key();
    String keyStr = key.toString();
    String prefix = keyStr.substring(0, 2).toLowerCase();

    AssetManager.putAsset(asset);

    File expectedSubdir = new File(tempDir.toFile(), prefix);
    File assetFile = new File(expectedSubdir, keyStr);
    File infoFile = new File(expectedSubdir, keyStr + ".info");

    waitFor(assetFile::exists, 5000);

    // Corrupt the file on disk
    FileUtils.writeByteArrayToFile(assetFile, new byte[] {0x01, 0x02, 0x03});

    // Remove from in-memory cache to force disk load
    AssetManager.removeAsset(key);

    Asset loaded = AssetManager.getAsset(key);
    assertNull(loaded, "Corrupted asset should fail to load");
    assertFalse(assetFile.exists(), "Corrupted asset file must be purged");
    assertFalse(infoFile.exists(), "Companion info file must be purged when asset is corrupted");
  }

  @Test
  void testUnsupportedHashAlgorithmIsNotPurged() throws Exception {
    MD5Key key = new MD5Key(SAMPLE_PNG_BYTES);
    File assetFile = AssetManager.getSubdirAssetFile(key);
    FileUtils.writeByteArrayToFile(assetFile, SAMPLE_PNG_BYTES);
    Properties props = new Properties();
    props.put(AssetManager.HASH_ALGORITHM, "SHA-256");
    try (OutputStream out = new FileOutputStream(AssetManager.getSubdirAssetInfoFile(key))) {
      props.store(out, "Future Asset Info");
    }

    assertNull(AssetManager.getAsset(key));
    assertTrue(assetFile.exists(), "Assets from newer versions must not be treated as corrupt");
  }

  @Test
  void testShortKeyHandling() {
    MD5Key shortKey = new MD5Key("a");
    assertNull(AssetManager.getSubdirectoryPrefix(shortKey));

    File cacheFile = AssetManager.getAssetCacheFile(shortKey);
    assertEquals(new File(tempDir.toFile(), "a"), cacheFile);

    File infoFile = AssetManager.getAssetInfoFile(shortKey);
    assertEquals(new File(tempDir.toFile(), "a.info"), infoFile);

    File lnkFile = AssetManager.getAssetLinkFile(shortKey);
    assertEquals(new File(tempDir.toFile(), "a.lnk"), lnkFile);
  }

  @Test
  void testDirectoryTraversalKeyIsRejected() {
    MD5Key traversalKey = new MD5Key("../escaped");
    assertNull(AssetManager.getAssetCacheFile(traversalKey));
    assertNull(AssetManager.getAssetInfoFile(traversalKey));
    assertNull(AssetManager.getAssetLinkFile(traversalKey));
    assertNull(AssetManager.getAsset(traversalKey));
  }

  @Test
  void testKeyAliasingAnotherCachedAssetDoesNotDeleteIt() throws Exception {
    MD5Key victimKey = new MD5Key(SAMPLE_PNG_BYTES);
    File victimFile = new File(tempDir.toFile(), victimKey.toString());
    FileUtils.writeByteArrayToFile(victimFile, SAMPLE_PNG_BYTES);
    new File(tempDir.toFile(), "zz").mkdirs();

    assertNull(AssetManager.getAssetCacheFile(new MD5Key("zz/../" + victimKey)));
    assertNull(AssetManager.getAsset(new MD5Key("zz/../" + victimKey)));
    assertTrue(victimFile.exists(), "Asset referred to by an aliased key must not be purged");
  }

  private static void writeLegacyInfo(File infoFile, String name, Asset.Type type)
      throws IOException {
    Properties props = new Properties();
    props.put(AssetManager.NAME, name);
    if (type != null) {
      props.put(AssetManager.TYPE, type.name());
    }
    try (OutputStream out = new FileOutputStream(infoFile)) {
      props.store(out, "Legacy Asset Info");
    }
  }

  private static Properties loadProperties(File file) throws IOException {
    Properties props = new Properties();
    try (var in = Files.newInputStream(file.toPath())) {
      props.load(in);
    }
    return props;
  }

  private static void assertHardLinked(File legacyFile, File subdirFile) throws IOException {
    assertTrue(subdirFile.exists(), "Hard link in subdirectory must exist");
    Object legacyKey = Files.getAttribute(legacyFile.toPath(), "fileKey");
    Object subdirKey = Files.getAttribute(subdirFile.toPath(), "fileKey");
    if (legacyKey != null && subdirKey != null) {
      assertEquals(legacyKey, subdirKey, "Subdirectory file must be a hard link to legacy file");
    }
  }

  private static void waitFor(BooleanSupplier condition, long timeoutMs) throws Exception {
    long deadline = System.currentTimeMillis() + timeoutMs;
    while (!condition.getAsBoolean()) {
      if (System.currentTimeMillis() > deadline) {
        fail("Condition timed out after " + timeoutMs + "ms");
      }
      Thread.sleep(20);
    }
  }
}
