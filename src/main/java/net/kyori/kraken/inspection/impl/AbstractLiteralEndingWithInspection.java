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

import com.intellij.codeInspection.CommonQuickFixBundle;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiExpression;
import com.intellij.psi.PsiLiteralExpression;
import com.intellij.psi.PsiType;
import com.siyeh.ig.PsiReplacementUtil;
import net.kyori.kraken.inspection.AbstractVisitingInspection;
import net.kyori.kraken.util.KrakenBundle;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.PropertyKey;

public abstract class AbstractLiteralEndingWithInspection extends AbstractVisitingInspection {
  private final LocalQuickFix fix = new LocalQuickFix() {
    @Override
    public @Nls @NotNull String getFamilyName() {
      return CommonQuickFixBundle.message("fix.replace.x.with.y", AbstractLiteralEndingWithInspection.this.from, AbstractLiteralEndingWithInspection.this.to);
    }

    @Override
    public void applyFix(final @NotNull Project project, final @NotNull ProblemDescriptor descriptor) {
      final PsiExpression literal = (PsiExpression) descriptor.getPsiElement();
      final String text = literal.getText();
      final int length = text.length();
      final int end = length - 1;
      if(text.charAt(end) == AbstractLiteralEndingWithInspection.this.from) {
        final String newText = text.substring(0, end) + AbstractLiteralEndingWithInspection.this.to;
        PsiReplacementUtil.replaceExpression(literal, newText);
      }
    }
  };
  private final PsiType type;
  private final char from;
  private final char to;
  private final String problemKey;

  protected AbstractLiteralEndingWithInspection(final PsiType type, final char from, final char to, final @NotNull @PropertyKey(resourceBundle = KrakenBundle.NAME) String problemKey) {
    this.type = type;
    this.from = from;
    this.to = to;
    this.problemKey = problemKey;
  }

  @Override
  protected @NotNull Visitor createVisitor() {
    return new Visitor() {
      @Override
      public void visitLiteralExpression(final @NotNull PsiLiteralExpression expression) {
        final PsiType type = expression.getType();
        if(type == null) return;
        if(!type.equals(AbstractLiteralEndingWithInspection.this.type)) return;

        final String text = expression.getText();
        if(text == null) return;

        final int length = text.length();
        if(length == 0) return;
        final int end = length - 1;
        if(text.charAt(end) != AbstractLiteralEndingWithInspection.this.from) return;

        this.problems().add(
          expression,
          KrakenBundle.get(AbstractLiteralEndingWithInspection.this.problemKey),
          AbstractLiteralEndingWithInspection.this.fix,
          ProblemHighlightType.WARNING
        );
      }
    };
  }
}
