/*******************************************************************************
 * Copyright (c) 2011 Andrey Sablin (Sablin.Andrey@gmail.com)
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andrey Sablin (Sablin.Andrey@gmail.com) - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.android.internal.support;

import org.eclipse.wb.android.internal.Activator;
import org.eclipse.wb.android.internal.model.widgets.ViewInfo;
import org.eclipse.wb.draw2d.geometry.Dimension;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableObjectEx;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.swt.graphics.Image;

import com.android.resources.Density;
import com.android.resources.ScreenOrientation;
import com.android.sdklib.internal.avd.AvdInfo;
import com.android.sdklib.internal.avd.AvdManager;

import org.apache.commons.lang.StringUtils;

import java.util.List;
import java.util.Map;

/**
 * Display parameters for rendering session.
 * 
 * @author sablin_aa
 * @coverage android.support
 */
public final class DeviceManager {
  ////////////////////////////////////////////////////////////////////////////
  //
  // DisplayMetrics
  //
  ////////////////////////////////////////////////////////////////////////////
  public static abstract class DisplayMetrics {
    /**
     * The screen width.
     */
    public abstract int getScreenWidth();

    /**
     * The screen height.
     */
    public abstract int getScreenHeight();

    /**
     * The density factor for the screen.
     */
    public abstract Density getDensity();

    /**
     * The screen actual dpi in X.
     */
    public int getXdpi() {
      return getDensity().getDpiValue();
    }

    /**
     * The screen actual dpi in Y.
     */
    public int getYdpi() {
      return getDensity().getDpiValue();
    }

    /**
     * Use {@link ScreenOrientation} for this display.
     */
    public abstract void useOrientation(ScreenOrientation orientation);

    /**
     * Textual presentation.
     */
    public abstract String getPrompt();

    /**
     * Sets this {@link DeviceManager} for given resource.
     */
    public abstract void setThis(IResource resource);

    /**
     * Sets this {@link DeviceManager} for given object.
     */
    public void setThis(ViewInfo object) {
      setThis(object.getContext().getFile());
    }
  }
  ////////////////////////////////////////////////////////////////////////////
  //
  // Default
  //
  ////////////////////////////////////////////////////////////////////////////
  public static abstract class DisplayMetricsImpl extends DisplayMetrics {
    private final int screenWidth;
    private final int screenHeight;
    private final Density density;
    private boolean inverseMetrics = false;

    DisplayMetricsImpl(int screenWidth, int screenHeight, Density density) {
      this.screenWidth = screenWidth;
      this.screenHeight = screenHeight;
      this.density = density != null ? density : Density.MEDIUM;
    }

    DisplayMetricsImpl(int screenWidth, int screenHeight) {
      this(screenWidth, screenHeight, Density.MEDIUM);
    }

    @Override
    public int getScreenWidth() {
      return inverseMetrics ? screenHeight : screenWidth;
    }

    @Override
    public int getScreenHeight() {
      return inverseMetrics ? screenWidth : screenHeight;
    }

    @Override
    public Density getDensity() {
      return density;
    }

    @Override
    public void useOrientation(ScreenOrientation orientation) {
      int width = getScreenWidth();
      int height = getScreenHeight();
      if (width > height) {
        inverseMetrics = ScreenOrientation.PORTRAIT.equals(orientation);
      } else { // if(width < height){
        inverseMetrics = ScreenOrientation.LANDSCAPE.equals(orientation);
      }
    }

    @Override
    public void setThis(IResource resource) {
      // none
    }
  }

  /**
   * Default metrics.
   */
  public static final DisplayMetrics DEFAULT = new DisplayMetricsImpl(320, 480) {
    @Override
    public String getPrompt() {
      return "default";
    }
  };

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utilities
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return stored {@link Metrics} for given resource.
   */
  public static DisplayMetrics getMetrics(IResource resource, List<AvdInfo> avds) {
    // try xVGA+dpi
    {
      String xvgaDpiValue = getXvgaDpiValue(resource);
      if (!StringUtils.isEmpty(xvgaDpiValue)) {
        DisplayMetrics metrics = getXvgaDpiMetrics(xvgaDpiValue);
        if (metrics != null) {
          return metrics;
        }
      }
    }
    // try AVD
    {
      String avdName = getAvdName(resource);
      if (!StringUtils.isEmpty(avdName)) {
        for (AvdInfo avd : avds) {
          if (avd.getName().equalsIgnoreCase(avdName)) {
            DisplayMetrics metrics = getAvdMetrics(avd);
            if (metrics != null) {
              return metrics;
            }
          }
        }
      }
    }
    return DEFAULT;
  }

  /**
   * @return stored {@link Metrics} for given object.
   */
  public static DisplayMetrics getMetrics(ViewInfo object) {
    return getMetrics(object.getContext().getFile(), object.getAndroidBridge().getAvds());
  }

  /**
   * Remember metrics for...
   */
  public static void setMetrics(IResource resource, DisplayMetrics metrics) {
    try {
      resource.setPersistentProperty(KEY_DEVICE_NAME, null);
      resource.setPersistentProperty(KEY_xVGA_DPI, null);
      if (metrics != null) {
        metrics.setThis(resource);
      }
    } catch (Exception e) {
      DesignerPlugin.log(e);
    }
  }

  /**
   * Remember metrics for {@link ViewInfo}.
   */
  public static void setMetrics(ViewInfo view, DisplayMetrics metrics) {
    setMetrics(view.getContext().getFile(), metrics);
  }

  /**
   * @return Screen {@link Dimension} for given xVGA string.
   */
  private static Dimension parseXvga(String skinName) {
    Dimension resolution;
    if ("QVGA".equalsIgnoreCase(skinName)) {
      resolution = new Dimension(240, 320);
    } else if ("WQVGA400".equalsIgnoreCase(skinName)) {
      resolution = new Dimension(240, 400);
    } else if ("WQVGA432".equalsIgnoreCase(skinName)) {
      resolution = new Dimension(240, 432);
    } else if ("HVGA".equalsIgnoreCase(skinName)) {
      resolution = new Dimension(320, 480);
    } else if ("WVGA800".equalsIgnoreCase(skinName)) {
      resolution = new Dimension(480, 800);
    } else if ("WVGA854".equalsIgnoreCase(skinName)) {
      resolution = new Dimension(480, 854);
    } else if ("WXGA".equalsIgnoreCase(skinName)) {
      resolution = new Dimension(1280, 800);
    } else {
      // parse custom
      String skinX = StringUtils.substringBefore(skinName, "x");
      String skinY = StringUtils.substringAfter(skinName, "x");
      try {
        resolution = new Dimension(Integer.parseInt(skinX), Integer.parseInt(skinY));
      } catch (NumberFormatException e) {
        // none
        resolution = null;
      }
    }
    return resolution;
  }

  /**
   * @return {@link Density} for given DPI string.
   */
  private static Density parseDensity(String densityName) {
    Density density;
    try {
      density = Density.getEnum(Integer.parseInt(densityName));
    } catch (NumberFormatException e) {
      // default
      density = null;
    }
    return density;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // AVDs metrics
  //
  ////////////////////////////////////////////////////////////////////////////
  private static final QualifiedName KEY_DEVICE_NAME = new QualifiedName(Activator.PLUGIN_ID,
      "deviceName");

  /**
   * @return the {@link DisplayMetrics} for given {@link AvdInfo} or <code>null</code> if unknown.
   */
  public static DisplayMetrics getAvdMetrics(final AvdInfo avd) {
    Map<String, String> properties = avd.getProperties();
    Dimension resolution = parseXvga(properties.get(AvdManager.AVD_INI_SKIN_NAME));
    DisplayMetrics metrics;
    //
    if (resolution != null) {
      Density density = parseDensity(properties.get("hw.lcd.density"));
      metrics = new DisplayMetricsImpl(resolution.width, resolution.height, density) {
        String prompt = avd.getName();

        @Override
        public String getPrompt() {
          return prompt;
        }

        @Override
        public void setThis(IResource resource) {
          try {
            resource.setPersistentProperty(KEY_DEVICE_NAME, prompt);
          } catch (Exception e) {
            DesignerPlugin.log(e);
          }
        };
      };
    } else {
      // unknown
      metrics = null;
    }
    return metrics;
  }

  /**
   * @return the AVD name for given {@link IResource}.
   */
  private static String getAvdName(final IResource resource) {
    return ExecutionUtils.runObjectIgnore(new RunnableObjectEx<String>() {
      public String runObject() throws Exception {
        return resource.getPersistentProperty(KEY_DEVICE_NAME);
      }
    }, null);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // xVGA metrics
  //
  ////////////////////////////////////////////////////////////////////////////
  private static final QualifiedName KEY_xVGA_DPI = new QualifiedName(Activator.PLUGIN_ID,
      "devaceXvgaDpi");

  /**
   * @return the {@link DisplayMetrics} for given xVGA+dpi string or <code>null</code> if unknown.
   */
  public static DisplayMetrics getXvgaDpiMetrics(final String value) {
    int delimiter = StringUtils.indexOf(value, "/");
    if (delimiter == -1) {
      return null;
    }
    String xvgaString = StringUtils.substring(value, 0, delimiter);
    String densityString = StringUtils.substring(value, delimiter + 1);
    Dimension resolution = parseXvga(xvgaString);
    DisplayMetrics metrics;
    if (resolution != null) {
      Density density = parseDensity(densityString);
      metrics = new DisplayMetricsImpl(resolution.width, resolution.height, density) {
        @Override
        public String getPrompt() {
          return value;
        }

        @Override
        public void setThis(IResource resource) {
          try {
            resource.setPersistentProperty(KEY_xVGA_DPI, value);
          } catch (Exception e) {
            DesignerPlugin.log(e);
          }
        };
      };
    } else {
      // unknown
      metrics = null;
    }
    return metrics;
  }

  /**
   * @return the xVGA+dpi value for given {@link IResource}.
   */
  private static String getXvgaDpiValue(final IResource resource) {
    return ExecutionUtils.runObjectIgnore(new RunnableObjectEx<String>() {
      public String runObject() throws Exception {
        return resource.getPersistentProperty(KEY_xVGA_DPI);
      }
    }, null);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Orientation
  //
  ////////////////////////////////////////////////////////////////////////////
  private static final QualifiedName KEY_ORIENTATION = new QualifiedName(Activator.PLUGIN_ID,
      "deviceOrientation");

  /**
   * @return the {@link ScreenOrientation} for given {@link IResource}.
   */
  public static ScreenOrientation getOrientation(final IResource resource) {
    return ExecutionUtils.runObjectIgnore(new RunnableObjectEx<ScreenOrientation>() {
      public ScreenOrientation runObject() throws Exception {
        String orientationString = resource.getPersistentProperty(KEY_ORIENTATION);
        ScreenOrientation orientation = ScreenOrientation.getEnum(orientationString);
        return orientation == null ? ScreenOrientation.PORTRAIT : orientation;
      }
    }, null);
  }

  /**
   * @return the {@link ScreenOrientation} for given {@link ViewInfo}.
   */
  public static ScreenOrientation getOrientation(final ViewInfo object) {
    return getOrientation(object.getContext().getFile());
  }

  /**
   * Sets the {@link ScreenOrientation} for given {@link IResource}.
   */
  public static void setOrientation(final IResource resource, ScreenOrientation value) {
    try {
      String valueString = value != null ? value.getResourceValue() : null;
      resource.setPersistentProperty(KEY_ORIENTATION, valueString);
    } catch (CoreException e) {
      DesignerPlugin.log(e);
    }
  }

  /**
   * Sets the {@link ScreenOrientation} for given {@link ViewInfo}.
   */
  public static void setOrientation(final ViewInfo object, ScreenOrientation value) {
    setOrientation(object.getContext().getFile(), value);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Themes
  //
  ////////////////////////////////////////////////////////////////////////////
  private static final QualifiedName KEY_THEME = new QualifiedName(Activator.PLUGIN_ID,
      "deviceTheme");

  /**
   * @return the theme name for given {@link IResource}.
   */
  public static String getThemeName(final IResource resource) {
    return ExecutionUtils.runObjectIgnore(new RunnableObjectEx<String>() {
      public String runObject() throws Exception {
        String themeString = resource.getPersistentProperty(KEY_THEME);
        return StringUtils.isEmpty(themeString) ? "Theme" : themeString;
      }
    }, null);
  }

  /**
   * @return the theme name for given {@link ViewInfo}.
   */
  public static String getThemeName(final ViewInfo object) {
    return getThemeName(object.getContext().getFile());
  }

  /**
   * Sets the theme name for given {@link IResource}.
   */
  public static void setThemeName(final IResource resource, String value) {
    try {
      resource.setPersistentProperty(KEY_THEME, value);
    } catch (CoreException e) {
      DesignerPlugin.log(e);
    }
  }

  /**
   * Sets the theme name for given {@link ViewInfo}.
   */
  public static void setThemeName(final ViewInfo object, String value) {
    setThemeName(object.getContext().getFile(), value);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Device image 
  //
  ////////////////////////////////////////////////////////////////////////////
  private static final QualifiedName KEY_SKIN =
      new QualifiedName(Activator.PLUGIN_ID, "deviceSkin");

  /**
   * @return <code>true</code> skin enabled.
   */
  public static boolean getShowDeviceImage(final ViewInfo object) {
    return ExecutionUtils.runObjectIgnore(new RunnableObjectEx<Boolean>() {
      public Boolean runObject() throws Exception {
        String skinString = object.getContext().getFile().getPersistentProperty(KEY_SKIN);
        if (StringUtils.isEmpty(skinString)) {
          skinString = "true";
        }
        return Boolean.valueOf(skinString);
      }
    }, Boolean.TRUE);
  }

  public static void setShowDeviceImage(final ViewInfo object, boolean value) {
    try {
      object.getContext().getFile().setPersistentProperty(KEY_SKIN, value ? null : "false");
    } catch (CoreException e) {
      DesignerPlugin.log(e);
    }
  }

  /**
   * Skin image.
   */
  public static abstract class DisplaySkin {
    public abstract Image getImage();

    public abstract Rectangle getClientArea();
  }
  private static class DisplaySkinImpl extends DisplaySkin {
    private final Image image;
    private final Rectangle clientArea;

    DisplaySkinImpl(Image image, Rectangle clientArea) {
      this.image = image;
      this.clientArea = clientArea;
    }

    @Override
    public Image getImage() {
      return image;
    }

    @Override
    public Rectangle getClientArea() {
      return clientArea;
    }
  }

  /**
   * @return the {@link Image} device decoration for given {@link ViewInfo}.
   */
  public static DisplaySkin getDeviceImage(final ViewInfo object) {
    DisplayMetrics metrics = getMetrics(object);
    int screenWidth = metrics.getScreenWidth();
    int screenHeight = metrics.getScreenHeight();
    boolean portrait = ScreenOrientation.PORTRAIT.equals(getOrientation(object));
    if (screenWidth == 240 && screenHeight == 320) {
      return getDisplaySkin("device/Skins/240x320", new Rectangle(23, 70, 240, 320), portrait);
    } else if (screenWidth == 240 && screenHeight == 400) {
      return getDisplaySkin("device/Skins/240x400", new Rectangle(23, 75, 240, 400), portrait);
    } else if (screenWidth == 240 && screenHeight == 432) {
      return getDisplaySkin("device/Skins/240x432", new Rectangle(23, 85, 240, 432), portrait);
    } else if (screenWidth == 320 && screenHeight == 480) {
      return getDisplaySkin("device/Skins/320x480", new Rectangle(30, 104, 320, 480), portrait);
    } else if (screenWidth == 480 && screenHeight == 800) {
      return getDisplaySkin("device/Skins/480x800", new Rectangle(45, 160, 480, 800), portrait);
    } else if (screenWidth == 480 && screenHeight == 854) {
      return getDisplaySkin("device/Skins/480x854", new Rectangle(45, 170, 480, 854), portrait);
    }
    return null;
  }

  private static DisplaySkinImpl getDisplaySkin(String imagePathPrefix,
      Rectangle clientArea,
      boolean portrait) {
    DisplaySkinImpl displaySkin;
    if (portrait) {
      displaySkin =
          new DisplaySkinImpl(Activator.getImage(imagePathPrefix + "-port.png"), clientArea);
    } else {
      displaySkin =
          new DisplaySkinImpl(Activator.getImage(imagePathPrefix + "-land.png"),
              clientArea.transpose());
    }
    return displaySkin;
  }
}