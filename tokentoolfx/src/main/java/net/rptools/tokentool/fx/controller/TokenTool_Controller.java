package net.rptools.tokentool.fx.controller;

import java.awt.Point;
import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.Vector;

import javax.imageio.ImageIO;

import javafx.css.PseudoClass;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseDragEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import net.rptools.tokentool.AppActions;
import net.rptools.tokentool.fx.util.FxImageUtil;
import net.rptools.tokentool.fx.util.TransferToken;

public class TokenTool_Controller {
	private Image overlayImage; // The overlay image with mask removed
	private Point dragStart = new Point();

	@FXML
	private ResourceBundle resources;
	@FXML
	private URL location;

	@FXML
	private TitledPane saveOptionsPane;
	@FXML
	private TitledPane overlayOptionsPane;
	@FXML
	private TitledPane zoomOptionsPane;
	@FXML
	private TreeView<String> overlayTreeView;

	@FXML
	private ImageView maskImageView; // The mask layer used to crop the Portrait layer
	@FXML
	private ImageView overlayImageView; // The overlay layer to apply on top of everything
	@FXML
	private ImageView tokenPreview;
	@FXML
	private ImageView portraitImageView; // The bottom "Portrait" layer

	@FXML
	private Group compositeGroup;
	@FXML
	private StackPane compositeTokenPane;
	@FXML
	private BorderPane tokenPreviewPane;

	@FXML
	private TextField fileNameTextField;

	@FXML
	void initialize() {
		assert portraitImageView != null : "fx:id=\"portraitImageView\" was not injected: check your FXML file 'TokenTool.fxml'.";
		assert maskImageView != null : "fx:id=\"maskImageView\" was not injected: check your FXML file 'TokenTool.fxml'.";
		assert overlayImageView != null : "fx:id=\"overlayImageView\" was not injected: check your FXML file 'TokenTool.fxml'.";
		assert tokenPreview != null : "fx:id=\"tokenPreview\" was not injected: check your FXML file 'TokenTool.fxml'.";
		assert saveOptionsPane != null : "fx:id=\"saveOptionsPane\" was not injected: check your FXML file 'TokenTool.fxml'.";
		assert overlayOptionsPane != null : "fx:id=\"overlayOptionsPane\" was not injected: check your FXML file 'TokenTool.fxml'.";
		assert overlayTreeView != null : "fx:id=\"overlayTreeview\" was not injected: check your FXML file 'TokenTool.fxml'.";
		assert zoomOptionsPane != null : "fx:id=\"zoomOptionsPane\" was not injected: check your FXML file 'TokenTool.fxml'.";
		assert compositeGroup != null : "fx:id=\"compositeGroup\" was not injected: check your FXML file 'TokenTool.fxml'.";
		assert compositeTokenPane != null : "fx:id=\"compositeTokenPane\" was not injected: check your FXML file 'TokenTool.fxml'.";
		assert tokenPreviewPane != null : "fx:id=\"tokenPreviewPane\" was not injected: check your FXML file 'TokenTool.fxml'.";

		overlayTreeView.setShowRoot(false);

		overlayTreeView.getSelectionModel()
				.selectedItemProperty()
				.addListener((observable, oldValue, newValue) -> updateCompositImageView((TreeItem<?>) newValue));

		addPseudoClassToLeafs(overlayTreeView);

		tokenPreview.fitWidthProperty().bind(tokenPreviewPane.widthProperty());
		tokenPreview.fitHeightProperty().bind(tokenPreviewPane.heightProperty());
		updateCompositImageView();
	}

	@FXML
	public void updateMainView(ActionEvent event) {
		System.out.println("probably should do something here...");
	}

	@FXML
	void compositeTokenPane_MouseDragged(MouseEvent event) {
		//		System.out.println("drag delta: " + (event.getX() - dragStart.x) + ", " + (event.getY() - dragStart.y));
		//		System.out.println("portraitImageView: " + portraitImageView.getTranslateX() + ", " + portraitImageView.getTranslateY());

		portraitImageView.setTranslateX(event.getX() - dragStart.x);
		portraitImageView.setTranslateY(event.getY() - dragStart.y);
		updateCompositImageView();
	}

	@FXML
	void compositeTokenPane_MouseDragExited(MouseDragEvent event) {
		//		System.out.println("Mouse Drag Exited: " + event.getX() + ", " + event.getY());
	}

	@FXML
	void compositeTokenPane_MousePressed(MouseEvent event) {
		//	    dragDelta.setLocation(portraitImageView.getLayoutX() - event.getSceneX(), portraitImageView.getLayoutY() - event.getSceneY());
		dragStart.setLocation(event.getX(), event.getY());
		//		System.out.println("dragDelta start: " + dragStart);
		portraitImageView.setCursor(Cursor.MOVE);
	}

	@FXML
	void compositeTokenPane_MouseReleased(MouseEvent event) {
		portraitImageView.setCursor(Cursor.HAND);
		//		System.out.println("portraitImageView translated from: " + portraitImageView.getLayoutX() + ", " + portraitImageView.getLayoutY());
		portraitImageView.setLayoutX(portraitImageView.getLayoutX() + portraitImageView.getTranslateX());
		portraitImageView.setLayoutY(portraitImageView.getLayoutY() + portraitImageView.getTranslateY());
		portraitImageView.setTranslateX(0);
		portraitImageView.setTranslateY(0);
		updateCompositImageView();
		//		System.out.println("portraitImageView translated to:   " + portraitImageView.getLayoutX() + ", " + portraitImageView.getLayoutY());
	}

	@FXML
	void compositeTokenPane_MouseEntered(MouseEvent event) {
		//		System.out.println("Mouse Entered: " + event.getX() + ", " + event.getY());
		portraitImageView.setCursor(Cursor.HAND);
	}

	@FXML
	void compositeTokenPane_MouseExited(MouseEvent event) {
		//		System.out.println("Mouse Exited: " + event.getX() + ", " + event.getY());
	}

	@FXML
	void compositeTokenPane_MouseMoved(MouseEvent event) {
		//		System.out.println("Mouse Moved: " + event.getX() + ", " + event.getY());
	}

	@FXML
	void compositeTokenPane_OnScroll(ScrollEvent event) {
		//		System.out.println("Weee! x: " + event.getDeltaX());
		//		System.out.println("Weee! y: " + event.getDeltaY());
		//		System.out.println("Weee! unit: " + event.getTextDeltaYUnits());

		Double scale = portraitImageView.getScaleX() + (event.getDeltaY() * 0.001);
		if (scale < 0.05)
			scale = 0.05;

		portraitImageView.setScaleX(scale);
		portraitImageView.setScaleY(scale);
		updateCompositImageView();
		//		System.out.println("Scale: " + scale);
	}

	@FXML
	void tokenPreview_OnDragDetected(MouseEvent event) {
		Dragboard db = tokenPreview.startDragAndDrop(TransferMode.ANY);
		ClipboardContent content = new ClipboardContent();
		Vector<File> files = new Vector<File>();

		boolean saveAsToken = false;

		try {
			//			File tempTokenFile = TransferToken.getTempFileAsToken(saveAsToken);
			File tempTokenFile = new File(System.getProperty("java.io.tmpdir") + "token.png");
			System.out.println(tempTokenFile.getAbsolutePath());

			// remember the temp file to delete it later on ...
			TransferToken.lastFile = tempTokenFile;

			if (saveAsToken) {
				AppActions.saveToken(tempTokenFile, true);
			} else {
				ImageIO.write(SwingFXUtils.fromFXImage(tokenPreview.getImage(), null), "png", tempTokenFile);
			}

			files.add(tempTokenFile);
			content.putFiles(files);
		} catch (Exception e) {
			System.out.println(e);
		} finally {
			content.putImage(tokenPreview.getImage());
			db.setContent(content);
			event.consume();
		}
	}

	@FXML
	void tokenPreview_OnDragDone(DragEvent event) {

	}

	public void expandOverlayOptionsPane(boolean expand) {
		overlayOptionsPane.setExpanded(expand);
	}

	public void updateOverlayTreeview(TreeItem<String> treeItems) {
		overlayTreeView.setRoot(treeItems);
	}

	private void updateCompositImageView(TreeItem<?> newValue) {
		try {
			overlayImage = new Image(newValue.getValue().toString()); // Load the overlay
			overlayImageView.setImage(FxImageUtil.magentaToTransparency(overlayImage, 1));
			maskImageView.setImage(FxImageUtil.grabMask(overlayImage));

			updateCompositImageView();
		} catch (IllegalArgumentException e) {
			// Not a valid URL, most likely this is just because it's a directory node.
		}
	}

	private void updateCompositImageView() {
		try {
			tokenPreview.setImage(FxImageUtil.composePreview(compositeTokenPane, maskImageView));
		} catch (IllegalArgumentException e) {
			// Not a valid URL, most likely this is just because it's a directory node.
		}
	}

	private void addPseudoClassToLeafs(TreeView<String> tree) {
		PseudoClass leaf = PseudoClass.getPseudoClass("leaf");

		tree.setCellFactory(tv -> {
			TreeCell<String> cell = new TreeCell<>();
			cell.itemProperty().addListener((obs, oldValue, newValue) -> {
				if (newValue == null) {
					cell.setText("");
					cell.setGraphic(null);
				} else {
					cell.setText(newValue.toString());
					cell.setGraphic(cell.getTreeItem().getGraphic());
				}
				//cell.setGraphic(newValue);
			});
			cell.treeItemProperty().addListener((obs, oldTreeItem, newTreeItem) -> cell.pseudoClassStateChanged(leaf, newTreeItem != null && newTreeItem.isLeaf()));
			return cell;
		});
	}
}
