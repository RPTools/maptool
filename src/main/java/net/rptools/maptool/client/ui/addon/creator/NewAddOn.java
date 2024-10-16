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
 * <http://www.gnu.org/licenses/> and specifically the Affero license
 * text at <http://www.gnu.org/licenses/agpl.html>.
 */
package net.rptools.maptool.client.ui.addon.creator;

import java.util.List;

/**
 * Represents a new AddOn to be created.
 *
 * @param name The name of the AddOn.
 * @param version The version of the AddOn.
 * @param namespace The namespace of the AddOn.
 * @param gitURL The git URL of the AddOn.
 * @param website The website of the AddOn.
 * @param license The license of the AddOn.
 * @param shortDescription The short description of the AddOn.
 * @param description The description of the AddOn.
 * @param authors The authors of the AddOn.
 * @param createEvents Whether to create example events.
 * @param createSlashCommands Whether to create example slash commands.
 * @param createMTSProperties Whether to create example MTS properties.
 * @param createUDFs Whether to create example UDFs. (not yet implemented).
 * @param readme The readme of the AddOn.
 * @param licenseText The license text of the AddOn.
 */
public record NewAddOn(
    String name,
    String version,
    String namespace,
    String gitURL,
    String website,
    String license,
    String shortDescription,
    String description,
    List<String> authors,
    boolean createEvents,
    boolean createSlashCommands,
    boolean createMTSProperties,
    boolean createUDFs,
    String readme,
    String licenseText) {}
