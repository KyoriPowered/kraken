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
package net.kyori.kraken.inspection;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.JavaElementVisitor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractVisitingInspection extends AbstractInspection {
  @Override
  public final @NotNull PsiElementVisitor buildVisitor(final @NotNull ProblemsHolder holder, final boolean isOnTheFly) {
    final Visitor visitor = this.createVisitor();
    visitor.problems = new Problems(holder, isOnTheFly);
    return visitor;
  }

  protected abstract @NotNull Visitor createVisitor();

  protected static abstract class Visitor extends JavaElementVisitor {
    Problems problems;

    protected Visitor() {
    }

    protected final @NotNull Problems problems() {
      if(this.problems == null) {
        throw new IllegalStateException("uh oh");
      }
      return this.problems;
    }
  }

  // yes, this class is just a wrapper for registerProblem and createProblemDescriptor
  protected static final class Problems {
    final ProblemsHolder problems;
    final boolean onTheFly;

    Problems(final ProblemsHolder problems, final boolean onTheFly) {
      this.problems = problems;
      this.onTheFly = onTheFly;
    }

    public void add(final @NotNull PsiElement psiElement, final @Nls(capitalization = Nls.Capitalization.Sentence) @NotNull String descriptionTemplate, final LocalQuickFix fix, final @NotNull ProblemHighlightType highlightType) {
      this.problems.registerProblem(this.problems.getManager().createProblemDescriptor(psiElement, descriptionTemplate, fix, highlightType, this.onTheFly));
    }
  }
}
