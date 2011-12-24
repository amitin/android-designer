/*******************************************************************************
 * Copyright (c) 2011 Alexander Mitin (Alexander.Mitin@gmail.com)
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexander Mitin (Alexander.Mitin@gmail.com) - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.android.internal.support;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import org.eclipse.wb.android.internal.IExceptionConstants;
import org.eclipse.wb.android.internal.parser.AndroidHierarchyBuilder;
import org.eclipse.wb.android.internal.parser.AndroidNativeParser;
import org.eclipse.wb.android.internal.support.DeviceManager.DisplayMetrics;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.internal.core.utils.IOUtils2;
import org.eclipse.wb.internal.core.utils.asm.ToBytesClassAdapter;
import org.eclipse.wb.internal.core.utils.exception.DesignerException;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableObjectEx;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.core.utils.ui.ImageUtils;
import org.eclipse.wb.internal.core.xml.model.EditorContext;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.swt.graphics.Image;

import com.android.ide.common.rendering.LayoutLibrary;
import com.android.ide.common.rendering.api.ILayoutPullParser;
import com.android.ide.common.rendering.api.IProjectCallback;
import com.android.ide.common.rendering.api.LayoutLog;
import com.android.ide.common.rendering.api.RenderResources;
import com.android.ide.common.rendering.api.RenderSession;
import com.android.ide.common.rendering.api.ResourceValue;
import com.android.ide.common.rendering.api.Result;
import com.android.ide.common.rendering.api.SessionParams;
import com.android.ide.common.rendering.api.SessionParams.RenderingMode;
import com.android.ide.common.rendering.api.StyleResourceValue;
import com.android.ide.common.resources.ResourceRepository;
import com.android.ide.common.resources.ResourceResolver;
import com.android.ide.common.resources.configuration.FolderConfiguration;
import com.android.ide.common.resources.platform.AttrsXmlParser;
import com.android.ide.common.resources.platform.DeclareStyleableInfo;
import com.android.ide.common.sdk.LoadStatus;
import com.android.ide.eclipse.adt.AdtPlugin;
import com.android.ide.eclipse.adt.internal.editors.layout.ProjectCallback;
import com.android.ide.eclipse.adt.internal.resources.manager.ProjectResources;
import com.android.ide.eclipse.adt.internal.resources.manager.ResourceManager;
import com.android.ide.eclipse.adt.internal.sdk.AndroidTargetData;
import com.android.ide.eclipse.adt.internal.sdk.LayoutDevice.DeviceConfig;
import com.android.ide.eclipse.adt.internal.sdk.Sdk;
import com.android.layoutlib.api.ILayoutBridge;
import com.android.resources.ResourceType;
import com.android.sdklib.IAndroidTarget;
import com.android.sdklib.internal.avd.AvdInfo;

import org.apache.commons.lang.StringUtils;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodAdapter;
import org.objectweb.asm.MethodVisitor;

import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Supports interacting with layoutlib.jar.
 * 
 * @author mitin_aa
 * @coverage android.support
 */
@SuppressWarnings({"restriction", "deprecation"})
public final class AndroidBridge {
  private final LayoutLibrary m_layoutLib;
  private final IProject m_project;
  private RenderSession m_renderSession;
  private final EditorContext m_context;
  private AttrsXmlParser m_attrsSdkParser;
  private AttrsXmlParser m_attrsProjectParser;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public AndroidBridge(EditorContext androidEditorContext) throws Exception {
    m_context = androidEditorContext;
    m_project = m_context.getJavaProject().getProject();
    m_layoutLib = getLayoutLibrary();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Layout lib
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Returns a {@link LayoutLibrary} that is ready for rendering, wait for Sdk & target to load.
   */
  private LayoutLibrary getLayoutLibrary() throws Exception {
    AndroidTargetData data = getTargetData();
    LayoutLibrary layoutLibrary = data.getLayoutLibrary();
    return checkLegacy(data, layoutLibrary);
  }

  public void dispose() {
    if (m_renderSession != null) {
      m_renderSession.dispose();
    }
  }

  private void render(SessionParams params) {
    m_renderSession = m_layoutLib.createSession(params);
    Result result = m_renderSession.getResult();
    if (!result.isSuccess()) {
      Throwable cause = result.getException();
      String errorMessage = result.getErrorMessage();
      throw new DesignerException(IExceptionConstants.BRIDGE_OPERATION_NOT_SUCCESS,
          cause,
          result.getStatus().toString(),
          StringUtils.isEmpty(errorMessage) ? "<none>" : errorMessage);
    }
  }

  /*
   * Not implemented in bridge 
   *
  public Object getPropertyValue(Object nativeObject, String name) {
    checkSession();
    Result result = m_renderSession.getProperty(nativeObject, name);
    if (result.isSuccess()) {
      return result.getData();
    }
    return null;
  }*/
  /*
   * returns not all default values
   *
  public Map<String, String> getDefaultValues(Object cookie) {
    checkSession();
    Map<String, String> defaultProperties = m_renderSession.getDefaultProperties(cookie);
    return defaultProperties;
  }
  */
  /**
   * 
   */
  public void parse() {
    ILayoutPullParser androidNativeParser =
        new AndroidNativeParser(m_context.getRootElement(), true);
    render(androidNativeParser);
  }

  /**
   * 
   */
  public void render() {
    ILayoutPullParser androidNativeParser = new AndroidNativeParser(m_context.getRootElement());
    render(androidNativeParser);
  }

  private void render(ILayoutPullParser androidNativeParser) {
    // TODO: use settings on design pane
    RenderingMode renderingMode = RenderingMode.NORMAL;
    Object projectKey = null;
    int targetSdkVersion = getTarget().getVersion().getApiLevel();
    int minSdkVersion = 5; // Android 2.0
    DisplayMetrics displayMetrics = DeviceManager.getMetrics(m_context.getFile(), getAvds());
    displayMetrics.useOrientation(DeviceManager.getOrientation(m_context.getFile()));
    // prepare Log
    LayoutLog log = new LayoutLog() {
      @Override
      public void error(String s, String s1, Object obj) {
        System.err.println("Error: " + s + " " + s1);
      }

      @Override
      public void error(String s, String s1, Throwable throwable1, Object obj) {
        // TODO:
        System.err.println("Error: " + s + " " + s1);
        throwable1.printStackTrace();
      }

      @Override
      public void fidelityWarning(String s, String s1, Throwable throwable1, Object obj) {
        // TODO:
        System.err.println("Warning: " + s + " " + s1);
        throwable1.printStackTrace();
      }

      @Override
      public void warning(String s, String s1, Object obj) {
        // TODO:
        System.err.println("Warning: " + s + " " + s1);
      }
    };
    // prepare resources
    String themeName = DeviceManager.getThemeName(m_context.getFile());
    RenderResources renderResources = createResourceResolver(getConfig(), themeName);
    ProjectResources projectRes = getProjectResources();
    IProjectCallback projectCallback = new ProjectCallback(m_layoutLib, projectRes, m_project);
    SessionParams params =
        new SessionParams(androidNativeParser,
            renderingMode,
            projectKey,
            displayMetrics.getScreenWidth(),
            displayMetrics.getScreenHeight(),
            displayMetrics.getDensity(),
            displayMetrics.getXdpi(),
            displayMetrics.getYdpi(),
            renderResources,
            projectCallback,
            minSdkVersion,
            targetSdkVersion,
            log);
    render(params);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Resources
  //
  ////////////////////////////////////////////////////////////////////////////
  public FolderConfiguration getConfig() {
    // TODO use settings on design pane
    /*{
      FolderConfiguration configuration = new FolderConfiguration();
      configuration.createDefault();
      configuration.setVersionQualifier(new VersionQualifier(bridge.getTarget().getVersion().getApiLevel()));
      configuration.setPixelDensityQualifier(qualifier);
      configuration.setScreenDimensionQualifier(qualifier);
      configuration.setScreenOrientationQualifier(qualifier);
      configuration.setScreenSizeQualifier(qualifier);
    }*/
    DeviceConfig deviceConfig =
        getSdk().getLayoutDeviceManager().getCombinedList().get(0).getConfigs().get(0);
    FolderConfiguration config = deviceConfig.getConfig();
    return config;
  }

  private RenderResources createResourceResolver(FolderConfiguration config, String themeName) {
    Map<ResourceType, Map<String, ResourceValue>> projectResources = getProjectResources(config);
    Map<ResourceType, Map<String, ResourceValue>> frameworkResources =
        getFrameworkResources(config);
    return ResourceResolver.create(projectResources, frameworkResources, themeName, false);
  }

  private Map<ResourceType, Map<String, ResourceValue>> getProjectResources(FolderConfiguration config) {
    // Get the project resources
    ProjectResources resources = getProjectResources();
    return resources.getConfiguredResources(config);
  }

  private Map<ResourceType, Map<String, ResourceValue>> getFrameworkResources(FolderConfiguration config) {
    // Get the framework resources
    ResourceRepository resources = getFrameworkResources();
    return resources.getConfiguredResources(config);
  }

  /**
   * Returns a {@link ProjectResources} for the framework resources of current target.
   * 
   * @return the framework resources or null if not found.
   */
  public ResourceRepository getFrameworkResources() {
    AndroidTargetData data = getTargetData();
    if (data != null) {
      return data.getFrameworkResources();
    }
    return null;
  }

  /**
   * Returns a {@link ProjectResources} for the project resources.
   * 
   * @return the project resources or null if not found.
   */
  public ProjectResources getProjectResources() {
    // Get the resources of the file's project.
    ResourceManager manager = ResourceManager.getInstance();
    return manager.getProjectResources(m_project);
  }

  /**
   * See
   * {@link com.android.ide.eclipse.adt.internal.editors.layout.configuration.ConfigurationComposite#updateThemes()}
   * 
   */
  public List<String> getThemes() {
    ArrayList<String> themes = Lists.newArrayList();
    FolderConfiguration config = getConfig();
    // First list any themes that are declared by the manifest
    /*{ NOTHING ACTUAL
      String defaultTheme = getDefaultTheme();
      if (defaultTheme == null) {
        ScreenSize screenSize = config.getScreenSizeQualifier().getValue();
        int apiLevel = getTarget().getVersion().getApiLevel();
        if (apiLevel >= 11 && screenSize == ScreenSize.XLARGE) {
          defaultTheme = "Theme.Holo";
        } else {
          defaultTheme = "Theme";
        }
    }
      // TODO
    }*/
    // get the themes from the project.
    {
      Map<ResourceType, Map<String, ResourceValue>> resources = getProjectResources(config);
      if (resources != null) {
        // get the styles.
        Map<String, ResourceValue> styles = resources.get(ResourceType.STYLE);
        if (styles != null) {
          // collect the themes out of all the styles, ie styles that extend,
          // directly or indirectly a platform theme.
          for (ResourceValue value : styles.values()) {
            if (isTheme(value, styles)) {
              themes.add(value.getName());
            }
          }
        }
      }
    }
    // get the themes from the Framework.
    {
      Map<ResourceType, Map<String, ResourceValue>> resources = getFrameworkResources(config);
      if (resources != null) {
        // get the styles.
        Map<String, ResourceValue> styles = resources.get(ResourceType.STYLE);
        // collect the themes out of all the styles.
        for (ResourceValue value : styles.values()) {
          String name = value.getName();
          if (name.startsWith("Theme.") || name.equals("Theme")) {
            themes.add(value.getName());
          }
        }
      }
    }
    // sort themes
    Collections.sort(themes);
    return themes;
  }

  /*private String getDefaultTheme() {
    // prepare document
    IFolderWrapper projectFolder = new IFolderWrapper(m_project);
    IAbstractFile manifestFile = AndroidManifest.getManifest(projectFolder);
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    InputSource is = new InputSource(manifestFile.getContents());
    factory.setNamespaceAware(true);
    factory.setValidating(false);
    DocumentBuilder builder = factory.newDocumentBuilder();
    Document document = builder.parse(is);
    //
    Element root = document.getDocumentElement();
    String defaultTheme =
        root.getAttributeNS(SdkConstants.NS_RESOURCES, AndroidManifest.ATTRIBUTE_THEME);
    if (!StringUtils.isEmpty(defaultTheme))
      return defaultTheme;
    return null;
  }*/
  /**
   * Returns whether the given <var>style</var> is a theme. This is done by making sure the parent
   * is a theme.
   * 
   * See {@link
   * com.android.ide.eclipse.adt.internal.editors.layout.configuration.ConfigurationComposite#
   * isTheme(ResourceValue, Map<String, ResourceValue>)}
   * 
   * @param value
   *          the style to check
   * @param styleMap
   *          the map of styles for the current project. Key is the style name.
   * @return True if the given <var>style</var> is a theme.
   */
  private boolean isTheme(ResourceValue value, Map<String, ResourceValue> styleMap) {
    String ANDROID_NS_NAME_PREFIX = "android:";
    String name = value.getName();
    if (value instanceof StyleResourceValue) {
      StyleResourceValue style = (StyleResourceValue) value;
      boolean frameworkStyle = false;
      String parentStyle = style.getParentStyle();
      if (parentStyle == null) {
        // if there is no specified parent style we look an implied one.
        // For instance 'Theme.light' is implied child style of 'Theme',
        // and 'Theme.light.fullscreen' is implied child style of 'Theme.light'
        int index = name.lastIndexOf('.');
        if (index != -1) {
          parentStyle = name.substring(0, index);
        }
      } else {
        // remove the useless @ if it's there
        parentStyle = StringUtils.removeStart(parentStyle, "@");
        // check for framework identifier.
        if (parentStyle.startsWith(ANDROID_NS_NAME_PREFIX)) {
          frameworkStyle = true;
          parentStyle = parentStyle.substring(ANDROID_NS_NAME_PREFIX.length());
        }
        // at this point we could have the format style/<name>. we want only the name
        if (parentStyle.startsWith("style/")) {
          parentStyle = parentStyle.substring("style/".length());
        }
      }
      if (parentStyle != null && !frameworkStyle) {
        // if it's a project style, we check this is a theme.
        value = styleMap.get(parentStyle);
        if (value != null) {
          return isTheme(value, styleMap);
        }
      }
    }
    return name.startsWith("Theme.") || name.equals("Theme");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Objects
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @param androidViewInfoVisitor
   */
  public void accept(AndroidHierarchyBuilder visitor) throws Exception {
    checkSession();
    List<com.android.ide.common.rendering.api.ViewInfo> views = m_renderSession.getRootViews();
    accept(visitor, views.get(0), null);
    m_legacyViewsCollection.clear();
  }

  /**
   * @param visitor
   * @param viewInfo
   * @param object
   */
  private void accept(AndroidHierarchyBuilder visitor,
      com.android.ide.common.rendering.api.ViewInfo viewInfo,
      ObjectInfo parentModel) throws Exception {
    if (viewInfo.getViewObject() == null) {
      // maybe it's legacy API?
      Object key = viewInfo.getCookie();
      Object view = m_legacyViewsCollection.get(key);
      if (view != null) {
        ReflectionUtils.setField(viewInfo, "mViewObject", view);
      }
    }
    ObjectInfo objectInfo = visitor.visit(viewInfo, parentModel);
    for (com.android.ide.common.rendering.api.ViewInfo view : viewInfo.getChildren()) {
      accept(visitor, view, objectInfo);
    }
  }

  /**
   * @return the image of the current rendering.
   */
  public Image getImage() {
    checkSession();
    BufferedImage image = m_renderSession.getImage();
    return ImageUtils.convertToSWT(image);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Sdk
  //
  ////////////////////////////////////////////////////////////////////////////
  public EditorContext getContext() {
    return m_context;
  }

  public static Sdk getSdk() {
    waitLoad(new RunnableObjectEx<LoadStatus>() {
      public LoadStatus runObject() throws Exception {
        return AdtPlugin.getDefault().getSdkLoadStatus();
      }
    });
    return Sdk.getCurrent();
  }

  public IAndroidTarget getTarget() {
    return getSdk().getTarget(m_project);
  }

  public AndroidTargetData getTargetData() {
    final Sdk currentSdk = getSdk();
    final IAndroidTarget target = getTarget();
    if (target == null) {
      // TODO: throw DesEx
      throw new RuntimeException("no target");
    }
    // get real target data
    AndroidTargetData dataLoading = currentSdk.getTargetData(target);
    if (dataLoading == null) {
      // not yet loaded
      waitLoad(new RunnableObjectEx<LoadStatus>() {
        public LoadStatus runObject() throws Exception {
          return currentSdk.checkAndLoadTargetData(target, null);
        }
      });
    }
    // should be loaded here
    final AndroidTargetData data = currentSdk.getTargetData(target);
    waitLoad(new RunnableObjectEx<LoadStatus>() {
      public LoadStatus runObject() throws Exception {
        return data.getLayoutLibrary().getStatus();
      }
    });
    return data;
  }

  /**
   * @return the attribute parser for Sdk attributes.
   */
  private AttrsXmlParser getAttrsSdkParser() {
    if (m_attrsSdkParser == null) {
      // FIXME: re-create on target change
      String path = getSdkAttributesPath();
      m_attrsSdkParser = new AttrsXmlParser(path, AdtPlugin.getDefault());
      m_attrsSdkParser.preload();
    }
    return m_attrsSdkParser;
  }

  /**
   * @return the attribute parser for project-defined attributes.
   */
  private AttrsXmlParser getAttrsProjectParser() {
    if (m_attrsProjectParser == null) {
      IFile attrsFile = m_project.getFile("res/values/attrs.xml");
      if (!attrsFile.isAccessible()) {
        return null;
      }
      String path = attrsFile.getLocation().toOSString();
      m_attrsProjectParser = new AttrsXmlParser(path, AdtPlugin.getDefault());
      m_attrsProjectParser.preload();
    }
    return m_attrsProjectParser;
  }

  /**
   * @return the attributes description for given class.
   */
  public DeclareStyleableInfo getStyleable(Class<?> componentClass, String key) {
    boolean frameworkClass = AndroidUtils.isFrameworkClass(componentClass);
    // get parser
    AttrsXmlParser parser = frameworkClass ? getAttrsSdkParser() : getAttrsProjectParser();
    if (parser == null) {
      // possibly no attrs.xml
      return null;
    }
    Map<String, DeclareStyleableInfo> styleableList = parser.getDeclareStyleableList();
    return styleableList.get(key);
  }

  /**
   * @return
   */
  public Map<String, Integer> getEnumFlagValues(Class<?> componentClass, String attrLocalName) {
    if (!AndroidUtils.isFrameworkClass(componentClass)) {
      AttrsXmlParser parser = getAttrsProjectParser();
      if (parser != null) {
        Map<String, Integer> map = parser.getEnumFlagValues().get(attrLocalName);
        if (map != null) {
          return map;
        }
      }
    }
    AttrsXmlParser parser = getAttrsSdkParser();
    return parser.getEnumFlagValues().get(attrLocalName);
  }

  /**
   * @return the path to "attrs.xml" file of the current target.
   */
  private String getSdkAttributesPath() {
    return getTarget().getPath(IAndroidTarget.ATTRIBUTES);
  }

  public List<AvdInfo> getAvds() {
    // TODO filter
    AvdInfo[] avds = getSdk().getAvdManager().getValidAvds();
    return Lists.newArrayList(avds);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils/Misc
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Checks for LoadStatus == LOADED provided by <code>loadStatusGetter</code>. If the status is
   * FAILED or timeout exceeded, throws the exception.
   * 
   * // FIXME: use better way waiting for sdk to load.
   */
  private static void waitLoad(RunnableObjectEx<LoadStatus> loadStatusGetter) {
    boolean error = false;
    try {
      long timeout = 2 * 60 * 1000;
      long startWait = System.currentTimeMillis();
      LoadStatus status;
      while ((status = loadStatusGetter.runObject()) == LoadStatus.LOADING) {
        ExecutionUtils.sleep(10);
        if (System.currentTimeMillis() - startWait > timeout) {
          status = LoadStatus.FAILED;
          break;
        }
      }
      if (status == LoadStatus.FAILED) {
        error = true;
      }
    } catch (Throwable e) {
      // shouldn't happen, but spit anyway
      ReflectionUtils.propagate(e);
    }
    if (error) {
      // TODO: use DesEx
      throw new RuntimeException("load failed");
    }
  }

  private void checkSession() {
    if (m_renderSession == null || !m_renderSession.getResult().isSuccess()) {
      throw new DesignerException(IExceptionConstants.INVALID_BRIDGE_STATE);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Legacy API support
  //
  ////////////////////////////////////////////////////////////////////////////
  private final Map<Object, Object> m_legacyViewsCollection = Maps.newHashMap();

  /**
   * Checks for legacy API and rewrites legacy bridge to be able to collect View instances.
   */
  private LayoutLibrary checkLegacy(AndroidTargetData data, LayoutLibrary layoutLibrary)
      throws Exception {
    if (ReflectionUtils.getFieldObject(layoutLibrary, "mBridge") != null) {
      // do nothing, modern api
      return layoutLibrary;
    }
    // create new bridge using rewriting class loader
    Object legacyBridge = ReflectionUtils.getFieldObject(layoutLibrary, "mLegacyBridge");
    URLClassLoader legacyClassLoader = (URLClassLoader) legacyBridge.getClass().getClassLoader();
    URLClassLoader newClassLoader =
        new LegacyBridgeClassLoader(legacyClassLoader.getURLs(),
            AndroidBridge.class.getClassLoader());
    // create new
    Class<?> legacyClass = newClassLoader.loadClass(LayoutLibrary.CLASS_BRIDGE);
    ILayoutBridge newLegacyBridge = (ILayoutBridge) legacyClass.newInstance();
    // re-init
    IAndroidTarget target = (IAndroidTarget) ReflectionUtils.getFieldObject(data, "mTarget");
    String fontPath = target.getPath(IAndroidTarget.FONTS);
    newLegacyBridge.init(fontPath, data.getEnumValueMap());
    // store
    ReflectionUtils.setField(layoutLibrary, "mLegacyBridge", newLegacyBridge);
    ReflectionUtils.setField(layoutLibrary, "mClassLoader", newClassLoader);
    // setup collector
    ReflectionUtils.setField(newLegacyBridge, "collector", new IViewsCollector() {
      public void collect(Object view, Object key) {
        if (view != null) {
          m_legacyViewsCollection.put(key, view);
        }
      }
    });
    return layoutLibrary;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Inner classes
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Rewriting class loader which hooks 'visit()' method in legacy bridge and invokes collector for
   * View objects.
   */
  private static final class LegacyBridgeClassLoader extends URLClassLoader {
    private static final String VIEWS_COLLECTOR_NAME =
        "com/android/ide/eclipse/designer/internal/support/IViewsCollector";

    ////////////////////////////////////////////////////////////////////////////
    //
    // Constructor
    //
    ////////////////////////////////////////////////////////////////////////////
    private LegacyBridgeClassLoader(URL[] urls, ClassLoader parent) {
      super(urls, parent);
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // ClassLoader
    //
    ////////////////////////////////////////////////////////////////////////////
    @Override
    protected Class<?> findClass(String className) throws ClassNotFoundException {
      if (LayoutLibrary.CLASS_BRIDGE.equals(className)) {
        String classResourceName = className.replace('.', '/') + ".class";
        InputStream input = getResourceAsStream(classResourceName);
        if (input == null) {
          throw new ClassNotFoundException(className);
        } else {
          try {
            // read class bytes
            byte[] bytes = IOUtils2.readBytes(input);
            ClassReader classReader = new ClassReader(bytes);
            // rewrite
            ToBytesClassAdapter rewriter = new ToBytesClassAdapter() {
              private boolean isFieldPresent;

              @Override
              public FieldVisitor visitField(int access,
                  String name,
                  String desc,
                  String signature,
                  Object value) {
                if (name.equals("collector")) {
                  isFieldPresent = true;
                }
                return cv.visitField(access, name, desc, signature, value);
              }

              @Override
              public void visitEnd() {
                if (!isFieldPresent) {
                  // inject 'collector' field
                  FieldVisitor fv =
                      cv.visitField(0, "collector", "L" + VIEWS_COLLECTOR_NAME + ";", null, null);
                  if (fv != null) {
                    fv.visitEnd();
                  }
                }
                cv.visitEnd();
              }

              @Override
              public MethodVisitor visitMethod(int access,
                  String name,
                  String desc,
                  String signature,
                  String[] exceptions) {
                if ("visit".equals(name)) {
                  MethodVisitor mv = cv.visitMethod(access, name, desc, signature, exceptions);
                  return new MethodAdapter(mv) {
                    private boolean beforeRetNull;

                    @Override
                    public void visitInsn(int opcode) {
                      // invoke 'collector.collect()' just before normal return
                      if (opcode >= IRETURN && opcode <= RETURN || opcode == ATHROW) {
                        if (beforeRetNull) {
                          // this is 'return null' code, do nothing
                          beforeRetNull = false;
                        } else {
                          // 'collector.collect(view, bridgecontext.getViewKey(view));'
                          mv.visitVarInsn(ALOAD, 0);
                          mv.visitFieldInsn(
                              GETFIELD,
                              "com/android/layoutlib/bridge/Bridge",
                              "collector",
                              "L" + VIEWS_COLLECTOR_NAME + ";");
                          mv.visitVarInsn(ALOAD, 1);
                          mv.visitVarInsn(ALOAD, 2);
                          mv.visitVarInsn(ALOAD, 1);
                          mv.visitMethodInsn(
                              INVOKEVIRTUAL,
                              "com/android/layoutlib/bridge/BridgeContext",
                              "getViewKey",
                              "(Landroid/view/View;)Ljava/lang/Object;");
                          mv.visitMethodInsn(
                              INVOKEINTERFACE,
                              VIEWS_COLLECTOR_NAME,
                              "collect",
                              "(Ljava/lang/Object;Ljava/lang/Object;)V");
                        }
                      } else if (opcode == ACONST_NULL) {
                        // skip injecting before 'return null'
                        beforeRetNull = true;
                      }
                      mv.visitInsn(opcode);
                    };
                  };
                } else {
                  return super.visitMethod(access, name, desc, signature, exceptions);
                }
              }
            };
            // do rewriting
            classReader.accept(rewriter, 0);
            bytes = rewriter.toByteArray();
            // define package
            {
              String pkgName = StringUtils.substringBeforeLast(className, ".");
              if (getPackage(pkgName) == null) {
                definePackage(pkgName, null, null, null, null, null, null, null);
              }
            }
            // return modified class
            return defineClass(className, bytes, 0, bytes.length);
          } catch (Throwable e) {
            throw new ClassNotFoundException("Error loading class " + className, e);
          }
        }
      } else {
        // other classes
        return super.findClass(className);
      }
    }
  }
}
