/*
 * This software copyright by various authors including the RPTools.net
 * development team, and licensed under the LGPL Version 3 or, at your option,
 * any later version.
 * 
 * Portions of this software were originally covered under the Apache Software
 * License, Version 1.1 or Version 2.0.
 * 
 * See the file LICENSE elsewhere in this distribution for license details.
 */

package net.rptools.maptool.client.ui.campaignproperties;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.StringReader;
import java.text.ParseException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.swing.AbstractAction;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;

import net.rptools.lib.swing.SwingUtil;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.ui.zone.ZoneRenderer;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.AssetManager;
import net.rptools.maptool.model.Campaign;
import net.rptools.maptool.model.CampaignProperties;
import net.rptools.maptool.model.GUID;
import net.rptools.maptool.model.Light;
import net.rptools.maptool.model.LightSource;
import net.rptools.maptool.model.ShapeType;
import net.rptools.maptool.model.SightType;
import net.rptools.maptool.model.drawing.DrawableColorPaint;
import net.rptools.maptool.util.PersistenceUtil;
import net.rptools.maptool.util.StringUtil;

import com.jeta.forms.components.panel.FormPanel;

public class CampaignPropertiesDialog extends JDialog {
	public enum Status {
		OK, CANCEL
	}

	private TokenPropertiesManagementPanel tokenPropertiesPanel;
	private TokenStatesController tokenStatesController;
	private TokenBarController tokenBarController;

	private Status status;
	private FormPanel formPanel;
	private Campaign campaign;

	public CampaignPropertiesDialog(JFrame owner) {
		super(owner, "Campaign Properties", true);
		setMinimumSize(new Dimension(450, 450)); // These sizes mess up my custom LAF settings. :(
		//		setPreferredSize(new Dimension(450, 450));	// If the dialog were packed() would they be needed?

		initialize();
		pack(); // FJE

		//		setSize(635, 605);
	}

	public Status getStatus() {
		return status;
	}

	@Override
	public void setVisible(boolean b) {
		if (b) {
			SwingUtil.centerOver(this, MapTool.getFrame());
		} else {
			MapTool.getFrame().repaint();
		}
		super.setVisible(b);
	}

	private void initialize() {
		setLayout(new GridLayout());
		formPanel = new FormPanel("net/rptools/maptool/client/ui/forms/campaignPropertiesDialog.xml");

		initTokenPropertiesDialog(formPanel);
		tokenStatesController = new TokenStatesController(formPanel);
		tokenBarController = new TokenBarController(formPanel);
		tokenBarController.setNames(tokenStatesController.getNames());

		initOKButton();
		initCancelButton();
		initAddRepoButton();
		initAddGalleryIndexButton();
		initDeleteRepoButton();

		initImportButton();
		initExportButton();

		add(formPanel);

		// Escape key
		formPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "cancel");
		formPanel.getActionMap().put("cancel", new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				cancel();
			}
		});
		getRootPane().setDefaultButton(getOKButton());
	}

	private void initTokenPropertiesDialog(FormPanel panel) {
		tokenPropertiesPanel = new TokenPropertiesManagementPanel();

		panel.getFormAccessor("propertiesPanel").replaceBean("tokenPropertiesPanel", tokenPropertiesPanel);
		panel.reset();
	}

	public JTextField getNewServerTextField() {
		return formPanel.getTextField("newServer");
	}

	private void initAddRepoButton() {
		JButton button = (JButton) formPanel.getButton("addRepoButton");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String newRepo = getNewServerTextField().getText();
				if (newRepo == null || newRepo.length() == 0) {
					return;
				}
				// TODO: Check for uniqueness
				((DefaultListModel) getRepositoryList().getModel()).addElement(newRepo);
			}
		});
	}

	private void initAddGalleryIndexButton() {
		JButton button = (JButton) formPanel.getButton("addGalleryIndexButton");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// TODO: Check for uniqueness
				((DefaultListModel) getRepositoryList().getModel()).addElement("http://www.rptools.net/image-indexes/gallery.rpax.gz");
			}
		});
	}

	public void initDeleteRepoButton() {
		JButton button = (JButton) formPanel.getButton("deleteRepoButton");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int[] selectedRows = getRepositoryList().getSelectedIndices();
				Arrays.sort(selectedRows);
				for (int i = selectedRows.length - 1; i >= 0; i--) {
					((DefaultListModel) getRepositoryList().getModel()).remove(selectedRows[i]);
				}
			}
		});
	}

	private void cancel() {
		status = Status.CANCEL;
		setVisible(false);
	}

	private void accept() {
		try {
			copyUIToCampaign();
			AssetManager.updateRepositoryList();
			status = Status.OK;
			setVisible(false);
		} catch (IllegalArgumentException iae) {
			MapTool.showError(iae.getMessage(), iae);
		}
	}

	public void setCampaign(Campaign campaign) {
		this.campaign = campaign;
		copyCampaignToUI(campaign.getCampaignProperties());
	}

	private void copyCampaignToUI(CampaignProperties campaignProperties) {
		JEditorPane epane = (JEditorPane) formPanel.getComponentByName("lightHelp");
		epane.setCaretPosition(0);
		tokenPropertiesPanel.copyCampaignToUI(campaignProperties);
		updateRepositoryList(campaignProperties);

		String text;
		text = updateSightPanel(campaignProperties.getSightTypeMap());
		getSightPanel().setText(text);
		getSightPanel().setCaretPosition(0);

		text = updateLightPanel(campaignProperties.getLightSourcesMap());
		getLightPanel().setText(text);
		getLightPanel().setCaretPosition(0);

		tokenStatesController.copyCampaignToUI(campaignProperties);
		tokenBarController.copyCampaignToUI(campaignProperties);
		//		updateTableList();
	}

	private String updateSightPanel(Map<String, SightType> sightTypeMap) {
		StringBuilder builder = new StringBuilder();
		for (SightType sight : sightTypeMap.values()) {
			builder.append(sight.getName()).append(": ");

			switch (sight.getShape()) {
			case SQUARE:
				builder.append("square ");
				if (sight.getDistance() != 0)
					builder.append("distance=").append(StringUtil.formatDecimal(sight.getDistance())).append(' ');
				break;
			case CIRCLE:
				builder.append("circle ");
				if (sight.getDistance() != 0)
					builder.append("distance=").append(StringUtil.formatDecimal(sight.getDistance())).append(' ');
				break;
			case CONE:
				builder.append("cone ");
				if (sight.getArc() != 0)
					builder.append("arc=").append(StringUtil.formatDecimal(sight.getArc())).append(' ');
				if (sight.getOffset() != 0)
					builder.append("offset=").append(StringUtil.formatDecimal(sight.getOffset())).append(' ');
				if (sight.getDistance() != 0)
					builder.append("distance=").append(StringUtil.formatDecimal(sight.getDistance())).append(' ');
				break;
			default:
				throw new IllegalArgumentException("Invalid shape?!");
			}
			// Multiplier
			if (sight.getMultiplier() != 1 && sight.getMultiplier() != 0) {
				builder.append("x").append(StringUtil.formatDecimal(sight.getMultiplier())).append(' ');
			}
			// Personal light
			if (sight.getPersonalLightSource() != null) {
				LightSource source = sight.getPersonalLightSource();

				double range = source.getMaxRange();
				builder.append("r").append(StringUtil.formatDecimal(range)).append(' ');
			}
			builder.append('\n');
		}
		return builder.toString();
	}

	private String updateLightPanel(Map<String, Map<GUID, LightSource>> lightSources) {
		StringBuilder builder = new StringBuilder();
		for (Entry<String, Map<GUID, LightSource>> entry : lightSources.entrySet()) {
			builder.append(entry.getKey());
			builder.append("\n----\n");

			for (LightSource lightSource : entry.getValue().values()) {
				builder.append(lightSource.getName()).append(":");

				if (lightSource.getType() != LightSource.Type.NORMAL) {
					builder.append(' ').append(lightSource.getType().name().toLowerCase());
				}
				String lastShape = ""; // this forces 'circle' to be printed
				double lastArc = 90;
				boolean lastGM = false;
				boolean lastOwner = false;
				for (Light light : lightSource.getLightList()) {
					String shape = null;
					// TODO: This HAS to change, the lights need to be auto describing, this hard wiring sucks
					if (lightSource.getType() == LightSource.Type.AURA) {
						// Currently these are mutually exclusive but perhaps not in the future?
						if (light.isGM() && light.isGM() != lastGM)
							builder.append(" GM");
						if (light.isOwnerOnly() && light.isOwnerOnly() != lastOwner)
							builder.append(" OWNER");
						lastGM = light.isGM();
						lastOwner = light.isOwnerOnly();
					}
					if (light.getShape() != null) {
						switch (light.getShape()) {
						case SQUARE:
						case CIRCLE:
							// TODO: Make this a preference
							shape = light.getShape().toString().toLowerCase();
							break;
						case CONE:
						//							if (light.getArcAngle() != 0 && light.getArcAngle() != 90 && light.getArcAngle() != lastArc)
						{
							lastArc = light.getArcAngle();
							shape = "cone arc=" + StringUtil.formatDecimal(lastArc);
						}
							//							else
							//								shape = "cone";
							break;
						}
						if (!lastShape.equals(shape))
							builder.append(' ').append(shape);
						lastShape = shape;
					}
					builder.append(' ').append(StringUtil.formatDecimal(light.getRadius()));
					if (light.getPaint() instanceof DrawableColorPaint) {
						Color color = (Color) light.getPaint().getPaint();
						builder.append(toHex(color));
					}
				}
				if (lightSource.getLumens() != 0) {
					builder.append(" lumens=" + lightSource.getLumens());
				}
				builder.append('\n');
			}
			builder.append('\n');
		}
		return builder.toString();
	}

	private String toHex(Color color) {
		StringBuilder builder = new StringBuilder("#");

		builder.append(padLeft(Integer.toHexString(color.getRed()), '0', 2));
		builder.append(padLeft(Integer.toHexString(color.getGreen()), '0', 2));
		builder.append(padLeft(Integer.toHexString(color.getBlue()), '0', 2));

		return builder.toString();
	}

	private String padLeft(String str, char padChar, int length) {
		while (str.length() < length) {
			str = padChar + str;
		}
		return str;
	}

	private void updateRepositoryList(CampaignProperties properties) {
		DefaultListModel model = new DefaultListModel();
		for (String repo : properties.getRemoteRepositoryList()) {
			model.addElement(repo);
		}
		getRepositoryList().setModel(model);
	}

	public JList getRepositoryList() {
		return formPanel.getList("repoList");
	}

	private void copyUIToCampaign() {
		tokenPropertiesPanel.copyUIToCampaign(campaign);

		campaign.getRemoteRepositoryList().clear();
		for (int i = 0; i < getRepositoryList().getModel().getSize(); i++) {
			String repo = (String) getRepositoryList().getModel().getElementAt(i);
			campaign.getRemoteRepositoryList().add(repo);
		}
		Map<String, Map<GUID, LightSource>> lightMap;
		lightMap = commitLightMap(getLightPanel().getText(), campaign.getLightSourcesMap());
		campaign.getLightSourcesMap().clear();
		campaign.getLightSourcesMap().putAll(lightMap);

		commitSightMap(getSightPanel().getText());
		tokenStatesController.copyUIToCampaign(campaign);
		tokenBarController.copyUIToCampaign(campaign);

		ZoneRenderer zr = MapTool.getFrame().getCurrentZoneRenderer();
		if (zr != null) {
			zr.getZoneView().flush();
			zr.flushFog();
			zr.flushLight();
			MapTool.getFrame().refresh();
		}
	}

	private void commitSightMap(final String text) {
		List<SightType> sightList = new LinkedList<SightType>();
		LineNumberReader reader = new LineNumberReader(new BufferedReader(new StringReader(text)));
		String line = null;
		String toBeParsed = null, errmsg = null;
		List<String> errlog = new LinkedList<String>();
		try {
			while ((line = reader.readLine()) != null) {
				line = line.trim();

				// Blanks
				if (line.length() == 0 || line.indexOf(":") < 1) {
					continue;
				}
				// Parse line
				int split = line.indexOf(":");
				String label = line.substring(0, split).trim();
				String value = line.substring(split + 1).trim();

				if (label.length() == 0) {
					continue;
				}
				// Parse Details
				double magnifier = 1;
				LightSource personalLight = null;

				String[] args = value.split("\\s+");
				ShapeType shape = ShapeType.CIRCLE;
				int arc = 90;
				float range = 0;
				int offset = 0;
				double pLightRange = 0;

				for (String arg : args) {
					assert arg.length() > 0; // The split() uses "one or more spaces", removing empty strings
					try {
						shape = ShapeType.valueOf(arg.toUpperCase());
						continue;
					} catch (IllegalArgumentException iae) {
						// Expected when not defining a shape
					}
					try {
						if (arg.startsWith("x")) {
							toBeParsed = arg.substring(1); // Used in the catch block, below
							errmsg = "msg.error.mtprops.sight.multiplier"; // (ditto)
							magnifier = StringUtil.parseDecimal(toBeParsed);
						} else if (arg.startsWith("r")) { // XXX Why not "r=#" instead of "r#"??
							toBeParsed = arg.substring(1);
							errmsg = "msg.error.mtprops.sight.range";
							pLightRange = StringUtil.parseDecimal(toBeParsed);
						} else if (arg.startsWith("arc=") && arg.length() > 4) {
							toBeParsed = arg.substring(4);
							errmsg = "msg.error.mtprops.sight.arc";
							arc = StringUtil.parseInteger(toBeParsed);
						} else if (arg.startsWith("distance=") && arg.length() > 9) {
							toBeParsed = arg.substring(9);
							errmsg = "msg.error.mtprops.sight.distance";
							range = StringUtil.parseDecimal(toBeParsed).floatValue();
						} else if (arg.startsWith("offset=") && arg.length() > 7) {
							toBeParsed = arg.substring(7);
							errmsg = "msg.error.mtprops.sight.offset";
							offset = StringUtil.parseInteger(toBeParsed);
						} else {
							toBeParsed = arg;
							errmsg = I18N.getText("msg.error.mtprops.sight.unknownField", reader.getLineNumber(), toBeParsed);
							errlog.add(errmsg);
						}
					} catch (ParseException e) {
						assert errmsg != null;
						errlog.add(I18N.getText(errmsg, reader.getLineNumber(), toBeParsed));
					}
				}
				if (pLightRange > 0) {
					personalLight = new LightSource();
					personalLight.add(new Light(shape, 0, pLightRange, arc, null));
				}
				SightType sight = new SightType(label, magnifier, personalLight, shape, arc);
				sight.setDistance(range);
				sight.setOffset(offset);

				// Store
				sightList.add(sight);
			}
		} catch (IOException ioe) {
			MapTool.showError("msg.error.mtprops.sight.ioexception", ioe);
		}
		if (!errlog.isEmpty()) {
			// Show the user a list of errors so they can (attempt to) correct all of them at once
			MapTool.showFeedback(errlog.toArray());
			errlog.clear();
			throw new IllegalArgumentException(); // Don't save sights...
		}
		campaign.setSightTypes(sightList);
	}

	/**
	 * Converts the string stored in <code>getLightPanel().getText()</code> into
	 * a Map that relates a group of light sources to a Map of GUID and
	 * LightSource.
	 * <p>
	 * The format for the text is as follows:
	 * <ol>
	 * <li>Any line starting with a dash ("-") is a comment and is ignored.
	 * <li>Blank lines (those containing only zero or more spaces) are group
	 * separators.
	 * <li>The first line of a sequence is the group name.
	 * <li>Within a group, any line without a colon (":") is ignored.
	 * <li>Remaining lines are of the following format:
	 * <p>
	 * <b>
	 * <code>[Gm | Owner] [Circle+ | Square | Cone] [Normal+ | Aura] [Arc=angle] distance [#rrggbb]</code>
	 * </b>
	 * </p>
	 * <p>
	 * Brackets indicate optional components. A plus sign follows any default
	 * value for a given field. Fields starting with an uppercase letter are
	 * literal text (although they are case-insensitive). Fields that do not
	 * start with an uppercase letter represent user-supplied values, typically
	 * numbers (such as <code>angle</code>, <code>distance</code>, and
	 * <code>#rrggbb</code>). The <code>GM</code>/<code>Owner</code> field is
	 * only valid for Auras.
	 * </p>
	 * </ol>
	 * </p>
	 */
	private Map<String, Map<GUID, LightSource>> commitLightMap(final String text, final Map<String, Map<GUID, LightSource>> originalLightSourcesMap) {
		Map<String, Map<GUID, LightSource>> lightMap = new TreeMap<String, Map<GUID, LightSource>>();
		LineNumberReader reader = new LineNumberReader(new BufferedReader(new StringReader(text)));
		String line = null;
		List<String> errlog = new LinkedList<String>();

		try {
			String currentGroupName = null;
			Map<GUID, LightSource> lightSourceMap = null;

			while ((line = reader.readLine()) != null) {
				line = line.trim();

				// Comments
				if (line.length() > 0 && line.charAt(0) == '-') {
					continue;
				}
				// Blank lines
				if (line.length() == 0) {
					if (currentGroupName != null) {
						lightMap.put(currentGroupName, lightSourceMap);
					}
					currentGroupName = null;
					continue;
				}
				// New group
				if (currentGroupName == null) {
					currentGroupName = line;
					lightSourceMap = new HashMap<GUID, LightSource>();
					continue;
				}
				// Item
				int split = line.indexOf(":");
				if (split < 1) {
					continue;
				}

				String name = line.substring(0, split).trim();
				LightSource lightSource = new LightSource(name);
				ShapeType shape = ShapeType.CIRCLE; // TODO: Make a preference for default shape
				double arc = 0;
				boolean gmOnly = false;
				boolean owner = false;
				String distance = null;
				for (String arg : line.substring(split + 1).split("\\s+")) {
					arg = arg.trim();
					if (arg.length() == 0) {
						continue;
					}
					if (arg.equalsIgnoreCase("GM")) {
						gmOnly = true;
						owner = false;
						continue;
					}
					if (arg.equalsIgnoreCase("OWNER")) {
						gmOnly = false;
						owner = true;
						continue;
					}
					// Lumens designation
					if (arg.toUpperCase().startsWith("LUMENS=")) {
						try {
							lightSource.setLumens(
									Integer.parseInt(arg.substring(7)));
							continue;
						} catch (NullPointerException noe) {
							errlog.add(I18N.getText(
									"msg.error.mtprops.light.lumens",
									reader.getLineNumber(), arg));
						}
					}
					// Shape designation ?
					try {
						shape = ShapeType.valueOf(arg.toUpperCase());
						continue;
					} catch (IllegalArgumentException iae) {
						// Expected when not defining a shape
					}

					// Type designation ?
					try {
						LightSource.Type type = LightSource.Type.valueOf(arg.toUpperCase());
						lightSource.setType(type);
						continue;
					} catch (IllegalArgumentException iae) {
						// Expected when not defining a shape
					}

					// Parameters
					split = arg.indexOf('=');
					if (split > 0) {
						String key = arg.substring(0, split);
						String value = arg.substring(split + 1);

						// TODO: Make this a generic map to pass instead of 'arc'
						if ("arc".equalsIgnoreCase(key)) {
							try {
								arc = StringUtil.parseDecimal(value);
								shape = ShapeType.CONE; // If the user specifies an arc, force the shape to CONE
							} catch (ParseException pe) {
								errlog.add(I18N.getText("msg.error.mtprops.light.arc", reader.getLineNumber(), value));
							}
						}
						continue;
					}
					Color color = null;
					distance = arg;
					split = arg.indexOf("#");
					if (split > 0) {
						String colorString = arg.substring(split); // Keep the '#'
						distance = arg.substring(0, split);
						color = Color.decode(colorString);
					}
					boolean isAura = lightSource.getType() == LightSource.Type.AURA;
					if (!isAura && (gmOnly || owner)) {
						errlog.add(I18N.getText("msg.error.mtprops.light.gmOrOwner", reader.getLineNumber()));
						gmOnly = false;
						owner = false;
					}
					owner = gmOnly == true ? false : owner;
					try {
						Light t = new Light(shape, 0, StringUtil.parseDecimal(distance), arc, color != null ? new DrawableColorPaint(color) : null, gmOnly, owner);
						lightSource.add(t);
					} catch (ParseException pe) {
						errlog.add(I18N.getText("msg.error.mtprops.light.distance", reader.getLineNumber(), distance));
					}
				}
				// Keep ID the same if modifying existing light
				// TODO FJE Why?  Is there some benefit to doing so?  Changes to light sources require the map to be re-rendered anyway, don't they?
				if (originalLightSourcesMap.containsKey(currentGroupName)) {
					for (LightSource ls : originalLightSourcesMap.get(currentGroupName).values()) {
						if (ls.getName().equalsIgnoreCase(name)) {
							lightSource.setId(ls.getId());
							break;
						}
					}
				}
				lightSourceMap.put(lightSource.getId(), lightSource);
			}
			// Last group
			if (currentGroupName != null) {
				lightMap.put(currentGroupName, lightSourceMap);
			}
		} catch (IOException ioe) {
			MapTool.showError("msg.error.mtprops.light.ioexception", ioe);
		}
		if (!errlog.isEmpty()) {
			MapTool.showFeedback(errlog.toArray());
			errlog.clear();
			throw new IllegalArgumentException(); // Don't save lights...
		}
		return lightMap;
	}

	public JEditorPane getLightPanel() {
		return (JEditorPane) formPanel.getTextComponent("lightPanel");
	}

	public JEditorPane getSightPanel() {
		return (JEditorPane) formPanel.getTextComponent("sightPanel");
	}

	public JTextArea getTokenPropertiesTextArea() {
		return (JTextArea) formPanel.getTextComponent("tokenProperties");
	}

	public JButton getOKButton() {
		return (JButton) formPanel.getButton("okButton");
	}

	private void initOKButton() {
		getOKButton().addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				accept();
			}
		});
	}

	public JButton getCancelButton() {
		return (JButton) formPanel.getButton("cancelButton");
	}

	public JButton getImportButton() {
		return (JButton) formPanel.getButton("importButton");
	}

	public JButton getExportButton() {
		return (JButton) formPanel.getButton("exportButton");
	}

	private void initCancelButton() {
		getCancelButton().addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				status = Status.CANCEL;
				setVisible(false);
			}
		});
	}

	private void initImportButton() {
		getImportButton().addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser chooser = MapTool.getFrame().getLoadPropsFileChooser();

				if (chooser.showOpenDialog(MapTool.getFrame()) != JFileChooser.APPROVE_OPTION)
					return;

				final File selectedFile = chooser.getSelectedFile();
				EventQueue.invokeLater(new Runnable() {
					public void run() {
						CampaignProperties properties = PersistenceUtil.loadCampaignProperties(selectedFile);
						// TODO: Allow specifying whether it is a replace or merge
						if (properties != null) {
							MapTool.getCampaign().mergeCampaignProperties(properties);
							copyCampaignToUI(properties);
						}
					}
				});
			}
		});
	}

	private void initExportButton() {
		getExportButton().addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// TODO: Remove this hack.  Specifically, make the export use a properties object
				// composed of the current dialog entries instead of directly from the campaign
				copyUIToCampaign();
				// END HACK

				JFileChooser chooser = MapTool.getFrame().getSavePropsFileChooser();
				if (chooser.showSaveDialog(MapTool.getFrame()) != JFileChooser.APPROVE_OPTION)
					return;

				File selectedFile = chooser.getSelectedFile();
				if (selectedFile.exists()) {
					if (selectedFile.getName().endsWith(".rpgame")) {
						if (!MapTool.confirm("Import into game settings file?")) {
							return;
						}
					} else if (!MapTool.confirm("Overwrite existing file?")) {
						return;
					}
				}
				try {
					PersistenceUtil.saveCampaignProperties(campaign, chooser.getSelectedFile());
					MapTool.showInformation("Properties Saved.");
				} catch (IOException ioe) {
					MapTool.showError("Could not save properties: ", ioe);
				}
			}
		});
	}

	public static void main(String[] args) {
		JFrame frame = new JFrame("Testing campaign properties dialog syntax-specific fields");
		CampaignPropertiesDialog cpd = new CampaignPropertiesDialog(frame);
		// @formatter:off
		String lights = "D20\n" +
				"----\n" +
				"Lantern, Bullseye - 60 : cone arc=60 60#f0f0f0 120#330000\n" +
				"Lantern, Hooded - 30 : circle 30 60#330000 arc=60 120#f0f0f0\n" +
				"Torch - 20 : circle 20 40#330000\n" +
				"\n" +
				"Aura\n" +
				"----\n" +
				"Arc 120deg OWNERonly - 20 : owner aura arc=120 22.5#115511\n" +
				"Arc 60deg - 60 : aura cone arc=60 62.5#77ffaa\n" +
				"Circle - 20 : aura circle 22.5#220000\n" +
				"Circle GM+Owner : aura circle GM Owner 62.5#ff8080\n" +
				"Circle GMonly : aura circle GM 62.5#ff8080\n" +
				"Fancy - 30/60/120 : aura GM circle 30 60#330000 owner arc=60 120#f0f0f0\n" +
				"\n";
		// @formatter:on
		System.out.print(lights);

		Map<String, Map<GUID, LightSource>> originalLightSourcesMap = new HashMap<String, Map<GUID, LightSource>>();
		Map<String, Map<GUID, LightSource>> lightMap = new HashMap<String, Map<GUID, LightSource>>();
		try {
			lightMap = cpd.commitLightMap(lights, originalLightSourcesMap);
		} catch (Exception e) {
		}

		String text = cpd.updateLightPanel(lightMap);
		System.out.print(text);

		// keySet() might be empty if an exception occurred.
		for (String string : lightMap.keySet()) {
			System.out.println("\nGroup Name: " + string);
			System.out.println("-------------");
			for (GUID guid : lightMap.get(string).keySet()) {
				LightSource ls = lightMap.get(string).get(guid);
				System.out.println(ls.getType() + ", " + ls.getName() + ":");
				for (Light light : ls.getLightList()) {
					System.out.print("  [shape=" + light.getShape());
					if (light.getShape() == ShapeType.CONE) {
						System.out.print(", arc=" + light.getArcAngle());
						System.out.print(", facing=" + light.getFacingOffset());
					}
					System.out.print(", gm=" + light.isGM());
					System.out.print(", owner=" + light.isOwnerOnly());
					System.out.print(", radius=" + light.getRadius());
					System.out.print(", color=" + light.getPaint() + "]\n");
				}
			}
		}
		System.exit(1);
	}
}
