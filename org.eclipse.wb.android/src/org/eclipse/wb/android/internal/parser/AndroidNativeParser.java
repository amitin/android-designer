package org.eclipse.wb.android.internal.parser;

import com.google.common.collect.Lists;

import org.eclipse.wb.internal.core.utils.xml.DocumentElement;
import org.eclipse.wb.internal.core.xml.model.utils.NamespacesHelper;

import com.android.ide.common.rendering.api.ILayoutPullParser;
import com.android.ide.eclipse.adt.internal.editors.layout.BasePullParser;

import org.apache.commons.lang.StringUtils;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.util.List;

/**
 * Custom parser that implements {@link ILayoutPullParser} (which itself extends
 * {@link XmlPullParser}).
 * 
 * @author mitin_aa
 * @coverage android.parser
 */
@SuppressWarnings("restriction")
public final class AndroidNativeParser extends BasePullParser {
  private final List<DocumentElement> mNodeStack = Lists.newArrayList();
  private final DocumentElement mRoot;
  private final NamespacesHelper mNamespacesHelper;
  private final boolean mParseOnly;

  public AndroidNativeParser(DocumentElement rootElement) {
    this(rootElement, false);
  }

  public AndroidNativeParser(DocumentElement rootElement, boolean parseOnly) {
    mRoot = rootElement;
    mParseOnly = parseOnly;
    mNamespacesHelper = new NamespacesHelper(mRoot);
    push(mRoot);
  }

  public ILayoutPullParser getParser(String s) {
    return null;
  }

  public Object getViewCookie() {
    return getCurrentNode();
  }

  public int getAttributeCount() {
    DocumentElement node = getCurrentNode();
    if (node != null) {
      if (mParseOnly) {
        if ("include".equalsIgnoreCase(node.getTag())) {
          return node.getAttribute("layout") == null ? 0 : 1;
        }
        return 0;
      }
      return node.getDocumentAttributes().size();
    }
    return 0;
  }

  public String getAttributeName(int i) {
    return null;
  }

  public String getAttributeNamespace(int i) {
    return null;
  }

  public String getAttributePrefix(int i) {
    return null;
  }

  public String getAttributeValue(int i) {
    return null;
  }

  public String getAttributeValue(String namespace, String localName) {
    DocumentElement node = getCurrentNode();
    if (mParseOnly) {
      if (!("include".equalsIgnoreCase(node.getTag()) && "layout".equalsIgnoreCase(localName))) {
        return null;
      }
    }
    if (StringUtils.isEmpty(namespace)) {
      return node.getAttribute(localName);
    } else {
      String ns = mNamespacesHelper.getName(namespace);
      return node.getAttribute(ns + ":" + localName);
    }
  }

  public int getDepth() {
    return mNodeStack.size();
  }

  public String getName() {
    if (mParsingState == START_TAG || mParsingState == END_TAG) {
      return getCurrentNode().getTagLocal();
    }
    return null;
  }

  public String getNamespace() {
    if (mParsingState == START_TAG || mParsingState == END_TAG) {
      return getCurrentNode().getTagNS();
    }
    return null;
  }

  public String getPositionDescription() {
    return "XML DOM element depth:" + mNodeStack.size();
  }

  public String getPrefix() {
    return null;
  }

  public boolean isEmptyElementTag() throws XmlPullParserException {
    if (mParsingState == START_TAG) {
      return getCurrentNode().getChildren().size() == 0;
    }
    throw new XmlPullParserException("Call to isEmptyElementTag while not in START_TAG", this, null);
  }

  @Override
  public void onNextFromStartDocument() {
    mParsingState = START_TAG;
  }

  @Override
  public void onNextFromStartTag() {
    // get the current node, and look for text or children (children first)
    DocumentElement node = getCurrentNode();
    List<DocumentElement> children = node.getChildren();
    if (children.size() > 0) {
      // move to the new child, and don't change the state.
      push(children.get(0));
      // in case the current state is CURRENT_DOC, we set the proper state.
      mParsingState = START_TAG;
    } else {
      if (mParsingState == START_DOCUMENT) {
        // this handles the case where there's no node.
        mParsingState = END_DOCUMENT;
      } else {
        mParsingState = END_TAG;
      }
    }
  }

  private DocumentElement getCurrentNode() {
    if (mNodeStack.size() > 0) {
      return mNodeStack.get(mNodeStack.size() - 1);
    }
    return null;
  }

  private void push(DocumentElement node) {
    mNodeStack.add(node);
  }

  private DocumentElement pop() {
    return mNodeStack.remove(mNodeStack.size() - 1);
  }

  @Override
  public void onNextFromEndTag() {
    // look for a sibling. if no sibling, go back to the parent
    DocumentElement node = getCurrentNode();
    node = getNextSibling(node);
    if (node != null) {
      // to go to the sibling, we need to remove the current node,
      pop();
      // and add its sibling.
      push(node);
      mParsingState = START_TAG;
    } else {
      // move back to the parent
      pop();
      // we have only one element left (mRoot), then we're done with the document.
      if (mNodeStack.size() == 1) {
        mParsingState = END_DOCUMENT;
      } else {
        mParsingState = END_TAG;
      }
    }
  }

  private DocumentElement getNextSibling(DocumentElement node) {
    DocumentElement parent = node.getParent();
    if (parent == null) {
      // root node
      return null;
    }
    List<DocumentElement> children = parent.getChildren();
    int childrenCount = children.size();
    if (childrenCount > 1 && parent.getChildAt(childrenCount - 1) != node) {
      int index = parent.indexOf(node);
      return index >= 0 && index < childrenCount - 1 ? children.get(index + 1) : null;
    }
    return null;
  }

  @SuppressWarnings("deprecation")
  public Object getViewKey() {
    return getViewCookie();
  }
}