package com.mampod.router_compiler;

import com.google.auto.service.AutoService;
import com.mampod.apt_annotation.BindView;
import com.squareup.javapoet.JavaFile;

import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

@AutoService(Processor.class)
public class BindViewProcessor extends AbstractProcessor {
    Filer filer;
    Elements mElementUtils;
    Messager mMessager;
    Types typeUtils;
    private Map<String, ClassCreatorProxy> mProxyMap = new HashMap<>();

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        filer = processingEnv.getFiler();
        mElementUtils = processingEnv.getElementUtils();
        mMessager = processingEnv.getMessager();
        typeUtils = processingEnv.getTypeUtils();

        System.out.println("apt");
        mMessager.printMessage(Diagnostic.Kind.NOTE, "hello-apt");
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        HashSet<String> set = new HashSet<>();
        set.add(BindView.class.getCanonicalName());
        return set;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return processingEnv.getSourceVersion();
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        mMessager.printMessage(Diagnostic.Kind.NOTE, "processing...");
        mProxyMap.clear();
        //得到所有的注解
        Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith(BindView.class);
        for (Element element : elements) {
            VariableElement variableElement = (VariableElement) element;
            TypeElement classElement = (TypeElement) variableElement.getEnclosingElement();
            String fullClassName = classElement.getQualifiedName().toString();
            ClassCreatorProxy proxy = mProxyMap.get(fullClassName);
            if (proxy == null) {
                proxy = new ClassCreatorProxy(mElementUtils, classElement);
                mProxyMap.put(fullClassName, proxy);
            }
            BindView bindAnnotation = variableElement.getAnnotation(BindView.class);
            int id = bindAnnotation.value();
            proxy.putElement(id, variableElement);
        }
//        //通过遍历mProxyMap，创建java文件
//        for (String key : mProxyMap.keySet()) {
//            ClassCreatorProxy proxyInfo = mProxyMap.get(key);
//            try {
//                mMessager.printMessage(Diagnostic.Kind.NOTE, " --> create " + proxyInfo.getProxyClassFullName());
//                JavaFileObject jfo = processingEnv.getFiler().createSourceFile(proxyInfo.getProxyClassFullName(), proxyInfo.getTypeElement());
//                Writer writer = jfo.openWriter();
//                writer.write(proxyInfo.generateJavaCode());
//                writer.flush();
//                writer.close();
//            } catch (IOException e) {
//                mMessager.printMessage(Diagnostic.Kind.NOTE, " --> create " + proxyInfo.getProxyClassFullName() + "error");
//            }
//        }

        //通过javapoet生成
        for (String key : mProxyMap.keySet()) {
            ClassCreatorProxy proxyInfo = mProxyMap.get(key);
            JavaFile javaFile = JavaFile.builder(proxyInfo.getPackageName(), proxyInfo.generateJavaCode2()).build();
            try {
                //　生成文件
                javaFile.writeTo(processingEnv.getFiler());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        mMessager.printMessage(Diagnostic.Kind.NOTE, "process finish ...");
        return true;
    }
}