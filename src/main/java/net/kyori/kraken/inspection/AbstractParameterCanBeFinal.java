package net.kyori.kraken.inspection;

import com.intellij.codeInsight.daemon.GroupNames;
import com.intellij.codeInspection.AbstractBaseJavaLocalInspectionTool;
import com.intellij.codeInspection.CleanupLocalInspectionTool;
import com.intellij.codeInspection.InspectionManager;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.WriteExternalException;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiIdentifier;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiModifier;
import com.intellij.psi.PsiParameter;
import com.intellij.psi.PsiVariable;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiUtil;
import net.kyori.kraken.util.KrakenMessages;
import org.jdom.Element;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class AbstractParameterCanBeFinal extends AbstractBaseJavaLocalInspectionTool implements CleanupLocalInspectionTool {
  private final LocalQuickFix quickFix = new QuickFix();
  public static final @NonNls String SHORT_NAME = "AbstractParameterCanBeFinal";

  @Override
  public @NotNull String getDisplayName() {
    return KrakenMessages.get("inspection.abstract_parameter_can_be_final.display_name");
  }

  @Override
  public @NotNull String getGroupDisplayName() {
    return GroupNames.STYLE_GROUP_NAME;
  }

  @Override
  public @NotNull String getShortName() {
    return SHORT_NAME;
  }

  @Override
  public void writeSettings(final @NotNull Element node) throws WriteExternalException {
  }

  @Override
  public @Nullable ProblemDescriptor[] checkMethod(final @NotNull PsiMethod method, final @NotNull InspectionManager manager, final boolean isOnTheFly) {
    if(method.getBody() != null) {
      return null;
    }

    final Set<PsiParameter> parameters = Arrays.stream(method.getParameterList().getParameters())
      .filter(parameter -> parameter.getModifierList() != null)
      .filter(parameter -> !parameter.getModifierList().hasExplicitModifier(PsiModifier.FINAL))
      .collect(Collectors.toSet());

    if(parameters.isEmpty()) {
      return null;
    }

    final List<ProblemDescriptor> problems = new ArrayList<>();

    for(final PsiParameter parameter : parameters) {
      final PsiIdentifier identifier = parameter.getNameIdentifier();
      final PsiElement element = identifier != null ? identifier : parameter;
      problems.add(manager.createProblemDescriptor(element, KrakenMessages.get("inspection.abstract_parameter_can_be_final.problem"), this.quickFix, ProblemHighlightType.GENERIC_ERROR_OR_WARNING, isOnTheFly));
    }

    return problems.toArray(ProblemDescriptor.EMPTY_ARRAY);
  }

  private static class QuickFix implements LocalQuickFix {
    @Override
    public @NotNull String getFamilyName() {
      return KrakenMessages.get("inspection.abstract_parameter_can_be_final.quickfix");
    }

    @Override
    public void applyFix(final @NotNull Project project, final @NotNull ProblemDescriptor problem) {
      final PsiElement element = problem.getPsiElement();
      if(element == null) {
        return;
      }
      final PsiVariable variable = PsiTreeUtil.getParentOfType(element, PsiVariable.class, false);
      if(variable == null) {
        return;
      }
      variable.normalizeDeclaration();
      PsiUtil.setModifierProperty(variable, PsiModifier.FINAL, true);
    }
  }
}
