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

import com.google.common.collect.Lists;

import org.eclipse.wb.android.internal.Activator;
import org.eclipse.wb.android.internal.model.widgets.ViewInfo;
import org.eclipse.wb.android.internal.support.DeviceManager.DisplayMetrics;
import org.eclipse.wb.core.model.broadcast.ObjectEventListener;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.core.xml.model.IRootProcessor;
import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ContributionItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

import com.android.resources.ScreenOrientation;
import com.android.sdklib.IAndroidTarget;
import com.android.sdklib.internal.avd.AvdInfo;

import org.apache.commons.lang.StringUtils;

import java.util.List;

/**
 * Implementation of {@link IRootProcessor} that provides device selection action on editor toolbar.
 * 
 * @author sablin_aa
 * @coverage android.support
 */
public final class DeviceSelectionSupport implements IRootProcessor {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Instance
  //
  ////////////////////////////////////////////////////////////////////////////
  public static final IRootProcessor INSTANCE = new DeviceSelectionSupport();

  private DeviceSelectionSupport() {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IRootProcessor
  //
  ////////////////////////////////////////////////////////////////////////////
  public void process(final XmlObjectInfo root) throws Exception {
    root.addBroadcastListener(new ObjectEventListener() {
      private AbstractDeviceSelectionItem deviceOrientationSelectionItem;
      private DeviceScreenSelectionItem deviceScreenSelectionItem;
      private DeviceThemeSelectionItem deviceThemeSelectionItem;

      @Override
      public void addHierarchyActions(List<Object> actions) throws Exception {
        ViewInfo rootView = (ViewInfo) root;
        // orientation switcher
        {
          if (deviceOrientationSelectionItem == null) {
            deviceOrientationSelectionItem = new AbstractDeviceSelectionItem(rootView) {
              private ScreenOrientation nextOrientation;

              @Override
              public void fill(ToolBar toolBar, int index) {
                toolItem = new ToolItem(toolBar, SWT.PUSH);
                toolItem.addListener(SWT.Selection, new Listener() {
                  public void handleEvent(Event event) {
                    ExecutionUtils.runLog(new RunnableEx() {
                      public void run() throws Exception {
                        // remember in resource
                        DeviceManager.setOrientation(rootObject, nextOrientation);
                        // apply
                        rootObject.refresh();
                      }
                    });
                  }
                });
                // update now
                updateItems();
              };

              @Override
              protected void updateItemsInternal() {
                toolItem.setToolTipText("Flip orientation");
                ScreenOrientation orientation = DeviceManager.getOrientation(rootObject);
                if (ScreenOrientation.LANDSCAPE.equals(orientation)) {
                  nextOrientation = ScreenOrientation.PORTRAIT;
                  toolItem.setImage(Activator.getImage("device/landscape.png"));
                } else {
                  nextOrientation = ScreenOrientation.LANDSCAPE;
                  toolItem.setImage(Activator.getImage("device/portrait.png"));
                }
              };
            };
          }
          actions.add(deviceOrientationSelectionItem);
          deviceOrientationSelectionItem.updateItems();
        }
        // screen selector
        {
          if (deviceScreenSelectionItem == null) {
            deviceScreenSelectionItem = new DeviceScreenSelectionItem(rootView);
          }
          actions.add(deviceScreenSelectionItem);
          deviceScreenSelectionItem.updateItems();
        }
        // theme selector
        {
          if (deviceThemeSelectionItem == null) {
            deviceThemeSelectionItem = new DeviceThemeSelectionItem(rootView);
          }
          actions.add(deviceThemeSelectionItem);
          deviceThemeSelectionItem.updateItems();
        }
      }
    });
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // SelectionItems
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * The {@link ContributionItem} button.
   */
  private static class AbstractDeviceSelectionItem extends ContributionItem {
    protected final ViewInfo rootObject;
    protected ToolItem toolItem;

    ////////////////////////////////////////////////////////////////////////////
    //
    // Constructor
    //
    ////////////////////////////////////////////////////////////////////////////
    public AbstractDeviceSelectionItem(ViewInfo rootObject) {
      this.rootObject = rootObject;
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // ContributionItem
    //
    ////////////////////////////////////////////////////////////////////////////
    @Override
    public void fill(final ToolBar toolBar, int index) {
    }

    /**
     * Updates this item.
     */
    protected void updateItems() {
      if (toolItem != null) {
        ExecutionUtils.runLog(new RunnableEx() {
          public void run() throws Exception {
            updateItemsInternal();
          }
        });
      }
    }

    protected void updateItemsInternal() {
    }
  }
  /**
   * The {@link ContributionItem} with drop down menu for device configuration parameters.
   */
  private static class AbstractDeviceMenuSelectionItem extends AbstractDeviceSelectionItem {
    protected Menu menu;

    ////////////////////////////////////////////////////////////////////////////
    //
    // Constructor
    //
    ////////////////////////////////////////////////////////////////////////////
    public AbstractDeviceMenuSelectionItem(ViewInfo rootObject) {
      super(rootObject);
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // ContributionItem
    //
    ////////////////////////////////////////////////////////////////////////////
    @Override
    public void fill(final ToolBar toolBar, int index) {
      toolItem = new ToolItem(toolBar, SWT.DROP_DOWN);
      // bind menu
      createMenu(toolBar);
      toolItem.addListener(SWT.Selection, new Listener() {
        public void handleEvent(Event event) {
          createMenu(toolBar);
          // prepare location
          Point menuLocation;
          {
            Rectangle bounds = toolItem.getBounds();
            menuLocation = toolBar.toDisplay(bounds.x, bounds.y + bounds.height);
          }
          // show menu
          menu.setLocation(menuLocation);
          menu.setVisible(true);
        }
      });
      // update now
      updateItems();
    }

    @Override
    public void dispose() {
      disposeMenu();
      super.dispose();
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Actions
    //
    ////////////////////////////////////////////////////////////////////////////
    /**
     * Disposes drop-down {@link Menu}.
     */
    protected void disposeMenu() {
      if (menu != null) {
        menu.dispose();
        menu = null;
      }
    }

    /**
     * Creates drop down {@link Menu} with {@link Action}'s for device selection.
     */
    protected void createMenu(Control parent) {
      disposeMenu();
    }
  }
  /**
   * The {@link ContributionItem} with drop down menu of accessible devices.
   */
  private static final class DeviceScreenSelectionItem extends AbstractDeviceMenuSelectionItem {
    ////////////////////////////////////////////////////////////////////////////
    //
    // Constructor
    //
    ////////////////////////////////////////////////////////////////////////////
    public DeviceScreenSelectionItem(ViewInfo rootObject) {
      super(rootObject);
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Actions
    //
    ////////////////////////////////////////////////////////////////////////////
    @Override
    protected void createMenu(Control parent) {
      super.createMenu(parent);
      menu = new Menu(parent);
      // create default item
      {
        MenuItem menuItem = new MenuItem(menu, SWT.NONE);
        menuItem.setText("default");
        // add listeners
        menuItem.addListener(SWT.Selection, new Listener() {
          public void handleEvent(Event event) {
            setMetrics(rootObject, null);
          }
        });
      }
      //
      new MenuItem(menu, SWT.SEPARATOR);
      createXvgaItems();
      //
      new MenuItem(menu, SWT.SEPARATOR);
      createAvdItems();
      // skins
      new MenuItem(menu, SWT.SEPARATOR);
      {
        final MenuItem menuItem = new MenuItem(menu, SWT.CHECK);
        menuItem.setText("Show skin");
        menuItem.setSelection(DeviceManager.getShowDeviceImage(rootObject));
        // add listeners
        menuItem.addListener(SWT.Selection, new Listener() {
          public void handleEvent(Event event) {
            ExecutionUtils.runLog(new RunnableEx() {
              public void run() throws Exception {
                // remember in resource
                DeviceManager.setShowDeviceImage(rootObject, menuItem.getSelection());
                // apply
                rootObject.refresh();
              }
            });
          }
        });
      }
    }

    private void createXvgaItems() {
      // Small
      createXvgaItem(menu, "QVGA/120", "Small");
      // Normal
      {
        final MenuItem normalMenuItem = new MenuItem(menu, SWT.CASCADE);
        final Menu normalMenu = new Menu(normalMenuItem);
        normalMenuItem.setText("Normal");
        normalMenuItem.setMenu(normalMenu);
        createXvgaItem(normalMenu, "HVGA/160", "HVGA");
        createXvgaItem(normalMenu, "WVGA800/240", "WVGA800");
        createXvgaItem(normalMenu, "WVGA854/240", "WVGA854");
        createXvgaItem(normalMenu, "WQVGA400/120", "WQVGA400");
        createXvgaItem(normalMenu, "WQVGA432/120", "WQVGA432");
      }
      // Large
      {
        final MenuItem largeMenuItem = new MenuItem(menu, SWT.CASCADE);
        final Menu largeMenu = new Menu(largeMenuItem);
        largeMenuItem.setText("Large");
        largeMenuItem.setMenu(largeMenu);
        createXvgaItem(largeMenu, "WVGA800/160", "WVGA800");
        createXvgaItem(largeMenu, "WVGA854/160", "WVGA854");
      }
      // Extra Large
      createXvgaItem(menu, "WXGA/160", "xLarge");
    }

    private void createXvgaItem(Menu parentMenu, String xvgaDpiValue, String text) {
      DisplayMetrics metrics = DeviceManager.getXvgaDpiMetrics(xvgaDpiValue);
      if (metrics == null) {
        MenuItem menuItem = new MenuItem(parentMenu, SWT.NONE);
        menuItem.setText(text);
        menuItem.setEnabled(false);
      } else {
        final MenuItem menuItem = new MenuItem(parentMenu, SWT.NONE);
        menuItem.setText(text);
        menuItem.setData(metrics);
        // add listeners
        menuItem.addListener(SWT.Selection, new Listener() {
          public void handleEvent(Event event) {
            setMetrics(rootObject, (DisplayMetrics) menuItem.getData());
          }
        });
      }
    }

    private void createAvdItems() {
      IAndroidTarget androidTarget = rootObject.getAndroidBridge().getTarget();
      final MenuItem avdMenuItem = new MenuItem(menu, SWT.CASCADE);
      final Menu avdMenu = new Menu(avdMenuItem);
      avdMenuItem.setText("AVDs");
      avdMenuItem.setMenu(avdMenu);
      for (AvdInfo avd : rootObject.getAndroidBridge().getAvds()) {
        final MenuItem menuItem = new MenuItem(avdMenu, SWT.NONE);
        menuItem.setText(avd.getName());
        DisplayMetrics metrics = DeviceManager.getAvdMetrics(avd);
        if (metrics != null) {
          menuItem.setData(metrics);
          menuItem.setEnabled(androidTarget.canRunOn(avd.getTarget()));
          // add listeners
          menuItem.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
              setMetrics(rootObject, (DisplayMetrics) menuItem.getData());
            }
          });
        } else {
          menuItem.setEnabled(false);
        }
      }
    }

    private void setMetrics(final ViewInfo object, final DisplayMetrics metrics) {
      ExecutionUtils.runLog(new RunnableEx() {
        public void run() throws Exception {
          // remember in resource
          DeviceManager.setMetrics(rootObject, metrics);
          // apply
          object.refresh();
        }
      });
    }

    @Override
    protected void updateItemsInternal() {
      toolItem.setText(DeviceManager.getMetrics(rootObject).getPrompt());
      toolItem.setToolTipText("Select screen type...");
      // FIXME toolItem.setImage(Activator.getImage("device/device.png"));
    }
  }
  /**
   * The {@link ContributionItem} with drop down menu of accessible themes.
   */
  private static final class DeviceThemeSelectionItem extends AbstractDeviceMenuSelectionItem {
    public DeviceThemeSelectionItem(ViewInfo rootObject) {
      super(rootObject);
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Actions
    //
    ////////////////////////////////////////////////////////////////////////////
    @Override
    protected void createMenu(Control parent) {
      super.createMenu(parent);
      menu = new Menu(parent);
      for (ThemeMenuItemInfo themeInfo : getThemesHierarchy()) {
        addThemeMenuItem(menu, themeInfo);
      }
    }

    private void addThemeMenuItem(Menu parentMenu, final ThemeMenuItemInfo themeInfo) {
      if (themeInfo.children.isEmpty()) {
        final MenuItem menuItem = new MenuItem(parentMenu, SWT.NONE);
        menuItem.setText(themeInfo.name);
        menuItem.setEnabled(!StringUtils.isEmpty(themeInfo.fullName));
        // add listeners
        menuItem.addListener(SWT.Selection, new Listener() {
          public void handleEvent(Event event) {
            setTheme(rootObject, themeInfo.fullName);
          }
        });
      } else {
        final MenuItem menuItem = new MenuItem(parentMenu, SWT.CASCADE);
        menuItem.setText(themeInfo.name);
        final Menu subMenu = new Menu(menuItem);
        menuItem.setMenu(subMenu);
        if (!StringUtils.isEmpty(themeInfo.fullName)) {
          final MenuItem selfMenuItem = new MenuItem(subMenu, SWT.NONE);
          selfMenuItem.setText("<self>");
          selfMenuItem.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
              setTheme(rootObject, themeInfo.fullName);
            }
          });
        }
        for (ThemeMenuItemInfo childThemeInfo : themeInfo.children) {
          addThemeMenuItem(subMenu, childThemeInfo);
        }
      }
    }

    private void setTheme(final ViewInfo object, final String theme) {
      ExecutionUtils.runLog(new RunnableEx() {
        public void run() throws Exception {
          // remember in resource
          DeviceManager.setThemeName(object, theme);
          // apply
          object.refresh();
        }
      });
    }

    @Override
    protected void updateItemsInternal() {
      String themeName = DeviceManager.getThemeName(rootObject);
      int index = StringUtils.lastIndexOf(themeName, ".");
      toolItem.setText(index == -1 ? themeName : StringUtils.substring(themeName, index + 1));
      toolItem.setToolTipText(themeName);
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Utilities
    //
    ////////////////////////////////////////////////////////////////////////////
    private static class ThemeMenuItemInfo {
      String name;
      String fullName;
      List<ThemeMenuItemInfo> children = Lists.newArrayList();

      ThemeMenuItemInfo getChildTheme(String name) {
        for (ThemeMenuItemInfo child : children) {
          if (name.equals(child.name)) {
            return child;
          }
        }
        return null;
      }

      ThemeMenuItemInfo getTheme(String name) {
        ThemeMenuItemInfo info;
        int index = StringUtils.indexOf(name, ".");
        if (index == -1) {
          info = getChildTheme(name);
          if (info == null) {
            info = new ThemeMenuItemInfo();
            info.name = name;
            children.add(info);
          }
        } else {
          String prefixName = StringUtils.substring(name, 0, index);
          info = getChildTheme(prefixName);
          if (info == null) {
            info = new ThemeMenuItemInfo();
            info.name = prefixName;
            children.add(info);
          }
          String suffixName = StringUtils.substring(name, index + 1);
          info = info.getTheme(suffixName);
        }
        return info;
      }
    }

    private List<ThemeMenuItemInfo> getThemesHierarchy() {
      ThemeMenuItemInfo rootThemeInfo = new ThemeMenuItemInfo();
      for (String themeName : rootObject.getAndroidBridge().getThemes()) {
        ThemeMenuItemInfo themeInfo = rootThemeInfo.getTheme(themeName);
        themeInfo.fullName = themeName;
      }
      return rootThemeInfo.children;
    }
  }
}