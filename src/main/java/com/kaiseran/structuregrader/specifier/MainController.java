package com.kaiseran.structuregrader.specifier;

import edu.kaiseran.structuregrader.core.*;
import edu.kaiseran.structuregrader.core.specification.clazz.ClassMapSuite;
import edu.kaiseran.structuregrader.core.visitor.Spec;
import edu.kaiseran.structuregrader.core.wrapper.*;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
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
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ResourceBundle;

public class MainController implements Initializable {
	public static final String EZJAR_PATH = "C:\\Users\\kaiseran PC\\IdeaProjects\\ezjar\\build\\libs\\ezjar-1.0-SNAPSHOT.jar";
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
	public Button scanPackageButton;
	@FXML
	public TextArea logTextArea;
	@FXML
	public Button removeSpecButton;
	@FXML
	public Button clearPackageButton;
	@FXML
	public Button specifyPackageButton;
	@FXML
	public Button exportButton;
	@FXML
	public Button clearSpecificationStackButton;

	@NonNull
	private SimpleObjectProperty<URLClassLoader> urlClassLoaderSop = new SimpleObjectProperty<>(null);
	@NonNull
	private SimpleObjectProperty<ClassMap> classMapSop = new SimpleObjectProperty<>(null);
	@NonNull
	private SimpleStringProperty pkgToScanSsp = new SimpleStringProperty(null);
	@NonNull
	private SimpleObjectProperty<ClassMapSuite> classMapSuiteSop = new SimpleObjectProperty<>(null);

	@Override
	public void initialize(final URL location, final ResourceBundle resources) {
		pkgToScanField.disableProperty().bind(Bindings.isNull(urlClassLoaderSop));
		scanPackageButton.disableProperty().bind(Bindings.isNull(urlClassLoaderSop));
		final BooleanBinding classMapSopIsNullBind = Bindings.isNull(classMapSop);
		final BooleanBinding specificationTreeIsEmptyBind = Bindings.isNull(specificationTree.rootProperty());
		clearPackageButton.disableProperty().bind(classMapSopIsNullBind);
		specifyPackageButton.disableProperty().bind(classMapSopIsNullBind.or(specificationTreeIsEmptyBind.not()));

		removeSpecButton.disableProperty().bind(specificationTree.getSelectionModel().selectedItemProperty().isNull());
		clearSpecificationStackButton.disableProperty().bind(specificationTreeIsEmptyBind);
		exportButton.disableProperty().bind(specificationTreeIsEmptyBind);

		urlClassLoaderSop.addListener((observable, oldValue, newValue) -> classMapSop.set(null));

		classMapSop.addListener(($1, $2, newValue) -> {
			if (newValue != null) {
				final TreeItem<String> root = new TreeItem<>(newValue.getName());
				newValue.getItems().forEach((name, clazz) -> root.getChildren().add(treeItemFromClass(clazz)));
				projectTree.setRoot(root);
			} else {
				projectTree.setRoot(null);
			}
		});

		pkgToScanSsp.addListener(($1, $2, newValue) -> {
			assert urlClassLoaderSop.get() != null;

			try {
				classMapSop.set(ClassMap.buildFromPackage(
						urlClassLoaderSop.get(),
						pkgToScanField.getText()
				));
			} catch (IOException e) {
				e.printStackTrace();
				logTextArea.appendText(e.getMessage() + '\n');
			}
		});

		classMapSuiteSop.addListener(($1, $2, newValue) -> {
			if (newValue != null) {
				final TreeItem<String> rootItem = treeItemFromSpec(
						newValue,
						"root",
						null,
						() -> logTextArea.appendText("Cannot delete root specification from specification stack!\n")
				);
				specificationTree.setRoot(rootItem);
			} else {
				specificationTree.setRoot(null);
			}
		});

		// Debug macro
		try {
			final URL[] urls = new URL[]{
					new URL("file:" + EZJAR_PATH)
			};
			urlClassLoaderSop.set(URLClassLoader.newInstance(urls));
			loadedJarLabel.setText(EZJAR_PATH);
			pkgToScanField.setText("proj1");
			scanPackageWasClicked();
			specifyPackageWasClicked();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}

	}

	@FXML
	public void openSpecStackWasClicked() {
		logTextArea.appendText("openSpecStackWasClicked()\n"); // TODO: Remove

		FileChooser fileChooser = new FileChooser();
		fileChooser.setSelectedExtensionFilter(
				new FileChooser.ExtensionFilter(".spec files", ".spec")
		);
		final File file = fileChooser.showOpenDialog(getWindow());
		if (file != null) {
			try {
				final String specJson = Files.readString(Path.of(file.getAbsolutePath()));
				final ClassMapSuite classMapSuite = ClassMapSuite.JsonHelper.fromJson(
						specJson,
						$ -> {
						} // NOOP noncomplianceConsumer
				);
				classMapSuiteSop.set(classMapSuite);
			} catch (IOException e) {
				logTextArea.appendText(e.getLocalizedMessage() + '\n');
			}
		}
	}

	@FXML
	public void analyzeProjectWasClicked() {
		logTextArea.appendText("analyzeProjectWasClicked()\n"); // TODO: Remove

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
	public void scanPackageWasClicked() {
		logTextArea.appendText("scanPkgWasClicked()\n"); // TODO: Remove

		pkgToScanSsp.set(pkgToScanField.getText());
	}

	@FXML
	public void removeSpecWasClicked() {
		logTextArea.appendText("removeSpecWasClicked()\n"); // TODO: Remove

		// This isn't a rigorous cast, but we have complete control over the contents of the tree and only ever insert
		// custom TreeItems which implement Removable.
		// We could have subclassed TreeView but that would take too long to write.
		((Removable) specificationTree.getSelectionModel().getSelectedItem()).remove();
	}

	@FXML
	public void clearPackageWasClicked() {
		logTextArea.appendText('\n' + "resetProjectWasClicked()"); // TODO: Remove
		classMapSop.set(null);
	}

	@FXML
	public void specifyPackageWasClicked() {
		logTextArea.appendText("confirmAndSpecifyWasClicked()\n"); // TODO: Remove
		final ClassMapSuite classMapSuite = ClassMapSuite.ClassMapSuiteFactory.getDefaultInst().buildFromCollection(
				classMapSop.get(),
				classMapSop.get().getName(),
				$ -> {
				} // NOOP noncomplianceConsumer
		);
		classMapSuiteSop.set(classMapSuite);
	}

	@FXML
	public void exportWasClicked() {
		logTextArea.appendText("exportWasClicked()\n"); // TODO: Remove

		try {
			final String json = ClassMapSuite.JsonHelper.toJson(classMapSuiteSop.get());
			FileChooser fileChooser = new FileChooser();
			fileChooser.setTitle("Save Specification Stack");
			final File file = fileChooser.showSaveDialog(getWindow());
			if (file != null) {
				Files.writeString(Path.of(file.getAbsolutePath()), json);
			}
		} catch (IOException e) {
			logTextArea.appendText(e.getMessage() + '\n');
		}
	}

	@FXML
	public void clearSpecificationStackWasClicked() {
		logTextArea.appendText("clearSpecificationStackWasClicked()\n"); // TODO: Remove
		classMapSuiteSop.set(null);
	}

	private TreeItem<String> treeItemFromClass(@NonNull final ClassWrapper clazz) {
		final TreeItem<String> classItem = new TreeItem<>(clazz.getName());

		// Make an item for inner classes
		final NamedMap<ClassWrapper> declaredClasses = clazz.getDeclaredClasses();
		if (!declaredClasses.getItems().isEmpty()) {
			final TreeItem<String> declaredClassesRoot = new TreeItem<>("Declared Classes");
			declaredClasses.getItems().forEach(
					(name, innerClass) -> declaredClassesRoot.getChildren().add(treeItemFromClass(innerClass))
			);
			classItem.getChildren().add(declaredClassesRoot);
		}

		// Make an item for annotations
		final NamedMap<AnnotationWrapper> annotations = clazz.getAnnotationWrappers();
		if (!annotations.getItems().isEmpty()) {
			final TreeItem<String> annotationsRoot = new TreeItem<>("Runtime-Retained Annotations");
			annotations.getItems().forEach(
					(name, annotation) -> annotationsRoot.getChildren().add(new TreeItem<>(annotation.getName()))
			);
			classItem.getChildren().add(annotationsRoot);
		}

		// Make an item for constructors
		final NamedSet<ConstructorWrapper> constructors = clazz.getConstructors();
		if (!constructors.getItems().isEmpty()) {
			final TreeItem<String> constructorsRoot = new TreeItem<>("Constructors");
			constructors.getItems().forEach(
					constructor -> constructorsRoot.getChildren().add(new TreeItem<>(constructor.getSignature()))
			);
			classItem.getChildren().add(constructorsRoot);
		}

		// Make an item for fields
		final NamedMap<FieldWrapper> fields = clazz.getFields();
		if (!fields.getItems().isEmpty()) {
			final TreeItem<String> fieldsRoot = new TreeItem<>("Fields");
			fields.getItems().forEach(
					(name, field) -> fieldsRoot.getChildren().add(new TreeItem<>(field.getName()))
			);
			classItem.getChildren().add(fieldsRoot);
		}

		// Make an item for methods
		final NamedMap<MethodWrapper> methods = clazz.getMethods();
		if (!methods.getItems().isEmpty()) {
			final TreeItem<String> methodsRoot = new TreeItem<>("Methods");
			methods.getItems().forEach(
					(name, method) -> {
						final Type genericType = method.getGenericType();
						final Type type = method.getType();

						// TODO [kaiseran|2/26/2020]: This should really be an enum, it's a one-or-the-other scenario.
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

	private TreeItem<String> treeItemFromSpec(
			@NonNull final Spec rootSpec,
			final String prefix,
			final TreeItem<String> parentItem,
			final Runnable removeFromParentCollection
	) {
		final String rootTitle = rootSpec.specTypeName() + ": "
				+ (prefix != null ? prefix + " -> " : "") + rootSpec.getParentName();
		final SpecTreeItem rootItem = new SpecTreeItem(
				rootTitle,
				rootSpec
		);
		rootItem.setRemove(() -> {
			if (removeFromParentCollection != null) {
				removeFromParentCollection.run();
			}

			if (parentItem != null) {
				parentItem.getChildren().remove(rootItem);
			}

			if (removeFromParentCollection != null || parentItem != null) {
				specificationTree.refresh();
			}
		});

		// It's very weak OO to have these casts which can be reasonably expected to fail.
		// This was done for time savings and no other reason. I'm sure a better way to do this exists, I didn't have time
		// to look for it.
		try {
			final NamedSpecSet<? extends Spec> specSet = HasChildSet.class.cast(rootSpec).getChildSet();
			final SetTreeItem setRootItem = new SetTreeItem(specSet.getName(), specSet);
			setRootItem.setRemove(() -> {
				specSet.clear();
				rootItem.getChildren().remove(setRootItem);
				specificationTree.refresh();
			});
			specSet.forEach(spec -> setRootItem.getChildren().add(treeItemFromSpec(spec, null, setRootItem, () -> specSet.remove(spec))));
			rootItem.getChildren().add(setRootItem);
		} catch (ClassCastException e) {
			// NOOP. A failed cast is not a sign of a problem, it just means that the spec had no child set.
		}

		try {
			final NamedSpecMap<?, ? extends Spec> specMap = HasChildMap.class.cast(rootSpec).getChildMap();
			final MapTreeItem mapRootItem = new MapTreeItem(specMap.getName(), specMap);
			mapRootItem.setRemove(() -> {
				specMap.clear();
				rootItem.getChildren().remove(mapRootItem);
				specificationTree.refresh();
			});
			specMap.forEach((key, spec) -> mapRootItem.getChildren().add(treeItemFromSpec(spec, key.toString(), mapRootItem, () -> specMap.remove(key))));
			rootItem.getChildren().add(mapRootItem);
		} catch (ClassCastException e) {
			// NOOP. A failed cast is not a sign of a problem, it just means that the spec had no child map.
		}

		return rootItem;
	}

	private Window getWindow() {
		return this.root
				.getScene()
				.getWindow();
	}

	private interface Removable {
		void remove();
	}

	private static class SpecTreeItem extends TreeItem<String> implements Removable {
		private final Spec spec;
		@Setter
		private Runnable remove;

		public SpecTreeItem(
				@NonNull final String title,
				/*Nullable*/ final Spec spec
		) {
			super(title);
			this.spec = spec;
		}

		public void remove() {
			remove.run();
		}
	}

	private static class MapTreeItem extends TreeItem<String> implements Removable {
		private final NamedSpecMap map;
		@Setter
		private Runnable remove;

		public MapTreeItem(
				@NonNull final String title,
				@NonNull final NamedSpecMap map
		) {
			super(title);
			this.map = map;
		}

		public void remove() {
			remove.run();
		}
	}

	private static class SetTreeItem extends TreeItem<String> implements Removable {
		private final NamedSpecSet set;
		@Setter
		private Runnable remove;

		public SetTreeItem(
				@NonNull final String title,
				@NonNull final NamedSpecSet set
		) {
			super(title);
			this.set = set;
		}

		public void remove() {
			remove.run();
		}
	}
}
