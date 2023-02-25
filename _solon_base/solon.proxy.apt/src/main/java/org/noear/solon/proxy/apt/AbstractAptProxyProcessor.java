package org.noear.solon.proxy.apt;

import com.squareup.javapoet.JavaFile;
import org.noear.solon.proxy.apt.util.ClassFileBuilder;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Apt 代理处理器
 *
 * @author noear
 * @since 2.2
 */
public abstract class AbstractAptProxyProcessor extends AbstractProcessor {
    /**
     * 类文件构建器
     */
    private ClassFileBuilder classFileBuilder = new ClassFileBuilder();

    /**
     * 支持的注解类型
     */
    private final Map<String, Class<? extends Annotation>> mAnnoMap = new LinkedHashMap<>();

    public AbstractAptProxyProcessor() {
        super();
        initSupportedAnnotation();
    }

    /**
     * 添加支持的注解
     */
    protected void addSupportedAnnotation(Class<? extends Annotation> annoClz) {
        mAnnoMap.put(annoClz.getCanonicalName(), annoClz);
    }

    /**
     * 初始化支持的注解
     */
    protected abstract void initSupportedAnnotation();

    /**
     * 支持的注解类型
     */
    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return mAnnoMap.keySet();
    }

    /**
     * 支持的版本
     */
    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    /**
     * 处理：1.搜集信息 2.生成java源文件
     */
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (!annotations.isEmpty()) {
            try {
                for (TypeElement e : annotations) {
                    String annoKey = e.asType().toString();
                    Class<? extends Annotation> annoType = mAnnoMap.get(annoKey);

                    if (annoType != null) {
                        generateCode(roundEnv.getElementsAnnotatedWith(annoType));
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return true;
        }

        return false;
    }

    /**
     * @param elements
     */
    private void generateCode(Set<? extends Element> elements) throws IOException {
        if (elements == null) {
            return;
        }

        for (Element element : elements) {
            if (element instanceof TypeElement) {
                //由于是在类上注解，那么获取TypeElement
                TypeElement typeElement = (TypeElement) element;

                if (typeElement.getModifiers().contains(Modifier.ABSTRACT)) {
                    throw new IllegalStateException("Abstract classes are not supported as proxy components");
                }

                if (typeElement.getModifiers().contains(Modifier.FINAL)) {
                    throw new IllegalStateException("Final classes are not supported as proxy components");
                }

                if (typeElement.getModifiers().contains(Modifier.PUBLIC) == false) {
                    throw new IllegalStateException("Not public classes are not supported as proxy components");
                }


                //构建 java 文件
                JavaFile javaFile = classFileBuilder.build(processingEnv,typeElement);
                //写入源文件
                javaFile.writeTo(processingEnv.getFiler());
            }
        }
    }
}