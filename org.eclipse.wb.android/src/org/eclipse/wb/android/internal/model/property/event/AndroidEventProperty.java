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
package org.eclipse.wb.android.internal.model.property.event;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import org.eclipse.wb.android.internal.editor.AndroidPairResourceProvider;
import org.eclipse.wb.android.internal.model.widgets.ViewInfo;
import org.eclipse.wb.android.internal.support.AndroidUtils;
import org.eclipse.wb.android.internal.support.IdSupport;
import org.eclipse.wb.core.editor.IContextMenuConstants;
import org.eclipse.wb.core.model.broadcast.ObjectEventListener;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.event.EventsPropertyUtils;
import org.eclipse.wb.internal.core.model.util.ObjectInfoAction;
import org.eclipse.wb.internal.core.model.variable.NamesManager;
import org.eclipse.wb.internal.core.utils.GenericTypeResolver;
import org.eclipse.wb.internal.core.utils.GenericsUtils;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.ast.AstParser;
import org.eclipse.wb.internal.core.utils.ast.BodyDeclarationTarget;
import org.eclipse.wb.internal.core.utils.ast.DomGenerics;
import org.eclipse.wb.internal.core.utils.ast.StatementTarget;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.jdt.core.CodeUtils;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;
import org.eclipse.wb.internal.core.xml.model.property.event.AbstractListenerProperty;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AnonymousTypeDeclaration2;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.ThisExpression;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.texteditor.ITextEditor;


import org.apache.commons.lang.StringUtils;

import java.lang.reflect.Type;
import java.text.MessageFormat;
import java.util.List;

/**
 * {@link Property} for single Android UI event.
 * 
 * @author mitin_aa
 * @coverage android.model.property
 */
public final class AndroidEventProperty extends AbstractListenerProperty {
  // constants
  private static final String IDENTIFIER_FIND_VIEW_BY_ID = "findViewById";
  private static final String SIGNATURE_FIND_VIEW_BY_ID = IDENTIFIER_FIND_VIEW_BY_ID + "(int)";
  private static final String SIGNATURE_SET_CONTENT_VIEW = "setContentView(int)";
  private static final String KEY_COMPANION_EDITOR = "companion editor";
  // fields
  private final ViewInfo m_view;
  private final ListenerInfo m_listener;
  private IFile m_javaFile;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public AndroidEventProperty(XmlObjectInfo object, ListenerInfo listener) {
    super(object, listener.getSimpleName(), AndroidEventPropertyEditor.INSTANCE);
    m_view = (ViewInfo) object;
    m_listener = listener;
    m_view.getRoot().addBroadcastListener(new ObjectEventListener() {
      @Override
      public void refreshDispose() throws Exception {
        // remove cached editor 
        m_view.getRoot().putArbitraryValue(KEY_COMPANION_EDITOR, null);
        clearAST();
      }
    });
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Property
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public boolean isModified() throws Exception {
    return getSetListenerLine() != -1;
  }

  @Override
  public void setValue(Object value) throws Exception {
    if (value == UNKNOWN_VALUE) {
      if (MessageDialog.openConfirm(
          DesignerPlugin.getShell(),
          "Confirm",
          "Are you sure to delete '" + m_listener.getSimpleName() + "' event handler?")) {
        removeListener();
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void removeListener() throws Exception {
    if (!prepareAST()) {
      return;
    }
    MethodInvocation listenerMethod = findSetListenerMethod();
    if (listenerMethod != null) {
      m_editor.removeStatement(AstNodeUtils.getEnclosingStatement(listenerMethod));
      saveAST();
      ExecutionUtils.refresh(m_object);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Context menu
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void addListenerActions(IMenuManager manager, IMenuManager implementMenuManager)
      throws Exception {
    IAction[] actions = createListenerMethodActions();
    // append existing stub action
    if (actions[0] != null) {
      manager.appendToGroup(IContextMenuConstants.GROUP_EVENTS, actions[0]);
    }
    // append existing or new method action
    implementMenuManager.add(actions[0] != null ? actions[0] : actions[1]);
  }

  /**
   * For given {@link ListenerMethodProperty} creates two {@link Action}'s:
   * 
   * [0] - for existing stub method, may be <code>null</code>;<br>
   * [1] - for creating new stub method.
   */
  private IAction[] createListenerMethodActions() throws Exception {
    IAction[] actions = new IAction[2];
    // try to find existing
    {
      int line = getSetListenerLine();
      if (line != -1) {
        actions[0] = new ObjectInfoAction(m_object) {
          @Override
          protected void runEx() throws Exception {
            openListener();
          }
        };
        actions[0].setText(m_listener.getSimpleName() + " -> line " + line);
        actions[0].setImageDescriptor(EventsPropertyUtils.LISTENER_METHOD_IMAGE_DESCRIPTOR);
      }
    }
    // in any case prepare action for creating new stub method
    {
      actions[1] = new ObjectInfoAction(m_object) {
        @Override
        protected void runEx() throws Exception {
          openListener();
        }
      };
      actions[1].setText(m_listener.getSimpleName());
      actions[1].setImageDescriptor(EventsPropertyUtils.LISTENER_METHOD_IMAGE_DESCRIPTOR);
    }
    //
    return actions;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Handler
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void openListener() throws Exception {
    if (!prepareAST()) {
      return;
    }
    MethodDeclaration mdOnCreate = AstNodeUtils.getMethodByName(m_typeDeclaration, "onCreate");
    String id = IdSupport.getSimpleId(IdSupport.getId(m_view));
    // get target to add set-listener method invocation
    // TODO: only local unique variables supported
    Statement stFindViewById = ensureFindViewById(mdOnCreate, id);
    if (stFindViewById == null) {
      return;
    }
    // get reference expression to variable
    String referenceExpression = getReferenceExpression(stFindViewById);
    MethodInvocation setListenerMethod = findSetListenerMethod(mdOnCreate, referenceExpression);
    if (setListenerMethod == null) {
      StatementTarget target = new StatementTarget(stFindViewById, false);
      // prepare listener source
      String source = "new " + getListenerTypeNameSource() + "() {\n}";
      // add listener and get added listener type
      setListenerMethod =
          addMethodInvocation(referenceExpression, target, m_listener.getMethodSignature(), source);
      // ensure listener type
      TypeDeclaration listenerType = findListenerTypeDeclaration(mdOnCreate, referenceExpression);
      // implement all methods
      {
        List<ListenerMethodInfo> interfaceMethods = m_listener.getMethods();
        for (ListenerMethodInfo interfaceMethodInfo : interfaceMethods) {
          if (interfaceMethodInfo.isAbstract()) {
            addListenerMethod(listenerType, interfaceMethodInfo);
          }
        }
      }
    }
    saveAST();
    ExecutionUtils.refresh(m_object);
    {
      MethodDeclaration method = findListenerMethod(setListenerMethod, referenceExpression);
      openMethodInEditor(method);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Working with AST
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return a variable name to get referent with.
   */
  private String getReferenceExpression(Statement statement) {
    if (statement instanceof VariableDeclarationStatement) {
      VariableDeclarationStatement vdStatement = (VariableDeclarationStatement) statement;
      List<?> fragments = vdStatement.fragments();
      for (Object fragment : fragments) {
        if (fragment instanceof VariableDeclarationFragment) {
          VariableDeclarationFragment vdFragment = (VariableDeclarationFragment) fragment;
          return vdFragment.getName().getIdentifier();
        }
      }
    }
    return null;
  }

  /**
   * Adds {@link MethodInvocation}.
   */
  private MethodInvocation addMethodInvocation(String reference,
      StatementTarget target,
      String signature,
      String arguments) throws Exception {
    // create invocation source
    String invocationSource;
    {
      String methodName = StringUtils.substringBefore(signature, "(");
      invocationSource = MessageFormat.format("{0}.{1}({2});", reference, methodName, arguments);
    }
    // add statement with invocation
    ExpressionStatement statement =
        (ExpressionStatement) m_editor.addStatement(invocationSource, target);
    return (MethodInvocation) statement.getExpression();
  }

  /**
   * @return the {@link TypeDeclaration} for the listener added.
   */
  private TypeDeclaration findListenerTypeDeclaration(ASTNode node, String referenceExpression) {
    Expression argument = getListenerArgumentExpression(node, referenceExpression);
    if (argument != null) {
      // check for "this"
      if (argument instanceof ThisExpression) {
        return AstNodeUtils.getEnclosingType(argument);
      }
      // check for listener creation
      if (argument instanceof ClassInstanceCreation) {
        ClassInstanceCreation creation = (ClassInstanceCreation) argument;
        // check for anonymous class
        if (creation.getAnonymousClassDeclaration() != null) {
          return new AnonymousTypeDeclaration2(creation.getAnonymousClassDeclaration());
        }
        // find inner type
        return AstNodeUtils.getTypeDeclaration(creation);
      }
    }
    // no listener found
    return null;
  }

  /**
   * @return the {@link Expression} used as direct argument for <code>setOnXXXListener()</code>.
   */
  private Expression getListenerArgumentExpression(ASTNode node, String reference) {
    MethodInvocation invocation = findSetListenerMethod(node, reference);
    if (invocation != null) {
      return (Expression) invocation.arguments().get(0);
    }
    // no listener found
    return null;
  }

  /**
   * @return the listener method in event listener.
   */
  private MethodDeclaration findListenerMethod(ASTNode node, String referenceExpression) {
    TypeDeclaration listenerType = findListenerTypeDeclaration(node, referenceExpression);
    if (listenerType != null) {
      return AstNodeUtils.getMethodBySignature(
          listenerType,
          m_listener.getMethods().get(0).getSignatureAST());
    }
    return null;
  }

  /**
   * @return the line number for set-listener method or -1 if no such invocation.
   */
  int getSetListenerLine() throws Exception {
    if (prepareAST()) {
      MethodInvocation listenerMethod = findSetListenerMethod();
      if (listenerMethod != null) {
        return m_editor.getLineNumber(listenerMethod.getStartPosition());
      }
    }
    return -1;
  }

  /**
   * @return the set-listener method invocation, ex. button1.setOnClickListener(). Returns
   *         <code>null</code> if the View has no id or this View is not yet referenced in Java.
   */
  private MethodInvocation findSetListenerMethod() throws Exception {
    MethodDeclaration onCreateMethod = AstNodeUtils.getMethodByName(m_typeDeclaration, "onCreate");
    String id = IdSupport.getSimpleId(IdSupport.getIdOrNull(m_view));
    // no id set yet
    if (id == null) {
      return null;
    }
    MethodInvocation miFindViewById =
        getMethodInvocationWithId(onCreateMethod, SIGNATURE_FIND_VIEW_BY_ID, id);
    // not referenced in Java
    if (miFindViewById == null) {
      return null;
    }
    // get reference expression to variable
    String reference = getReferenceExpression(AstNodeUtils.getEnclosingStatement(miFindViewById));
    return findSetListenerMethod(onCreateMethod, reference);
  }

  /**
   * Searches for set-listener method within <code>node</code> using <code>reference</code>
   * variable.
   */
  private MethodInvocation findSetListenerMethod(ASTNode node, String reference) {
    String addListenerMethodSignature = m_listener.getMethodSignature();
    // try to find listener adding
    return getMethodInvocationWithReference(node, addListenerMethodSignature, reference);
  }

  /**
   * Adds listener method implementation.
   */
  private MethodDeclaration addListenerMethod(TypeDeclaration typeDeclaration,
      ListenerMethodInfo methodInfo) throws Exception {
    // prepare annotations
    List<String> annotations = Lists.newArrayList();
    // prepare parameter names
    String[] parameterNames = null;
    {
      String listenerTypeName = m_listener.getListenerType().getCanonicalName();
      IType listenerType = m_editor.getJavaProject().findType(listenerTypeName);
      IMethod listenerMethod = CodeUtils.findMethod(listenerType, methodInfo.getSignature());
      parameterNames = listenerMethod.getParameterNames();
    }
    // prepare header code
    String headerCode;
    {
      // prepare parameters
      String parametersCode = "";
      {
        String[] parameterTypes = methodInfo.getActualParameterTypes();
        for (int i = 0; i < parameterTypes.length; i++) {
          String parameterType = parameterTypes[i];
          // comma
          if (parametersCode.length() != 0) {
            parametersCode += ", ";
          }
          // append type
          parametersCode += parameterType;
          parametersCode += " ";
          // append name
          parametersCode += parameterNames[i];
        }
      }
      // prepare full header code
      headerCode =
          "public "
              + methodInfo.getMethod().getReturnType().getName()
              + " "
              + methodInfo.getName()
              + "("
              + parametersCode
              + ")";
    }
    // prepare body
    List<String> bodyLines = getListenerMethodBody(methodInfo);
    // add method
    BodyDeclarationTarget target = new BodyDeclarationTarget(typeDeclaration, false);
    return m_editor.addMethodDeclaration(annotations, headerCode, bodyLines, target);
  }

  private static List<String> getListenerMethodBody(ListenerMethodInfo methodInfo) {
    Class<?> returnType = methodInfo.getMethod().getReturnType();
    if (returnType == Void.TYPE) {
      return ImmutableList.of();
    } else {
      String defaultValue = AstParser.getDefaultValue(returnType.getName());
      return ImmutableList.of("return " + defaultValue + ";");
    }
  }

  /**
   * @return the name of listener type, including generic arguments.
   */
  private String getListenerTypeNameSource() {
    // simple case - no generics
    {
      Class<?> listenerType = m_listener.getListenerType();
      if (listenerType.getTypeParameters().length == 0) {
        return listenerType.getCanonicalName();
      }
    }
    // listener with generics
    Type listenerType = m_listener.getMethod().getGenericParameterTypes()[0];
    GenericTypeResolver resolver_2 = m_listener.getResolver();
    return GenericsUtils.getTypeName(resolver_2, listenerType);
  }

  /**
   * Finds 'findViewById' invocation to get local variable for set listener. Adds local variable if
   * not found.
   * 
   * @return statement after which a set-listener method invocation should be added.
   */
  private Statement ensureFindViewById(MethodDeclaration methodDeclaration, String id)
      throws Exception {
    MethodInvocation methodInvocation =
        getMethodInvocationWithId(methodDeclaration, SIGNATURE_FIND_VIEW_BY_ID, id);
    if (methodInvocation != null) {
      return AstNodeUtils.getEnclosingStatement(methodInvocation);
    }
    MethodInvocation miSetContentView =
        getMethodInvocationWithId(methodDeclaration, SIGNATURE_SET_CONTENT_VIEW, getThisLayoutId());
    if (miSetContentView == null) {
      // can't determine where to place local variable maybe TODO: add field?
      return null;
    }
    StatementTarget target = new StatementTarget(miSetContentView, false);
    // get variable name
    String qualifiedClassName =
        ReflectionUtils.getCanonicalName(m_view.getDescription().getComponentClass());
    String baseName = NamesManager.getDefaultName(qualifiedClassName);
    String uniqueVariableName =
        m_editor.getUniqueVariableName(target.getPosition(), baseName, null);
    // get resource id for layout
    String layoutIdRef =
        AndroidUtils.getPackageFromManifest(m_view.getContext().getJavaProject().getProject())
            + ".R.id."
            + id;
    // add source 
    String source =
        qualifiedClassName
            + " "
            + uniqueVariableName
            + " = ("
            + qualifiedClassName
            + ")"
            + IDENTIFIER_FIND_VIEW_BY_ID
            + "("
            + layoutIdRef
            + ");";
    return m_editor.addStatement(source, target);
  }

  /**
   * @return the {@link MethodInvocation} with required signature for given id or <code>null</code>
   *         if none.
   */
  private MethodInvocation getMethodInvocationWithId(MethodDeclaration methodDeclaration,
      final String signature,
      final String id) {
    final MethodInvocation[] result = new MethodInvocation[1];
    methodDeclaration.accept(new ASTVisitor() {
      @Override
      public void endVisit(MethodInvocation node) {
        // check for already found
        if (result[0] != null) {
          return;
        }
        // proceed
        IMethodBinding binding = AstNodeUtils.getMethodBinding(node);
        if (binding != null) {
          // compare signature
          String methodSignature = AstNodeUtils.getMethodSignature(binding);
          if (signature.equals(methodSignature)) {
            Object object = node.arguments().get(0);
            if (object instanceof QualifiedName) {
              // compare reference
              QualifiedName name = (QualifiedName) object;
              if (id.equals(name.getName().getIdentifier())) {
                result[0] = node;
              }
            }
          }
        }
      }
    });
    return result[0];
  }

  /**
   * @return the method invocation with given signature and having <code>reference</code> as
   *         expression.
   */
  private MethodInvocation getMethodInvocationWithReference(ASTNode node,
      final String signature,
      final String reference) {
    final MethodInvocation[] result = new MethodInvocation[1];
    node.accept(new ASTVisitor() {
      @Override
      public void endVisit(MethodInvocation miNode) {
        // check for already found
        if (result[0] != null) {
          return;
        }
        // proceed
        IMethodBinding binding = AstNodeUtils.getMethodBinding(miNode);
        if (binding != null) {
          // compare signature
          String methodSignature = AstNodeUtils.getMethodSignature(binding);
          if (signature.equals(methodSignature)) {
            Expression expression = miNode.getExpression();
            if (expression instanceof SimpleName) {
              SimpleName simpleName = (SimpleName) expression;
              if (simpleName.getIdentifier().equals(reference)) {
                result[0] = miNode;
              }
            }
          }
        }
      }
    });
    return result[0];
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Opens source of companion Java file at position that corresponds {@link MethodDeclaration}.
   */
  private void openMethodInEditor(MethodDeclaration method) throws Exception {
    prepareJavaFile();
    if (m_javaFile == null) {
      return;
    }
    IEditorPart javaEditor = IDE.openEditor(DesignerPlugin.getActivePage(), m_javaFile);
    if (javaEditor instanceof ITextEditor) {
      ((ITextEditor) javaEditor).selectAndReveal(method.getStartPosition(), 0);
    }
  }

  /**
   * @return the layout ID corresponding to this xml-objects hierarchy.
   */
  private String getThisLayoutId() {
    IFile xmlFile = m_object.getContext().getFile();
    return StringUtils.removeEndIgnoreCase(xmlFile.getName(), ".xml");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // AST life-cycle
  //
  ////////////////////////////////////////////////////////////////////////////
  private AstEditor m_editor;
  private TypeDeclaration m_typeDeclaration;
  private long m_formFileModification;

  /**
   * Prepares {@link #m_editor} for {@link #m_javaFile}.
   */
  private boolean prepareAST() throws Exception {
    prepareJavaFile();
    if (m_javaFile == null) {
      return false;
    }
    m_editor = (AstEditor) m_view.getRoot().getArbitraryValue(KEY_COMPANION_EDITOR);
    long currentModification = m_javaFile.getModificationStamp();
    if (m_editor == null || currentModification != m_formFileModification) {
      m_formFileModification = currentModification;
      ICompilationUnit unit = JavaCore.createCompilationUnitFrom(m_javaFile);
      m_editor = new AstEditor(unit);
      m_view.getRoot().putArbitraryValue(KEY_COMPANION_EDITOR, m_editor);
    }
    m_typeDeclaration = DomGenerics.types(m_editor.getAstUnit()).get(0);
    return true;
  }

  /**
   * Saves changes performed in {@link #m_editor}.
   */
  private void saveAST() throws Exception {
    m_editor.saveChanges(false);
  }

  /**
   * Clears {@link #m_editor} after finishing AST operations.
   */
  private void clearAST() {
    m_editor = null;
    m_typeDeclaration = null;
  }

  /**
   * Searches for a companion java file.
   */
  private void prepareJavaFile() {
    if (m_javaFile == null) {
      IFile xmlFile = m_object.getContext().getFile();
      m_javaFile = AndroidPairResourceProvider.INSTANCE.getPair(xmlFile);
    }
  }
}
