package com.qyengyn.easydarktheme.java;

import com.intellij.codeInsight.generation.PsiMethodMember;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiModifier;
import com.intellij.psi.PsiModifierList;
import com.intellij.psi.PsiParameterList;

import java.util.ArrayList;
import java.util.List;

public class JavaPsiUtils {

    public static PsiMethodMember[] findAllPublicSetters(final PsiClass aClass) {
        PsiMethod[] allMethods = aClass.getAllMethods();
        List<PsiMethodMember> result = new ArrayList<>();
        for (PsiMethod method : allMethods) {
            if (!method.getName().startsWith("set")) continue;

            PsiParameterList parameterList = method.getParameterList();
            if (parameterList.getParametersCount() != 1) continue;

            PsiModifierList modifierList = method.getModifierList();
            if (!modifierList.hasModifierProperty(PsiModifier.PUBLIC)) continue;
            if (modifierList.hasModifierProperty(PsiModifier.STATIC)) continue;

            result.add(new PsiMethodMember(method));
        }
        return result.toArray(new PsiMethodMember[0]);
    }

    public static PsiMethodMember[] findAllPublicGetters(final PsiClass aClass) {
        PsiMethod[] allMethods = aClass.getAllMethods();
        List<PsiMethodMember> result = new ArrayList<>();
        for (PsiMethod method : allMethods) {
            if (!method.getName().startsWith("get")) continue;

            PsiParameterList parameterList = method.getParameterList();
            if (parameterList.getParametersCount() != 0) continue;

            PsiModifierList modifierList = method.getModifierList();
            if (!modifierList.hasModifierProperty(PsiModifier.PUBLIC)) continue;
            if (modifierList.hasModifierProperty(PsiModifier.STATIC)) continue;

            result.add(new PsiMethodMember(method));
        }
        return result.toArray(new PsiMethodMember[0]);
    }
}
