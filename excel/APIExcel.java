package com.jiuqi.nr.definition.excel;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.github.crab2died.ExcelUtils;
import com.github.crab2died.sheet.wrapper.SimpleSheetWrapper;
import com.jiuqi.nr.definition.controller.IFormulaRunTimeController;

import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;

/**
 * @author: rensiyu
 * @createTime: 2022/08/04
 */
public class APIExcel {
    @Test
    void export() {
//        Class<IRunTimeViewController> aClass = IRunTimeViewController.class;
        Class<IFormulaRunTimeController> aClass = IFormulaRunTimeController.class;
        Method[] declaredMethods = aClass.getDeclaredMethods();

        List<String> header = new ArrayList<>();
        header.add("Return Type");
        header.add("Method");
        header.add("Description");
        header.add("Exception");

        List<SimpleSheetWrapper> simpleSheetWrappers = new ArrayList<>();
        List<List> list2 = new ArrayList<>();
        for (Method declaredMethod : declaredMethods) {
            List<String> list = new ArrayList<>();
            if (declaredMethod.getGenericReturnType() instanceof ParameterizedTypeImpl) {
                ParameterizedTypeImpl genericReturnType = (ParameterizedTypeImpl) declaredMethod.getGenericReturnType();
                String typeName = ((ParameterizedTypeImpl) declaredMethod.getGenericReturnType()).getActualTypeArguments()[0].getTypeName();
                String[] split = typeName.split("\\.");
                list.add("`" + genericReturnType.getRawType().getSimpleName() + "<" + split[split.length - 1] + ">`");
            } else {
                list.add("`" + declaredMethod.getReturnType().getSimpleName() + "`");
            }
            //            ParameterizedTypeImpl genericReturnType = (ParameterizedTypeImpl) declaredMethod.getGenericReturnType();
            //            if (genericReturnType.getRawType() != null) {
            //                list.add("`" +genericReturnType.getRawType().getComponentType() + "<"+declaredMethod +">`");
            //            }
            String methodName = declaredMethod.getName() + "(";
            Parameter[] parameters = declaredMethod.getParameters();
            if (parameters.length == 0) {
                methodName += ")";
            } else {

                for (int i = 0; i < parameters.length; i++) {
                    if (i < parameters.length - 1) {
                        methodName += parameters[i].getType().getSimpleName() + " " + parameters[i].getName() + ", ";
                    } else {
                        methodName += parameters[i].getType().getSimpleName() + " " + parameters[i].getName() + ")";
                    }
                }
            }

            list.add("[`" + methodName + "`](#" + methodName + ")");
            list.add("");
            String e = "";
            Class<?>[] exceptionTypes = declaredMethod.getExceptionTypes();
            for (Class<?> exceptionType : exceptionTypes) {
                e += exceptionType.getSimpleName() + " ";
            }

            list.add(e);
            list2.add(list);
        }
        SimpleSheetWrapper simpleSheetWrapper = new SimpleSheetWrapper();
        simpleSheetWrapper.setHeader(header);
        simpleSheetWrapper.setSheetName(aClass.getSimpleName());
        simpleSheetWrapper.setData(list2);
        simpleSheetWrappers.add(simpleSheetWrapper);

        try (OutputStream fos = new FileOutputStream("/Users/rsy/WorkSpace/IFormulaRunTimeController.xlsx")) {
            ExcelUtils.getInstance().simpleSheet2Excel(simpleSheetWrappers, true, fos);
        } catch (Exception e) {

        }

    }
}
