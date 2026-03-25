package org.refactoringminer.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.nio.file.Files;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.refactoringminer.api.Refactoring;
import org.refactoringminer.api.RefactoringType;

import gr.uom.java.xmi.UMLModel;
import gr.uom.java.xmi.UMLModelASTReader;
import gr.uom.java.xmi.diff.AddTypeAnnotationRefactoring;
import gr.uom.java.xmi.diff.UMLModelDiff;

public class TestAddTypeAnnotationRefactoring {

    private static final String BASE = System.getProperty("user.dir")
            + "/src/test/resources/python/add-type-annotation/";

    private List<Refactoring> detect(String scenarioDir, String relFilePath) throws Exception {
        File beforeFile = new File(BASE + scenarioDir + "/before/" + relFilePath);
        File afterFile  = new File(BASE + scenarioDir + "/after/"  + relFilePath);
        String beforeContent = new String(Files.readAllBytes(beforeFile.toPath()));
        String afterContent  = new String(Files.readAllBytes(afterFile.toPath()));

        Map<String, String> filesBefore = new LinkedHashMap<>();
        Map<String, String> filesAfter  = new LinkedHashMap<>();
        filesBefore.put(relFilePath, beforeContent);
        filesAfter.put(relFilePath, afterContent);

        UMLModel modelBefore = new UMLModelASTReader(filesBefore, Collections.emptySet(), false).getUmlModel();
        UMLModel modelAfter  = new UMLModelASTReader(filesAfter,  Collections.emptySet(), false).getUmlModel();

        UMLModelDiff diff = modelBefore.diff(modelAfter);
        return diff.getRefactorings();
    }

    /**
     * Scenario 1: def add(self, x, y) -> def add(self, x: int, y: int) -> int:
     * Expected: 2 ADD_PARAMETER_TYPE_ANNOTATION (x, y) + 1 ADD_RETURN_TYPE_ANNOTATION
     */
    @Test
    public void testFullAnnotationAdded() throws Exception {
        List<Refactoring> refactorings = detect("scenario1", "Calculator.py");

        List<Refactoring> paramAnnotations = refactorings.stream()
                .filter(r -> r.getRefactoringType() == RefactoringType.ADD_PARAMETER_TYPE_ANNOTATION)
                .collect(Collectors.toList());
        List<Refactoring> returnAnnotations = refactorings.stream()
                .filter(r -> r.getRefactoringType() == RefactoringType.ADD_RETURN_TYPE_ANNOTATION)
                .collect(Collectors.toList());

        assertEquals(2, paramAnnotations.size(), "Expected 2 ADD_PARAMETER_TYPE_ANNOTATION refactorings");
        assertEquals(1, returnAnnotations.size(), "Expected 1 ADD_RETURN_TYPE_ANNOTATION refactoring");

        List<String> annotatedParams = paramAnnotations.stream()
                .map(r -> ((AddTypeAnnotationRefactoring) r).getParameterName())
                .sorted()
                .collect(Collectors.toList());
        assertEquals(List.of("x", "y"), annotatedParams);

        AddTypeAnnotationRefactoring returnRef = (AddTypeAnnotationRefactoring) returnAnnotations.get(0);
        assertEquals("int", returnRef.getAnnotatedType());
    }

    /**
     * Scenario 2: def add(self, x: int, y) -> def add(self, x: int, y: int)
     * Expected: exactly 1 ADD_PARAMETER_TYPE_ANNOTATION for y only
     */
    @Test
    public void testPartialAnnotationAdded() throws Exception {
        List<Refactoring> refactorings = detect("scenario2", "Calculator.py");

        List<Refactoring> paramAnnotations = refactorings.stream()
                .filter(r -> r.getRefactoringType() == RefactoringType.ADD_PARAMETER_TYPE_ANNOTATION)
                .collect(Collectors.toList());
        List<Refactoring> returnAnnotations = refactorings.stream()
                .filter(r -> r.getRefactoringType() == RefactoringType.ADD_RETURN_TYPE_ANNOTATION)
                .collect(Collectors.toList());

        assertEquals(1, paramAnnotations.size(), "Expected exactly 1 ADD_PARAMETER_TYPE_ANNOTATION");
        assertEquals(0, returnAnnotations.size(), "Expected no ADD_RETURN_TYPE_ANNOTATION");

        AddTypeAnnotationRefactoring ref = (AddTypeAnnotationRefactoring) paramAnnotations.get(0);
        assertEquals("y", ref.getParameterName());
        assertEquals("int", ref.getAnnotatedType());
    }

    /**
     * Scenario 3: def get_name(self) -> def get_name(self) -> str:
     * Expected: 1 ADD_RETURN_TYPE_ANNOTATION, no ADD_PARAMETER_TYPE_ANNOTATION
     */
    @Test
    public void testReturnTypeAnnotationAdded() throws Exception {
        List<Refactoring> refactorings = detect("scenario3", "DataService.py");

        List<Refactoring> paramAnnotations = refactorings.stream()
                .filter(r -> r.getRefactoringType() == RefactoringType.ADD_PARAMETER_TYPE_ANNOTATION)
                .collect(Collectors.toList());
        List<Refactoring> returnAnnotations = refactorings.stream()
                .filter(r -> r.getRefactoringType() == RefactoringType.ADD_RETURN_TYPE_ANNOTATION)
                .collect(Collectors.toList());

        assertEquals(0, paramAnnotations.size(), "Expected no ADD_PARAMETER_TYPE_ANNOTATION");
        assertEquals(1, returnAnnotations.size(), "Expected 1 ADD_RETURN_TYPE_ANNOTATION");

        AddTypeAnnotationRefactoring ref = (AddTypeAnnotationRefactoring) returnAnnotations.get(0);
        assertEquals("str", ref.getAnnotatedType());
    }

    /**
     * Scenario 4: def process(self, items) -> def process(self, items: list[int])
     * Expected: 1 ADD_PARAMETER_TYPE_ANNOTATION for items with type list[int]
     */
    @Test
    public void testComplexTypeAnnotationAdded() throws Exception {
        List<Refactoring> refactorings = detect("scenario4", "Processor.py");

        List<Refactoring> paramAnnotations = refactorings.stream()
                .filter(r -> r.getRefactoringType() == RefactoringType.ADD_PARAMETER_TYPE_ANNOTATION)
                .collect(Collectors.toList());

        assertEquals(1, paramAnnotations.size(), "Expected 1 ADD_PARAMETER_TYPE_ANNOTATION");

        AddTypeAnnotationRefactoring ref = (AddTypeAnnotationRefactoring) paramAnnotations.get(0);
        assertEquals("items", ref.getParameterName());
        assertEquals("list[int]", ref.getAnnotatedType());
    }

    /**
     * Scenario 1 used in reverse: verifies that when both before and after have type annotations,
     * no ADD_PARAMETER_TYPE_ANNOTATION is reported (no false positives).
     * Uses scenario1/after as both before and after (identical files).
     */
    @Test
    public void testNoFalsePositivesWhenAnnotationsUnchanged() throws Exception {
        File afterFile = new File(BASE + "scenario1/after/Calculator.py");
        String content = new String(Files.readAllBytes(afterFile.toPath()));

        Map<String, String> files = new LinkedHashMap<>();
        files.put("Calculator.py", content);

        UMLModel model = new UMLModelASTReader(files, Collections.emptySet(), false).getUmlModel();
        UMLModelDiff diff = model.diff(model);
        List<Refactoring> refactorings = diff.getRefactorings();

        long typeAnnotationCount = refactorings.stream()
                .filter(r -> r.getRefactoringType() == RefactoringType.ADD_PARAMETER_TYPE_ANNOTATION
                          || r.getRefactoringType() == RefactoringType.ADD_RETURN_TYPE_ANNOTATION)
                .count();

        assertEquals(0, typeAnnotationCount, "Expected no type annotation refactorings when files are identical");
    }
}
