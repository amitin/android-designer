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

/**
 * Special interface for legacy bridge API to collect View instances, which are not provided by
 * legacy API.
 * 
 * Note: The object instance of this interface injected into bridge instance using rewriting class
 * loader.
 * 
 * @author mitin_aa
 */
public interface IViewsCollector {
  /**
   * Invoked from Bridge.visit() method just before it returns.
   */
  void collect(Object view, Object cookie);
}
