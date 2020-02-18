package com.kaiseran.structuregrader.specifier;

import edu.kaiseran.structuregrader.core.ClassMap;
import edu.kaiseran.structuregrader.core.NamedMap;
import edu.kaiseran.structuregrader.core.NamedSet;
import edu.kaiseran.structuregrader.core.specification.clazz.ClassMapSuite;
import edu.kaiseran.structuregrader.core.wrapper.*;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import lombok.NonNull;
import lombok.Setter;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ResourceBundle;

public class MainController implements Initializable {
	@FXML
	@Setter
	public AnchorPane root;
	@FXML
	public TreeView<String> projectTree;
	@FXML
	public TreeView<String> specificationTree;
	@FXML
	public Label nameLabel;
	@FXML
	public TextField pkgToScanField;
	@FXML
	public Label loadedJarLabel;
	@FXML
	public Button scanPkgButton;
	@FXML
	public TextArea logTextArea;
	@FXML
	public Button removeSpecButton;
	@FXML
	public Button resetPackageButton;
	@FXML
	public Button confirmAndSpecifyButton;

	@NonNull
	private SimpleObjectProperty<URLClassLoader> urlClassLoaderSop = new SimpleObjectProperty<>(null);
	@NonNull
	private SimpleObjectProperty<ClassMap> classMapSop = new SimpleObjectProperty<>(null);

	@Override
	public void initialize(final URL location, final ResourceBundle resources) {
		pkgToScanField.disableProperty().bind(Bindings.isNull(urlClassLoaderSop));
		scanPkgButton.disableProperty().bind(Bindings.isNull(urlClassLoaderSop));
		resetPackageButton.disableProperty().bind(Bindings.isNull(classMapSop));
		confirmAndSpecifyButton.disableProperty().bind(Bindings.isNull(classMapSop));

		urlClassLoaderSop.addListener((observable, oldValue, newValue) -> classMapSop.set(null));

		classMapSop.addListener((observable, oldValue, newValue) -> {
			if (newValue != null) {
				final TreeItem<String> root = new TreeItem<>(newValue.getName());
				newValue.getItems().forEach((name, clazz) -> root.getChildren().add(treeItemFromClass(clazz)));
				projectTree.setRoot(root);
			} else {
				projectTree.setRoot(null);
			}
		});
	}

	@FXML
	public void openSpecStackWasClicked(final ActionEvent actionEvent) {
		logTextArea.appendText('\n' + "openSpecStackWasClicked()"); // TODO: Remove

	}

	@FXML
	public void analyzeProjectWasClicked(final ActionEvent actionEvent) {
		logTextArea.appendText('\n' + "analyzeProjectWasClicked()"); // TODO: Remove

		FileChooser fileChooser = new FileChooser();
		fileChooser.setSelectedExtensionFilter(
				new FileChooser.ExtensionFilter(".jar files", ".jar")
		);

		final File jar = fileChooser.showOpenDialog(getWindow());

		try {
			final URL jarUrl = new URL("file:" + jar.getAbsolutePath());
			final URL[] urls = {jarUrl};

			urlClassLoaderSop.set(URLClassLoader.newInstance(urls));
			loadedJarLabel.setText(jarUrl.getFile());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@FXML
	public void scanPkgWasClicked() {
		logTextArea.appendText('\n' + "scanPkgWasClicked()"); // TODO: Remove

		assert urlClassLoaderSop.get() != null;

		try {
			classMapSop.set(ClassMap.buildFromPackage(
					urlClassLoaderSop.get(),
					pkgToScanField.getText()
			));
		} catch (IOException e) {
			e.printStackTrace();
			logTextArea.appendText('\n' + e.getMessage());
		}
	}

	@FXML
	public void removeSpecWasClicked() {
		logTextArea.appendText('\n' + "removeSpecWasClicked()"); // TODO: Remove
	}

	@FXML
	public void resetPackageWasClicked() {
		logTextArea.appendText('\n' + "resetProjectWasClicked()"); // TODO: Remove
		classMapSop.set(null);
	}

	@FXML
	public void confirmAndSpecifyWasClicked() {
		logTextArea.appendText('\n' + "confirmAndSpecifyWasClicked()"); // TODO: Remove
		ClassMapSuite suite = ClassMapSuite.ClassMapSuiteFactory.getDefaultInst().buildFromCollection(
				classMapSop.get(),
				classMapSop.getName(),
				null
		);
	}

	private TreeItem<String> treeItemFromClass(@NonNull final ClassWrapper clazz) {
		final TreeItem<String> classItem = new TreeItem<>(clazz.getName());

		final NamedMap<ClassWrapper> declaredClasses = clazz.getDeclaredClasses();
		if (!declaredClasses.getItems().isEmpty()) {
			final TreeItem<String> declaredClassesRoot = new TreeItem<>("Declared Classes");
			declaredClasses.getItems().forEach(
					(name, innerClass) -> declaredClassesRoot.getChildren().add(treeItemFromClass(innerClass))
			);
			classItem.getChildren().add(declaredClassesRoot);
		}

		final NamedMap<AnnotationWrapper> annotations = clazz.getAnnotationWrappers();
		if (!annotations.getItems().isEmpty()) {
			final TreeItem<String> annotationsRoot = new TreeItem<>("Runtime-Retained Annotations");
			annotations.getItems().forEach(
					(name, annotation) -> annotationsRoot.getChildren().add(new TreeItem<>(annotation.getName()))
			);
			classItem.getChildren().add(annotationsRoot);
		}

		final NamedSet<ConstructorWrapper> constructors = clazz.getConstructors();
		if (!constructors.getItems().isEmpty()) {
			final TreeItem<String> constructorsRoot = new TreeItem<>("Constructors");
			constructors.getItems().forEach(
					constructor -> constructorsRoot.getChildren().add(new TreeItem<>(constructor.getSignature()))
			);
			classItem.getChildren().add(constructorsRoot);
		}


		final NamedMap<FieldWrapper> fields = clazz.getFields();
		if (!fields.getItems().isEmpty()) {
			final TreeItem<String> fieldsRoot = new TreeItem<>("Fields");
			fields.getItems().forEach(
					(name, field) -> fieldsRoot.getChildren().add(new TreeItem<>(field.getName()))
			);
			classItem.getChildren().add(fieldsRoot);
		}


		final NamedMap<MethodWrapper> methods = clazz.getMethods();
		if (!methods.getItems().isEmpty()) {
			final TreeItem<String> methodsRoot = new TreeItem<>("Methods");
			methods.getItems().forEach(
					(name, method) -> {
						final Type genericType = method.getGenericType();
						final Type type = method.getType();

						assert genericType != null || type != null;

						final String itemStr = (
								genericType != null ?
										genericType.getTypeName() :
										type.getTypeName()
						) + ' ' + method.getName();
						methodsRoot.getChildren().add(new TreeItem<>(itemStr));
					}
			);
			classItem.getChildren().add(methodsRoot);
		}

		final NamedSet<ClassWrapper> interfaces = clazz.getInterfaces();
		if (!interfaces.getItems().isEmpty()) {
			final TreeItem<String> interfacesRoot = new TreeItem<>("Interfaces");
			interfaces.getItems().forEach(
					interface_ -> interfacesRoot.getChildren().add(new TreeItem<>(interface_.getName()))
			);
			classItem.getChildren().add(interfacesRoot);
		}

		return classItem;
	}

	private Window getWindow() {
		return this.root
				.getScene()
				.getWindow();
	}
}
