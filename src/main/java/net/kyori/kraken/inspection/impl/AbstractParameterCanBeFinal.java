/*
 * This file is part of kraken, licensed under the MIT License.
 *
 * Copyright (c) 2019-2020 KyoriPowered
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package net.kyori.kraken.inspection.impl;

import com.intellij.codeInspection.CleanupLocalInspectionTool;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiIdentifier;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiModifier;
import com.intellij.psi.PsiVariable;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiUtil;
import net.kyori.kraken.inspection.AbstractVisitingInspection;
import net.kyori.kraken.util.KrakenBundle;
import net.kyori.kraken.util.Mu;
import net.kyori.kraken.util.Psi;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

public class AbstractParameterCanBeFinal extends AbstractVisitingInspection implements CleanupLocalInspectionTool {
  private final LocalQuickFix fix = new LocalQuickFix() {
    @Override
    public @Nls(capitalization = Nls.Capitalization.Sentence) @NotNull String getFamilyName() {
      return KrakenBundle.get("inspection.abstract_parameter_can_be_final.quickfix");
    }

    @Override
    public void applyFix(final @NotNull Project project, final @NotNull ProblemDescriptor problem) {
      final PsiElement element = problem.getPsiElement();
      if(element == null) return;

      final PsiVariable variable = PsiTreeUtil.getParentOfType(element, PsiVariable.class, false);
      if(variable == null) return;

      variable.normalizeDeclaration();

      PsiUtil.setModifierProperty(variable, PsiModifier.FINAL, true);
    }
  };

  @Override
  protected @NotNull Visitor createVisitor() {
    return new Visitor() {
      @Override
      public void visitMethod(final PsiMethod method) {
        if(method.getBody() != null) return;

        Psi.parameters(method)
          .filter(Mu.not(Psi.hasExplicitModifier(PsiModifier.FINAL)))
          .forEach(parameter -> {
            final PsiIdentifier identifier = parameter.getNameIdentifier();
            this.problems().add(
              identifier != null ? identifier : parameter,
              KrakenBundle.get("inspection.abstract_parameter_can_be_final.problem"),
              AbstractParameterCanBeFinal.this.fix,
              ProblemHighlightType.GENERIC_ERROR_OR_WARNING
            );
          });
      }
    };
  }
}
