package net.rptools.lib;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.rptools.lib.io.PackedFile;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;

/**
 * An object of this class is created in the {@link PersistenceUtil} code for the purpose of applying XML transforms to
 * data being read or written by a tool (MapTool). The application registers one or more
 * {@link ModelVersionTransformation} objects under a particular version. When the {@link #transform(String, String)}
 * method is called with an XML string and a version number string, all transforms currently registered will be searched
 * and those with a version number earlier than the one passed in will have their <code>transform</code> method called.
 * <p>
 * For example, a version number of "1.3.53" registered with a manager means that only versions later than that will
 * apply the given transform. So version "1.3.54" would have the transformation performed as would version "1.3.53.1".
 * Note that version numbers are cleaned (via {@link #cleanVersionNumber(String)} prior to being compared.
 * <p>
 * The current implementation is less-than-ideal as it requires:
 * <ul>
 * <li>the entire XML passed in as a String
 * <li>all XML is processed with transforms even if the version numbers indicate that it's not necessary
 * </ul>
 * <p>
 * Those can be mitigated somewhat by performing an upfront check of the file version number against the version numbers
 * stored in this object. If there are no transforms required then the alternate code in {@link PackedFile} that uses
 * InputStreams and Readers can be used instead, significantly reducing the memory footprint of the import process.
 * 
 * @author tcroft
 */
public class ModelVersionManager {
	private final Map<String, List<ModelVersionTransformation>> transformMap = new LinkedHashMap<String, List<ModelVersionTransformation>>();

	/**
	 * Compare the given version number (expected to be read from a file about to be imported) against the version
	 * numbers that have been registered with this transformation so far and return <code>true</code> only if later
	 * version numbers have been registered.
	 * <p>
	 * Essentially this allows the calling code to skip the step of reading the XML into a String, resulting in better
	 * performance since the InputStream or Reader can be passed directly to the XStream library. And because this will
	 * be the common case, it should typically improve performance.
	 * 
	 * @param fileVersion
	 *            version number string read from file being imported
	 * @return true if there are transforms needed
	 */
	public synchronized boolean isTransformationRequired(String fileVersion) {
		fileVersion = cleanVersionNumber(fileVersion);

		String[] entries = getTransforms();
		for (String entry : entries) {
			if (entry.equals(fileVersion) || isBefore(entry, fileVersion)) {
				continue;
			}
			return true;
		}
		return false;
	}

	/**
	 * Registers one or more transformations to execute when a campaign file earlier than the supplied version number is
	 * being processed. For example, if the version parameter were <b>1.3.66</b> then the list of transforms would run
	 * for any version before that, such as <b>1.3.64.1</b> or <b>1.3.65</b>.
	 * 
	 * @param version
	 *            time at which the change was made (only digits and dots are preserved)
	 * @param transforms
	 *            one or more transformations to apply to the input string
	 */
	public synchronized void registerTransformation(String version, ModelVersionTransformation... transforms) {
		version = cleanVersionNumber(version);

		List<ModelVersionTransformation> transformList = transformMap.get(version);
		if (transformList == null) {
			transformList = new LinkedList<ModelVersionTransformation>();
			transformMap.put(version, transformList);
		}
		for (ModelVersionTransformation transform : transforms) {
			transformList.add(transform);
		}
	}

	/**
	 * Converts the input string <code>xml</b> by applying all transformations
	 * that are registered for versions after <code>fileVersion</code>. Transformations are guaranteed to be applied in
	 * the order provided for any given version, and versions will be applied in order based on a simple String-based
	 * sort (this is wrong for single-digit versions compared against double-digit versions).
	 * 
	 * @param xml
	 *            normally XML input, but could be any string
	 * @param fileVersion
	 *            typically of the form <b>a.b.c</b> but can have any number of components
	 * @return the resulting string after all transforms are complete
	 */
	public synchronized String transform(String xml, String fileVersion) {
		fileVersion = cleanVersionNumber(fileVersion);

		String[] entries = getTransforms();
		for (String entry : entries) {
			if (isBefore(fileVersion, entry)) {
				for (ModelVersionTransformation transform : transformMap.get(entry)) {
					xml = transform.transform(xml);
				}
			}
		}
		return xml;
	}

	private String[] getTransforms() {
		Set<String> set = transformMap.keySet();
		String[] entries = new String[set.size()];
		set.toArray(entries);
		Arrays.sort(entries);
		return entries;
	}

	/**
	 * Tests whether the first parameter is a version number that comes before the version number stored in the second
	 * parameter. Version numbers are of the form <code>1.3.51</code> or <code>1.3.64.1</code> and may have any number
	 * of components separated by periods. If either parameter contains a component which cannot be parsed as an
	 * integer, the component is treated as <code>0</code> for the purposes of this comparison. Also, if a parameter has
	 * fewer components than the other, it is considered to have <code>0</code> in the place of each missing component.
	 * 
	 * @param version1
	 *            version registered as containing a change
	 * @param version2
	 *            version read in from the <b>properties.xml</b> entry
	 * @return true if version1 is before version2. false otherwise.
	 */
	public static boolean isBefore(String version1, String version2) {
		if (StringUtils.isBlank(version1)) {
			// if we don't know the version, then we should start at the beginning (before everything)
			return true;
		}
		if (StringUtils.isBlank(version2)) {
			// if there is no comparison version, then we should be up to date (nothing to change)
			return false;
		}

		String[] v1 = version1.indexOf(".") > 0 ? version1.split("\\.") : new String[] { version1 };
		String[] v2 = version2.indexOf(".") > 0 ? version2.split("\\.") : new String[] { version2 };

		int maxIndex = Math.max(v1.length, v2.length);
		int val1 = 0;
		int val2 = 0;
		for (int index = 0; index < maxIndex; ++index) {
			val1 = (index < v1.length) ? NumberUtils.toInt(v1[index], 0) : 0;
			val2 = (index < v2.length) ? NumberUtils.toInt(v2[index], 0) : 0;

			if (val1 < val2)
				return true;
			if (val1 > val2)
				return false;
		}
		return false;
	}

	/**
	 * Removes any characters from a version string other than digits and periods. If cleaning results in an empty
	 * value, <code>"0"</code> is returned instead.
	 * 
	 * @param version
	 *            the string to be cleaned
	 * @return the version with non-digits and periods stripped
	 */
	public static String cleanVersionNumber(String version) {
		if (version == null)
			return ModelVersionManager.DEFAULT_EMPTY_VERSION;
		String cleaned = version.replaceAll("[^\\d.]", "");
		return (cleaned.length() > 0) ? cleaned : ModelVersionManager.DEFAULT_EMPTY_VERSION;
	}

	private static final String DEFAULT_EMPTY_VERSION = "0";
}
