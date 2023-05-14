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
package net.rptools.maptool.model.sheet.stats;

import java.net.URL;

/**
 * Record for maintaining stat sheet details.
 *
 * @param name The name of the stat sheet.
 * @param description The description of the stat sheet.
 * @param entry The entry point for the stat sheet.
 * @param propertyType the Property Type that this stat sheet is for.
 * @param width The width of the stat sheet.
 * @param height The height of the stat sheet.
 * @param namespace The namespace of the add-on that provides the spreadsheet.
 */
public record StatSheet(
    String name,
    String description,
    URL entry,
    String propertyType,
    String width,
    String height,
    String namespace) {}
