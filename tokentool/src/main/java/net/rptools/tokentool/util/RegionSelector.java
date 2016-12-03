package net.rptools.tokentool.util;

import java.awt.AWTException;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Stroke;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Area;
import java.awt.image.BufferedImage;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

public class RegionSelector {

	private Rectangle bounds;

	public void run(Rectangle bounds) {

		this.bounds = bounds;

		Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
		try {
			BufferedImage image = new Robot().createScreenCapture(new Rectangle(0, 0, size.width, size.height));

			JDialog frame = new RegionSelectorDialog(image);
			frame.setUndecorated(true);
			frame.setBounds(0, 0, size.width, size.height);
			frame.setModal(true);

			frame.setVisible(true);
		} catch (AWTException ae) {
			ae.printStackTrace();
		}
	}

	private class RegionSelectorDialog extends JDialog {

		private BufferedImage screenImage;
		private Rectangle[][] resizeBounds = new Rectangle[3][3];
		private int dragRow;
		private int dragCol;
		private int dragOffsetX;
		private int dragOffsetY;
		private boolean isDragging;

		private int hotspotSize = 10;

		public RegionSelectorDialog(BufferedImage screenImage) {
			this.screenImage = screenImage;

			setContentPane(createContentPane());
			getContentPane().requestFocusInWindow();
		}

		private JPanel createContentPane() {
			final JPanel panel = new JPanel() {

				boolean firstPaint = true;

				@Override
				protected void paintComponent(Graphics g) {

					Dimension size = getSize();
					Graphics2D g2d = (Graphics2D) g;
					Stroke oldStroke = g2d.getStroke();

					// Background
					g.drawImage(screenImage, 0, 0, null);

					// Region
					g.setColor(Color.red);
					g2d.setStroke(new BasicStroke(3));
					g2d.draw(bounds);
					g2d.setStroke(oldStroke);

					// Top row
					resizeBounds[0][0] = new Rectangle(bounds.x, bounds.y, hotspotSize, hotspotSize);
					resizeBounds[0][1] = new Rectangle(bounds.x + bounds.width / 2 - hotspotSize / 2, bounds.y, hotspotSize, hotspotSize);
					resizeBounds[0][2] = new Rectangle(bounds.x + bounds.width - hotspotSize, bounds.y, hotspotSize, hotspotSize);

					// Middle row
					resizeBounds[1][0] = new Rectangle(bounds.x, bounds.y + bounds.height / 2 - hotspotSize / 2, hotspotSize, hotspotSize);
					resizeBounds[1][2] = new Rectangle(bounds.x + bounds.width - hotspotSize, bounds.y + bounds.height / 2 - hotspotSize / 2, hotspotSize, hotspotSize);

					// Bottom row
					resizeBounds[2][0] = new Rectangle(bounds.x, bounds.y + bounds.height - hotspotSize, hotspotSize, hotspotSize);
					resizeBounds[2][1] = new Rectangle(bounds.x + bounds.width / 2 - hotspotSize / 2, bounds.y + bounds.height - hotspotSize, hotspotSize, hotspotSize);
					resizeBounds[2][2] = new Rectangle(bounds.x + bounds.width - hotspotSize, bounds.y + bounds.height - hotspotSize, hotspotSize, hotspotSize);

					g2d.setColor(Color.black);
					for (int row = 0; row < resizeBounds.length; row++) {
						for (int col = 0; col < resizeBounds[row].length; col++) {

							Rectangle rect = resizeBounds[row][col];
							if (rect == null) {
								continue;
							}

							g2d.fill(rect);
						}
					}

					// Instructions
					FontMetrics fm = g.getFontMetrics();
					String str = "Drag area or resize area, press ENTER to commit";
					int strwidth = SwingUtilities.computeStringWidth(fm, str);
					Rectangle strBounds = new Rectangle(bounds.x + bounds.width / 2 - strwidth / 2 - 5, bounds.y + bounds.height / 2 - fm.getHeight() + 3, strwidth + 10, fm.getHeight() + 10);

					g.setColor(Color.white);
					g2d.fill(strBounds);
					g2d.setStroke(new BasicStroke(3));

					g.setColor(Color.black);
					g2d.draw(strBounds);
					g2d.setStroke(oldStroke);

					g.drawString(str, bounds.x + bounds.width / 2 - strwidth / 2, bounds.y + bounds.height / 2 - fm.getHeight() / 2 + fm.getAscent());

					// Dim the rest of the screen
					if (firstPaint) {
						repaint();
					} else {
						Area screen = new Area(new Rectangle(0, 0, size.width, size.height));
						screen.subtract(new Area(bounds));
						g.setColor(new Color(0, 0, 0, 50));
						g2d.fill(screen);
					}
					firstPaint = false;
				}

				@Override
				public boolean isFocusable() {
					return true;
				}

			};
			panel.addMouseListener(new MouseAdapter() {
				@Override
				public void mousePressed(java.awt.event.MouseEvent e) {
					if (!bounds.contains(e.getPoint())) {
						close();
						return;
					}

					for (int row = 0; row < resizeBounds.length; row++) {
						for (int col = 0; col < resizeBounds[row].length; col++) {

							Rectangle rect = resizeBounds[row][col];
							if (rect == null) {
								continue;
							}

							if (rect.contains(e.getPoint())) {
								dragRow = row;
								dragCol = col;

								isDragging = true;
								return;
							}
						}
					}
					if (bounds.contains(e.getPoint())) {
						dragRow = -1;
						dragCol = -1;
						dragOffsetX = e.getX() - bounds.x;
						dragOffsetY = e.getY() - bounds.y;

						isDragging = true;
						return;
					}

					isDragging = false;
				}

				@Override
				public void mouseReleased(MouseEvent e) {
					isDragging = false;
				}
			});
			panel.addFocusListener(new FocusListener() {
				public void focusGained(java.awt.event.FocusEvent e) {

				}

				public void focusLost(java.awt.event.FocusEvent e) {
					close();
				}
			});
			panel.addMouseMotionListener(new MouseMotionListener() {

				public void mouseMoved(MouseEvent e) {

					for (int row = 0; row < resizeBounds.length; row++) {
						for (int col = 0; col < resizeBounds[row].length; col++) {

							Rectangle rect = resizeBounds[row][col];
							if (rect == null) {
								continue;
							}

							if (rect.contains(e.getPoint())) {
								if (row == 0 && col == 0) {
									setCursor(Cursor.getPredefinedCursor(Cursor.NW_RESIZE_CURSOR));
								}
								if (row == 0 && col == 1) {
									setCursor(Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR));
								}
								if (row == 0 && col == 2) {
									setCursor(Cursor.getPredefinedCursor(Cursor.NE_RESIZE_CURSOR));
								}
								if (row == 1 && col == 0) {
									setCursor(Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR));
								}
								if (row == 1 && col == 2) {
									setCursor(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR));
								}
								if (row == 2 && col == 0) {
									setCursor(Cursor.getPredefinedCursor(Cursor.SW_RESIZE_CURSOR));
								}
								if (row == 2 && col == 1) {
									setCursor(Cursor.getPredefinedCursor(Cursor.S_RESIZE_CURSOR));
								}
								if (row == 2 && col == 2) {
									setCursor(Cursor.getPredefinedCursor(Cursor.SE_RESIZE_CURSOR));
								}
								return;
							}
						}
					}
					setCursor(Cursor.getDefaultCursor());
				}

				public void mouseDragged(MouseEvent e) {
					if (isDragging) {
						if (dragRow == 0 && dragCol == 0) {
							bounds.width += bounds.x - e.getX();
							bounds.height += bounds.y - e.getY();
							bounds.x = e.getX();
							bounds.y = e.getY();
						}
						if (dragRow == 0 && dragCol == 1) {
							bounds.height += bounds.y - e.getY();
							bounds.y = e.getY();
						}
						if (dragRow == 0 && dragCol == 2) {
							bounds.width = e.getX() - bounds.x;
							bounds.height += bounds.y - e.getY();
							bounds.y = e.getY();
						}
						if (dragRow == 1 && dragCol == 0) {
							bounds.width += bounds.x - e.getX();
							bounds.x = e.getX();
						}
						if (dragRow == 1 && dragCol == 2) {
							bounds.width = e.getX() - bounds.x;
						}
						if (dragRow == 2 && dragCol == 0) {
							bounds.width += bounds.x - e.getX();
							bounds.height = e.getY() - bounds.y;
							bounds.x = e.getX();
						}
						if (dragRow == 2 && dragCol == 1) {
							bounds.height = e.getY() - bounds.y;
						}
						if (dragRow == 2 && dragCol == 2) {
							bounds.height = e.getY() - bounds.y;
							bounds.width = e.getX() - bounds.x;
						}
						if (dragRow == -1 && dragCol == -1) {
							bounds.x = e.getX() - dragOffsetX;
							bounds.y = e.getY() - dragOffsetY;
						}

						repaint();
					}
				}
			});
			panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("ENTER"), "close");
			panel.getActionMap().put("close", new AbstractAction() {
				public void actionPerformed(ActionEvent e) {
					close();
				}
			});

			return panel;
		}

		private void close() {
			setVisible(false);
			dispose();
		}

	}

	public Rectangle getBounds() {
		return bounds;
	}

	public static void main(String[] args) {

		RegionSelector selector = new RegionSelector();
		selector.run(new Rectangle(100, 100, 800, 600));
	}
}
