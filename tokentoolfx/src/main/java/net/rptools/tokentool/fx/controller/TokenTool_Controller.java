package net.rptools.tokentool.fx.controller;

import java.awt.Point;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NavigableSet;
import java.util.ResourceBundle;
import java.util.TreeSet;
import java.util.Vector;

import javax.imageio.ImageIO;

import org.apache.commons.io.FilenameUtils;

import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.css.PseudoClass;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.InputMethodEvent;
import javafx.scene.input.MouseDragEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import net.rptools.tokentool.AppActions;
import net.rptools.tokentool.fx.util.FxImageUtil;
import net.rptools.tokentool.fx.util.TransferToken;

public class TokenTool_Controller {
	//	private Image overlayImage; // The overlay image with mask removed
	private Point dragStart = new Point();
	private Point portraitImageStart = new Point();

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
	private StackPane compositeTokenPane;
	@FXML
	private BorderPane tokenPreviewPane;
	@FXML
	private ScrollPane portraitScrollPane;

	@FXML
	private Group compositeGroup;

	@FXML
	private TreeView<Path> overlayTreeView;

	@FXML
	private ImageView portraitImageView; // The bottom "Portrait" layer
	@FXML
	private ImageView maskImageView; // The mask layer used to crop the Portrait layer
	@FXML
	private ImageView overlayImageView; // The overlay layer to apply on top of everything
	@FXML
	private ImageView tokenImageView;

	@FXML
	private CheckBox useFileNumberingCheckbox;
	@FXML
	private CheckBox overlayUseAsBaseCheckbox;

	@FXML
	private TextField fileNameTextField;
	@FXML
	private TextField fileNameSuffixTextField;
	@FXML
	private Label overlayNameLabel;
	@FXML
	private ColorPicker backgroundColorPicker;
	@FXML
	private ToggleButton overlayAspectToggleButton;

	@FXML
	private Spinner<Double> overlayWidthSpinner;
	@FXML
	private Spinner<Double> overlayHeightSpinner;

	// A custom set of Width/Height sizes to use for Overlays
	private NavigableSet<Double> overlaySpinnerSteps = new TreeSet<Double>(Arrays.asList(50d, 100d, 128d, 150d, 200d, 256d, 300d, 400d, 500d, 512d, 600d, 700d, 750d, 800d, 900d, 1000d));

	@FXML
	void initialize() {
		// Note: A Pane is added to the compositeTokenPane so the ScrollPane doesn't consume the mouse events
		assert saveOptionsPane != null : "fx:id=\"saveOptionsPane\" was not injected: check your FXML file 'TokenTool.fxml'.";
		assert overlayOptionsPane != null : "fx:id=\"overlayOptionsPane\" was not injected: check your FXML file 'TokenTool.fxml'.";
		assert zoomOptionsPane != null : "fx:id=\"zoomOptionsPane\" was not injected: check your FXML file 'TokenTool.fxml'.";

		assert compositeTokenPane != null : "fx:id=\"compositeTokenPane\" was not injected: check your FXML file 'TokenTool.fxml'.";
		assert tokenPreviewPane != null : "fx:id=\"tokenPreviewPane\" was not injected: check your FXML file 'TokenTool.fxml'.";
		assert portraitScrollPane != null : "fx:id=\"portraitScrollPane\" was not injected: check your FXML file 'TokenTool.fxml'.";

		assert compositeGroup != null : "fx:id=\"compositeGroup\" was not injected: check your FXML file 'TokenTool.fxml'.";

		assert overlayTreeView != null : "fx:id=\"overlayTreeview\" was not injected: check your FXML file 'TokenTool.fxml'.";

		assert portraitImageView != null : "fx:id=\"portraitImageView\" was not injected: check your FXML file 'TokenTool.fxml'.";
		assert maskImageView != null : "fx:id=\"maskImageView\" was not injected: check your FXML file 'TokenTool.fxml'.";
		assert overlayImageView != null : "fx:id=\"overlayImageView\" was not injected: check your FXML file 'TokenTool.fxml'.";
		assert tokenImageView != null : "fx:id=\"tokenPreviewPortrait\" was not injected: check your FXML file 'TokenTool.fxml'.";

		assert useFileNumberingCheckbox != null : "fx:id=\"useFileNumberingCheckbox\" was not injected: check your FXML file 'TokenTool.fxml'.";
		assert overlayUseAsBaseCheckbox != null : "fx:id=\"overlayUseAsBaseCheckbox\" was not injected: check your FXML file 'TokenTool.fxml'.";

		assert fileNameTextField != null : "fx:id=\"fileNameTextField\" was not injected: check your FXML file 'TokenTool.fxml'.";
		assert fileNameSuffixTextField != null : "fx:id=\"fileNameSuffixTextField\" was not injected: check your FXML file 'TokenTool.fxml'.";
		assert overlayNameLabel != null : "fx:id=\"overlayNameLabel\" was not injected: check your FXML file 'TokenTool.fxml'.";
		assert backgroundColorPicker != null : "fx:id=\"backgroundColorPicker\" was not injected: check your FXML file 'TokenTool.fxml'.";
		assert overlayAspectToggleButton != null : "fx:id=\"overlayAspectToggleButton\" was not injected: check your FXML file 'TokenTool.fxml'.";

		assert overlayWidthSpinner != null : "fx:id=\"overlayWidthSpinner\" was not injected: check your FXML file 'TokenTool.fxml'.";
		assert overlayHeightSpinner != null : "fx:id=\"overlayHeightSpinner\" was not injected: check your FXML file 'TokenTool.fxml'.";

		overlayTreeView.setShowRoot(false);

		overlayTreeView.getSelectionModel()
				.selectedItemProperty()
				.addListener((observable, oldValue, newValue) -> updateCompositImageView((TreeItem<Path>) newValue));

		addPseudoClassToLeafs(overlayTreeView);

		backgroundColorPicker.setValue(Color.TRANSPARENT);
		ObjectProperty<Background> background = compositeTokenPane.backgroundProperty();
		background.bind(Bindings.createObjectBinding(() -> {
			BackgroundFill fill = new BackgroundFill(backgroundColorPicker.getValue(), CornerRadii.EMPTY, Insets.EMPTY);
			return new Background(fill);
		}, backgroundColorPicker.valueProperty()));

		overlayWidthSpinner.getValueFactory().valueProperty().bindBidirectional(overlayHeightSpinner.getValueFactory().valueProperty());
		overlayWidthSpinner.valueProperty().addListener((observable, oldValue, newValue) -> overlayWidthSpinner_onTextChanged(oldValue, newValue));
		overlayHeightSpinner.valueProperty().addListener((observable, oldValue, newValue) -> overlayHeightSpinner_onTextChanged(oldValue, newValue));

		//		tokenImageView.fitWidthProperty().bind(tokenPreviewPane.widthProperty());
		//		tokenImageView.fitHeightProperty().bind(tokenPreviewPane.heightProperty());
	}

	public Color getBackgroundColor() {
		return backgroundColorPicker.getValue();
	}

	@FXML
	void updateMainView(ActionEvent event) {
		System.out.println("probably should do something here...");
	}

	@FXML
	void compositeTokenPane_MouseDragged(MouseEvent event) {
		//		System.out.println("drag delta: " + (event.getX() - dragStart.x) + ", " + (event.getY() - dragStart.y));
		//		System.out.println("portraitImageView: " + portraitImageView.getTranslateX() + ", " + portraitImageView.getTranslateY());

		portraitImageView.setTranslateX(event.getX() - dragStart.x + portraitImageStart.x);
		portraitImageView.setTranslateY(event.getY() - dragStart.y + portraitImageStart.y);

		updateTokenPreviewImageView();
	}

	@FXML
	void compositeTokenPane_MouseDragExited(MouseDragEvent event) {
		//		System.out.println("Mouse Drag Exited: " + event.getX() + ", " + event.getY());
	}

	@FXML
	void compositeTokenPane_MousePressed(MouseEvent event) {
		dragStart.setLocation(event.getX(), event.getY());
		portraitImageStart.setLocation(portraitImageView.getTranslateX(), portraitImageView.getTranslateY());
		portraitImageView.setCursor(Cursor.MOVE);
	}

	@FXML
	void compositeTokenPane_MouseReleased(MouseEvent event) {
		portraitImageView.setCursor(Cursor.HAND);
		updateTokenPreviewImageView();
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
		//				System.out.println("Mouse Moved: " + event.getX() + ", " + event.getY());
	}

	@FXML
	void compositeTokenPane_OnScroll(ScrollEvent event) {
		if (event.isShiftDown()) {
			Double r = portraitImageView.getRotate() + event.getDeltaX() / 20;
			//			System.out.println("Rotation: " + r + " event.getDeltaX() " + event.getDeltaX());

			if (r < -360d || r > 360d)
				r = 0d;

			//			System.out.println("Rotation: " + r);
			portraitImageView.setRotate(r);
		} else {
			Double scale = portraitImageView.getScaleY() * Math.pow(1.001, event.getDeltaY());
			//			System.out.println("scale: " + scale + " event.getDeltaY() " + event.getDeltaY());
			portraitImageView.setScaleX(scale);
			portraitImageView.setScaleY(scale);
		}
		updateTokenPreviewImageView();
		//		System.out.println("Scale: " + scale);
	}

	@FXML
	void compositeTokenPane_DragDropped(DragEvent event) {
		Dragboard db = event.getDragboard();

		if (db.hasImage()) {
			updatePortrait(db.getImage());
			event.setDropCompleted(true);
		} else if (db.hasFiles()) {
			db.getFiles().forEach(file -> {
				try {
					String tokenName = FilenameUtils.getBaseName(file.toURI().toURL().toExternalForm());
					if (!tokenName.isEmpty())
						fileNameTextField.setText(tokenName);
					else
						fileNameTextField.setText("token");

					updatePortrait(new Image(file.toURI().toURL().toExternalForm()));
				} catch (Exception e) {
					System.out.println("Could not load image " + file);
					e.printStackTrace();
				}
			});
			event.setDropCompleted(true);
		} else if (db.hasUrl()) {
			String tokenName = FilenameUtils.getBaseName(db.getUrl());
			if (!tokenName.isEmpty())
				fileNameTextField.setText(tokenName);
			else
				fileNameTextField.setText("token");

			updatePortrait(new Image(db.getUrl()));
			event.setDropCompleted(true);
		}
	}

	private void updatePortrait(Image newPortraitImage) {
		double w = newPortraitImage.getWidth();
		double h = newPortraitImage.getHeight();
		double pw = portraitScrollPane.getWidth();
		double ph = portraitScrollPane.getHeight();

		portraitImageView.setImage(newPortraitImage);
		portraitImageView.setTranslateX((pw - w) / 2);
		portraitImageView.setTranslateY((ph - h) / 2);
		portraitImageView.setScaleX(1);
		portraitImageView.setScaleY(1);
		portraitImageView.setRotate(0d);

		updateTokenPreviewImageView();
	}

	@FXML
	void compositeTokenPane_DragDone(DragEvent event) {
		System.out.println("compositeTokenPane_DragDone Done!");
		updateTokenPreviewImageView();
	}

	@FXML
	void compositeTokenPane_DragOver(DragEvent event) {
		//		System.out.println("compositeTokenPane_DraggedOver event " + event.toString());
		if (event.getDragboard().hasImage() || event.getDragboard().hasFiles() || event.getDragboard().hasUrl()) {
			// Set Pane color to an alpha green
			event.acceptTransferModes(TransferMode.COPY);
		} else {
			// Set Pane color to an alpha red?
			event.acceptTransferModes(TransferMode.ANY);
		}
	}

	@FXML
	void tokenImageView_OnDragDetected(MouseEvent event) {
		Dragboard db = tokenImageView.startDragAndDrop(TransferMode.ANY);
		ClipboardContent content = new ClipboardContent();
		Vector<File> files = new Vector<File>();

		boolean saveAsToken = false;

		try {
			File tempTokenFile = TransferToken.getTempFileAsToken(saveAsToken, useFileNumberingCheckbox.isSelected(), fileNameTextField.getText(), fileNameSuffixTextField);

			// remember the temp file to delete it later on ...
			TransferToken.lastFile = tempTokenFile;

			if (saveAsToken) {
				AppActions.saveToken(tempTokenFile, true);
			} else {
				ImageIO.write(SwingFXUtils.fromFXImage(tokenImageView.getImage(), null), "png", tempTokenFile);
				//				System.out.println("Drag size: " + tokenImageView.getImage().getWidth() + ", " + tokenImageView.getImage().getHeight());
			}

			files.add(tempTokenFile);
			content.putFiles(files);
			tempTokenFile.deleteOnExit();
		} catch (Exception e) {
			System.out.println(e);
		} finally {
			content.putImage(tokenImageView.getImage());
			db.setContent(content);
			event.consume();
		}
	}

	@FXML
	void tokenImageView_OnDragDone(DragEvent event) {
		// Finished creating token...
		//		System.out.println("tokenPreview_OnDragDone Done! " + TransferToken.lastFile);
	}

	@FXML
	void overlayUseAsBaseCheckbox_onAction(ActionEvent event) {
		if (overlayUseAsBaseCheckbox.isSelected())
			compositeGroup.toBack();
		else
			portraitScrollPane.toBack();

		updateTokenPreviewImageView();
	}

	@FXML
	void backgroundColorPicker_onAction(ActionEvent event) {
		updateTokenPreviewImageView();
		Color backGroundColor = backgroundColorPicker.getValue();
		System.out.println("backgroundColorPicker_onAction " + backGroundColor.toString());
	}

	@FXML
	void overlayAspectToggleButton_onAction(ActionEvent event) {
		if (overlayAspectToggleButton.isSelected()) {
			overlayImageView.setPreserveRatio(true);
			maskImageView.setPreserveRatio(true);
			overlayWidthSpinner.getValueFactory().valueProperty().bindBidirectional(overlayHeightSpinner.getValueFactory().valueProperty());
		} else {
			overlayImageView.setPreserveRatio(false);
			maskImageView.setPreserveRatio(false);
			overlayWidthSpinner.getValueFactory().valueProperty().unbindBidirectional(overlayHeightSpinner.getValueFactory().valueProperty());
		}

		updateTokenPreviewImageView();
	}

	void overlayWidthSpinner_onTextChanged(double oldValue, double newValue) {
		if (newValue < overlaySpinnerSteps.first())
			newValue = overlaySpinnerSteps.first();

		if (newValue > overlaySpinnerSteps.last())
			newValue = overlaySpinnerSteps.last();

		if (newValue > oldValue)
			overlayWidthSpinner.getValueFactory().setValue(overlaySpinnerSteps.ceiling(newValue));
		else
			overlayWidthSpinner.getValueFactory().setValue(overlaySpinnerSteps.floor(newValue));

		overlayImageView.setFitWidth(overlayWidthSpinner.getValue());
		maskImageView.setFitWidth(overlayWidthSpinner.getValue());

		updateTokenPreviewImageView();
	}

	void overlayHeightSpinner_onTextChanged(double oldValue, double newValue) {
		if (newValue < overlaySpinnerSteps.first())
			newValue = overlaySpinnerSteps.first();

		if (newValue > overlaySpinnerSteps.last())
			newValue = overlaySpinnerSteps.last();

		if (newValue > oldValue)
			overlayHeightSpinner.getValueFactory().setValue(overlaySpinnerSteps.ceiling(newValue));
		else
			overlayHeightSpinner.getValueFactory().setValue(overlaySpinnerSteps.floor(newValue));

		overlayImageView.setFitHeight(overlayHeightSpinner.getValue());
		maskImageView.setFitHeight(overlayHeightSpinner.getValue());

		updateTokenPreviewImageView();
	}

	public void expandOverlayOptionsPane(boolean expand) {
		overlayOptionsPane.setExpanded(expand);
	}

	public void updateOverlayTreeview(TreeItem<Path> overlayTreeItems) {
		overlayTreeView.setRoot(overlayTreeItems);
	}

	public void updateTokenPreviewImageView() {
		tokenImageView
				.setImage(FxImageUtil.composePreview(compositeTokenPane, backgroundColorPicker.getValue(), portraitImageView, maskImageView, overlayImageView, overlayUseAsBaseCheckbox.isSelected()));
		tokenImageView.setPreserveRatio(true);

		//		System.out.println("tokenImageView image width: " + tokenImageView.getImage().getWidth());
		//		System.out.println("tokenImageView getFitWidth: " + tokenImageView.getFitWidth());
	}

	private void addPseudoClassToLeafs(TreeView<Path> tree) {
		PseudoClass leaf = PseudoClass.getPseudoClass("leaf");

		tree.setCellFactory(tv -> {
			TreeCell<Path> cell = new TreeCell<>();
			cell.itemProperty().addListener((obs, oldValue, newValue) -> {
				if (newValue == null) {
					cell.setText("");
					cell.setGraphic(null);
				} else {
					cell.setText(newValue.toFile().getName());
					cell.setGraphic(cell.getTreeItem().getGraphic());
				}
			});
			cell.treeItemProperty().addListener((obs, oldTreeItem, newTreeItem) -> cell.pseudoClassStateChanged(leaf, newTreeItem != null && newTreeItem.isLeaf()));
			return cell;
		});
	}

	private void updateCompositImageView(TreeItem<Path> treeNode) {
		// I'm not a leaf on the wind! (Sub directory node)
		if (treeNode.getChildren().size() > 0)
			return;

		try {
			Path filePath = treeNode.getValue();

			// Set the Image Views
			maskImageView = FxImageUtil.getMaskImage(maskImageView, filePath);
			overlayImageView = FxImageUtil.getOverlayImage(overlayImageView, filePath);

			// Set the text label
			overlayNameLabel.setText(FilenameUtils.getBaseName(filePath.toFile().getName()));

			updateTokenPreviewImageView();
		} catch (IOException e) {
			// Not a valid URL, most likely this is just because it's a directory node.
			e.printStackTrace();
		}
	}
}
